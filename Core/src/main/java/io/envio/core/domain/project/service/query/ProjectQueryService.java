package io.envio.core.domain.project.service.query;

import java.util.List;

import io.envio.core.domain.project.entity.History;
import io.envio.core.domain.project.entity.Project;

public interface ProjectQueryService {

	History getLatestHistory(Long projectId, String githubUserId);

	List<History> getProjectHistories(Long projectId);

	List<Project> getUserProjects(Long userId);

	Project findById(Long projectId);
}
