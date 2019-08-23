package com.a6raywa1cher.pasttyspring.configs.security;

import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@ToString
public class TokenAuthentication implements Authentication {
	private boolean isAuthenticated;
	private String token;
	private String refreshTokenUUID;
	private TokenUser user;
	private LocalDateTime exp;

	public TokenAuthentication(String token, TokenUser user, String refreshTokenUUID, LocalDateTime exp) {
		this.token = token;
		this.user = user;
		this.refreshTokenUUID = refreshTokenUUID;
		this.exp = exp;
		this.isAuthenticated = exp.isAfter(LocalDateTime.now());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return user.getRole().getTree();
	}

	@Override
	public Object getCredentials() {
		return token;
	}

	@Override
	public Object getDetails() {
		return user;
	}

	@Override
	public TokenUser getPrincipal() {
		return user;
	}

	@Override
	public boolean isAuthenticated() {
		if (isAuthenticated && exp != null && exp.isAfter(LocalDateTime.now())) {
			return true;
		} else if (isAuthenticated) {
			isAuthenticated = false;
		}
		return false;
	}

	@Override
	public void setAuthenticated(boolean b) throws IllegalArgumentException {
		if (b) {
			throw new IllegalArgumentException();
		}
		isAuthenticated = false;
	}

	@Override
	public String getName() {
		return user.getUsername();
	}
}
