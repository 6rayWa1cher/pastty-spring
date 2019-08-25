package com.a6raywa1cher.pasttyspring.rest.dto.request;

import com.a6raywa1cher.pasttyspring.models.Script;
import com.a6raywa1cher.pasttyspring.rest.ControllerValidations;
import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class PatchScriptDTO {
	@Pattern(regexp = ControllerValidations.SCRIPT_NAME_REGEX)
	private String name;

	@Size(max = ControllerValidations.SCRIPT_TITLE_LENGTH)
	private String title;

	@Size(max = ControllerValidations.SCRIPT_DESCRIPTION_LENGTH)
	private String description;

	private Boolean visible;

	private String code;

	@Size(max = ControllerValidations.SCRIPT_DIALECT_LENGTH)
	private String dialect;

	public void patch(Script toPatch) {
		if (name != null) toPatch.setName(name);
		if (title != null) toPatch.setTitle(title);
		if (description != null) toPatch.setDescription(description);
		if (visible != null) toPatch.setVisible(visible);
		if (dialect != null) toPatch.setDialect(dialect);
	}
}
