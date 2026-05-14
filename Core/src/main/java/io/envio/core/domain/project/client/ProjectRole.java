package io.envio.core.domain.project.client;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public enum ProjectRole {
	READ,
	TRIAGE,
	WRITE,
	MAINTAIN,
	ADMIN;

	private static final Set<ProjectRole> CREATE_ALLOWED_ROLES = Set.of(WRITE, MAINTAIN, ADMIN);

	public boolean canCreateProject() {
		return CREATE_ALLOWED_ROLES.contains(this);
	}

	public static ProjectRole fromGithubPermissions(final Map<String, Boolean> permissions) {
		if (Boolean.TRUE.equals(permissions.get("admin"))) {
			return ADMIN;
		}
		if (Boolean.TRUE.equals(permissions.get("maintain"))) {
			return MAINTAIN;
		}
		if (Boolean.TRUE.equals(permissions.get("push"))) {
			return WRITE;
		}
		if (Boolean.TRUE.equals(permissions.get("triage"))) {
			return TRIAGE;
		}
		return READ;
	}

	public static ProjectRole fromGithubPermissionName(final String permissionName) {
		if (permissionName == null || permissionName.isBlank()) {
			return READ;
		}

		return switch (permissionName.toLowerCase(Locale.ROOT)) {
			case "admin" -> ADMIN;
			case "maintain" -> MAINTAIN;
			case "push", "write" -> WRITE;
			case "triage" -> TRIAGE;
			default -> READ;
		};
	}
}
