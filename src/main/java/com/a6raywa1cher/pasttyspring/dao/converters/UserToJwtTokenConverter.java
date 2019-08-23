package com.a6raywa1cher.pasttyspring.dao.converters;

import com.a6raywa1cher.pasttyspring.models.RefreshJwtToken;
import com.a6raywa1cher.pasttyspring.models.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class UserToJwtTokenConverter {
	public static RefreshJwtToken apply(String token, User user) {
		DecodedJWT decodedJWT = JWT.decode(token);
		LocalDateTime exp = LocalDateTime.ofInstant(
				decodedJWT.getExpiresAt().toInstant(),
				ZoneId.systemDefault());
		String uuid = decodedJWT.getId();
		return new RefreshJwtToken(uuid, user, exp);
	}
}
