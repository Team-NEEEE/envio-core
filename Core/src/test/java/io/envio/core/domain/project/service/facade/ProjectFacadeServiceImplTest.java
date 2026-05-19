package io.envio.core.domain.project.service.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.envio.core.common.error.ErrorCode;
import io.envio.core.domain.project.dto.request.ProjectPushReqDto;
import io.envio.core.domain.project.dto.response.ProjectPullResDto;
import io.envio.core.domain.project.entity.EncryptedKey;
import io.envio.core.domain.project.entity.History;
import io.envio.core.domain.project.entity.Project;
import io.envio.core.domain.project.exception.ProjectException;
import io.envio.core.domain.project.repository.EncryptedKeyRepository;
import io.envio.core.domain.project.service.authorization.ProjectMembershipValidator;
import io.envio.core.domain.project.service.command.ProjectCommandService;
import io.envio.core.domain.project.service.query.ProjectQueryService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Project facade authorization")
class ProjectFacadeServiceImplTest {

	@Mock
	private ProjectCommandService projectCommandService;

	@Mock
	private ProjectQueryService projectQueryService;

	@Mock
	private ProjectMembershipValidator projectMembershipValidator;

	@Mock
	private EncryptedKeyRepository encryptedKeyRepository;

	@InjectMocks
	private ProjectFacadeServiceImpl projectFacadeService;

	@Test
	@DisplayName("rejects pull when requested GitHub user differs from authenticated user")
	void pullRejectsDifferentGithubUser() {
		assertThatThrownBy(() -> projectFacadeService.pull(1L, 10L, "user-1", "user-2"))
			.isInstanceOf(ProjectException.class)
			.hasMessage(ErrorCode.ACCESS_DENIED.getMessage());

		verify(projectMembershipValidator).validateProjectMember(1L, 10L);
		verify(projectQueryService, never()).getLatestHistory(1L, "user-2");
	}

	@Test
	@DisplayName("pull includes wrapped master key when device id is provided")
	void pullIncludesWrappedMasterKeyWhenDeviceIdProvided() {
		Project project = Project.builder()
			.id(1L)
			.projectName("test-project")
			.build();
		History history = History.builder()
			.historiesId(100L)
			.project(project)
			.versionId(1L)
			.encryptedEnvironment(Map.of("key", "value"))
			.build();
		EncryptedKey encryptedKey = EncryptedKey.builder()
			.encryptedKey("wrapped-master-key")
			.build();

		when(projectQueryService.getLatestHistory(1L, "user-1")).thenReturn(history);
		when(encryptedKeyRepository.findByUserDeviceIdAndProjectIdAndUserDeviceUserGithubIdAndActiveTrue(
			20L,
			1L,
			"user-1"
		)).thenReturn(Optional.of(encryptedKey));

		ProjectPullResDto result = projectFacadeService.pull(1L, 10L, "user-1", "user-1", "20");

		assertThat(result.wrappedMasterKey()).isEqualTo("wrapped-master-key");
	}

	@Test
	@DisplayName("rejects push when request body GitHub user differs from authenticated user")
	void pushRejectsDifferentGithubUser() {
		ProjectPushReqDto reqDto = ProjectPushReqDto.builder()
			.githubUserId("user-2")
			.encryptedEnvironment(Map.of("key", "value"))
			.parentVersionId(0L)
			.build();

		assertThatThrownBy(() -> projectFacadeService.push(1L, 10L, "user-1", reqDto))
			.isInstanceOf(ProjectException.class)
			.hasMessage(ErrorCode.ACCESS_DENIED.getMessage());

		verify(projectMembershipValidator).validateProjectMember(1L, 10L);
		verify(projectCommandService, never()).push(1L, reqDto);
	}
}
