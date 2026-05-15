package io.envio.core.domain.project.service.authorization;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.envio.core.common.error.ErrorCode;
import io.envio.core.domain.project.exception.ProjectException;
import io.envio.core.domain.project.repository.EncryptedKeyRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMembershipValidator {

	private final EncryptedKeyRepository encryptedKeyRepository;

	public void validateProjectMember(final Long projectId, final Long userId) {
		boolean isProjectMember = encryptedKeyRepository.existsByProjectIdAndUserDeviceUserIdAndActiveTrue(
			projectId,
			userId
		);
		if (!isProjectMember) {
			throw new ProjectException(ErrorCode.ACCESS_DENIED);
		}
	}
}
