package io.envio.core.domain.project.service.command;

import io.envio.core.domain.project.dto.request.ProjectPushReqDto;
import io.envio.core.domain.project.entity.History;

public interface ProjectCommandService {

	History push(Long projectId, ProjectPushReqDto reqDto);
}
