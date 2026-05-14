package io.envio.core.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.envio.core.domain.project.dto.response.ProjectResDto;
import io.envio.core.domain.project.entity.EncryptedKey;
import io.envio.core.domain.project.entity.Project;
import io.envio.core.domain.project.repository.EncryptedKeyRepository;
import io.envio.core.domain.project.repository.ProjectRepository;
import io.envio.core.domain.user.entity.User;
import io.envio.core.domain.user.entity.UserDevice;
import io.envio.core.domain.user.entity.UserRole;
import io.envio.core.domain.user.repository.UserDeviceRepository;
import io.envio.core.domain.user.repository.UserRepository;
import io.envio.core.domain.user.service.facade.UserFacadeService;

@SpringBootTest
@Transactional
@DisplayName("유저_프로젝트_연동_통합_테스트")
class UserProjectIntegrationTest {

	@Autowired
	private UserFacadeService userFacadeService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserDeviceRepository userDeviceRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private EncryptedKeyRepository encryptedKeyRepository;

	@Test
	@DisplayName("GitHub_ID로_사용자를_식별하고_여러_기기에_걸친_중복_프로젝트를_제거하여_조회한다")
	void getMyProjects_withDistinctFiltering() {
		// 1. 유저 한 명 생성
		String githubId = "envio-user";
		User user = userRepository.save(User.builder()
			.githubId(githubId)
			.email("user@envio.io")
			.role(UserRole.DEVELOPER)
			.build());

		// 2. 유저의 기기 2개 생성 (MacBook, Windows)
		UserDevice macbook = userDeviceRepository.save(UserDevice.builder()
			.user(user)
			.deviceName("MacBook-Pro")
			.publicKey("public-key-1")
			.build());

		UserDevice windows = userDeviceRepository.save(UserDevice.builder()
			.user(user)
			.deviceName("Windows-PC")
			.publicKey("public-key-2")
			.build());

		// 3. 프로젝트 2개 생성 (Project-X, Project-Y)
		Project projectX = projectRepository.save(Project.builder()
			.projectName("Project-X")
			.versionId(1L)
			.build());

		Project projectY = projectRepository.save(Project.builder()
			.projectName("Project-Y")
			.versionId(1L)
			.build());

		// 4. 기기별 프로젝트 키 할당 (중복 상황 연출)
		// MacBook은 X, Y 둘 다 참여
		encryptedKeyRepository.save(EncryptedKey.builder()
			.userDevice(macbook)
			.project(projectX)
			.encryptedKey("key-mac-x")
			.active(true)
			.build());

		encryptedKeyRepository.save(EncryptedKey.builder()
			.userDevice(macbook)
			.project(projectY)
			.encryptedKey("key-mac-y")
			.active(true)
			.build());

		// Windows PC도 Project-X에 참여 (중복 발생 포인트)
		encryptedKeyRepository.save(EncryptedKey.builder()
			.userDevice(windows)
			.project(projectX)
			.encryptedKey("key-win-x")
			.active(true)
			.build());

		// 5. 유저 파사드를 통해 GitHub ID로 프로젝트 목록 조회
		List<ProjectResDto> myProjects = userFacadeService.getMyProjects(githubId);

		// 6. 검증
		// Project-X는 기기가 2개지만 목록에는 1개만 나와야 함 (Distinct 확인)
		// Project-Y는 기기가 1개라 당연히 1개 나옴
		// 총 결과는 2개여야 함
		assertThat(myProjects).hasSize(2);
		
		// 프로젝트 이름들이 정확히 포함되어 있는지 확인
		List<String> projectNames = myProjects.stream()
			.map(ProjectResDto::projectName)
			.toList();
		
		assertThat(projectNames).containsExactlyInAnyOrder("Project-X", "Project-Y");
	}
}
