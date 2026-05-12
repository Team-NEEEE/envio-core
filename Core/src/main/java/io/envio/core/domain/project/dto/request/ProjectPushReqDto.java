package io.envio.core.domain.project.dto.request;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ProjectPushReqDto(
	@NotBlank(message = "GitHub 사용자 ID는 필수입니다.")
	String githubUserId,

	@NotNull(message = "암호화된 환경변수는 필수입니다.")
	Map<String, Object> encryptedEnvironment,

	@NotNull(message = "기준 버전 ID는 필수입니다.")
	Long parentVersionId
) {
}
