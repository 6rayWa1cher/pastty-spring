package com.a6raywa1cher.pasttyspring.dao.impl;

import com.a6raywa1cher.pasttyspring.dao.interfaces.CommentService;
import com.a6raywa1cher.pasttyspring.dao.repository.CommentRepository;
import com.a6raywa1cher.pasttyspring.models.Comment;
import com.a6raywa1cher.pasttyspring.models.Script;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {
	private CommentRepository repository;

	@Autowired
	public CommentServiceImpl(CommentRepository repository) {
		this.repository = repository;
	}


	@Override
	public Optional<Comment> getByScriptAndId(Script script, Long id) {
		return repository.getByScriptAndId(script, id);
	}

	@Override
	public List<Comment> getByScript(Script script) {
		return repository.getByScript(script);
	}

	@Override
	public Comment save(Comment comment) {
		return repository.save(comment);
	}

	@Override
	public void delete(Comment comment) {
		repository.delete(comment);
	}
}
