package com.a6raywa1cher.pasttyspring.configs;

import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Duration;

@Data
@PropertySource("classpath:appconfig.yml")
@ConfigurationProperties(prefix = "app.auth")
@Validated
public class AuthConfig {
	/**
	 * JWT secret key.
	 */
	@NotBlank
	private String jwtSecretKey;

	@NotNull
	private DigestUtils hash;

	/**
	 * Duration of a new session.
	 */
	@NotNull
	private Duration sessionDuration;
}
