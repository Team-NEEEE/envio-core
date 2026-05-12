package io.envio.core.domain.project.service.facade;

import org.springframework.stereotype.Service;

import io.envio.core.domain.project.converter.ProjectConverter;
import io.envio.core.domain.project.dto.request.ProjectPushReqDto;
import io.envio.core.domain.project.dto.response.ProjectPullResDto;
import io.envio.core.domain.project.dto.response.ProjectPushResDto;
import io.envio.core.domain.project.entity.History;
import io.envio.core.domain.project.service.command.ProjectCommandService;
import io.envio.core.domain.project.service.query.ProjectQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectFacadeServiceImpl implements ProjectFacadeService {

	private final ProjectCommandService projectCommandService;
	private final ProjectQueryService projectQueryService;

	@Override
	public ProjectPullResDto pull(final Long projectId, final String githubUserId) {
		log.info("[Project] 최신 환경변수 조회 요청 - projectId: {}, githubUserId: {}", projectId, githubUserId);
		History history = projectQueryService.getLatestHistory(projectId);
		return ProjectConverter.toPullResponse(history, "최신 환경변수 조회에 성공했습니다.");
	}

	@Override
	public ProjectPushResDto push(final Long projectId, final ProjectPushReqDto reqDto) {
		log.info("[Project] 환경변수 업데이트 요청 - projectId: {}, githubUserId: {}", projectId, reqDto.githubUserId());
		History history = projectCommandService.push(projectId, reqDto);
		return ProjectConverter.toPushResponse(history, "환경변수 새 버전 생성에 성공했습니다.");
	}
}
