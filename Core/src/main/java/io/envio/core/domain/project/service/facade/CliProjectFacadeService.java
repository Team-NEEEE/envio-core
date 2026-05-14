package io.envio.core.domain.project.service.facade;

import io.envio.core.domain.project.dto.request.ProjectCreateReqDto;
import io.envio.core.domain.project.dto.request.ProjectWrappedKeySaveReqDto;
import io.envio.core.domain.project.dto.response.ProjectCreateResDto;
import io.envio.core.domain.project.dto.response.ProjectWrappedKeySaveResDto;

public interface CliProjectFacadeService {

	ProjectCreateResDto createProject(ProjectCreateReqDto reqDto);

	ProjectWrappedKeySaveResDto saveWrappedKeys(Long projectId, ProjectWrappedKeySaveReqDto reqDto);
}
