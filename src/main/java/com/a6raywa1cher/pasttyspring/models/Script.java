package com.a6raywa1cher.pasttyspring.models;

import com.a6raywa1cher.pasttyspring.models.enums.ScriptType;
import com.a6raywa1cher.pasttyspring.rest.ControllerValidations;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Script {
	@Id
	@GeneratedValue
	private Long id;

	@Column(length = ControllerValidations.SCRIPT_NAME_LENGTH, unique = true, nullable = false)
	private String name;

	@Column(length = ControllerValidations.SCRIPT_TITLE_LENGTH)
	private String title;

	@Column(length = ControllerValidations.SCRIPT_DESCRIPTION_LENGTH)
	private String description;

	@ManyToOne
	private User author;

	@Column(unique = true, nullable = false)
	private String pathToFile;

	@Column
	private LocalDateTime creationTime;

	@Column
	private boolean visible;

	@Column(length = ControllerValidations.SCRIPT_DIALECT_LENGTH)
	private String dialect;

	@Column
	@Enumerated(EnumType.ORDINAL)
	private ScriptType type;

	@Column
	private Long maxComputeTime;

	@OneToMany(mappedBy = "script", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Comment> commentList;

	@Override
	public String toString() {
		return "Script{" +
				"id=" + id +
				", name='" + name + '\'' +
				", title='" + title + '\'' +
				", description='" + description + '\'' +
				", author=" + (author == null ? null : author.getUsername()) +
				", pathToFile='" + pathToFile + '\'' +
				", creationTime=" + creationTime +
				", visible=" + visible +
				", dialect='" + dialect + '\'' +
				", type=" + type +
				", maxComputeTime=" + maxComputeTime +
				'}';
	}
}
