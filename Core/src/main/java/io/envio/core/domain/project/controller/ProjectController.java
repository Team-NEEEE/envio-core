package io.envio.core.domain.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.envio.core.common.response.BaseResponse;
import io.envio.core.common.util.ResponseUtils;
import io.envio.core.domain.project.dto.request.ProjectPushReqDto;
import io.envio.core.domain.project.dto.response.ProjectPullResDto;
import io.envio.core.domain.project.dto.response.ProjectPushResDto;
import io.envio.core.domain.project.service.facade.ProjectFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "프로젝트 (Project)", description = "프로젝트 환경변수 관리 API")
@RestController
@RequestMapping("/api/core/projects")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectController {

	private final ProjectFacadeService projectFacadeService;

	@Operation(summary = "최신 환경변수 조회 (Pull)", description = "특정 프로젝트의 최신 암호화 환경변수 버전을 조회합니다.")
	@PostMapping("/{projectId}/pull/latest")
	public ResponseEntity<BaseResponse<ProjectPullResDto>> pull(
		@PathVariable final Long projectId,
		@RequestParam final String githubUserId
	) {
		ProjectPullResDto response = projectFacadeService.pull(projectId, githubUserId);
		return ResponseUtils.ok(response);
	}

	@Operation(summary = "환경변수 새 버전 생성 (Push)", description = "로컬에서 암호화한 환경변수를 업로드하고 새 버전을 생성합니다.")
	@PostMapping("/{projectId}/push")
	public ResponseEntity<BaseResponse<ProjectPushResDto>> push(
		@PathVariable final Long projectId,
		@Valid @RequestBody final ProjectPushReqDto reqDto
	) {
		ProjectPushResDto response = projectFacadeService.push(projectId, reqDto);
		return ResponseUtils.ok(response);
	}
}
