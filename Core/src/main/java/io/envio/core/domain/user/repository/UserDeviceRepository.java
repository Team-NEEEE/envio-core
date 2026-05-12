package io.envio.core.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.envio.core.domain.user.entity.UserDevice;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
}
