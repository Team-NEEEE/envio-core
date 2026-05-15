package io.envio.core.domain.project.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.envio.core.common.error.ErrorCode;
import io.envio.core.domain.project.exception.ProjectException;

@Component
public class DefaultGithubRepositoryMemberClient implements GithubRepositoryMemberClient {

	private static final String GITHUB_API_VERSION = "2022-11-28";
	private static final String JWT_HEADER = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
	private static final byte[] PKCS8_RSA_PREFIX = {
		0x30, 0x0d, 0x06, 0x09, 0x2a, (byte)0x86, 0x48, (byte)0x86,
		(byte)0xf7, 0x0d, 0x01, 0x01, 0x01, 0x05, 0x00
	};

	private final RestClient restClient;
	private final String githubAppIdentifier;
	private final String githubAppPrivateKey;
	private final String githubAppPrivateKeyPath;

	public DefaultGithubRepositoryMemberClient(
		@Value("${GITHUB_APP_ID:}") final String githubAppId,
		@Value("${GITHUB_CLIENT_ID:}") final String githubClientId,
		@Value("${GITHUB_APP_PRIVATE_KEY:}") final String githubAppPrivateKey,
		@Value("${GITHUB_APP_PRIVATE_KEY_PATH:}") final String githubAppPrivateKeyPath
	) {
		this.restClient = RestClient.builder()
			.baseUrl("https://api.github.com")
			.build();
		this.githubAppIdentifier = githubAppId.isBlank() ? githubClientId : githubAppId;
		this.githubAppPrivateKey = githubAppPrivateKey;
		this.githubAppPrivateKeyPath = githubAppPrivateKeyPath;
	}

	@Override
	public GithubRepositoryAccess getRepositoryAccess(final String owner, final String repoName) {
		try {
			String appJwt = createAppJwt();
			Long installationId = fetchInstallationId(owner, repoName, appJwt);
			String installationToken = createInstallationAccessToken(installationId, appJwt);
			List<GithubRepositoryMember> members = fetchCollaborators(owner, repoName, installationToken);
			return new GithubRepositoryAccess(installationId, members);
		} catch (GeneralSecurityException | IOException | RestClientException exception) {
			throw new ProjectException(ErrorCode.ACCESS_DENIED);
		}
	}

	private Long fetchInstallationId(final String owner, final String repoName, final String appJwt) {
		GithubInstallationResponse response = restClient.get()
			.uri("/repos/{owner}/{repoName}/installation", owner, repoName)
			.headers(headers -> setGithubHeaders(headers, appJwt))
			.retrieve()
			.body(GithubInstallationResponse.class);

		if (response == null || response.id() == null) {
			throw new ProjectException(ErrorCode.ACCESS_DENIED);
		}
		return response.id();
	}

	private String createInstallationAccessToken(final Long installationId, final String appJwt) {
		GithubInstallationTokenResponse response = restClient.post()
			.uri("/app/installations/{installationId}/access_tokens", installationId)
			.headers(headers -> setGithubHeaders(headers, appJwt))
			.retrieve()
			.body(GithubInstallationTokenResponse.class);

		if (response == null || response.token() == null || response.token().isBlank()) {
			throw new ProjectException(ErrorCode.ACCESS_DENIED);
		}
		return response.token();
	}

	private List<GithubRepositoryMember> fetchCollaborators(
		final String owner,
		final String repoName,
		final String installationToken
	) {
		GithubCollaboratorResponse[] responses = restClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/repos/{owner}/{repoName}/collaborators")
				.queryParam("affiliation", "all")
				.queryParam("per_page", 100)
				.build(owner, repoName))
			.headers(headers -> setGithubHeaders(headers, installationToken))
			.retrieve()
			.body(GithubCollaboratorResponse[].class);

		if (responses == null) {
			return List.of();
		}

