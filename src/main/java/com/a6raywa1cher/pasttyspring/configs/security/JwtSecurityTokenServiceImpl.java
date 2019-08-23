package com.a6raywa1cher.pasttyspring.configs.security;

import com.a6raywa1cher.pasttyspring.configs.AuthConfig;
import com.a6raywa1cher.pasttyspring.dao.interfaces.RefreshJwtTokenService;
import com.a6raywa1cher.pasttyspring.models.RefreshJwtToken;
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
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtSecurityTokenServiceImpl implements JwtSecurityTokenService {
	public static final String REFRESH_ID_CLAIM = "rid";

	private final String appName;

	private final RefreshJwtTokenService tokenService;

	private final Algorithm algorithm;

	private final Duration accessTokenExpiration;

	private final Duration refreshTokenExpiration;

	private final JWTVerifier verifier;

	@Autowired
	public JwtSecurityTokenServiceImpl(RefreshJwtTokenService tokenService,
	                                   AuthConfig authConfig) {
		this.accessTokenExpiration = authConfig.getAccessTokenDuration();
		this.refreshTokenExpiration = authConfig.getRefreshTokenDuration();
		this.algorithm = Algorithm.HMAC256(authConfig.getJwtSecretKey());
		this.appName = "Pastty.io";
		this.verifier = JWT.require(algorithm)
				.withIssuer(appName)
				.build();
		this.tokenService = tokenService;
	}

	@Override
	public Optional<RefreshJwtToken> getToken(String token) {
		try {
			DecodedJWT jwtDecoded = verifier.verify(token);

			String uuid = jwtDecoded.getId();
			return tokenService.find(uuid);
		} catch (JWTVerificationException | NullPointerException exception) {
			return Optional.empty();
		}
	}

	@SuppressWarnings("DuplicatedCode")
	@Override
	public Optional<String> createAccessToken(RefreshJwtToken refreshJwtToken) {
		User user = refreshJwtToken.getUser();
		String refreshTokenUUID = refreshJwtToken.getUuid();
		try {
			UUID uuid = UUID.randomUUID();

			LocalDateTime now = LocalDateTime.now();
			LocalDateTime exp = now.plus(accessTokenExpiration);

			Date issuedDate = new Date();
			issuedDate.setTime(Timestamp.valueOf(now.minusSeconds(1)).getTime());

			Date expDate = new Date();
			expDate.setTime(Timestamp.valueOf(exp).getTime());

			TokenUser tokenUser = TokenUser.convert(user);

			String token = JWT.create()
					.withIssuer(appName)
					.withIssuedAt(issuedDate)
					.withNotBefore(issuedDate)
					.withExpiresAt(expDate)
					.withSubject(tokenUser.toJson())
					.withClaim(REFRESH_ID_CLAIM, refreshTokenUUID)
					.withJWTId(uuid.toString())
					.sign(algorithm);
			return Optional.of(token);
		} catch (JWTCreationException e) {
			return Optional.empty();
		}
	}

	@SuppressWarnings("DuplicatedCode")
	@Override
	public Optional<Pair<RefreshJwtToken, String>> createRefreshToken(User user) {
		try {
			UUID uuid = UUID.randomUUID();

			LocalDateTime now = LocalDateTime.now();
			LocalDateTime exp = now.plus(refreshTokenExpiration);

			Date issuedDate = new Date();
			issuedDate.setTime(Timestamp.valueOf(now.minusSeconds(1)).getTime());

			Date expDate = new Date();
			expDate.setTime(Timestamp.valueOf(exp).getTime());

			TokenUser tokenUser = TokenUser.convert(user);

			String token = JWT.create()
					.withIssuer(appName)
					.withIssuedAt(issuedDate)
					.withNotBefore(issuedDate)
					.withExpiresAt(expDate)
					.withSubject(tokenUser.toJson())
					.withJWTId(uuid.toString())
					.sign(algorithm);
			return Optional.of(Pair.of(tokenService.save(new RefreshJwtToken(uuid.toString(), user, exp)), token));
		} catch (JWTCreationException e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<TokenAuthentication> toAuthentication(String accessToken) {
		try {
			DecodedJWT jwtDecoded = verifier.verify(accessToken);
			TokenUser tokenUser = TokenUser.fromJson(jwtDecoded.getSubject());
			String refreshTokenUUID = jwtDecoded.getClaim(REFRESH_ID_CLAIM).asString();
			LocalDateTime exp = jwtDecoded.getExpiresAt().toInstant()
					.atZone(ZoneId.systemDefault())
					.toLocalDateTime();
			return Optional.of(new TokenAuthentication(accessToken, tokenUser, refreshTokenUUID, exp));
		} catch (JWTVerificationException jwt) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> getJti(String token) {
		try {
			DecodedJWT jwtDecoded = verifier.verify(token);

			return Optional.of(jwtDecoded.getId());
		} catch (JWTVerificationException | NullPointerException exception) {
			return Optional.empty();
		}
	}
}
