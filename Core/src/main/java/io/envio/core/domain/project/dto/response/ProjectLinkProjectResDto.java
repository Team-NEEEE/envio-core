package io.envio.core.domain.project.dto.response;

import lombok.Builder;

@Builder
public record ProjectLinkProjectResDto(
	Long projectId,
	String projectName,
	String githubRepoName,
	String organizationName
) {
}
