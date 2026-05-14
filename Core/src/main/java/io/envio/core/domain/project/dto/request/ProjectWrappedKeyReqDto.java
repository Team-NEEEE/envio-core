package io.envio.core.domain.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ProjectWrappedKeyReqDto(
	@NotNull
	Long userId,

	@NotNull
	Long userDeviceId,

	@NotBlank
	String encryptedKey
) {
}
