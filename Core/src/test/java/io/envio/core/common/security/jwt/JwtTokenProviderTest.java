package io.envio.core.common.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.envio.core.common.config.properties.JwtProperties;

@DisplayName("JWT token provider")
class JwtTokenProviderTest {

	private static final String SECRET = "test-secret-key";

	private JwtTokenProvider jwtTokenProvider;
	private JwtTestTokenFactory tokenFactory;

	@BeforeEach
	void setUp() {
		jwtTokenProvider = new JwtTokenProvider(new JwtProperties(SECRET), new ObjectMapper());
		tokenFactory = new JwtTestTokenFactory(SECRET);
	}

	@Test
	@DisplayName("parses valid access token")
	void parseAccessTokenParsesValidAccessToken() {
		String token = tokenFactory.accessToken(futureEpochSecond());

		JwtClaims claims = jwtTokenProvider.parseAccessToken(token);

		assertThat(claims.userId()).isEqualTo(1L);
		assertThat(claims.githubId()).isEqualTo("123456");
		assertThat(claims.email()).isEqualTo("user@example.com");
		assertThat(claims.role()).isEqualTo("VIEWER");
	}

	@Test
	@DisplayName("rejects refresh token")
	void parseAccessTokenRejectsRefreshToken() {
		String token = tokenFactory.refreshToken(futureEpochSecond());

		assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(token))
			.isInstanceOf(JwtParsingException.class)
			.hasMessageContaining("tokenType");
	}

	@Test
	@DisplayName("rejects expired token")
	void parseAccessTokenRejectsExpiredToken() {
		String token = tokenFactory.accessToken(Instant.now().minusSeconds(60L).getEpochSecond());

		assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(token))
			.isInstanceOf(JwtParsingException.class)
			.hasMessageContaining("expired");
	}

	@Test
	@DisplayName("rejects tampered signature")
	void parseAccessTokenRejectsTamperedSignature() {
		String token = tokenFactory.accessToken(futureEpochSecond());
		String tamperedToken = token.substring(0, token.length() - 2) + "aa";

		assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(tamperedToken))
			.isInstanceOf(JwtParsingException.class)
			.hasMessageContaining("signature");
	}

	@Test
	@DisplayName("rejects missing required claim")
	void parseAccessTokenRejectsMissingRequiredClaim() {
		String token = tokenFactory.tokenWithoutClaim("email", futureEpochSecond());

		assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(token))
			.isInstanceOf(JwtParsingException.class)
			.hasMessageContaining("email");
	}

	private long futureEpochSecond() {
		return Instant.now().plusSeconds(1800L).getEpochSecond();
	}
}
