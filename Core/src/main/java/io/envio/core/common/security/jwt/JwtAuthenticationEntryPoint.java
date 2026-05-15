package io.envio.core.common.security.jwt;

import java.io.IOException;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.envio.core.common.response.BaseResponse;
import io.envio.core.common.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private static final String UNAUTHORIZED_MESSAGE = "Authentication is required.";

	private final ObjectMapper objectMapper;

	@Override
	public void commence(
		@NonNull final HttpServletRequest request,
		@NonNull final HttpServletResponse response,
		@NonNull final AuthenticationException authException
	) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		ErrorResponse errorResponse = ErrorResponse.builder()
			.status(HttpStatus.UNAUTHORIZED)
			.message(UNAUTHORIZED_MESSAGE)
			.method(request.getMethod())
			.requestUri(request.getRequestURI())
			.errors(List.of())
			.build();

		objectMapper.writeValue(response.getWriter(), BaseResponse.fail(errorResponse));
	}
}
