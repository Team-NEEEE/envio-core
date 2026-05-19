package io.envio.core.domain.project.service.facade;

import java.util.List;

import io.envio.core.domain.project.dto.request.ProjectPushReqDto;
import io.envio.core.domain.project.dto.response.ProjectDetailResDto;
import io.envio.core.domain.project.dto.response.ProjectHistoryResDto;
import io.envio.core.domain.project.dto.response.ProjectPullResDto;
import io.envio.core.domain.project.dto.response.ProjectPushResDto;

public interface ProjectFacadeService {

	ProjectPullResDto pull(
		Long projectId,
		Long userId,
		String authenticatedGithubId,
		String githubUserId,
		String deviceId
	);

	default ProjectPullResDto pull(
		final Long projectId,
		final Long userId,
		final String authenticatedGithubId,
		final String githubUserId
	) {
		return pull(projectId, userId, authenticatedGithubId, githubUserId, null);
	}

	ProjectPullResDto pull(Long projectId, String githubUserId, String deviceId);

	default ProjectPullResDto pull(final Long projectId, final String githubUserId) {
		return pull(projectId, githubUserId, null);
	}

	ProjectPushResDto push(Long projectId, Long userId, String authenticatedGithubId, ProjectPushReqDto reqDto);

	ProjectPushResDto push(Long projectId, ProjectPushReqDto reqDto);

	List<ProjectHistoryResDto> getProjectHistory(Long projectId, Long userId);

	List<ProjectHistoryResDto> getProjectHistory(Long projectId);

	ProjectDetailResDto getProjectDetail(Long projectId, Long userId);
}
