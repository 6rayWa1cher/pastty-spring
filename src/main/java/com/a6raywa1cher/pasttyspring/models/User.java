package com.a6raywa1cher.pasttyspring.models;

import com.a6raywa1cher.pasttyspring.models.enums.Role;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class User {
	@Id
	@GeneratedValue
	private Long id;

	@Column(length = 20)
	private String username;

	@Column
	private String password;

	@Column
	@Enumerated(EnumType.ORDINAL)
	private Role role;

	@OneToMany(mappedBy = "author")
	private List<Script> scripts;

	@OneToMany(mappedBy = "user")
	private List<JwtToken> jwtTokens;
}
