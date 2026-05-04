package io.envio.core.domain.user_example.service.facade;

import org.springframework.stereotype.Service;

import io.envio.core.domain.user_example.converter.UserConverter;
import io.envio.core.domain.user_example.dto.request.UserCreateReqDto;
import io.envio.core.domain.user_example.dto.response.UserResDto;
import io.envio.core.domain.user_example.entity.User;
import io.envio.core.domain.user_example.service.command.UserCommandService;
import io.envio.core.domain.user_example.service.query.UserQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFacadeServiceImpl implements UserFacadeService {

	private final UserCommandService commandService;
	private final UserQueryService queryService;

	@Override
	public UserResDto createUser(final UserCreateReqDto reqDto) {
		User user = commandService.create(reqDto);
		return UserConverter.toUserResDto(user);
	}

	@Override
	public UserResDto getUser(final Long userId) {
		User user = queryService.findById(userId);
		return UserConverter.toUserResDto(user);
	}

	@Override
	public UserResDto getUserByEmployeeNumber(final String employeeNumber) {
		User user = queryService.findByEmployeeNumber(employeeNumber);
		return UserConverter.toUserResDto(user);
	}
}
