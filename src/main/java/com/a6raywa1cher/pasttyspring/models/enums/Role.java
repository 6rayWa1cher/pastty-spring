package com.a6raywa1cher.pasttyspring.models.enums;

import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum Role implements GrantedAuthority {
	ROLE_BLOCKED,
	ROLE_USER(ROLE_BLOCKED),
	ROLE_MODERATOR(ROLE_USER, ROLE_BLOCKED),
	ROLE_ADMIN(ROLE_MODERATOR, ROLE_USER, ROLE_BLOCKED);
	private Set<Role> tree;

	Role(Role... children) {
		this.tree = new HashSet<>();
		tree.addAll(Arrays.asList(children));
		tree.add(this);
	}

	public Set<Role> getTree() {
		return tree;
	}

	@Override
	public String getAuthority() {
		return this.name();
	}
}
