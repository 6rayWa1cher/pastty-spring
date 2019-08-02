package com.a6raywa1cher.pasttyspring.dao.repository;

import com.a6raywa1cher.pasttyspring.models.JwtToken;
import org.springframework.data.repository.CrudRepository;

public interface JwtTokenRepository extends CrudRepository<JwtToken, String> {
}
