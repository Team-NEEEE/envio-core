package io.envio.core.domain.user.service.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.core.domain.project.dto.response.ProjectResDto;
import io.envio.core.domain.project.service.facade.ProjectFacadeService;
import io.envio.core.domain.user.entity.User;
import io.envio.core.domain.user.service.query.UserQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFacadeServiceImpl implements UserFacadeService {

	private final UserQueryService userQueryService;
	private final ProjectFacadeService projectFacadeService;

	@Override
	public List<ProjectResDto> getMyProjects(final String githubId) {
		log.info("[User] 내 프로젝트 목록 조회 요청 - githubId: {}", githubId);
		User user = userQueryService.findByGithubId(githubId);
		return projectFacadeService.getUserProjects(user.getId());
	}
}
