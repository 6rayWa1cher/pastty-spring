package com.a6raywa1cher.pasttyspring.configs.security;

import com.a6raywa1cher.pasttyspring.models.User;
import com.a6raywa1cher.pasttyspring.models.enums.Role;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.IOException;

@Data
public class TokenUser {
	private Long id;

	private String username;

	private Role role;

	public static TokenUser fromJson(String json) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.readValue(json, TokenUser.class);
		} catch (IOException e) {
			return null;
		}
	}

	public static TokenUser convert(User user) {
		TokenUser tokenUser = new TokenUser();
		tokenUser.setId(user.getId());
		tokenUser.setUsername(user.getUsername());
		tokenUser.setRole(user.getRole());
		return tokenUser;
	}

	public String toJson() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return null;
		}
	}
}
