package io.envio.core.domain.project.service.facade;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.core.common.error.ErrorCode;
import io.envio.core.domain.project.client.GithubRepositoryAccess;
import io.envio.core.domain.project.client.GithubRepositoryMember;
import io.envio.core.domain.project.client.GithubRepositoryMemberClient;
import io.envio.core.domain.project.client.ProjectRole;
import io.envio.core.domain.project.dto.request.ProjectCreateReqDto;
import io.envio.core.domain.project.dto.request.ProjectWrappedKeyReqDto;
import io.envio.core.domain.project.dto.request.ProjectWrappedKeySaveReqDto;
import io.envio.core.domain.project.dto.response.ProjectCreateMemberResDto;
import io.envio.core.domain.project.dto.response.ProjectCreateResDto;
import io.envio.core.domain.project.dto.response.ProjectWrappedKeySaveResDto;
import io.envio.core.domain.project.entity.EncryptedKey;
import io.envio.core.domain.project.entity.Project;
import io.envio.core.domain.project.exception.ProjectException;
import io.envio.core.domain.project.repository.EncryptedKeyRepository;
import io.envio.core.domain.project.repository.ProjectRepository;
import io.envio.core.domain.user.entity.UserDevice;
import io.envio.core.domain.user.repository.UserDeviceRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CliProjectFacadeServiceImpl implements CliProjectFacadeService {

	private static final String CREATE_SUCCESS_MESSAGE = "프로젝트 생성에 성공했습니다.";
	private static final String WRAPPED_KEYS_SUCCESS_MESSAGE = "프로젝트 마스터 키 분배 등록에 성공했습니다.";

	private final ProjectRepository projectRepository;
	private final UserDeviceRepository userDeviceRepository;
	private final EncryptedKeyRepository encryptedKeyRepository;
	private final GithubRepositoryMemberClient githubRepositoryMemberClient;

	@Override
	public ProjectCreateResDto createProject(final ProjectCreateReqDto reqDto) {
		UserDevice requesterDevice = validateDevice(reqDto.deviceId(), reqDto.publicKey());
		RepositoryRef repositoryRef = RepositoryRef.parse(reqDto.repositoryUrl());

		if (projectRepository.existsByOrganizationNameAndProjectName(repositoryRef.owner(), repositoryRef.repoName())) {
			throw new ProjectException(ErrorCode.DATA_INTEGRITY_VIOLATION);
		}

		GithubRepositoryAccess repositoryAccess = githubRepositoryMemberClient.getRepositoryAccess(
			repositoryRef.owner(),
			repositoryRef.repoName()
		);
		Map<String, ProjectRole> eligibleRoles = getEligibleRoles(repositoryAccess.members());
		validateRequesterCanCreate(requesterDevice, eligibleRoles);

		Project project = projectRepository.save(Project.builder()
			.organizationName(repositoryRef.owner())
			.projectName(repositoryRef.repoName())
			.versionId(0L)
			.githubAppId(toInstallationId(repositoryAccess.installationId()))
			.build());

		List<ProjectCreateMemberResDto> members = toMemberResponses(eligibleRoles);

		log.info(
			"[Project] CLI 프로젝트 생성 완료 - projectId: {}, repository: {}",
			project.getId(),
			repositoryRef.githubRepoName()
		);
		return ProjectCreateResDto.builder()
			.message(CREATE_SUCCESS_MESSAGE)
			.projectId(project.getId())
			.projectName(project.getProjectName())
			.githubRepoName(repositoryRef.githubRepoName())
			.installationId(repositoryAccess.installationId())
			.members(members)
			.build();
	}

	@Override
	public ProjectWrappedKeySaveResDto saveWrappedKeys(
		final Long projectId,
		final ProjectWrappedKeySaveReqDto reqDto
	) {
		UserDevice requesterDevice = validateDevice(reqDto.deviceId(), reqDto.publicKey());
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new ProjectException(ErrorCode.PROJECT_NOT_FOUND));

		GithubRepositoryAccess repositoryAccess = githubRepositoryMemberClient.getRepositoryAccess(
			project.getOrganizationName(),
			project.getProjectName()
		);
		Map<String, ProjectRole> eligibleRoles = getEligibleRoles(repositoryAccess.members());
		validateRequesterCanCreate(requesterDevice, eligibleRoles);

		List<UserDevice> targetDevices = getEligibleDevices(eligibleRoles.keySet());
		Map<Long, UserDevice> targetDeviceById = targetDevices.stream()
			.collect(Collectors.toMap(UserDevice::getId, Function.identity()));
		Map<Long, ProjectWrappedKeyReqDto> wrappedKeyByDeviceId = toWrappedKeyMap(reqDto.wrappedKeys());

		validateWrappedKeys(targetDeviceById, wrappedKeyByDeviceId);

		targetDevices.forEach(targetDevice -> saveWrappedKey(
			project,
			targetDevice,
			wrappedKeyByDeviceId.get(targetDevice.getId()).encryptedKey()
		));

		log.info("[Project] CLI 프로젝트 마스터 키 분배 등록 완료 - projectId: {}, count: {}", projectId, targetDevices.size());
		return ProjectWrappedKeySaveResDto.builder()
			.message(WRAPPED_KEYS_SUCCESS_MESSAGE)
			.projectId(projectId)
			.updatedCount(targetDevices.size())
			.build();
	}

	private UserDevice validateDevice(final Long deviceId, final String publicKey) {
		return userDeviceRepository.findByIdAndPublicKey(deviceId, publicKey)
			.orElseThrow(() -> new ProjectException(ErrorCode.ACCESS_DENIED));
	}

	private Map<String, ProjectRole> getEligibleRoles(final Collection<GithubRepositoryMember> members) {
		return members.stream()
			.filter(member -> member.projectRole().canCreateProject())
			.collect(Collectors.toMap(
				GithubRepositoryMember::githubId,
				GithubRepositoryMember::projectRole,
				this::pickHigherRole
			));
	}

	private ProjectRole pickHigherRole(final ProjectRole left, final ProjectRole right) {
		return left.ordinal() >= right.ordinal() ? left : right;
	}

	private void validateRequesterCanCreate(
		final UserDevice requesterDevice,
		final Map<String, ProjectRole> eligibleRoles
	) {
		String requesterGithubId = requesterDevice.getUser().getGithubId();
		if (!eligibleRoles.containsKey(requesterGithubId)) {
			throw new ProjectException(ErrorCode.ACCESS_DENIED);
		}
	}

	private List<ProjectCreateMemberResDto> toMemberResponses(final Map<String, ProjectRole> eligibleRoles) {
		Map<String, ProjectRole> eligibleRoleByGithubId = Map.copyOf(eligibleRoles);
		return getEligibleDevices(eligibleRoleByGithubId.keySet()).stream()
			.sorted(Comparator.comparing(UserDevice::getId))
			.map(device -> ProjectCreateMemberResDto.builder()
				.userId(device.getUser().getId())
				.userDeviceId(device.getId())
				.githubId(device.getUser().getGithubId())
				.publicKey(device.getPublicKey())
				.projectRole(eligibleRoleByGithubId.get(device.getUser().getGithubId()).name())
				.build())
			.toList();
	}

	private List<UserDevice> getEligibleDevices(final Collection<String> githubIds) {
		if (githubIds.isEmpty()) {
			return List.of();
		}
		return userDeviceRepository.findAllByUserGithubIdIn(githubIds);
	}

	private Map<Long, ProjectWrappedKeyReqDto> toWrappedKeyMap(final List<ProjectWrappedKeyReqDto> wrappedKeys) {
		return wrappedKeys.stream()
			.collect(Collectors.toMap(
				ProjectWrappedKeyReqDto::userDeviceId,
				Function.identity(),
				(left, right) -> {
					throw new ProjectException(ErrorCode.DATA_INTEGRITY_VIOLATION);
				}
			));
	}

	private void validateWrappedKeys(
		final Map<Long, UserDevice> targetDeviceById,
		final Map<Long, ProjectWrappedKeyReqDto> wrappedKeyByDeviceId
	) {
		if (!wrappedKeyByDeviceId.keySet().equals(targetDeviceById.keySet())) {
			throw new ProjectException(ErrorCode.DATA_INTEGRITY_VIOLATION);
		}

		wrappedKeyByDeviceId.forEach((userDeviceId, wrappedKey) -> {
			UserDevice targetDevice = targetDeviceById.get(userDeviceId);
			if (!targetDevice.getUser().getId().equals(wrappedKey.userId())) {
				throw new ProjectException(ErrorCode.DATA_INTEGRITY_VIOLATION);
			}
		});
	}

	private void saveWrappedKey(final Project project, final UserDevice userDevice, final String encryptedKey) {
		encryptedKeyRepository.findByUserDeviceIdAndProjectId(userDevice.getId(), project.getId())
			.ifPresentOrElse(
				existingEncryptedKey -> existingEncryptedKey.updateEncryptedKey(encryptedKey),
				() -> encryptedKeyRepository.save(EncryptedKey.builder()
					.project(project)
					.userDevice(userDevice)
					.encryptedKey(encryptedKey)
					.active(true)
					.build())
			);
	}

	private String toInstallationId(final Long installationId) {
		return installationId == null ? null : String.valueOf(installationId);
	}

	private record RepositoryRef(
		String owner,
		String repoName
	) {

		private static RepositoryRef parse(final String repositoryUrl) {
			String normalizedRepositoryUrl = repositoryUrl.strip();
			if (normalizedRepositoryUrl.startsWith("git@github.com:")) {
				return fromPath(normalizedRepositoryUrl.substring("git@github.com:".length()));
			}

			try {
				URI uri = new URI(normalizedRepositoryUrl);
				if (uri.getHost() == null) {
					return fromPath(normalizedRepositoryUrl);
				}
				if (!"github.com".equalsIgnoreCase(uri.getHost())) {
					throw new ProjectException(ErrorCode.BAD_REQUEST);
				}
				return fromPath(uri.getPath());
			} catch (URISyntaxException exception) {
				return fromPath(normalizedRepositoryUrl);
			}
		}

		private static RepositoryRef fromPath(final String path) {
			String normalizedPath = path.strip()
				.replace("\\", "/")
				.replaceFirst("^/+", "")
				.replaceFirst("\\.git$", "");
			String[] parts = normalizedPath.split("/");

			if (parts.length < 2 || parts[0].isBlank() || parts[1].isBlank()) {
				throw new ProjectException(ErrorCode.BAD_REQUEST);
			}
			return new RepositoryRef(parts[0], parts[1]);
		}

		private String githubRepoName() {
			return owner + "/" + repoName;
		}
	}
}
