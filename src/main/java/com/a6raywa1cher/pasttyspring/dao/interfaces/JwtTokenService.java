package com.a6raywa1cher.pasttyspring.dao.interfaces;

import com.a6raywa1cher.pasttyspring.models.JwtToken;

import java.util.Optional;

public interface JwtTokenService {
	Optional<JwtToken> find(String uuid);

	JwtToken save(JwtToken save);
}
