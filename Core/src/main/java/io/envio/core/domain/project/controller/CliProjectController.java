package io.envio.core.domain.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.envio.core.common.response.BaseResponse;
import io.envio.core.common.util.ResponseUtils;
import io.envio.core.domain.project.dto.request.ProjectCreateReqDto;
import io.envio.core.domain.project.dto.request.ProjectWrappedKeySaveReqDto;
import io.envio.core.domain.project.dto.response.ProjectCreateResDto;
import io.envio.core.domain.project.dto.response.ProjectWrappedKeySaveResDto;
import io.envio.core.domain.project.service.facade.CliProjectFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "CLI Project", description = "CLI 프로젝트 생성 및 마스터 키 분배 API")
@RestController
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CliProjectController {

	private final CliProjectFacadeService cliProjectFacadeService;

	@Operation(summary = "CLI 프로젝트 생성", description = "GitHub 저장소 URL 기준으로 프로젝트를 생성하고 키 분배 대상 기기를 반환합니다.")
	@PostMapping("/api/v1/cli/create")
	public ResponseEntity<BaseResponse<ProjectCreateResDto>> createProject(
		@Valid @RequestBody final ProjectCreateReqDto reqDto
	) {
		ProjectCreateResDto response = cliProjectFacadeService.createProject(reqDto);
		return ResponseUtils.ok(response);
	}

	@Operation(summary = "CLI 프로젝트 마스터 키 분배 등록", description = "팀원별 공개키로 암호화한 프로젝트 마스터 키를 저장합니다.")
	@PutMapping("/api/projects/{projectId}/wrapped-keys")
	public ResponseEntity<BaseResponse<ProjectWrappedKeySaveResDto>> saveWrappedKeys(
		@PathVariable final Long projectId,
		@Valid @RequestBody final ProjectWrappedKeySaveReqDto reqDto
	) {
		ProjectWrappedKeySaveResDto response = cliProjectFacadeService.saveWrappedKeys(projectId, reqDto);
		return ResponseUtils.ok(response);
	}
}
