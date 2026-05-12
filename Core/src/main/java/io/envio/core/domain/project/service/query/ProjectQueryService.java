package io.envio.core.domain.project.service.query;

import io.envio.core.domain.project.entity.History;

public interface ProjectQueryService {

	History getLatestHistory(Long projectId);
}
