package io.envio.core.domain.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.envio.core.domain.project.entity.EncryptedKey;
import io.envio.core.domain.project.entity.Project;

public interface EncryptedKeyRepository extends JpaRepository<EncryptedKey, Long> {

	@Query(
		"SELECT DISTINCT e.project FROM EncryptedKey e " + "WHERE e.userDevice.user.id = :userId AND e.active = true")
	List<Project> findProjectsByUserId(@Param("userId") Long userId);

	boolean existsByProjectIdAndUserDeviceUserIdAndActiveTrue(Long projectId, Long userId);

	List<EncryptedKey> findByProjectId(Long projectId);

	List<EncryptedKey> findByUserDeviceId(Long userDeviceId);

	Optional<EncryptedKey> findByUserDeviceIdAndProjectId(
		Long userDeviceId,
		Long projectId
	);
}
