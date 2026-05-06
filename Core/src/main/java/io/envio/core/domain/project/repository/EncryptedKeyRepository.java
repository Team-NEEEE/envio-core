package io.envio.core.domain.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.envio.core.domain.project.entity.EncryptedKey;

public interface EncryptedKeyRepository extends JpaRepository<EncryptedKey, Long> {

	List<EncryptedKey> findByProjectId(Long projectId);

	List<EncryptedKey> findByUserDeviceId(Long userDeviceId);

	Optional<EncryptedKey> findByUserDeviceIdAndProjectId(
		Long userDeviceId,
		Long projectId
	);
}
