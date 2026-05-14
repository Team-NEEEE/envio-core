package io.envio.core.domain.project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.envio.core.domain.project.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

	boolean existsByOrganizationNameAndProjectName(String organizationName, String projectName);

	Optional<Project> findByOrganizationNameAndProjectName(String organizationName, String projectName);
}
