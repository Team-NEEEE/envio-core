package io.envio.core.domain.project.converter;

import io.envio.core.domain.project.dto.response.ProjectHistoryResDto;
import io.envio.core.domain.project.dto.response.ProjectPullResDto;
import io.envio.core.domain.project.dto.response.ProjectPushResDto;
import io.envio.core.domain.project.dto.response.ProjectResDto;
import io.envio.core.domain.project.entity.History;
import io.envio.core.domain.project.entity.Project;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ProjectConverter {

	public static ProjectResDto toProjectResDto(final Project project) {
		return ProjectResDto.builder()
			.projectId(project.getId())
			.projectName(project.getProjectName())
			.description(project.getDescription())
			.versionId(project.getVersionId())
			.build();
	}

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

	public static ProjectHistoryResDto toHistoryResponse(final History history) {
		return ProjectHistoryResDto.builder()
			.historyId(history.getHistoriesId())
			.projectId(history.getProject().getId())
			.versionId(history.getVersionId())
			.baseVersionId(history.getBaseVersionId())
			.githubId(history.getUserGithubId())
			.createdAt(history.getCreatedAt())
			.build();
	}
}
