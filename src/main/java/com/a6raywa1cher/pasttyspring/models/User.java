package com.a6raywa1cher.pasttyspring.models;

import com.a6raywa1cher.pasttyspring.models.enums.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(exclude = {"scripts", "refreshJwtTokens"})
public class User {
	@Id
	@GeneratedValue
	private Long id;

	@Column(length = 20)
	private String username;

	@Column(length = 256)
	private String password;

	@Column
	@Enumerated(EnumType.ORDINAL)
	private Role role;

	@OneToMany(mappedBy = "author")
	private List<Script> scripts;

	@OneToMany(mappedBy = "user")
	private List<RefreshJwtToken> refreshJwtTokens;

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", username='" + username + '\'' +
				", password='" + password + '\'' +
				", role=" + role +
				'}';
	}
}
