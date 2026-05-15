package io.envio.core.domain.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ProjectCreateReqDto(
	@NotBlank
	String repositoryUrl,

	@NotNull
	Long deviceId,

	@NotBlank
	String publicKey
) {
}
