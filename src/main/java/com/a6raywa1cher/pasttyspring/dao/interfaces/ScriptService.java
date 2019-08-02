package com.a6raywa1cher.pasttyspring.dao.interfaces;

import com.a6raywa1cher.pasttyspring.models.Script;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ScriptService {
	Page<Script> getList(Pageable pageable);

	Optional<Script> findByName(String name);

	Optional<Script> findById(Long id);

	Script save(Script script);
}
