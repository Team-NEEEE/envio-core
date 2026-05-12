package io.envio.core.domain.project.service.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.core.common.error.ErrorCode;
import io.envio.core.domain.project.entity.History;
import io.envio.core.domain.project.exception.ProjectException;
import io.envio.core.domain.project.repository.HistoryRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectQueryServiceImpl implements ProjectQueryService {

	private final HistoryRepository historyRepository;

	@Override
	public History getLatestHistory(final Long projectId) {
		return historyRepository.findFirstByProjectIdOrderByVersionIdDesc(projectId)
			.orElseThrow(() -> new ProjectException(ErrorCode.ENVIRONMENT_VERSION_NOT_INITIALIZED));
	}
}
