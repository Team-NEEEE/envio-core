package io.envio.core.domain.user.service.query;

import io.envio.core.domain.user.entity.User;

public interface UserQueryService {

	User findByGithubId(String githubId);
}
