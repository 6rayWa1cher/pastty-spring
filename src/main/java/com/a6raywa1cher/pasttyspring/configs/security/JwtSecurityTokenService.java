package com.a6raywa1cher.pasttyspring.configs.security;


import com.a6raywa1cher.pasttyspring.models.JwtToken;
import com.a6raywa1cher.pasttyspring.models.User;
import org.springframework.data.util.Pair;

import java.util.Optional;

public interface JwtSecurityTokenService extends SecurityTokenService {
	Optional<JwtToken> getToken(String token);

	Optional<Pair<JwtToken, String>> createToken(User user);

	Optional<String> getJti(String token);
}
