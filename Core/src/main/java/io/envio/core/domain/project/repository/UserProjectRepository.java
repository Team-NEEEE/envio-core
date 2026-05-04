package io.envio.core.domain.project.repository;

import io.envio.core.domain.project.entity.UserProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProjectRepository extends JpaRepository<UserProject, Long> {

    List<UserProject> findByProject_ProjectId(Long projectId);

    List<UserProject> findByUserDevice_UserDeviceId(Long userId);

    Optional<UserProject> findByUserDevice_UserDeviceIdAndProject_ProjectId(
            Long userId,
            Long projectId
    );
}
