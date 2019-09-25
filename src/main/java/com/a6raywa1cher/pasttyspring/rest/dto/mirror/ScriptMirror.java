package com.a6raywa1cher.pasttyspring.rest.dto.mirror;

import com.a6raywa1cher.pasttyspring.models.Script;
import com.a6raywa1cher.pasttyspring.models.enums.ScriptType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScriptMirror {
	private Long id;

	private String name;

	private String title;

	private String description;

	private UserMirror author;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private LocalDateTime creationTime;

	private Boolean visible;

	private String code;

	private String dialect;

	private ScriptType type;

	public static ScriptMirror convert(Script script) {
		if (script == null) {
			return null;
		}
		ScriptMirror mirror = new ScriptMirror();
		mirror.setId(script.getId());
		mirror.setName(script.getName());
		mirror.setTitle(script.getTitle());
		mirror.setDescription(script.getDescription());
		mirror.setAuthor(UserMirror.convert(script.getAuthor()));
		mirror.setCreationTime(script.getCreationTime());
		mirror.setVisible(script.isVisible());
		mirror.setDialect(script.getDialect());
		mirror.setType(script.getType());
		return mirror;
	}
}
