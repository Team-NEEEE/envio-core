package io.envio.core.domain.example.service.query;

import io.envio.core.domain.example.entity.User;

public interface UserQueryService {

	User findById(final Long userId);

	User findByEmployeeNumber(final String employeeNumber);
}
