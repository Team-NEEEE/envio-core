package io.envio.core.domain.project.service.authorization;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.envio.core.common.error.ErrorCode;
import io.envio.core.domain.project.exception.ProjectException;
import io.envio.core.domain.project.repository.EncryptedKeyRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Project membership validator")
class ProjectMembershipValidatorTest {

	@Mock
	private EncryptedKeyRepository encryptedKeyRepository;

	@InjectMocks
	private ProjectMembershipValidator projectMembershipValidator;

	@Test
	@DisplayName("allows active project member")
	void validateProjectMemberAllowsActiveProjectMember() {
		when(encryptedKeyRepository.existsByProjectIdAndUserDeviceUserIdAndActiveTrue(1L, 10L))
			.thenReturn(true);

		projectMembershipValidator.validateProjectMember(1L, 10L);

		verify(encryptedKeyRepository).existsByProjectIdAndUserDeviceUserIdAndActiveTrue(1L, 10L);
	}

	@Test
	@DisplayName("rejects non member")
	void validateProjectMemberRejectsNonMember() {
		when(encryptedKeyRepository.existsByProjectIdAndUserDeviceUserIdAndActiveTrue(1L, 10L))
			.thenReturn(false);

		assertThatThrownBy(() -> projectMembershipValidator.validateProjectMember(1L, 10L))
			.isInstanceOf(ProjectException.class)
			.hasMessage(ErrorCode.ACCESS_DENIED.getMessage());
	}
}
