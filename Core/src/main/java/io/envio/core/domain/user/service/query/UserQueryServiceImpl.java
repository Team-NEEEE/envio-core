package io.envio.core.domain.user.service.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.core.common.error.ErrorCode;
import io.envio.core.domain.user.entity.User;
import io.envio.core.domain.user.exception.UserException;
import io.envio.core.domain.user.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserQueryServiceImpl implements UserQueryService {

	private final UserRepository userRepository;

	@Override
	public User findByGithubId(final String githubId) {
		log.info("[User] GitHub ID로 사용자 조회 - githubId: {}", githubId);
		return userRepository.findByGithubId(githubId)
			.orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
	}
}
