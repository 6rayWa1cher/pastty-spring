package com.a6raywa1cher.pasttyspring.rest.dto.mirror;

import com.a6raywa1cher.pasttyspring.models.User;
import com.a6raywa1cher.pasttyspring.models.enums.Role;
import lombok.Data;

@Data
public class UserMirror {
	private Long id;

	private String username;

	private Role role;

	public static UserMirror convert(User user) {
		UserMirror mirror = new UserMirror();
		mirror.setId(user.getId());
		mirror.setUsername(user.getUsername());
		mirror.setRole(user.getRole());
		return mirror;
	}
}
