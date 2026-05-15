package io.envio.core.common.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;

final class JwtTestTokenFactory {

	private static final String HMAC_SHA256 = "HmacSHA256";

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String secret;

	JwtTestTokenFactory(final String secret) {
		this.secret = secret;
	}

	String accessToken(final long expiresAt) {
		return token(payload("access", expiresAt));
	}

	String refreshToken(final long expiresAt) {
		return token(payload("refresh", expiresAt));
	}

	String tokenWithoutClaim(final String claimName, final long expiresAt) {
		Map<String, Object> payload = payload("access", expiresAt);
		payload.remove(claimName);
		return token(payload);
	}

	String token(final Map<String, Object> payload) {
		Map<String, Object> header = Map.of(
			"alg", "HS256",
			"typ", "JWT"
		);
		String encodedHeader = encode(header);
		String encodedPayload = encode(payload);
		String unsignedToken = encodedHeader + "." + encodedPayload;
		return unsignedToken + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(sign(unsignedToken));
	}

	private Map<String, Object> payload(final String tokenType, final long expiresAt) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("sub", "1");
		payload.put("userId", 1L);
		payload.put("githubId", "123456");
		payload.put("email", "user@example.com");
		payload.put("role", "VIEWER");
		payload.put("tokenType", tokenType);
		payload.put("iat", expiresAt - 1800L);
		payload.put("exp", expiresAt);
		return payload;
	}

	private String encode(final Map<String, Object> value) {
		try {
			byte[] json = objectMapper.writeValueAsBytes(value);
			return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to encode JWT test value", ex);
		}
	}

	private byte[] sign(final String unsignedToken) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA256);
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
			return mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8));
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to sign JWT test token", ex);
		}
	}
}
