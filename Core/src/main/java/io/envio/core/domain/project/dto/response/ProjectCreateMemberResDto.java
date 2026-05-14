package io.envio.core.domain.project.dto.response;

import lombok.Builder;

@Builder
public record ProjectCreateMemberResDto(
	Long userId,
	Long userDeviceId,
	String githubId,
	String publicKey,
	String projectRole
) {
}
