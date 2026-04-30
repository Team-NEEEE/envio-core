package io.envio.core.domain.user.service.command;

import io.envio.core.domain.user.dto.request.UserCreateReqDto;
import io.envio.core.domain.user.entity.User;

public interface UserCommandService {

	User create(final UserCreateReqDto reqDto);
}
