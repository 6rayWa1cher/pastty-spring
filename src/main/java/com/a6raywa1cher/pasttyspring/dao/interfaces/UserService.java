package com.a6raywa1cher.pasttyspring.dao.interfaces;

import com.a6raywa1cher.pasttyspring.configs.security.TokenUser;
import com.a6raywa1cher.pasttyspring.models.User;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface UserService {
	Optional<User> findByUsername(String username);

	boolean isAnyoneElseRegistered();

	User save(User user);

	User findFromTokenUser(TokenUser tokenUser);

	Optional<User> findFromAuthentication(Authentication authentication);
}