		return Arrays.stream(responses)
			.map(GithubCollaboratorResponse::toMember)
			.toList();
	}

	private void setGithubHeaders(final HttpHeaders headers, final String bearerToken) {
		headers.set(HttpHeaders.ACCEPT, "application/vnd.github+json");
		headers.set("X-GitHub-Api-Version", GITHUB_API_VERSION);
		headers.setBearerAuth(bearerToken);
	}

	private String createAppJwt() throws GeneralSecurityException, IOException {
		if (githubAppIdentifier.isBlank()) {
			throw new ProjectException(ErrorCode.ACCESS_DENIED);
		}

		Instant now = Instant.now();
		String payload = "{\"iat\":" + now.minusSeconds(60).getEpochSecond()
			+ ",\"exp\":" + now.plusSeconds(540).getEpochSecond()
			+ ",\"iss\":\"" + githubAppIdentifier + "\"}";
		String signingInput = base64Url(JWT_HEADER.getBytes(StandardCharsets.UTF_8))
			+ "." + base64Url(payload.getBytes(StandardCharsets.UTF_8));
		byte[] signature = sign(signingInput.getBytes(StandardCharsets.UTF_8), loadPrivateKey());
		return signingInput + "." + base64Url(signature);
	}

	private byte[] sign(final byte[] signingInput, final PrivateKey privateKey) throws GeneralSecurityException {
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privateKey);
		signature.update(signingInput);
		return signature.sign();
	}

	private PrivateKey loadPrivateKey() throws GeneralSecurityException, IOException {
		String pem = readPrivateKeyPem();
		byte[] privateKeyDer = parsePrivateKeyDer(pem);
		return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyDer));
	}

	private String readPrivateKeyPem() throws IOException {
		if (!githubAppPrivateKey.isBlank()) {
			return githubAppPrivateKey.replace("\\n", "\n");
		}
		if (!githubAppPrivateKeyPath.isBlank()) {
			return Files.readString(Path.of(githubAppPrivateKeyPath), StandardCharsets.UTF_8);
		}
		throw new ProjectException(ErrorCode.ACCESS_DENIED);
	}

	private byte[] parsePrivateKeyDer(final String pem) {
		String normalizedPem = pem.strip();
		if (normalizedPem.contains("BEGIN RSA PRIVATE KEY")) {
			byte[] pkcs1 = decodePem(normalizedPem, "RSA PRIVATE KEY");
			return wrapPkcs1AsPkcs8(pkcs1);
		}
		return decodePem(normalizedPem, "PRIVATE KEY");
	}

	private byte[] decodePem(final String pem, final String label) {
		String base64 = pem
			.replace("-----BEGIN " + label + "-----", "")
			.replace("-----END " + label + "-----", "")
			.replaceAll("\\s", "");
		return Base64.getDecoder().decode(base64);
	}

	private byte[] wrapPkcs1AsPkcs8(final byte[] pkcs1) {
		byte[] version = derIntegerZero();
		byte[] privateKey = derOctetString(pkcs1);
		return derSequence(version, PKCS8_RSA_PREFIX, privateKey);
	}

	private byte[] derIntegerZero() {
		return new byte[] {0x02, 0x01, 0x00};
	}

	private byte[] derOctetString(final byte[] value) {
		return concat(new byte[] {0x04}, derLength(value.length), value);
	}

	private byte[] derSequence(final byte[]... values) {
		int contentLength = Arrays.stream(values)
			.mapToInt(value -> value.length)
			.sum();
		return concat(new byte[] {0x30}, derLength(contentLength), concat(values));
	}

	private byte[] derLength(final int length) {
		if (length < 128) {
			return new byte[] {(byte)length};
		}

		int value = length;
		int byteCount = 0;
		while (value > 0) {
			byteCount++;
			value >>= 8;
		}

		byte[] result = new byte[byteCount + 1];
		result[0] = (byte)(0x80 | byteCount);
		for (int index = byteCount; index > 0; index--) {
			result[index] = (byte)(length >> (8 * (byteCount - index)));
		}
		return result;
	}

	private byte[] concat(final byte[]... arrays) {
		int totalLength = Arrays.stream(arrays)
			.mapToInt(array -> array.length)
			.sum();
		byte[] result = new byte[totalLength];
		int offset = 0;
		for (byte[] array : arrays) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	private String base64Url(final byte[] value) {
		return Base64.getUrlEncoder()
			.withoutPadding()
			.encodeToString(value);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record GithubInstallationResponse(
		Long id
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record GithubInstallationTokenResponse(
		String token
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record GithubCollaboratorResponse(
		String login,
		@JsonProperty("role_name")
		String roleName,
		Map<String, Boolean> permissions
	) {

		private GithubRepositoryMember toMember() {
			ProjectRole role = roleName == null
				? ProjectRole.fromGithubPermissions(permissions == null ? Map.of() : permissions)
				: ProjectRole.fromGithubPermissionName(roleName);
			return new GithubRepositoryMember(login, role);
		}
	}
}
