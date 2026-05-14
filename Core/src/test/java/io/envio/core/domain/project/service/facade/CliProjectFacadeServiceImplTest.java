package io.envio.core.domain.project.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.envio.core.domain.project.client.GithubRepositoryAccess;
import io.envio.core.domain.project.client.GithubRepositoryMember;
import io.envio.core.domain.project.client.GithubRepositoryMemberClient;
import io.envio.core.domain.project.client.ProjectRole;
import io.envio.core.domain.project.dto.request.ProjectCreateReqDto;
import io.envio.core.domain.project.dto.request.ProjectWrappedKeyReqDto;
import io.envio.core.domain.project.dto.request.ProjectWrappedKeySaveReqDto;
import io.envio.core.domain.project.dto.response.ProjectCreateResDto;
import io.envio.core.domain.project.entity.Project;
import io.envio.core.domain.project.exception.ProjectException;
import io.envio.core.domain.project.repository.EncryptedKeyRepository;
import io.envio.core.domain.project.repository.ProjectRepository;
import io.envio.core.domain.user.entity.User;
import io.envio.core.domain.user.entity.UserDevice;
import io.envio.core.domain.user.entity.UserRole;
import io.envio.core.domain.user.repository.UserDeviceRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CLI 프로젝트 Facade 테스트")
class CliProjectFacadeServiceImplTest {

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private UserDeviceRepository userDeviceRepository;

	@Mock
	private EncryptedKeyRepository encryptedKeyRepository;

	@Mock
	private GithubRepositoryMemberClient githubRepositoryMemberClient;

	@Test
	@DisplayName("createProject는 Write 이상 GitHub 권한을 가진 등록 기기만 키 분배 대상으로 반환한다")
	void createProject_filtersGithubMembersByWritePermission() {
		// given
		CliProjectFacadeServiceImpl service = createService();
		UserDevice requesterDevice = userDevice(10L, 1L, "writer", "requester-public-key");
		UserDevice adminDevice = userDevice(11L, 2L, "admin", "admin-public-key");

		when(userDeviceRepository.findByIdAndPublicKey(10L, "requester-public-key"))
			.thenReturn(Optional.of(requesterDevice));
		when(projectRepository.existsByOrganizationNameAndProjectName("Team-NEEEE", "envio-cli"))
			.thenReturn(false);
		when(githubRepositoryMemberClient.getRepositoryAccess("Team-NEEEE", "envio-cli"))
			.thenReturn(new GithubRepositoryAccess(12345678L, List.of(
				new GithubRepositoryMember("writer", ProjectRole.WRITE),
				new GithubRepositoryMember("reader", ProjectRole.READ),
				new GithubRepositoryMember("admin", ProjectRole.ADMIN)
			)));
		when(projectRepository.save(any(Project.class))).thenReturn(Project.builder()
			.id(1L)
			.organizationName("Team-NEEEE")
			.projectName("envio-cli")
			.versionId(0L)
			.githubAppId("12345678")
			.build());
		when(userDeviceRepository.findAllByUserGithubIdIn(any()))
			.thenReturn(List.of(requesterDevice, adminDevice));

		ProjectCreateReqDto reqDto = ProjectCreateReqDto.builder()
			.repositoryUrl("https://github.com/Team-NEEEE/envio-cli.git")
			.deviceId(10L)
			.publicKey("requester-public-key")
			.build();

		// when
		ProjectCreateResDto response = service.createProject(reqDto);

		// then
		assertThat(response.projectId()).isEqualTo(1L);
		assertThat(response.projectName()).isEqualTo("envio-cli");
		assertThat(response.githubRepoName()).isEqualTo("Team-NEEEE/envio-cli");
		assertThat(response.installationId()).isEqualTo(12345678L);
		assertThat(response.members()).hasSize(2);
		assertThat(response.members())
			.extracting("githubId")
			.containsExactly("writer", "admin");

		ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
		verify(projectRepository).save(projectCaptor.capture());
		assertThat(projectCaptor.getValue().getOrganizationName()).isEqualTo("Team-NEEEE");
		assertThat(projectCaptor.getValue().getProjectName()).isEqualTo("envio-cli");
	}

	@Test
	@DisplayName("saveWrappedKeys는 Write 이상 등록 기기 전체의 encryptedKey가 없으면 실패한다")
	void saveWrappedKeys_requiresAllEligibleDeviceKeys() {
		// given
		CliProjectFacadeServiceImpl service = createService();
		Project project = Project.builder()
			.id(1L)
			.organizationName("Team-NEEEE")
			.projectName("envio-cli")
			.versionId(0L)
			.build();
		UserDevice requesterDevice = userDevice(10L, 1L, "writer", "requester-public-key");
		UserDevice adminDevice = userDevice(11L, 2L, "admin", "admin-public-key");

		when(userDeviceRepository.findByIdAndPublicKey(10L, "requester-public-key"))
			.thenReturn(Optional.of(requesterDevice));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(githubRepositoryMemberClient.getRepositoryAccess("Team-NEEEE", "envio-cli"))
			.thenReturn(new GithubRepositoryAccess(12345678L, List.of(
				new GithubRepositoryMember("writer", ProjectRole.WRITE),
				new GithubRepositoryMember("admin", ProjectRole.ADMIN)
			)));
		when(userDeviceRepository.findAllByUserGithubIdIn(any()))
			.thenReturn(List.of(requesterDevice, adminDevice));

		ProjectWrappedKeySaveReqDto reqDto = ProjectWrappedKeySaveReqDto.builder()
			.deviceId(10L)
			.publicKey("requester-public-key")
			.wrappedKeys(List.of(ProjectWrappedKeyReqDto.builder()
				.userId(1L)
				.userDeviceId(10L)
				.encryptedKey("encrypted-master-key")
				.build()))
			.build();

		// when, then
		assertThatThrownBy(() -> service.saveWrappedKeys(1L, reqDto))
			.isInstanceOf(ProjectException.class);
	}

	private CliProjectFacadeServiceImpl createService() {
		return new CliProjectFacadeServiceImpl(
			projectRepository,
			userDeviceRepository,
			encryptedKeyRepository,
			githubRepositoryMemberClient
		);
	}

	private UserDevice userDevice(
		final Long userDeviceId,
		final Long userId,
		final String githubId,
		final String publicKey
	) {
		User user = User.builder()
			.id(userId)
			.githubId(githubId)
			.email(githubId + "@envio.io")
			.role(UserRole.DEVELOPER)
			.build();
		return UserDevice.builder()
			.id(userDeviceId)
			.user(user)
			.deviceName(githubId + "-device")
			.publicKey(publicKey)
			.build();
	}
}
