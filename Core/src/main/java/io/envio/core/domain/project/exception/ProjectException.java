package io.envio.core.domain.project.exception;

import io.envio.core.common.error.ErrorCode;
import io.envio.core.common.error.exception.BusinessException;

public class ProjectException extends BusinessException {

	public ProjectException(ErrorCode errorCode) {
		super(errorCode);
	}
}
