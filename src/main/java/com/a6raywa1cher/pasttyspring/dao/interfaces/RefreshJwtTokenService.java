package com.a6raywa1cher.pasttyspring.dao.interfaces;

import com.a6raywa1cher.pasttyspring.models.RefreshJwtToken;
import com.a6raywa1cher.pasttyspring.models.User;

import java.util.Collection;
import java.util.Optional;

public interface RefreshJwtTokenService {
	Optional<RefreshJwtToken> find(String uuid);

	RefreshJwtToken save(RefreshJwtToken save);

	void delete(RefreshJwtToken token);

	Collection<RefreshJwtToken> getAllByUser(User user);

	void deleteAll(Collection<RefreshJwtToken> collection);

	void revokeAllByUser(User user);
}
