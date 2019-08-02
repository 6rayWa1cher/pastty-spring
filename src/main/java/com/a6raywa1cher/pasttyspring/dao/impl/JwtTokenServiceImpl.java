package com.a6raywa1cher.pasttyspring.dao.impl;

import com.a6raywa1cher.pasttyspring.dao.interfaces.JwtTokenService;
import com.a6raywa1cher.pasttyspring.dao.repository.JwtTokenRepository;
import com.a6raywa1cher.pasttyspring.models.JwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {
	private JwtTokenRepository repository;

	@Autowired
	public JwtTokenServiceImpl(JwtTokenRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<JwtToken> find(String uuid) {
		return repository.findById(uuid);
	}

	@Override
	public JwtToken save(JwtToken save) {
		return repository.save(save);
	}
}
