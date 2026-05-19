package io.envio.core.common.config.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;

@Validated
@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
	@NotEmpty List<String> allowedOrigins
) {
}
