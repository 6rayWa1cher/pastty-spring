package com.a6raywa1cher.pasttyspring.dao.impl;

import com.a6raywa1cher.pasttyspring.dao.interfaces.ScriptService;
import com.a6raywa1cher.pasttyspring.dao.repository.ScriptRepository;
import com.a6raywa1cher.pasttyspring.models.Script;
import com.a6raywa1cher.pasttyspring.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ScriptServiceImpl implements ScriptService {
	private ScriptRepository repository;

	@Autowired
	public ScriptServiceImpl(ScriptRepository repository) {
		this.repository = repository;
	}

	@Override
	public Page<Script> getList(Pageable pageable) {
		return repository.findAllByVisibleTrue(pageable);
	}

	@Override
	public Optional<Script> findByName(String name) {
		return repository.findByName(name);
	}

	@Override
	public Optional<Script> findById(Long id) {
		return repository.findById(id);
	}

	@Override
	public Page<Script> findAllByVisibleAndAuthor(boolean visible, User author, Pageable pageable) {
		return repository.findAllByVisibleAndAuthor(visible, author, pageable);
	}

	@Override
	public Script save(Script script) {
		return repository.save(script);
	}
}
