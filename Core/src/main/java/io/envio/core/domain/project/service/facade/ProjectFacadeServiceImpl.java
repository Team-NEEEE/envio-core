package io.envio.core.domain.project.service.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.core.common.error.ErrorCode;
import io.envio.core.domain.project.converter.ProjectConverter;
import io.envio.core.domain.project.dto.request.ProjectPushReqDto;
import io.envio.core.domain.project.dto.response.ProjectDetailResDto;
import io.envio.core.domain.project.dto.response.ProjectHistoryResDto;
import io.envio.core.domain.project.dto.response.ProjectPullResDto;
import io.envio.core.domain.project.dto.response.ProjectPushResDto;
import io.envio.core.domain.project.entity.EncryptedKey;
import io.envio.core.domain.project.entity.History;
import io.envio.core.domain.project.entity.Project;
import io.envio.core.domain.project.exception.ProjectException;
import io.envio.core.domain.project.repository.EncryptedKeyRepository;
import io.envio.core.domain.project.service.authorization.ProjectMembershipValidator;
import io.envio.core.domain.project.service.command.ProjectCommandService;
import io.envio.core.domain.project.service.query.ProjectQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectFacadeServiceImpl implements ProjectFacadeService {

	private final ProjectCommandService projectCommandService;
	private final ProjectQueryService projectQueryService;
	private final ProjectMembershipValidator projectMembershipValidator;
	private final EncryptedKeyRepository encryptedKeyRepository;

	@Override
	public ProjectPullResDto pull(
		final Long projectId,
		final Long userId,
		final String authenticatedGithubId,
		final String githubUserId,
		final String deviceId
	) {
		projectMembershipValidator.validateProjectMember(projectId, userId);
		validateSameGithubUser(authenticatedGithubId, githubUserId);
		return pullInternal(projectId, githubUserId, deviceId);
	}

	@Override
	public ProjectPullResDto pull(final Long projectId, final String githubUserId, final String deviceId) {
		projectMembershipValidator.validateProjectMember(projectId, githubUserId);
		return pullInternal(projectId, githubUserId, deviceId);
	}

	private ProjectPullResDto pullInternal(final Long projectId, final String githubUserId, final String deviceId) {
		log.info("[Project] 최신 환경변수 조회 요청 - projectId: {}, githubUserId: {}", projectId, githubUserId);
		History history = projectQueryService.getLatestHistory(projectId, githubUserId);
		String wrappedMasterKey = resolveWrappedMasterKey(projectId, githubUserId, deviceId);
		return ProjectConverter.toPullResponse(history, "최신 환경변수 조회에 성공했습니다.", wrappedMasterKey);
	}

	@Override
	public ProjectPushResDto push(
		final Long projectId,
		final Long userId,
		final String authenticatedGithubId,
		final ProjectPushReqDto reqDto
	) {
		projectMembershipValidator.validateProjectMember(projectId, userId);
		validateSameGithubUser(authenticatedGithubId, reqDto.githubUserId());
		return pushInternal(projectId, reqDto);
	}

	@Override
	public ProjectPushResDto push(final Long projectId, final ProjectPushReqDto reqDto) {
		projectMembershipValidator.validateProjectMember(projectId, reqDto.githubUserId());
		return pushInternal(projectId, reqDto);
	}

	private ProjectPushResDto pushInternal(final Long projectId, final ProjectPushReqDto reqDto) {
		log.info("[Project] 환경변수 업데이트 요청 - projectId: {}, githubUserId: {}", projectId, reqDto.githubUserId());
		History history = projectCommandService.push(projectId, reqDto);
		return ProjectConverter.toPushResponse(history, "환경변수 새 버전 생성에 성공했습니다.");
	}

	@Override
	public List<ProjectHistoryResDto> getProjectHistory(final Long projectId, final Long userId) {
		projectMembershipValidator.validateProjectMember(projectId, userId);
		return getProjectHistoryInternal(projectId);
	}

	@Override
	public List<ProjectHistoryResDto> getProjectHistory(final Long projectId) {
		return getProjectHistoryInternal(projectId);
	}

	private List<ProjectHistoryResDto> getProjectHistoryInternal(final Long projectId) {
		log.info("[Project] 히스토리 조회 요청 - projectId: {}", projectId);
		List<History> histories = projectQueryService.getProjectHistories(projectId);
		return histories.stream()
			.map(ProjectConverter::toHistoryResponse)
			.toList();
	}

	@Override
	public ProjectDetailResDto getProjectDetail(final Long projectId, final Long userId) {
		projectMembershipValidator.validateProjectMember(projectId, userId);
		return getProjectDetailInternal(projectId);
	}


	private ProjectDetailResDto getProjectDetailInternal(final Long projectId) {
		log.info("[Project] 상세 정보 조회 요청 - projectId: {}", projectId);
		Project project = projectQueryService.findById(projectId);
		return ProjectConverter.toProjectDetailResDto(project);
	}

	private void validateSameGithubUser(final String authenticatedGithubId, final String requestedGithubId) {
		if (!authenticatedGithubId.equals(requestedGithubId)) {
			throw new ProjectException(ErrorCode.ACCESS_DENIED);
		}
	}

	private String resolveWrappedMasterKey(final Long projectId, final String githubUserId, final String deviceId) {
		if (deviceId == null || deviceId.isBlank()) {
			return null;
		}
		Long userDeviceId = parseDeviceId(deviceId);
		EncryptedKey encryptedKey = encryptedKeyRepository
			.findByUserDeviceIdAndProjectIdAndUserDeviceUserGithubIdAndActiveTrue(
				userDeviceId,
				projectId,
				githubUserId
			)
			.orElseThrow(() -> new ProjectException(ErrorCode.JOIN_STATUS_PENDING));
		return encryptedKey.getEncryptedKey();
	}

	private Long parseDeviceId(final String deviceId) {
		try {
			return Long.valueOf(deviceId.strip());
		} catch (NumberFormatException exception) {
			throw new ProjectException(ErrorCode.BAD_REQUEST);
		}
	}
}
