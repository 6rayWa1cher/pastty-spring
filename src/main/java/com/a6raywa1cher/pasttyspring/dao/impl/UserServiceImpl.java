package com.a6raywa1cher.pasttyspring.dao.impl;

import com.a6raywa1cher.pasttyspring.configs.security.TokenAuthentication;
import com.a6raywa1cher.pasttyspring.configs.security.TokenUser;
import com.a6raywa1cher.pasttyspring.dao.interfaces.UserService;
import com.a6raywa1cher.pasttyspring.dao.repository.UserRepository;
import com.a6raywa1cher.pasttyspring.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
	private UserRepository repository;

	@Autowired
	public UserServiceImpl(UserRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return repository.findByUsername(username);
	}

	@Override
	public boolean isAnyoneElseRegistered() {
		return repository.count() > 0;
	}

	@Override
	public User save(User user) {
		return repository.save(user);
	}

	@Override
	public User findFromTokenUser(TokenUser tokenUser) {
		if (tokenUser == null) return null;
		return repository.findById(tokenUser.getId()).orElseThrow();
	}

	@Override
	public Optional<User> findFromAuthentication(Authentication authentication) {
		if (!(authentication instanceof TokenAuthentication)) {
			return Optional.empty();
		}
		return repository.findById(((TokenAuthentication) authentication).getUser().getId());
	}
}
