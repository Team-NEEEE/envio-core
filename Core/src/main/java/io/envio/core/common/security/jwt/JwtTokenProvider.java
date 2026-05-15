package io.envio.core.common.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.envio.core.common.config.properties.JwtProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private static final String HMAC_SHA256 = "HmacSHA256";
	private static final String JWT_ALGORITHM = "HS256";
	private static final String JWT_TYPE = "JWT";
	private static final String ACCESS_TOKEN_TYPE = "access";

	private final JwtProperties jwtProperties;
	private final ObjectMapper objectMapper;
	private final Clock clock = Clock.systemUTC();

	public JwtClaims parseAccessToken(final String token) {
		String[] parts = splitToken(token);
		Map<String, Object> header = decodeJson(parts[0], "Invalid JWT header");
		Map<String, Object> payload = decodeJson(parts[1], "Invalid JWT payload");

		validateHeader(header);
		validateSignature(parts);
		validateExpiresAt(payload);
		validateTokenType(payload);

		return new JwtClaims(
			requiredLong(payload, "userId"),
			requiredString(payload, "githubId"),
			requiredString(payload, "email"),
			requiredString(payload, "role")
		);
	}

	private String[] splitToken(final String token) {
		if (!StringUtils.hasText(token)) {
			throw new JwtParsingException("JWT token is blank");
		}

		String[] parts = token.split("\\.", -1);
		if (parts.length != 3 || !StringUtils.hasText(parts[0])
			|| !StringUtils.hasText(parts[1]) || !StringUtils.hasText(parts[2])) {
			throw new JwtParsingException("JWT token must have three parts");
		}
		return parts;
	}

	private Map<String, Object> decodeJson(final String encoded, final String message) {
		try {
			byte[] decoded = Base64.getUrlDecoder().decode(encoded);
			return objectMapper.readValue(decoded, new TypeReference<>() {
			});
		} catch (Exception ex) {
			throw new JwtParsingException(message, ex);
		}
	}

	private void validateHeader(final Map<String, Object> header) {
		if (!JWT_ALGORITHM.equals(header.get("alg"))) {
			throw new JwtParsingException("JWT alg must be HS256");
		}
		if (!JWT_TYPE.equals(header.get("typ"))) {
			throw new JwtParsingException("JWT typ must be JWT");
		}
	}

	private void validateSignature(final String[] parts) {
		byte[] expected = sign(parts[0] + "." + parts[1]);
		byte[] actual;
		try {
			actual = Base64.getUrlDecoder().decode(parts[2]);
		} catch (IllegalArgumentException ex) {
			throw new JwtParsingException("Invalid JWT signature encoding", ex);
		}

		if (!MessageDigest.isEqual(expected, actual)) {
			throw new JwtParsingException("Invalid JWT signature");
		}
	}

	private byte[] sign(final String unsignedToken) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA256);
			SecretKeySpec keySpec = new SecretKeySpec(
				jwtProperties.secret().getBytes(StandardCharsets.UTF_8),
				HMAC_SHA256
			);
			mac.init(keySpec);
			return mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8));
		} catch (Exception ex) {
			throw new JwtParsingException("Failed to verify JWT signature", ex);
		}
	}

	private void validateExpiresAt(final Map<String, Object> payload) {
		Long expiresAt = requiredLong(payload, "exp");
		if (Instant.ofEpochSecond(expiresAt).isBefore(Instant.now(clock))) {
			throw new JwtParsingException("JWT token has expired");
		}
	}

	private void validateTokenType(final Map<String, Object> payload) {
		if (!ACCESS_TOKEN_TYPE.equals(payload.get("tokenType"))) {
			throw new JwtParsingException("JWT tokenType must be access");
		}
	}

	private String requiredString(final Map<String, Object> payload, final String claimName) {
		Object value = payload.get(claimName);
		if (value instanceof String stringValue && StringUtils.hasText(stringValue)) {
			return stringValue;
		}
		throw new JwtParsingException("Required JWT claim is missing: " + claimName);
	}

	private Long requiredLong(final Map<String, Object> payload, final String claimName) {
		Object value = payload.get(claimName);
		if (value instanceof Number numberValue) {
			return numberValue.longValue();
		}
		throw new JwtParsingException("Required JWT claim is missing: " + claimName);
	}
}
