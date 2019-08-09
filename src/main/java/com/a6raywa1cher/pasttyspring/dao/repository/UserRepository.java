package com.a6raywa1cher.pasttyspring.dao.repository;

import com.a6raywa1cher.pasttyspring.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
	Optional<User> findByUsername(String username);
}
