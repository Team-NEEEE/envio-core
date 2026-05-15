package io.envio.core.domain.project.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.envio.core.domain.project.dto.request.ProjectPushReqDto;
import io.envio.core.domain.project.dto.response.ProjectHistoryResDto;
import io.envio.core.domain.project.entity.EncryptedKey;
import io.envio.core.domain.project.entity.Project;
import io.envio.core.domain.project.repository.EncryptedKeyRepository;
import io.envio.core.domain.project.repository.ProjectRepository;
import io.envio.core.domain.project.service.facade.ProjectFacadeService;
import io.envio.core.domain.user.entity.User;
import io.envio.core.domain.user.entity.UserDevice;
import io.envio.core.domain.user.entity.UserRole;
import io.envio.core.domain.user.repository.UserDeviceRepository;
import io.envio.core.domain.user.repository.UserRepository;

@SpringBootTest
@Transactional
@DisplayName("프로젝트_히스토리_흐름_통합_테스트")
class ProjectHistoryIntegrationTest {

	@Autowired
	private ProjectFacadeService projectFacadeService;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserDeviceRepository userDeviceRepository;

	@Autowired
	private EncryptedKeyRepository encryptedKeyRepository;

	@Test
	@DisplayName("여러_프로젝트의_생성부터_연속적인_Push_후_히스토리_조회까지_전체_흐름을_검증한다")
	void project_history_full_flow_test() {
		// 1. 프로젝트 A 생성
		Project projectA = projectRepository.save(Project.builder()
			.projectName("Project-A")
			.organizationName("Org-1")
			.versionId(0L)
			.build());

		// 2. 프로젝트 B 생성 (데이터 격리 확인용)
		Project projectB = projectRepository.save(Project.builder()
			.projectName("Project-B")
			.organizationName("Org-1")
			.versionId(0L)
			.build());

		User user = userRepository.save(User.builder()
			.githubId("user-1")
			.email("user1@envio.io")
			.role(UserRole.DEVELOPER)
			.build());
		UserDevice userDevice = userDeviceRepository.save(UserDevice.builder()
			.user(user)
			.deviceName("test-device")
			.publicKey("test-public-key")
			.build());
		encryptedKeyRepository.save(EncryptedKey.builder()
			.userDevice(userDevice)
			.project(projectA)
			.encryptedKey("project-a-key")
			.active(true)
			.build());
		encryptedKeyRepository.save(EncryptedKey.builder()
			.userDevice(userDevice)
			.project(projectB)
			.encryptedKey("project-b-key")
			.active(true)
			.build());

		// 3. 프로젝트 A에 대해 3번의 연속적인 Push 수행 (v1 -> v2 -> v3)
		// v1
		projectFacadeService.push(projectA.getId(), user.getId(), user.getGithubId(), ProjectPushReqDto.builder()
			.githubUserId("user-1")
			.encryptedEnvironment(Map.of("API_KEY", "enc_v1"))
			.parentVersionId(0L)
			.build());

		// v2
		projectFacadeService.push(projectA.getId(), user.getId(), "user-2", ProjectPushReqDto.builder()
			.githubUserId("user-2")
			.encryptedEnvironment(Map.of("API_KEY", "enc_v2"))
			.parentVersionId(1L)
			.build());

		// v3
		projectFacadeService.push(projectA.getId(), user.getId(), user.getGithubId(), ProjectPushReqDto.builder()
			.githubUserId("user-1")
			.encryptedEnvironment(Map.of("API_KEY", "enc_v3"))
			.parentVersionId(2L)
			.build());

		// 4. 프로젝트 B에 대해 2번의 연속적인 Push 수행 (v1 -> v2)
		projectFacadeService.push(projectB.getId(), user.getId(), "user-3", ProjectPushReqDto.builder()
			.githubUserId("user-3")
			.encryptedEnvironment(Map.of("DB_PASSWORD", "enc_b_v1"))
			.parentVersionId(0L)
			.build());

		projectFacadeService.push(projectB.getId(), user.getId(), "user-3", ProjectPushReqDto.builder()
			.githubUserId("user-3")
			.encryptedEnvironment(Map.of("DB_PASSWORD", "enc_b_v2"))
			.parentVersionId(1L)
			.build());

		// 5. 프로젝트 A의 히스토리 조회 및 검증
		List<ProjectHistoryResDto> historyA = projectFacadeService.getProjectHistory(projectA.getId(), user.getId());

		assertThat(historyA).hasSize(3);
		assertThat(historyA.getFirst().versionId()).isEqualTo(3L); // 최신순 정렬 확인
		assertThat(historyA.get(0).githubId()).isEqualTo("user-1");
		assertThat(historyA.get(0).baseVersionId()).isEqualTo(2L);

		assertThat(historyA.get(1).versionId()).isEqualTo(2L);
		assertThat(historyA.get(1).githubId()).isEqualTo("user-2");

		assertThat(historyA.get(2).versionId()).isEqualTo(1L);

		// 6. 프로젝트 B의 히스토리 조회 및 검증 (A의 데이터가 섞이지 않았는지 확인)
		List<ProjectHistoryResDto> historyB = projectFacadeService.getProjectHistory(projectB.getId(), user.getId());

		assertThat(historyB).hasSize(2);
		assertThat(historyB.get(0).versionId()).isEqualTo(2L);
		assertThat(historyB.get(0).githubId()).isEqualTo("user-3");
	}
}
