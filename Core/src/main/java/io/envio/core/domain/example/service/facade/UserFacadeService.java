package io.envio.core.domain.example.service.facade;

import io.envio.core.domain.example.dto.request.UserCreateReqDto;
import io.envio.core.domain.example.dto.response.UserResDto;

public interface UserFacadeService {

	UserResDto createUser(final UserCreateReqDto reqDto);

	UserResDto getUser(final Long userId);

	UserResDto getUserByEmployeeNumber(final String employeeNumber);
}
