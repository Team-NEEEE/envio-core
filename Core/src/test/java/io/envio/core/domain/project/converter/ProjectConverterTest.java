package io.envio.core.domain.project.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.envio.core.domain.project.dto.response.ProjectPullResDto;
import io.envio.core.domain.project.dto.response.ProjectPushResDto;
import io.envio.core.domain.project.entity.History;
import io.envio.core.domain.project.entity.Project;

@DisplayName("프로젝트_변환기_테스트")
class ProjectConverterTest {

	@Test
	@DisplayName("히스토리_엔티티를_Pull_응답_DTO로_변환한다")
	void toPullResponse_mapsHistoryToDto() {
		// given
		Project project = Project.builder()
			.id(1L)
			.projectName("test-project")
			.build();

		History history = History.builder()
			.historiesId(100L)
			.project(project)
			.versionId(7L)
			.encryptedEnvironment(Map.of("key", "value"))
			.build();

		// when
		ProjectPullResDto result = ProjectConverter.toPullResponse(history, "success");

		// then
		assertThat(result.historyId()).isEqualTo(100L);
		assertThat(result.versionId()).isEqualTo(7L);
		assertThat(result.message()).isEqualTo("success");
		assertThat(result.encryptedEnvironment()).containsEntry("key", "value");
		assertThat(result.wrappedMasterKey()).isNull();
	}

	@Test
	@DisplayName("히스토리_엔티티를_래핑키가_포함된_Pull_응답_DTO로_변환한다")
	void toPullResponse_mapsWrappedMasterKeyToDto() {
		// given
		Project project = Project.builder()
			.id(1L)
			.projectName("test-project")
			.build();

		History history = History.builder()
			.historiesId(100L)
			.project(project)
			.versionId(7L)
			.encryptedEnvironment(Map.of("key", "value"))
			.build();

		// when
		ProjectPullResDto result = ProjectConverter.toPullResponse(history, "success", "wrapped-key");

		// then
		assertThat(result.wrappedMasterKey()).isEqualTo("wrapped-key");
	}

	@Test
	@DisplayName("히스토리_엔티티를_Push_응답_DTO로_변환한다")
	void toPushResponse_mapsHistoryToDto() {
		// given
		Project project = Project.builder()
			.id(1L)
			.projectName("test-project")
			.build();

		History history = History.builder()
			.historiesId(101L)
			.project(project)
			.versionId(8L)
			.baseVersionId(7L)
			.build();

		// when
		ProjectPushResDto result = ProjectConverter.toPushResponse(history, "created");

		// then
		assertThat(result.historyId()).isEqualTo(101L);
		assertThat(result.versionId()).isEqualTo(8L);
		assertThat(result.parentVersionId()).isEqualTo(7L);
		assertThat(result.message()).isEqualTo("created");
	}
}
