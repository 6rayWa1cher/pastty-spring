package com.a6raywa1cher.pasttyspring.dao.repository;

import com.a6raywa1cher.pasttyspring.models.RefreshJwtToken;
import com.a6raywa1cher.pasttyspring.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RefreshJwtTokenRepository extends CrudRepository<RefreshJwtToken, String> {
	List<RefreshJwtToken> findAllByUser(User user);
}
