package io.envio.core.domain.example.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.envio.core.common.error.ErrorCode;
import io.envio.core.domain.example.converter.UserConverter;
import io.envio.core.domain.example.dto.request.UserCreateReqDto;
import io.envio.core.domain.example.entity.User;
import io.envio.core.domain.example.exception.UserException;
import io.envio.core.domain.example.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCommandServiceImpl implements UserCommandService {

	private final UserRepository userRepository;

	@Override
	public User create(final UserCreateReqDto reqDto) {
		validateEmployeeNumber(reqDto.employeeNumber());
		User user = userRepository.save(UserConverter.toEntity(reqDto));
		log.info("[User] 사용자 생성 성공 - userId: {}, employeeNumber: {}", user.getId(), user.getEmployeeNumber());
		return user;
	}

	private void validateEmployeeNumber(final String employeeNumber) {
		if (userRepository.existsByEmployeeNumber(employeeNumber)) {
			throw new UserException(ErrorCode.USER_ALREADY_EXISTS);
		}
	}
}
