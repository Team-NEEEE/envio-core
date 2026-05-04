package io.envio.core.domain.user_example.service.query;

import io.envio.core.domain.user_example.entity.User;

public interface UserQueryService {

	User findById(final Long userId);

	User findByEmployeeNumber(final String employeeNumber);
}
