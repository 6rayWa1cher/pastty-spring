package com.a6raywa1cher.pasttyspring.rest.dto.mirror;

import com.a6raywa1cher.pasttyspring.models.RefreshJwtToken;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RefreshJwtTokenMirror {
	private String uuid;

	private UserMirror userMirror;

	private LocalDateTime expDate;

	public static RefreshJwtTokenMirror convert(RefreshJwtToken refreshJwtToken) {
		if (refreshJwtToken == null) {
			return null;
		}
		RefreshJwtTokenMirror mirror = new RefreshJwtTokenMirror();
		mirror.setUuid(refreshJwtToken.getUuid());
		mirror.setExpDate(refreshJwtToken.getExpDate());
		mirror.setUserMirror(UserMirror.convert(refreshJwtToken.getUser()));
		return mirror;
	}
}
