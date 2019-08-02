package com.a6raywa1cher.pasttyspring.rest.dto.request;

import com.a6raywa1cher.pasttyspring.rest.ControllerValidations;
import com.a6raywa1cher.pasttyspring.rest.ScriptController;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel
public class UploadScriptDTO {
	@Size(max = ControllerValidations.SCRIPT_NAME_LENGTH)
	private String name = ScriptController.generateRandomName();

	@Size(max = ControllerValidations.SCRIPT_TITLE_LENGTH)
	private String title;

	@Size(max = ControllerValidations.SCRIPT_DESCRIPTION_LENGTH)
	private String description;

	private boolean visible = true;

	@NotBlank
	@Size(max = ControllerValidations.SCRIPT_DIALECT_LENGTH)
	private String dialect;

	@NotBlank
	private String code;
}
