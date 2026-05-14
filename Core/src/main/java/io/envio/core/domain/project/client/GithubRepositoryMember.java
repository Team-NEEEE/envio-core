package io.envio.core.domain.project.client;

public record GithubRepositoryMember(
	String githubId,
	ProjectRole projectRole
) {
}
