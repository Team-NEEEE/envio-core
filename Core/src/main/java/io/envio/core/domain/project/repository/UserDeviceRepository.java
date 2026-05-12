package io.envio.core.domain.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.envio.core.domain.project.entity.UserDevice;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
}
