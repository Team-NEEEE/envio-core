package io.envio.core.domain.project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.envio.core.domain.project.entity.History;

public interface HistoryRepository extends JpaRepository<History, Long> {

	Optional<History> findFirstByProjectIdOrderByVersionIdDesc(Long projectId);
}
