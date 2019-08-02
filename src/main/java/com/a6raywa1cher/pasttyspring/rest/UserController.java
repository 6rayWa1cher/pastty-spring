package com.a6raywa1cher.pasttyspring.rest;

import com.a6raywa1cher.pasttyspring.configs.security.HashingService;
import com.a6raywa1cher.pasttyspring.dao.interfaces.UserService;
import com.a6raywa1cher.pasttyspring.models.User;
import com.a6raywa1cher.pasttyspring.models.enums.Role;
import com.a6raywa1cher.pasttyspring.rest.dto.exceptions.UserWithProvidedUsernameExistsException;
import com.a6raywa1cher.pasttyspring.rest.dto.mirror.UserMirror;
import com.a6raywa1cher.pasttyspring.rest.dto.request.UserRegistrationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.transaction.Transactional;
import javax.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {
	private UserService userService;
	private HashingService hashingService;

	@Autowired
	public UserController(UserService userService, HashingService hashingService) {
		this.userService = userService;
		this.hashingService = hashingService;
	}

	@PostMapping("/reg")
	@Transactional
	public ResponseEntity<UserMirror> register(@RequestBody @Valid UserRegistrationDTO dto) {
		if (userService.findByUsername(dto.getUsername()).isPresent()) {
			throw new UserWithProvidedUsernameExistsException();
		}
		User user = new User();
		user.setUsername(dto.getUsername());
		user.setPassword(hashingService.hash(dto.getPassword()));
		user.setRole(Role.USER);
		return ResponseEntity.ok(UserMirror.convert(userService.save(user)));
	}
}
