package com.a6raywa1cher.pasttyspring.dao.repository;

import com.a6raywa1cher.pasttyspring.models.Script;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface ScriptRepository extends PagingAndSortingRepository<Script, Long> {
	Page<Script> findAllByVisibleTrue(Pageable pageable);

	Optional<Script> findByName(String name);
}
