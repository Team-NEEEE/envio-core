package io.envio.core.domain.project.repository;

import io.envio.core.domain.project.entity.EncryptedKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EncryptedKeyRepository extends JpaRepository<EncryptedKey, Long> {

    List<EncryptedKey> findByProject_Id(Long projectId);

    List<EncryptedKey> findByUserDevice_Id(Long userDeviceId);

    Optional<EncryptedKey> findByUserDevice_IdAndProject_Id(
            Long userDeviceId,
            Long projectId
    );
}
