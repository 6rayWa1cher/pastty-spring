package com.a6raywa1cher.pasttyspring.models;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class Script {
	@Id
	@GeneratedValue
	private Long id;

	@Column(length = 36, unique = true)
	private String name;

	@Column(length = 150)
	private String title;

	@Column(length = 5000)
	private String description;

	@ManyToOne
	private User author;

	@Column
	private LocalDateTime creationTime;

	@Column
	private boolean visible;

	@Column
	private String dialect;

	@Column
	private Long maxComputeTime;
}
