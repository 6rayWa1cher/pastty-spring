package com.a6raywa1cher.pasttyspring.configs.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component("authProvider")
public class JwtAuthenticationProvider implements AuthenticationProvider {
	private RefreshTokenRevokeCache cache;

	@Autowired
	public JwtAuthenticationProvider(RefreshTokenRevokeCache cache) {
		this.cache = cache;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (!supports(authentication.getClass())) {
			return null;
		}
		TokenAuthentication tokenAuthentication = (TokenAuthentication) authentication;
		LocalDateTime now = LocalDateTime.now();
		if (tokenAuthentication.getExp().isBefore(now)) {
			tokenAuthentication.setAuthenticated(false);
			throw new CredentialsExpiredException("AccessToken is expired");
		}
		if (cache.isRevoked(tokenAuthentication.getRefreshTokenUUID())) {
			tokenAuthentication.setAuthenticated(false);
			throw new CredentialsExpiredException("RefreshToken revoked");
		}
		return authentication;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(TokenAuthentication.class);
	}
}
