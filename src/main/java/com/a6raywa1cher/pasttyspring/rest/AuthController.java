package com.a6raywa1cher.pasttyspring.rest;

import com.a6raywa1cher.pasttyspring.configs.AuthConfig;
import com.a6raywa1cher.pasttyspring.configs.security.HashingService;
import com.a6raywa1cher.pasttyspring.configs.security.JwtSecurityTokenService;
import com.a6raywa1cher.pasttyspring.configs.security.RefreshTokenRevokeCache;
import com.a6raywa1cher.pasttyspring.dao.interfaces.RefreshJwtTokenService;
import com.a6raywa1cher.pasttyspring.dao.interfaces.UserService;
import com.a6raywa1cher.pasttyspring.models.RefreshJwtToken;
import com.a6raywa1cher.pasttyspring.models.User;
import com.a6raywa1cher.pasttyspring.rest.dto.mirror.RefreshJwtTokenMirror;
import com.a6raywa1cher.pasttyspring.rest.dto.request.GetAccessTokenDTO;
import com.a6raywa1cher.pasttyspring.rest.dto.request.LoginDTO;
import com.a6raywa1cher.pasttyspring.rest.dto.request.LogoutDTO;
import com.a6raywa1cher.pasttyspring.rest.dto.response.GetAccessTokenResponse;
import com.a6raywa1cher.pasttyspring.rest.dto.response.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {
	private RefreshJwtTokenService refreshJwtTokenService;
	private RefreshTokenRevokeCache cache;
	private HashingService hashingService;
	private AuthConfig authConfig;
	private UserService userService;
	private JwtSecurityTokenService tokenService;

	@Autowired
	public AuthController(RefreshJwtTokenService refreshJwtTokenService, RefreshTokenRevokeCache cache, HashingService hashingService,
	                      AuthConfig authConfig, UserService userService,
	                      JwtSecurityTokenService tokenService) {
		this.refreshJwtTokenService = refreshJwtTokenService;
		this.cache = cache;
		this.hashingService = hashingService;
		this.authConfig = authConfig;
		this.userService = userService;
		this.tokenService = tokenService;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginDTO dto) {
		String username = dto.getUsername();
		Optional<User> user = userService.findByUsername(username);
		if (user.isEmpty() || !hashingService.compare(dto.getPassword(), user.get().getPassword())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		Pair<RefreshJwtToken, String> refreshJwtTokenStringPair = tokenService.createRefreshToken(user.get()).orElseThrow();
		String accessJwtToken = tokenService.createAccessToken(refreshJwtTokenStringPair.getFirst()).orElseThrow();
		return ResponseEntity.ok(new LoginResponse(RefreshJwtTokenMirror.convert(refreshJwtTokenStringPair.getFirst()),
				refreshJwtTokenStringPair.getSecond(), accessJwtToken));
	}

	@PostMapping("/get_access")
	public ResponseEntity<GetAccessTokenResponse> getAccessToken(@RequestBody @Valid GetAccessTokenDTO dto) {
		Optional<String> refreshUUID = tokenService.getJti(dto.getRefreshToken());
		if (refreshUUID.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		Optional<RefreshJwtToken> refreshJwtToken = refreshJwtTokenService.find(refreshUUID.get());
		if (refreshJwtToken.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		RefreshJwtToken refresh = refreshJwtToken.get();
		if (refresh.getExpDate().isBefore(LocalDateTime.now())) {
			refreshJwtTokenService.delete(refresh);
			cache.revoke(refreshUUID.get());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		String accessToken = tokenService.createAccessToken(refresh).orElseThrow();
		GetAccessTokenResponse response = new GetAccessTokenResponse();
		response.setAccessToken(accessToken);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(@RequestBody @Valid LogoutDTO dto) {
		Optional<String> refreshUUID = tokenService.getJti(dto.getRefreshToken());
		if (refreshUUID.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		Optional<RefreshJwtToken> refreshJwtToken = refreshJwtTokenService.find(refreshUUID.get());
		if (refreshJwtToken.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		RefreshJwtToken refresh = refreshJwtToken.get();
		refreshJwtTokenService.delete(refresh);
		cache.revoke(refreshUUID.get());
		return ResponseEntity.ok().build();
	}

	@PostMapping("/full_logout")
	public ResponseEntity<?> logoutFromAllDevices(Authentication authentication) {
		Optional<User> user = userService.findFromAuthentication(authentication);
		if (user.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		refreshJwtTokenService.revokeAllByUser(user.get());
		return ResponseEntity.ok().build();
	}

	@GetMapping("/restricted_area")
	public ResponseEntity<String> restricted() {
		return ResponseEntity.ok("Good!");
	}
}
