package io.envio.core.domain.user_example.service.facade;

import io.envio.core.domain.user_example.dto.request.UserCreateReqDto;
import io.envio.core.domain.user_example.dto.response.UserResDto;

public interface UserFacadeService {

	UserResDto createUser(final UserCreateReqDto reqDto);

	UserResDto getUser(final Long userId);

	UserResDto getUserByEmployeeNumber(final String employeeNumber);
}
