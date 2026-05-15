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
	@DisplayName("사용자_등록부터_기기설정_조직별_프로젝트연동_데이터푸시_및_그룹화조회까지_풀플로우를_검증한다")
	void full_system_lifecycle_test() {
		// [Step 1] 사용자 가입 및 식별
		String githubId = "full-flow-user";
		User user = userRepository.save(User.builder()
			.githubId(githubId)
			.email("fullflow@envio.io")
			.role(UserRole.OWNER)
			.build());

		// [Step 2] 사용자의 기기 등록
		UserDevice dev1 = userDeviceRepository.save(UserDevice.builder()
			.user(user)
			.deviceName("Work-Laptop")
			.publicKey("pub-key-work")
			.build());

		// [Step 3] 여러 조직에 걸친 프로젝트 생성
		// 조직 A: Envio-Core
		Project projectCore = projectRepository.save(Project.builder()
			.projectName("Backend-API")
			.organizationName("Envio-Core")
			.description("Core Server")
			.versionId(0L)
			.build());

		// 조직 B: Personal-Study
		Project projectStudy = projectRepository.save(Project.builder()
			.projectName("Algorithm-Lab")
			.organizationName("Personal-Study")
			.description("Study Repo")
			.versionId(0L)
			.build());

		// [Step 4] 기기들과 프로젝트 연동 (암호화 키 할당)
		encryptedKeyRepository.save(EncryptedKey.builder()
			.userDevice(dev1)
			.project(projectCore)
			.encryptedKey("enc-key-core")
			.active(true)
			.build());

		encryptedKeyRepository.save(EncryptedKey.builder()
			.userDevice(dev1)
			.project(projectStudy)
			.encryptedKey("enc-key-study")
			.active(true)
			.build());

		// [Step 5] 환경변수 업데이트 (Push)
		projectFacadeService.push(projectCore.getId(), user.getId(), githubId, ProjectPushReqDto.builder()
			.githubUserId(githubId)
			.encryptedEnvironment(Map.of("DB_URL", "jdbc:postgresql://v1"))
			.parentVersionId(0L)
			.build());

		// [Step 6] 대시보드 조회 (조직별 그룹화 결과 검증)
		// UI Flow: 로그인 -> 조직 리스트 확인 -> 특정 조직 클릭 -> 프로젝트 리스트 확인
		Map<String, List<ProjectResDto>> myProjectsGrouped = userFacadeService.getMyProjects(githubId);

		// 1. 조직 리스트(Key) 검증
		assertThat(myProjectsGrouped).hasSize(2);
		assertThat(myProjectsGrouped.keySet()).containsExactlyInAnyOrder("Envio-Core", "Personal-Study");

		// 2. 'Envio-Core' 조직 클릭 시 프로젝트 리스트 검증
		List<ProjectResDto> coreProjects = myProjectsGrouped.get("Envio-Core");
		assertThat(coreProjects).hasSize(1);
		assertThat(coreProjects.getFirst().projectName()).isEqualTo("Backend-API");
		assertThat(coreProjects.getFirst().versionId()).isEqualTo(1L);

		// 3. 'Personal-Study' 조직 클릭 시 프로젝트 리스트 검증
		List<ProjectResDto> studyProjects = myProjectsGrouped.get("Personal-Study");
		assertThat(studyProjects).hasSize(1);
		assertThat(studyProjects.getFirst().projectName()).isEqualTo("Algorithm-Lab");

		// [Step 7] 프로젝트 상세 정보 조회 (특정 프로젝트 클릭 시 상세 이동)
		Long targetProjectId = coreProjects.getFirst().projectId();
		ProjectDetailResDto detail = projectFacadeService.getProjectDetail(targetProjectId, user.getId());
		assertThat(detail.projectName()).isEqualTo("Backend-API");
		assertThat(detail.organizationName()).isEqualTo("Envio-Core");

		// [Step 8] 프로젝트 버전 히스토리 조회
		List<ProjectHistoryResDto> history = projectFacadeService.getProjectHistory(targetProjectId, user.getId());
		assertThat(history).hasSize(1);
		assertThat(history.get(0).versionId()).isEqualTo(1L);
		assertThat(history.get(0).githubId()).isEqualTo(githubId);
	}

	@Test
	@DisplayName("여러_사용자가_서로_다른_조직과_프로젝트에_권한을_가질_때_데이터_격리_및_그룹화를_검증한다")
	void multi_user_system_flow_test() {
		// [Step 1] 사용자들 생성 (관리자, 개발자, 학생)
		User owner = userRepository.save(User.builder().githubId("owner-user").email("owner@envio.io").role(UserRole.OWNER).build());
		User dev = userRepository.save(User.builder().githubId("dev-user").email("dev@envio.io").role(UserRole.DEVELOPER).build());
		User student = userRepository.save(User.builder().githubId("student-user").email("student@envio.io").role(UserRole.VIEWER).build());

		// [Step 2] 각 사용자의 기기 등록
		UserDevice ownerDev = userDeviceRepository.save(UserDevice.builder().user(owner).deviceName("Owner-Mac").publicKey("pub-owner").build());
		UserDevice devDev = userDeviceRepository.save(UserDevice.builder().user(dev).deviceName("Dev-Laptop").publicKey("pub-dev").build());
		UserDevice studentDev = userDeviceRepository.save(UserDevice.builder().user(student).deviceName("Student-Tab").publicKey("pub-student").build());

		// [Step 3] 프로젝트 생성
		Project projectCore = projectRepository.save(Project.builder().projectName("Core-API").organizationName("Envio-Team").versionId(1L).build());
		Project projectWeb = projectRepository.save(Project.builder().projectName("Web-Front").organizationName("Envio-Team").versionId(1L).build());
		Project projectStudy = projectRepository.save(Project.builder().projectName("Java-Study").organizationName("Personal").versionId(1L).build());

		// [Step 4] 권한 할당 (EncryptedKey)
		// Owner는 모든 프로젝트에 접근 가능
		encryptedKeyRepository.save(EncryptedKey.builder().userDevice(ownerDev).project(projectCore).encryptedKey("key-owner-core").active(true).build());
		encryptedKeyRepository.save(EncryptedKey.builder().userDevice(ownerDev).project(projectWeb).encryptedKey("key-owner-web").active(true).build());
		encryptedKeyRepository.save(EncryptedKey.builder().userDevice(ownerDev).project(projectStudy).encryptedKey("key-owner-study").active(true).build());

		// Developer는 Envio-Team의 Core-API만 접근 가능
		encryptedKeyRepository.save(EncryptedKey.builder().userDevice(devDev).project(projectCore).encryptedKey("key-dev-core").active(true).build());

		// Student는 Personal의 Java-Study만 접근 가능
		encryptedKeyRepository.save(EncryptedKey.builder().userDevice(studentDev).project(projectStudy).encryptedKey("key-student-study").active(true).build());

		// [Step 5] 사용자별 조회 결과 검증

		// 1. Owner 조회: 2개 조직, 총 3개 프로젝트
		Map<String, List<ProjectResDto>> ownerMap = userFacadeService.getMyProjects(owner.getGithubId());
		assertThat(ownerMap).hasSize(2);
		assertThat(ownerMap.get("Envio-Team")).hasSize(2); // Core-API, Web-Front
		assertThat(ownerMap.get("Personal")).hasSize(1);    // Java-Study

		// 2. Developer 조회: 1개 조직, 1개 프로젝트 (데이터 격리 확인)
		Map<String, List<ProjectResDto>> devMap = userFacadeService.getMyProjects(dev.getGithubId());
		assertThat(devMap).hasSize(1);
		assertThat(devMap.containsKey("Envio-Team")).isTrue();
		assertThat(devMap.containsKey("Personal")).isFalse(); // 권한 없는 조직은 뜨지 않음
		assertThat(devMap.get("Envio-Team")).hasSize(1);
		assertThat(devMap.get("Envio-Team").get(0).projectName()).isEqualTo("Core-API");

		// 3. Student 조회: 1개 조직, 1개 프로젝트
		Map<String, List<ProjectResDto>> studentMap = userFacadeService.getMyProjects(student.getGithubId());
		assertThat(studentMap).hasSize(1);
		assertThat(studentMap.containsKey("Personal")).isTrue();
		assertThat(studentMap.get("Personal").get(0).projectName()).isEqualTo("Java-Study");
	}
}
