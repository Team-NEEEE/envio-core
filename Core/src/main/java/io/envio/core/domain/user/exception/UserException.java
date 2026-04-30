package io.envio.core.domain.user.exception;

import io.envio.core.common.error.ErrorCode;
import io.envio.core.common.error.exception.BusinessException;

public class UserException extends BusinessException {

	public UserException(final ErrorCode errorCode) {
		super(errorCode);
	}
}
