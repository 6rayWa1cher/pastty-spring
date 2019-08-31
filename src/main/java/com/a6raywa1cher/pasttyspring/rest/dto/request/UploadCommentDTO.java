package com.a6raywa1cher.pasttyspring.rest.dto.request;

import com.a6raywa1cher.pasttyspring.rest.ControllerValidations;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UploadCommentDTO {
	@NotBlank
	@Size(max = ControllerValidations.COMMENT_LENGTH)
	private String body;

	private Long parentId;
}
