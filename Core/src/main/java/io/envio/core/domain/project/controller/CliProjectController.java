package io.envio.core.domain.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.envio.core.common.response.BaseResponse;
import io.envio.core.common.util.ResponseUtils;
import io.envio.core.domain.project.dto.request.ProjectCreateReqDto;
import io.envio.core.domain.project.dto.request.ProjectLinkReqDto;
import io.envio.core.domain.project.dto.request.ProjectWrappedKeySaveReqDto;
import io.envio.core.domain.project.dto.response.ProjectCreateResDto;
import io.envio.core.domain.project.dto.response.ProjectHistoryResDto;
import io.envio.core.domain.project.dto.response.ProjectLinkResDto;
import io.envio.core.domain.project.dto.response.ProjectWrappedKeySaveResDto;
import io.envio.core.domain.project.service.facade.CliProjectFacadeService;
import io.envio.core.domain.project.service.facade.ProjectFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "CLI Project", description = "CLI project create, link, and wrapped key APIs")
@RestController
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CliProjectController {

	private final ProjectFacadeService projectFacadeService;

	private final CliProjectFacadeService cliProjectFacadeService;

	@Operation(
		summary = "CLI project create",
		description = "Creates a project and returns target devices for key wrapping."
	)
	@PostMapping("/api/v1/cli/create")
	public ResponseEntity<BaseResponse<ProjectCreateResDto>> createProject(
		@Valid @RequestBody final ProjectCreateReqDto reqDto
	) {
		ProjectCreateResDto response = cliProjectFacadeService.createProject(reqDto);
		return ResponseUtils.ok(response);
	}

	@Operation(
		summary = "CLI project link",
		description = "Links a local working directory and returns this device's wrapped master key."
	)
	@PostMapping("/api/core/projects/link")
	public ResponseEntity<BaseResponse<ProjectLinkResDto>> linkProject(
		@Valid @RequestBody final ProjectLinkReqDto reqDto
	) {
		ProjectLinkResDto response = cliProjectFacadeService.linkProject(reqDto);
		return ResponseUtils.ok(response);
	}

	@Operation(
		summary = "CLI wrapped key save",
		description = "Saves project master keys wrapped for each target user device."
	)
	@PutMapping("/api/projects/{projectId}/wrapped-keys")
	public ResponseEntity<BaseResponse<ProjectWrappedKeySaveResDto>> saveWrappedKeys(
		@PathVariable final Long projectId,
		@Valid @RequestBody final ProjectWrappedKeySaveReqDto reqDto
	) {
		ProjectWrappedKeySaveResDto response = cliProjectFacadeService.saveWrappedKeys(projectId, reqDto);
		return ResponseUtils.ok(response);
	}

	@Operation(summary = "프로젝트 히스토리 조회", description = "특정 프로젝트의 버전 히스토리를 조회합니다.")
	@GetMapping("/api/cli/projects/{projectId}/history")
	public ResponseEntity<BaseResponse<List<ProjectHistoryResDto>>> getProjectHistory(
		@PathVariable final Long projectId
	) {
		List<ProjectHistoryResDto> response = projectFacadeService.getProjectHistory(projectId);
		return ResponseUtils.ok(response);
	}
}
