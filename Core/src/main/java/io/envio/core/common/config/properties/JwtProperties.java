package io.envio.core.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "auth.jwt")
public record JwtProperties(
	@NotBlank String secret
) {
}
