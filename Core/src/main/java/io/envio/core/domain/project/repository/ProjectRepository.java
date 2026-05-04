package io.envio.core.domain.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.envio.core.domain.project.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
