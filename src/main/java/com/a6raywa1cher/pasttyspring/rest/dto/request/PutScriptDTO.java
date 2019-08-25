package com.a6raywa1cher.pasttyspring.rest.dto.request;

import com.a6raywa1cher.pasttyspring.models.Script;
import com.a6raywa1cher.pasttyspring.rest.ControllerValidations;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class PutScriptDTO {
	@NotBlank
	@Pattern(regexp = ControllerValidations.SCRIPT_NAME_REGEX)
	private String name;

	@Size(max = ControllerValidations.SCRIPT_TITLE_LENGTH)
	private String title;

	@Size(max = ControllerValidations.SCRIPT_DESCRIPTION_LENGTH)
	private String description;

	@NotNull
	private Boolean visible;

	@NotBlank
	private String code;

	@NotBlank
	@Size(max = ControllerValidations.SCRIPT_DIALECT_LENGTH)
	private String dialect;

	public void put(Script script) {
		script.setName(name);
		script.setTitle(title);
		script.setDescription(description);
		script.setVisible(visible);
		script.setDialect(dialect);
	}
}
