package io.envio.core.common.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.envio.core.common.config.properties.JwtProperties;

import jakarta.servlet.FilterChain;

@DisplayName("JWT authentication filter")
class JwtAuthenticationFilterTest {

	private static final String SECRET = "test-secret-key";

	private JwtAuthenticationFilter jwtAuthenticationFilter;
	private JwtTestTokenFactory tokenFactory;

	@BeforeEach
	void setUp() {
		ObjectMapper objectMapper = JsonMapper.builder()
			.addModule(new JavaTimeModule())
			.build();
		JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(new JwtProperties(SECRET), objectMapper);
		JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(objectMapper);
		jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider, entryPoint);
		tokenFactory = new JwtTestTokenFactory(SECRET);
		SecurityContextHolder.clearContext();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("registers SecurityContext for valid bearer token")
	void doFilterRegistersSecurityContextForValidBearerToken() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/core/users/me/projects");
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addHeader("Authorization", "Bearer " + tokenFactory.accessToken(futureEpochSecond()));
		AtomicBoolean chainInvoked = new AtomicBoolean(false);

		jwtAuthenticationFilter.doFilter(request, response, chainThatMarks(chainInvoked));

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		assertThat(chainInvoked.get()).isTrue();
		assertThat(authentication).isNotNull();
		assertThat(authentication.getPrincipal()).isInstanceOf(JwtClaims.class);
		assertThat(authentication.getAuthorities()).isEmpty();
	}

	@Test
	@DisplayName("does not register authentication when Authorization header is missing")
	void doFilterDoesNotRegisterAuthenticationWhenHeaderIsMissing() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/core/users/me/projects");
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicBoolean chainInvoked = new AtomicBoolean(false);

		jwtAuthenticationFilter.doFilter(request, response, chainThatMarks(chainInvoked));

		assertThat(chainInvoked.get()).isTrue();
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	@DisplayName("returns 401 for invalid token")
	void doFilterReturnsUnauthorizedForInvalidToken() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/core/users/me/projects");
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addHeader("Authorization", "Bearer invalid.token.value");
		AtomicBoolean chainInvoked = new AtomicBoolean(false);

		jwtAuthenticationFilter.doFilter(request, response, chainThatMarks(chainInvoked));

		assertThat(chainInvoked.get()).isFalse();
		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	private FilterChain chainThatMarks(final AtomicBoolean chainInvoked) {
		return (request, response) -> chainInvoked.set(true);
	}

	private long futureEpochSecond() {
		return Instant.now().plusSeconds(1800L).getEpochSecond();
	}
}
