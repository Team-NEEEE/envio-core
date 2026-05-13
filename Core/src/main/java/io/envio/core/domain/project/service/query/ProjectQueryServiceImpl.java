package io.envio.core.domain.project.service.query;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.core.common.error.ErrorCode;
import io.envio.core.domain.project.entity.History;
import io.envio.core.domain.project.entity.Project;
import io.envio.core.domain.project.exception.ProjectException;
import io.envio.core.domain.project.repository.EncryptedKeyRepository;
import io.envio.core.domain.project.repository.HistoryRepository;
import io.envio.core.domain.project.repository.ProjectRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectQueryServiceImpl implements ProjectQueryService {

	private final ProjectRepository projectRepository;
	private final HistoryRepository historyRepository;
	private final EncryptedKeyRepository encryptedKeyRepository;

	@Override
	public History getLatestHistory(final Long projectId, final String githubUserId) {
		// TODO: P1.2 - 해당 githubUserId가 projectId에 접근 권한이 있는지 검증하는 로직 필요
		// 현재는 멤버십 테이블이 없으므로 식별용으로만 활용

		return historyRepository.findFirstByProjectIdOrderByVersionIdDesc(projectId)
			.orElseThrow(() -> new ProjectException(ErrorCode.ENVIRONMENT_VERSION_NOT_INITIALIZED));
	}

	@Override
	public List<History> getProjectHistories(final Long projectId) {
		if (!projectRepository.existsById(projectId)) {
			throw new ProjectException(ErrorCode.PROJECT_NOT_FOUND);
		}
		return historyRepository.findAllByProjectIdOrderByVersionIdDesc(projectId);
	}

	@Override
	public List<Project> getUserProjects(final Long userId) {
		log.info("[Project] 사용자의 프로젝트 목록 조회 - userId: {}", userId);
		return encryptedKeyRepository.findProjectsByUserId(userId);
	}

	@Override
	public Project findById(final Long projectId) {
		log.info("[Project] 프로젝트 단건 조회 - projectId: {}", projectId);
		return projectRepository.findById(projectId)
			.orElseThrow(() -> new ProjectException(ErrorCode.PROJECT_NOT_FOUND));
	}
}
