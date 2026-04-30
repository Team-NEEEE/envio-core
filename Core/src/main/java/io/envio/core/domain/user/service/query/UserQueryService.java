package io.envio.core.domain.user.service.query;

import io.envio.core.domain.user.entity.User;

public interface UserQueryService {

	User findById(final Long userId);

	User findByEmployeeNumber(final String employeeNumber);
}
