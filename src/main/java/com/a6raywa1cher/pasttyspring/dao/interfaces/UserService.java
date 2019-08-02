package com.a6raywa1cher.pasttyspring.dao.interfaces;

import com.a6raywa1cher.pasttyspring.models.User;

import java.util.Optional;

public interface UserService {
	Optional<User> findByUsernameAndPassword(String username, String password);

	Optional<User> findByUsername(String username);

	User save(User user);
}
