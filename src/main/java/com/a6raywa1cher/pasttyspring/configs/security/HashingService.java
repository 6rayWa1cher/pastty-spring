package com.a6raywa1cher.pasttyspring.configs.security;

import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class HashingService {
	private Pbkdf2PasswordEncoder passwordEncoder;

	public HashingService() {
		passwordEncoder = new Pbkdf2PasswordEncoder();
		passwordEncoder.setAlgorithm(Pbkdf2PasswordEncoder.
				SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
	}

	public String hash(String s) {
		return passwordEncoder.encode(s);
	}

	public boolean compare(String raw, String encoded) {
		return passwordEncoder.matches(raw, encoded);
	}
}
