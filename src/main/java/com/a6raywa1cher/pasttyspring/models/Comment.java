package com.a6raywa1cher.pasttyspring.models;

import com.a6raywa1cher.pasttyspring.rest.ControllerValidations;
import lombok.Data;
import org.hibernate.annotations.AttributeAccessor;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class Comment {
	@Id
	@GeneratedValue
	@AttributeAccessor("property")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Comment upperLevel;

	@OneToMany(mappedBy = "upperLevel", cascade = CascadeType.ALL)
	private List<Comment> lowerLevel;

	@Column(length = ControllerValidations.COMMENT_LENGTH, nullable = false)
	private String body;

	@Column(nullable = false, updatable = false)
	private LocalDateTime creationTime;

	@Column(nullable = false)
	private boolean edited;

	@ManyToOne(optional = false)
	private Script script;

	@ManyToOne
	@Nullable
	private User user;

	@Override
	public String toString() {
		return "Comment{" +
				"id=" + id +
				", upperLevel=" + (upperLevel == null ? null : upperLevel.getId()) +
				", body='" + body + '\'' +
				", creationTime=" + creationTime +
				", edited=" + edited +
				", script=" + script.getId() +
				", user=" + (user == null ? null : user.getId()) +
				'}';
	}
}
