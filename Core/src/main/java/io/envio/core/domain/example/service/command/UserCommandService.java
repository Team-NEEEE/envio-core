package io.envio.core.domain.example.service.command;

import io.envio.core.domain.example.dto.request.UserCreateReqDto;
import io.envio.core.domain.example.entity.User;

public interface UserCommandService {

	User create(final UserCreateReqDto reqDto);
}
