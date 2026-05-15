package io.envio.core.domain.user.service.facade;

import java.util.List;
import java.util.Map;

import io.envio.core.domain.project.dto.response.ProjectResDto;

public interface UserFacadeService {

	Map<String, List<ProjectResDto>> getMyProjects(String githubId);
}
