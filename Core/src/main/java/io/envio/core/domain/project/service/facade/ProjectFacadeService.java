package io.envio.core.domain.project.service.facade;

import io.envio.core.domain.project.dto.request.ProjectPushReqDto;
import io.envio.core.domain.project.dto.response.ProjectPullResDto;
import io.envio.core.domain.project.dto.response.ProjectPushResDto;

public interface ProjectFacadeService {

	ProjectPullResDto pull(Long projectId, String githubUserId);

	ProjectPushResDto push(Long projectId, ProjectPushReqDto reqDto);
}
