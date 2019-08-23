package com.a6raywa1cher.pasttyspring.dao.impl;

import com.a6raywa1cher.pasttyspring.configs.security.RefreshTokenRevokeCache;
import com.a6raywa1cher.pasttyspring.dao.interfaces.RefreshJwtTokenService;
import com.a6raywa1cher.pasttyspring.dao.repository.RefreshJwtTokenRepository;
import com.a6raywa1cher.pasttyspring.models.RefreshJwtToken;
import com.a6raywa1cher.pasttyspring.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class RefreshJwtTokenServiceImpl implements RefreshJwtTokenService {
	private RefreshJwtTokenRepository repository;
	private RefreshTokenRevokeCache cache;

	@Autowired
	public RefreshJwtTokenServiceImpl(RefreshJwtTokenRepository repository, RefreshTokenRevokeCache cache) {
		this.repository = repository;
		this.cache = cache;
	}

	@Override
	public Optional<RefreshJwtToken> find(String uuid) {
		return repository.findById(uuid);
	}

	@Override
	public RefreshJwtToken save(RefreshJwtToken save) {
		return repository.save(save);
	}

	@Override
	public void delete(RefreshJwtToken token) {
		repository.delete(token);
	}

	@Override
	public Collection<RefreshJwtToken> getAllByUser(User user) {
		return repository.findAllByUser(user);
	}

	@Override
	public void deleteAll(Collection<RefreshJwtToken> collection) {
		repository.deleteAll(collection);
	}

	@Override
	public void revokeAllByUser(User user) {
		Collection<RefreshJwtToken> collection = this.getAllByUser(user);
		collection.forEach(token -> cache.revoke(token.getUuid()));
		this.deleteAll(collection);
	}
}
