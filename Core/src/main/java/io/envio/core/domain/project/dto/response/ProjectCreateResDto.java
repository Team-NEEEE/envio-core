package io.envio.core.domain.project.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record ProjectCreateResDto(
	String message,
	Long projectId,
	String projectName,
	String githubRepoName,
	Long installationId,
	List<ProjectCreateMemberResDto> members
) {
}
