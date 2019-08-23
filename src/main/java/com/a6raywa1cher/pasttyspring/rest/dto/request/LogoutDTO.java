package com.a6raywa1cher.pasttyspring.rest.dto.request;

import com.a6raywa1cher.pasttyspring.rest.ControllerValidations;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class LogoutDTO {
	@NotBlank
	@Pattern(regexp = ControllerValidations.JWT_REGEX)
	private String refreshToken;
}
