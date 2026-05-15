package io.envio.core.domain.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ProjectLinkReqDto(
	@NotBlank
	String publicKey,

	@NotNull
	Long deviceId,

	@NotBlank
	String userGithubId,

	@NotBlank
	String repositoryUrl,

	String owner,

	String repoName
) {
}
