package io.envio.core.domain.project.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record ProjectPullResDto(
	String message,
	Long historyId,
	Long projectId,
	String envName,
	Long versionId,
	Map<String, Object> encryptedEnvironment,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime createdAt,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime updatedAt
) {
}
