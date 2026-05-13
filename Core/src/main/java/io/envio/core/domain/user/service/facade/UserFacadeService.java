package io.envio.core.domain.user.service.facade;

import java.util.List;

import io.envio.core.domain.project.dto.response.ProjectResDto;

public interface UserFacadeService {

	List<ProjectResDto> getMyProjects(String githubId);
}
