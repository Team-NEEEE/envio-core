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
	public History getLatestHistory(final Long projectId, final String githubUserId) {
		// TODO: P1.2 - 해당 githubUserId가 projectId에 접근 권한이 있는지 검증하는 로직 필요
		// 현재는 멤버십 테이블이 없으므로 식별용으로만 활용

		return historyRepository.findFirstByProjectIdOrderByVersionIdDesc(projectId)
			.orElseThrow(() -> new ProjectException(ErrorCode.ENVIRONMENT_VERSION_NOT_INITIALIZED));
	}
}
