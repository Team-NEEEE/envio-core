package io.envio.core.domain.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.envio.core.common.error.ErrorCode;
import io.envio.core.domain.project.dto.request.ProjectPushReqDto;
import io.envio.core.domain.project.entity.History;
import io.envio.core.domain.project.entity.Project;
import io.envio.core.domain.project.exception.ProjectException;
import io.envio.core.domain.project.repository.HistoryRepository;
import io.envio.core.domain.project.repository.ProjectRepository;
import io.envio.core.domain.project.service.command.ProjectCommandService;
import io.envio.core.domain.project.service.facade.ProjectFacadeService;
import io.envio.core.domain.project.service.query.ProjectQueryService;

@SpringBootTest
@Transactional
@DisplayName("프로젝트_서비스_통합_테스트")
class ProjectServiceIntegrationTest {

	@Autowired
	private ProjectCommandService projectCommandService;

	@Autowired
	private ProjectQueryService projectQueryService;

	@Autowired
	private ProjectFacadeService projectFacadeService;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private HistoryRepository historyRepository;

	private Project savedProject;

	@BeforeEach
	void setUp() {
		Project project = Project.builder()
			.projectName("test-project")
			.organizationName("test-org")
			.versionId(0L)
			.build();
		savedProject = projectRepository.save(project);
	}

	@Test
	@DisplayName("최신_환경변수를_조회한다")
	void getLatestHistory_success() {
		// given
		History history = History.builder()
			.project(savedProject)
			.versionId(1L)
			.baseVersionId(0L)
			.userGithubId("user1")
			.encryptedEnvironment(Map.of("data", "encrypted"))
			.build();
		historyRepository.save(history);

		// when
		History result = projectQueryService.getLatestHistory(savedProject.getId(), "user1");

		// then
		assertThat(result.getVersionId()).isEqualTo(1L);
		assertThat(result.getUserGithubId()).isEqualTo("user1");
	}

	@Test
	@DisplayName("이력이_없는_경우_조회_시_예외가_발생한다")
	void getLatestHistory_throwsException_whenEmpty() {
		// when & then
		assertThatThrownBy(() -> projectQueryService.getLatestHistory(savedProject.getId(), "user1"))
			.isInstanceOf(ProjectException.class)
			.hasMessage(ErrorCode.ENVIRONMENT_VERSION_NOT_INITIALIZED.getMessage());
	}

	@Test
	@DisplayName("최초_Push_시_parentVersionId가_0이면_성공한다")
	void push_initial_success() {
		// given
		ProjectPushReqDto reqDto = ProjectPushReqDto.builder()
			.githubUserId("user1")
			.encryptedEnvironment(Map.of("key", "secret"))
			.parentVersionId(0L)
			.build();

		// when
		History result = projectCommandService.push(savedProject.getId(), reqDto);

		// then
		assertThat(result.getVersionId()).isEqualTo(1L);
		assertThat(result.getBaseVersionId()).isEqualTo(0L);
		assertThat(savedProject.getVersionId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("최초_Push_시_parentVersionId가_0이_아니면_예외가_발생한다")
	void push_initial_fails_when_not_zero() {
		// given
		ProjectPushReqDto reqDto = ProjectPushReqDto.builder()
			.githubUserId("user1")
			.encryptedEnvironment(Map.of("key", "secret"))
			.parentVersionId(100L) // 최초인데 100을 보냄
			.build();

		// when & then
		assertThatThrownBy(() -> projectCommandService.push(savedProject.getId(), reqDto))
			.isInstanceOf(ProjectException.class)
			.hasMessage(ErrorCode.VERSION_CONFLICT.getMessage());
	}

	@Test
	@DisplayName("일반적인_Push_상황에서_새_버전을_생성한다")
	void push_subsequent_success() {
		// given
		// 1. 첫 번째 push
		History firstHistory = History.builder()
			.project(savedProject)
			.versionId(1L)
			.baseVersionId(0L)
			.build();
		historyRepository.save(firstHistory);
		savedProject.updateVersion(1L);

		// 2. 두 번째 push 요청
		ProjectPushReqDto reqDto = ProjectPushReqDto.builder()
			.githubUserId("user1")
			.encryptedEnvironment(Map.of("key", "new-secret"))
			.parentVersionId(1L)
			.build();

		// when
		History result = projectCommandService.push(savedProject.getId(), reqDto);

		// then
		assertThat(result.getVersionId()).isEqualTo(2L);
		assertThat(result.getBaseVersionId()).isEqualTo(1L);
		assertThat(savedProject.getVersionId()).isEqualTo(2L);
	}

	@Test
	@DisplayName("버전_충돌_시_예외가_발생한다")
	void push_throwsException_whenVersionConflict() {
		// given
		History history = History.builder()
			.project(savedProject)
			.versionId(1L)
			.baseVersionId(0L)
			.build();
		historyRepository.save(history);
		savedProject.updateVersion(1L);

		ProjectPushReqDto reqDto = ProjectPushReqDto.builder()
			.githubUserId("user1")
			.encryptedEnvironment(Map.of("key", "secret"))
			.parentVersionId(0L) // 서버는 이미 1인데 0을 기준으로 push 시도
			.build();

		// when & then
		assertThatThrownBy(() -> projectCommandService.push(savedProject.getId(), reqDto))
			.isInstanceOf(ProjectException.class)
			.hasMessage(ErrorCode.VERSION_CONFLICT.getMessage());
	}

	@Test
	@DisplayName("존재하지_않는_프로젝트에_Push_시_예외가_발생한다")
	void push_fails_when_project_not_found() {
		// given
		ProjectPushReqDto reqDto = ProjectPushReqDto.builder()
			.githubUserId("user1")
			.encryptedEnvironment(Map.of("key", "secret"))
			.parentVersionId(0L)
			.build();

		// when & then
		assertThatThrownBy(() -> projectCommandService.push(999L, reqDto))
			.isInstanceOf(ProjectException.class)
			.hasMessage(ErrorCode.PROJECT_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("프로젝트_히스토리_전체를_조회한다")
	void getProjectHistory_success() {
		// given
		History h1 = History.builder()
			.project(savedProject)
			.versionId(1L)
			.userGithubId("user1")
			.build();
		History h2 = History.builder()
			.project(savedProject)
			.versionId(2L)
			.userGithubId("user2")
			.build();
		historyRepository.save(h1);
		historyRepository.save(h2);

		// when
		java.util.List<History> results = projectQueryService.getProjectHistories(savedProject.getId());

		// then
		assertThat(results).hasSize(2);
		assertThat(results.get(0).getVersionId()).isEqualTo(2L); // 최신순 정렬 확인
		assertThat(results.get(1).getVersionId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("프로젝트_상세_정보를_조회한다")
	void getProjectDetail_success() {
		// when
		io.envio.core.domain.project.dto.response.ProjectDetailResDto result = projectFacadeService.getProjectDetail(savedProject.getId());

		// then
		assertThat(result.projectId()).isEqualTo(savedProject.getId());
		assertThat(result.projectName()).isEqualTo("test-project");
		assertThat(result.organizationName()).isEqualTo("test-org");
	}
}
