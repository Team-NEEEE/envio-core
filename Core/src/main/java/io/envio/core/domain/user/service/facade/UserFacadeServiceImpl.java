package io.envio.core.domain.user.service.facade;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.core.domain.project.converter.ProjectConverter;
import io.envio.core.domain.project.dto.response.ProjectResDto;
import io.envio.core.domain.project.entity.Project;
import io.envio.core.domain.project.service.query.ProjectQueryService;
import io.envio.core.domain.user.entity.User;
import io.envio.core.domain.user.service.query.UserQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFacadeServiceImpl implements UserFacadeService {

	private final UserQueryService userQueryService;
	private final ProjectQueryService projectQueryService;

	@Override
	public Map<String, List<ProjectResDto>> getMyProjects(final String githubId) {
		log.info("[User] 내 프로젝트 목록 조회 요청 - githubId: {}", githubId);
		User user = userQueryService.findByGithubId(githubId);
		List<Project> projects = projectQueryService.getUserProjects(user.getId());
		
		return projects.stream()
			.collect(Collectors.groupingBy(
				Project::getOrganizationName,
				Collectors.mapping(ProjectConverter::toProjectResDto, Collectors.toList())
			));
	}
}
