package io.envio.core.domain.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Project 도메인 설계시 연관관계를 유지하기 위한 임시 UserDevice
@Entity
@Table(name = "user_devices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDevice {

	@Id
	@Column(name = "user_device_id")
	private Long id;

	// 필요하면 추가
	// private Long userId;
}