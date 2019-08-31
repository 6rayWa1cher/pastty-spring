package com.a6raywa1cher.pasttyspring.dao.interfaces;

import com.a6raywa1cher.pasttyspring.models.Comment;
import com.a6raywa1cher.pasttyspring.models.Script;

import java.util.List;
import java.util.Optional;

public interface CommentService {
	Optional<Comment> getByScriptAndId(Script script, Long id);

	List<Comment> getByScript(Script script);

	Comment save(Comment comment);

	void delete(Comment comment);
}
