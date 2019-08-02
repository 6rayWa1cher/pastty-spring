package com.a6raywa1cher.pasttyspring.configs.security;


import com.a6raywa1cher.pasttyspring.models.User;

import java.util.Optional;

public interface SecurityTokenService {
	String getToken(User user);

	Optional<User> checkToken(String token);
}
