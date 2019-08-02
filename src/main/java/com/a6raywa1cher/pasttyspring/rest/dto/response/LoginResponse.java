package com.a6raywa1cher.pasttyspring.rest.dto.response;

import com.a6raywa1cher.pasttyspring.rest.dto.mirror.JwtTokenMirror;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
	private JwtTokenMirror jwtToken;

	private String token;
}
