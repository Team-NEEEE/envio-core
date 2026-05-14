package io.envio.core.domain.project.client;

import java.util.List;

public record GithubRepositoryAccess(
	Long installationId,
	List<GithubRepositoryMember> members
) {
}
