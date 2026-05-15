package io.envio.core.common.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityConstants {

	public static final String[] PUBLIC_URLS = {
		"/swagger-ui/**",
		"/v3/api-docs/**",
		"/actuator/health",
		"/api/core/projects/*/pull/latest",
		"/api/core/projects/*/push",
		"/api/v1/cli/create",
		"/api/core/projects/link",
		"/api/projects/*/wrapped-keys"
	};
}
