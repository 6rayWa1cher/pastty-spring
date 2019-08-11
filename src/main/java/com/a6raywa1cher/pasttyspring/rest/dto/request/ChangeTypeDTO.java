package com.a6raywa1cher.pasttyspring.rest.dto.request;

import com.a6raywa1cher.pasttyspring.models.enums.ScriptType;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ChangeTypeDTO {
	@NotNull
	private ScriptType type;
}
