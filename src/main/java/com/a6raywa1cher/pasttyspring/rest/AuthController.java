package com.a6raywa1cher.pasttyspring.rest;

import com.a6raywa1cher.pasttyspring.configs.AuthConfig;
import com.a6raywa1cher.pasttyspring.configs.security.HashingService;
import com.a6raywa1cher.pasttyspring.configs.security.JwtSecurityTokenService;
import com.a6raywa1cher.pasttyspring.dao.interfaces.JwtTokenService;
import com.a6raywa1cher.pasttyspring.dao.interfaces.UserService;
import com.a6raywa1cher.pasttyspring.models.JwtToken;
import com.a6raywa1cher.pasttyspring.models.User;
import com.a6raywa1cher.pasttyspring.rest.dto.mirror.JwtTokenMirror;
import com.a6raywa1cher.pasttyspring.rest.dto.request.LoginDTO;
import com.a6raywa1cher.pasttyspring.rest.dto.response.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {
	private JwtTokenService jwtTokenService;
	private HashingService hashingService;
	private AuthConfig authConfig;
	private UserService userService;
	private JwtSecurityTokenService tokenService;

	@Autowired
	public AuthController(JwtTokenService jwtTokenService, HashingService hashingService,
	                      AuthConfig authConfig, UserService userService,
	                      JwtSecurityTokenService tokenService) {
		this.jwtTokenService = jwtTokenService;
		this.hashingService = hashingService;
		this.authConfig = authConfig;
		this.userService = userService;
		this.tokenService = tokenService;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginDTO dto) {
		String username = dto.getUsername();
		String password = hashingService.hash(dto.getPassword());
		Optional<User> user = userService.findByUsernameAndPassword(username, password);
		if (user.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		Pair<JwtToken, String> pair = tokenService.createToken(user.get()).orElseThrow();
		JwtTokenMirror jwtTokenMirror = JwtTokenMirror.convert(pair.getFirst());
//		log.info("User username:{} logged in from ip:{} uuid:{}",
//				dto.getUsername(), request.getRemoteAddr(), tokenService.getJti(token));
		return ResponseEntity.ok(new LoginResponse(jwtTokenMirror, pair.getSecond()));
	}

	@GetMapping("/restricted_area")
	public ResponseEntity<String> restricted() {
		return ResponseEntity.ok("Good!");
	}
}
