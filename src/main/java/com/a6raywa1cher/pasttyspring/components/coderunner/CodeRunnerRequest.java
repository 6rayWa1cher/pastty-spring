package com.a6raywa1cher.pasttyspring.components.coderunner;

import com.a6raywa1cher.pasttyspring.models.Script;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
public class CodeRunnerRequest {
	private final String uuid;
	private final Script toExec;
	private final String stdin;

	public CodeRunnerRequest(Script toExec, String stdin) {
		uuid = UUID.randomUUID().toString();
		this.toExec = toExec;
		this.stdin = stdin;
	}
}
