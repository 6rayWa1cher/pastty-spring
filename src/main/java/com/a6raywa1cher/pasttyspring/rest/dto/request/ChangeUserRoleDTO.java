package com.a6raywa1cher.pasttyspring.rest.dto.request;

import com.a6raywa1cher.pasttyspring.models.enums.Role;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ChangeUserRoleDTO {
	@NotNull
	private Role role;
}
