package com.a6raywa1cher.pasttyspring.models.enums;

import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum Role implements GrantedAuthority {
	BLOCKED,
	USER(BLOCKED),
	MODERATOR(USER, BLOCKED),
	ADMIN(MODERATOR, USER, BLOCKED);
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
