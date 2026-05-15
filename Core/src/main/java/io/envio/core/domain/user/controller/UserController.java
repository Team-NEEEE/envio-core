package io.envio.core.domain.user.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.envio.core.common.response.BaseResponse;
import io.envio.core.common.util.ResponseUtils;
import io.envio.core.domain.project.dto.response.ProjectResDto;
import io.envio.core.domain.user.service.facade.UserFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "사용자 (User)", description = "사용자 정보 및 소속 프로젝트 조회 API")
@RestController
@RequestMapping("/api/core/users")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserController {

	private final UserFacadeService userFacadeService;

	@Operation(summary = "내 조직, 조직에 속하는 프로젝트 조회", description = "현재 로그인한 사용자가 속한 조직을 조회합니다.")
	@GetMapping("/me/projects")
	public ResponseEntity<BaseResponse<Map<String, List<ProjectResDto>>>> getMyProjects(
		@RequestParam final String githubId
	) {
		Map<String, List<ProjectResDto>> response = userFacadeService.getMyProjects(githubId);
		return ResponseUtils.ok(response);
	}
}
