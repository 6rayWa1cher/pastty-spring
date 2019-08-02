package com.a6raywa1cher.pasttyspring.configs.security;

import com.a6raywa1cher.pasttyspring.configs.AuthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HashingService {
	private final AuthConfig authConfig;

	@Autowired
	public HashingService(AuthConfig authConfig) {
		this.authConfig = authConfig;
	}

	public String hash(String s) {
		return authConfig.getHash().digestAsHex(s);
	}
}
