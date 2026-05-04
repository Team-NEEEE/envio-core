package io.envio.core.domain.project.service;

import org.springframework.stereotype.Service;

import io.envio.core.domain.project.repository.EncryptedKeyRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectService {

	private final EncryptedKeyRepository userProjectRepository;

}
