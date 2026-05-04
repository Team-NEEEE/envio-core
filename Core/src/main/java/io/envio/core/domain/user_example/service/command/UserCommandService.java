package io.envio.core.domain.user_example.service.command;

import io.envio.core.domain.user_example.dto.request.UserCreateReqDto;
import io.envio.core.domain.user_example.entity.User;

public interface UserCommandService {

	User create(final UserCreateReqDto reqDto);
}
