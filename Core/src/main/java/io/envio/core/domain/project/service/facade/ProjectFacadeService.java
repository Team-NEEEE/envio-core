package io.envio.core.domain.project.service.facade;

import java.util.List;

import io.envio.core.domain.project.dto.request.ProjectPushReqDto;
import io.envio.core.domain.project.dto.response.ProjectDetailResDto;
import io.envio.core.domain.project.dto.response.ProjectHistoryResDto;
import io.envio.core.domain.project.dto.response.ProjectPullResDto;
import io.envio.core.domain.project.dto.response.ProjectPushResDto;
import io.envio.core.domain.project.dto.response.ProjectResDto;

public interface ProjectFacadeService {

	ProjectPullResDto pull(Long projectId, String githubUserId);

	ProjectPushResDto push(Long projectId, ProjectPushReqDto reqDto);

	List<ProjectHistoryResDto> getProjectHistory(Long projectId);

	List<ProjectResDto> getUserProjects(Long userId);

	ProjectDetailResDto getProjectDetail(Long projectId);
}
