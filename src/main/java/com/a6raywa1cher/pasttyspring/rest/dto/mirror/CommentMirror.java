package com.a6raywa1cher.pasttyspring.rest.dto.mirror;

import com.a6raywa1cher.pasttyspring.models.Comment;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CommentMirror {
	private Long id;

	private CommentMirror upperLevel;

	private List<CommentMirror> lowerLevel;

	private String body;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private LocalDateTime creationTime;

	private Boolean edited;

	private ScriptMirror script;

	private UserMirror user;

	public static CommentMirror convert(Comment comment) {
		return convert(comment, true, true, false);
	}

	public static CommentMirror convert(Comment comment, boolean includeUpperLevels, boolean includeLowerLevel, boolean includeLowerLevels) {
		if (comment == null) {
			return null;
		}
		CommentMirror mirror = new CommentMirror();
		mirror.setId(comment.getId());
		if (includeUpperLevels && comment.getUpperLevel() != null) {
			mirror.setUpperLevel(CommentMirror.convert(comment.getUpperLevel(), false, false, false));
		} else if (comment.getUpperLevel() != null) {
			CommentMirror upper = new CommentMirror();
			upper.setId(comment.getUpperLevel().getId());
			mirror.setUpperLevel(upper);
		}
		if (includeLowerLevels && comment.getLowerLevel() != null && comment.getLowerLevel().size() > 0) {
			mirror.setLowerLevel(comment.getLowerLevel().stream()
					.map(com -> CommentMirror.convert(com, false, true, true))
					.collect(Collectors.toList()));
		} else if (includeLowerLevel && comment.getLowerLevel() != null && comment.getLowerLevel().size() > 0) {
			mirror.setLowerLevel(comment.getLowerLevel().stream()
					.map(com -> CommentMirror.convert(com, false, false, false))
					.collect(Collectors.toList()));
		}
		mirror.setBody(comment.getBody());
		mirror.setCreationTime(comment.getCreationTime());
		mirror.setEdited(comment.isEdited());
		ScriptMirror scriptMirror = new ScriptMirror();
		scriptMirror.setId(comment.getScript().getId());
		scriptMirror.setName(comment.getScript().getName());
		mirror.setScript(scriptMirror);
		mirror.setUser(UserMirror.convert(comment.getUser()));
		return mirror;
	}
}
