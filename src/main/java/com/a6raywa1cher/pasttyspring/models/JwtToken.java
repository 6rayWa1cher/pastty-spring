package com.a6raywa1cher.pasttyspring.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtToken {
	@Id
	@Column(length = 36)
	private String uuid;

	@ManyToOne
	private User user;

	@Column
	private LocalDateTime expDate;
}
