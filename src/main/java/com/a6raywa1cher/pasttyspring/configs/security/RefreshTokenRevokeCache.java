package com.a6raywa1cher.pasttyspring.configs.security;

import com.a6raywa1cher.pasttyspring.configs.AuthConfig;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class RefreshTokenRevokeCache {
	// https://www.regextester.com/99148
	private static final String UUID_REGEX =
			"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
	private LoadingCache<String, Boolean> cache;

	@Autowired
	public RefreshTokenRevokeCache(AuthConfig authConfig) {
		Duration accessTokenDuration = authConfig.getAccessTokenDuration();
		this.cache = CacheBuilder.newBuilder()
				.expireAfterWrite(accessTokenDuration.getSeconds() + 5, TimeUnit.SECONDS)
				.build(new CacheLoader<>() {
					@Override
					public Boolean load(String key) throws Exception {
						return false;
					}
				});
	}

	public void revoke(String token) {
		if (!token.matches(UUID_REGEX)) {
			throw new IllegalArgumentException("Not uuid: " + token);
		}
		cache.put(token, true);
	}

	public boolean isRevoked(String token) {
		return Objects.equals(cache.getIfPresent(token), true);
	}
}
