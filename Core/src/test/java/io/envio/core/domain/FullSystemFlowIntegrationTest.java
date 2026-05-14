package io.envio.core.domain;

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
import io.envio.core.domain.project.dto.response.ProjectDetailResDto;
import io.envio.core.domain.project.dto.response.ProjectHistoryResDto;
import io.envio.core.domain.project.dto.response.ProjectResDto;
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
import io.envio.core.domain.user.service.facade.UserFacadeService;

@SpringBootTest
@Transactional
@DisplayName("전체_시스템_흐름_통합_테스트")
class FullSystemFlowIntegrationTest {

	@Autowired
	private UserFacadeService userFacadeService;

	@Autowired
	private ProjectFacadeService projectFacadeService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserDeviceRepository userDeviceRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private EncryptedKeyRepository encryptedKeyRepository;

	@Test
	@DisplayName("사용자_등록부터_기기설정_프로젝트연동_데이터푸시_및_전체조회까지_풀플로우를_검증한다")
	void full_system_lifecycle_test() {
		// [Step 1] 사용자 가입 및 식별
		String githubId = "full-flow-user";
		User user = userRepository.save(User.builder()
			.githubId(githubId)
			.email("fullflow@envio.io")
			.role(UserRole.OWNER)
			.build());

		// [Step 2] 사용자의 기기 등록 (여러 대의 기기 사용 상황)
		UserDevice dev1 = userDeviceRepository.save(UserDevice.builder()
			.user(user)
			.deviceName("Work-Laptop")
			.publicKey("pub-key-work")
			.build());

		UserDevice dev2 = userDeviceRepository.save(UserDevice.builder()
			.user(user)
			.deviceName("Home-PC")
			.publicKey("pub-key-home")
			.build());

		// [Step 3] 프로젝트 생성
		Project project = projectRepository.save(Project.builder()
			.projectName("Envio-Backend")
			.organizationName("Envio-Team")
			.description("Core API Server for Envio")
			.versionId(0L)
			.build());

		// [Step 4] 기기들과 프로젝트 연동 (암호화 키 할당)
		encryptedKeyRepository.save(EncryptedKey.builder()
			.userDevice(dev1)
			.project(project)
			.encryptedKey("enc-key-for-work")
			.active(true)
			.build());

		encryptedKeyRepository.save(EncryptedKey.builder()
			.userDevice(dev2)
			.project(project)
			.encryptedKey("enc-key-for-home")
			.active(true)
			.build());

		// [Step 5] 환경변수 업데이트 (Push - 버전 히스토리 생성)
		// v1 Push
		projectFacadeService.push(project.getId(), ProjectPushReqDto.builder()
			.githubUserId(githubId)
			.encryptedEnvironment(Map.of("DB_URL", "jdbc:postgresql://v1"))
			.parentVersionId(0L)
			.build());

		// v2 Push
		projectFacadeService.push(project.getId(), ProjectPushReqDto.builder()
			.githubUserId(githubId)
			.encryptedEnvironment(Map.of("DB_URL", "jdbc:postgresql://v2", "LOG_LEVEL", "DEBUG"))
			.parentVersionId(1L)
			.build());

		// [Step 6] 대시보드 조회 (내가 속한 프로젝트 목록)
		List<ProjectResDto> myProjects = userFacadeService.getMyProjects(githubId);
		assertThat(myProjects).hasSize(1);
		assertThat(myProjects.getFirst().projectName()).isEqualTo("Envio-Backend");
		assertThat(myProjects.getFirst().versionId()).isEqualTo(2L); // 최종 버전 반영 확인

		// [Step 7] 프로젝트 상세 정보 조회
		ProjectDetailResDto detail = projectFacadeService.getProjectDetail(project.getId());
		assertThat(detail.projectName()).isEqualTo("Envio-Backend");
		assertThat(detail.organizationName()).isEqualTo("Envio-Team");
		assertThat(detail.versionId()).isEqualTo(2L);

		// [Step 8] 프로젝트 버전 히스토리 전체 조회
		List<ProjectHistoryResDto> history = projectFacadeService.getProjectHistory(project.getId());
		assertThat(history).hasSize(2);
		assertThat(history.get(0).versionId()).isEqualTo(2L); // 최신순 정렬 확인
		assertThat(history.get(1).versionId()).isEqualTo(1L);
		assertThat(history.get(0).githubId()).isEqualTo(githubId);
	}
}
