package com.a6raywa1cher.pasttyspring.rest.dto.response;

import com.a6raywa1cher.pasttyspring.rest.dto.mirror.RefreshJwtTokenMirror;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
	private RefreshJwtTokenMirror refreshJwtToken;

	private String refreshToken;

	private String accessToken;
}
