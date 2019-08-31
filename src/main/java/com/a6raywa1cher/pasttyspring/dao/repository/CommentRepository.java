package com.a6raywa1cher.pasttyspring.dao.repository;

import com.a6raywa1cher.pasttyspring.models.Comment;
import com.a6raywa1cher.pasttyspring.models.Script;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends CrudRepository<Comment, Long> {
	Optional<Comment> getByScriptAndId(Script script, Long id);

	List<Comment> getByScript(Script script);
}
