package io.envio.core.domain.project.converter;

import io.envio.core.domain.project.dto.response.ProjectPullResDto;
import io.envio.core.domain.project.dto.response.ProjectPushResDto;
import io.envio.core.domain.project.entity.History;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ProjectConverter {

	public static ProjectPullResDto toPullResponse(final History history, final String message) {
		return ProjectPullResDto.builder()
			.message(message)
			.historyId(history.getHistoriesId())
			.projectId(history.getProject().getId())
			.envName(history.getProject().getProjectName()) // Assuming envName comes from project or similar logic
			.versionId(history.getVersionId())
			.encryptedEnvironment(history.getEncryptedEnvironment())
			.createdAt(history.getCreatedAt())
			.updatedAt(history.getUpdatedAt())
			.build();
	}

	public static ProjectPushResDto toPushResponse(final History history, final String message) {
		return ProjectPushResDto.builder()
			.message(message)
			.historyId(history.getHistoriesId())
			.projectId(history.getProject().getId())
			.envName(history.getProject().getProjectName())
			.versionId(history.getVersionId())
			.parentVersionId(history.getBaseVersionId())
			.build();
	}
}
