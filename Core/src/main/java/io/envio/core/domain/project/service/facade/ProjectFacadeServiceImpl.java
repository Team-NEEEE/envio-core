package io.envio.core.domain.project.service.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.core.domain.project.converter.ProjectConverter;
import io.envio.core.domain.project.dto.request.ProjectPushReqDto;
import io.envio.core.domain.project.dto.response.ProjectDetailResDto;
import io.envio.core.domain.project.dto.response.ProjectHistoryResDto;
import io.envio.core.domain.project.dto.response.ProjectPullResDto;
import io.envio.core.domain.project.dto.response.ProjectPushResDto;
import io.envio.core.domain.project.dto.response.ProjectResDto;
import io.envio.core.domain.project.entity.History;
import io.envio.core.domain.project.entity.Project;
import io.envio.core.domain.project.service.authorization.ProjectMembershipValidator;
import io.envio.core.domain.project.service.command.ProjectCommandService;
import io.envio.core.domain.project.service.query.ProjectQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectFacadeServiceImpl implements ProjectFacadeService {

	private final ProjectCommandService projectCommandService;
	private final ProjectQueryService projectQueryService;
	private final ProjectMembershipValidator projectMembershipValidator;

	@Override
	public ProjectPullResDto pull(final Long projectId, final Long userId, final String githubUserId) {
		projectMembershipValidator.validateProjectMember(projectId, userId);
		return pull(projectId, githubUserId);
	}

	@Override
	public ProjectPullResDto pull(final Long projectId, final String githubUserId) {
		log.info("[Project] 최신 환경변수 조회 요청 - projectId: {}, githubUserId: {}", projectId, githubUserId);
		History history = projectQueryService.getLatestHistory(projectId, githubUserId);
		return ProjectConverter.toPullResponse(history, "최신 환경변수 조회에 성공했습니다.");
	}

	@Override
	public ProjectPushResDto push(final Long projectId, final Long userId, final ProjectPushReqDto reqDto) {
		projectMembershipValidator.validateProjectMember(projectId, userId);
		return push(projectId, reqDto);
	}

	@Override
	public ProjectPushResDto push(final Long projectId, final ProjectPushReqDto reqDto) {
		log.info("[Project] 환경변수 업데이트 요청 - projectId: {}, githubUserId: {}", projectId, reqDto.githubUserId());
		History history = projectCommandService.push(projectId, reqDto);
		return ProjectConverter.toPushResponse(history, "환경변수 새 버전 생성에 성공했습니다.");
	}

	@Override
	public List<ProjectHistoryResDto> getProjectHistory(final Long projectId, final Long userId) {
		projectMembershipValidator.validateProjectMember(projectId, userId);
		return getProjectHistory(projectId);
	}

	@Override
	public List<ProjectHistoryResDto> getProjectHistory(final Long projectId) {
		log.info("[Project] 히스토리 조회 요청 - projectId: {}", projectId);
		List<History> histories = projectQueryService.getProjectHistories(projectId);
		return histories.stream()
			.map(ProjectConverter::toHistoryResponse)
			.toList();
	}

	@Override
	public ProjectDetailResDto getProjectDetail(final Long projectId, final Long userId) {
		projectMembershipValidator.validateProjectMember(projectId, userId);
		return getProjectDetail(projectId);
	}

	@Override
	public ProjectDetailResDto getProjectDetail(final Long projectId) {
		log.info("[Project] 상세 정보 조회 요청 - projectId: {}", projectId);
		Project project = projectQueryService.findById(projectId);
		return ProjectConverter.toProjectDetailResDto(project);
	}
}
