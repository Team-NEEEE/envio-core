package io.envio.core.domain.project.service;

import io.envio.core.domain.project.repository.UserProjectRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectService {

    private final UserProjectRepository userProjectRepository;



}
