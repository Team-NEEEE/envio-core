package io.envio.core.domain.project.service.query;

import java.util.List;

import io.envio.core.domain.project.entity.History;

public interface ProjectQueryService {

	History getLatestHistory(Long projectId, String githubUserId);

	List<History> getProjectHistories(Long projectId);
}
