package com.a6raywa1cher.pasttyspring.configs;

import com.a6raywa1cher.pasttyspring.configs.validators.annotation.FieldDurationBigger;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.auth")
@Validated
@FieldDurationBigger(field = "accessTokenDuration", biggerField = "refreshTokenDuration",
		message = "Access token must be smaller than refresh")
public class AuthConfig {
	/**
	 * JWT secret key.
	 */
	@NotBlank
	private String jwtSecretKey;

	/**
	 * Duration of access token (should be small).
	 */
	@NotNull
	private Duration accessTokenDuration;

	/**
	 * Duration of refresh token (should be bigger than accessToken).
	 */
	@NotNull
	private Duration refreshTokenDuration;
}
