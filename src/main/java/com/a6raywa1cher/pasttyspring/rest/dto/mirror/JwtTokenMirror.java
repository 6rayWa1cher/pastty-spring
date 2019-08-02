package com.a6raywa1cher.pasttyspring.rest.dto.mirror;

import com.a6raywa1cher.pasttyspring.models.JwtToken;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JwtTokenMirror {
	private String uuid;

	private UserMirror userMirror;

	private LocalDateTime expDate;

	public static JwtTokenMirror convert(JwtToken jwtToken) {
		JwtTokenMirror mirror = new JwtTokenMirror();
		mirror.setUuid(jwtToken.getUuid());
		mirror.setExpDate(jwtToken.getExpDate());
		mirror.setUserMirror(UserMirror.convert(jwtToken.getUser()));
		return mirror;
	}
}
