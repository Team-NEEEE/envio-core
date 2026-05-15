package io.envio.core.domain.project.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ProjectWrappedKeySaveReqDto(
	@NotNull
	Long deviceId,

	@NotBlank
	String publicKey,

	@Valid
	@NotEmpty
	List<ProjectWrappedKeyReqDto> wrappedKeys
) {
}
