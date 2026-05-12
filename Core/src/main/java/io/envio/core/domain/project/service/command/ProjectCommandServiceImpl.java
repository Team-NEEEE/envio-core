package io.envio.core.domain.project.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.core.common.error.ErrorCode;
import io.envio.core.domain.project.dto.request.ProjectPushReqDto;
import io.envio.core.domain.project.entity.History;
import io.envio.core.domain.project.entity.Project;
import io.envio.core.domain.project.exception.ProjectException;
import io.envio.core.domain.project.repository.HistoryRepository;
import io.envio.core.domain.project.repository.ProjectRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectCommandServiceImpl implements ProjectCommandService {

	private final ProjectRepository projectRepository;
	private final HistoryRepository historyRepository;

	@Override
	public History push(final Long projectId, final ProjectPushReqDto reqDto) {
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new ProjectException(ErrorCode.PROJECT_NOT_FOUND));

		// 최신 버전 확인 (충돌 체크)
		historyRepository.findFirstByProjectIdOrderByVersionIdDesc(projectId)
			.ifPresentOrElse(
				latest -> {
					if (!latest.getVersionId().equals(reqDto.parentVersionId())) {
						throw new ProjectException(ErrorCode.VERSION_CONFLICT);
					}
				},
				() -> {
					// 최초 push는 parentVersionId가 0이어야 함
					if (!reqDto.parentVersionId().equals(0L)) {
						throw new ProjectException(ErrorCode.VERSION_CONFLICT);
					}
				}
			);

		// 새로운 버전 번호 계산
		Long nextVersionId = reqDto.parentVersionId() + 1;

		// 새로운 히스토리 생성
		History history = History.builder()
			.project(project)
			.versionId(nextVersionId)
			.baseVersionId(reqDto.parentVersionId())
			.userGithubId(reqDto.githubUserId())
			.encryptedEnvironment(reqDto.encryptedEnvironment())
			.createdAt(java.time.LocalDateTime.now())
			.updatedAt(java.time.LocalDateTime.now())
			.build();

		// 프로젝트의 최신 버전 업데이트
		project.updateVersion(nextVersionId);

		History savedHistory = historyRepository.save(history);
		log.info("[Project] 새로운 환경변수 버전 생성 성공 - projectId: {}, versionId: {}", projectId, nextVersionId);
		return savedHistory;
	}
}
