package com.a6raywa1cher.pasttyspring.configs.security;

import com.a6raywa1cher.pasttyspring.configs.AuthConfig;
import com.a6raywa1cher.pasttyspring.dao.interfaces.JwtTokenService;
import com.a6raywa1cher.pasttyspring.models.JwtToken;
import com.a6raywa1cher.pasttyspring.models.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtSecurityTokenServiceImpl implements JwtSecurityTokenService {

	private final String appName;

	private final JwtTokenService tokenService;

	private final Algorithm algorithm;

	private final Duration expiration;

	private final JWTVerifier verifier;

	@Autowired
	public JwtSecurityTokenServiceImpl(JwtTokenService tokenService,
	                                   AuthConfig authConfig) {
		this.expiration = authConfig.getSessionDuration();
		this.algorithm = Algorithm.HMAC256(authConfig.getJwtSecretKey());
		this.appName = "Pastty.io";
		this.verifier = JWT.require(algorithm)
				.withIssuer(appName)
				.build();
		this.tokenService = tokenService;
	}

	@Override
	public String getToken(User user) {
		try {
			Pair<JwtToken, String> jwtToken = createToken(user).orElseThrow();

			return jwtToken.getSecond();
		} catch (RuntimeException e) {
			return null;
		}
	}

	@Override
	public Optional<User> checkToken(String token) {
		try {
			DecodedJWT jwtDecoded = verifier.verify(token);

			String uuid = jwtDecoded.getId();
			Optional<JwtToken> optionalJwtToken = tokenService.find(uuid);
			if (optionalJwtToken.isEmpty()) {
				return Optional.empty();
			}
			return Optional.of(optionalJwtToken.get().getUser());
		} catch (JWTVerificationException | NullPointerException exception) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<JwtToken> getToken(String token) {
		try {
			DecodedJWT jwtDecoded = verifier.verify(token);

			String uuid = jwtDecoded.getId();
			return tokenService.find(uuid);
		} catch (JWTVerificationException | NullPointerException exception) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Pair<JwtToken, String>> createToken(User user) {
		try {
			UUID uuid = UUID.randomUUID();

			LocalDateTime now = LocalDateTime.now();
			LocalDateTime exp = now.plus(expiration);

			Date issuedDate = new Date();
			issuedDate.setTime(Timestamp.valueOf(now.minusSeconds(1)).getTime());

			Date expDate = new Date();
			expDate.setTime(Timestamp.valueOf(exp).getTime());

			String token = JWT.create()
					.withIssuer(appName)
					.withClaim("username", user.getUsername())
					.withIssuedAt(issuedDate)
					.withNotBefore(issuedDate)
					.withExpiresAt(expDate)
					.withJWTId(uuid.toString())
					.sign(algorithm);
			return Optional.of(Pair.of(tokenService.save(new JwtToken(uuid.toString(), user, exp)), token));
		} catch (JWTCreationException e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> getJti(String token) {
		try {
			DecodedJWT jwtDecoded = JWT.decode(token);

			return Optional.of(jwtDecoded.getId());
		} catch (JWTVerificationException | NullPointerException exception) {
			return Optional.empty();
		}
	}
}
