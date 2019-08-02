package com.a6raywa1cher.pasttyspring.configs.security;

import com.a6raywa1cher.pasttyspring.dao.converters.UserToJwtTokenConverter;
import com.a6raywa1cher.pasttyspring.models.JwtToken;
import com.a6raywa1cher.pasttyspring.models.User;
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
	private JwtToken jwt;

	public TokenAuthentication(String token, User user) {
		this(token, UserToJwtTokenConverter.apply(token, user));
	}

	public TokenAuthentication(String token, JwtToken jwtSessionToken) {
		this.token = token;
		this.jwt = jwtSessionToken;
		this.isAuthenticated = jwtSessionToken.getExpDate().isAfter(LocalDateTime.now());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return jwt.getUser().getRole().getTree();
	}

	@Override
	public Object getCredentials() {
		return token;
	}

	@Override
	public Object getDetails() {
		return jwt.getUser();
	}

	@Override
	public User getPrincipal() {
		return jwt.getUser();
	}

	@Override
	public boolean isAuthenticated() {
		if (isAuthenticated && jwt != null && jwt.getExpDate().isAfter(LocalDateTime.now())) {
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
		return jwt.getUser().getUsername();
	}
}
