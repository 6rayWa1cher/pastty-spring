package com.a6raywa1cher.pasttyspring.configs.security;


import com.a6raywa1cher.pasttyspring.models.RefreshJwtToken;
import com.a6raywa1cher.pasttyspring.models.User;
import org.springframework.data.util.Pair;

import java.util.Optional;

public interface JwtSecurityTokenService {
	Optional<RefreshJwtToken> getToken(String token);

	Optional<String> createAccessToken(RefreshJwtToken refreshJwtToken);

	Optional<Pair<RefreshJwtToken, String>> createRefreshToken(User user);

	Optional<TokenAuthentication> toAuthentication(String accessToken);

	Optional<String> getJti(String token);
}
