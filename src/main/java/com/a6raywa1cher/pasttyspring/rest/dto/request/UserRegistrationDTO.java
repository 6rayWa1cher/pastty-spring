package com.a6raywa1cher.pasttyspring.rest.dto.request;

import com.a6raywa1cher.pasttyspring.rest.ControllerValidations;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UserRegistrationDTO {
	@Pattern(regexp = ControllerValidations.USERNAME_REGEX)
	private String username;

	@NotBlank
	@Size(max = ControllerValidations.PASSWORD_LENGTH)
	private String password;
}
