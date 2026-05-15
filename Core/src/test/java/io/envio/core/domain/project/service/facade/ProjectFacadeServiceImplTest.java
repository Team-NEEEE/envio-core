package io.envio.core.domain.project.service.facade;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.envio.core.common.error.ErrorCode;
import io.envio.core.domain.project.dto.request.ProjectPushReqDto;
import io.envio.core.domain.project.exception.ProjectException;
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
