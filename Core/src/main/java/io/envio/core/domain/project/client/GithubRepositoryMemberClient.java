package io.envio.core.domain.project.client;

public interface GithubRepositoryMemberClient {

	GithubRepositoryAccess getRepositoryAccess(String owner, String repoName);
}
