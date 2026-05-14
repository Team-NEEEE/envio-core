package io.envio.core.domain.user.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.envio.core.domain.user.entity.UserDevice;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

	Optional<UserDevice> findByIdAndPublicKey(Long id, String publicKey);

	List<UserDevice> findAllByUserGithubIdIn(Collection<String> githubIds);
}
