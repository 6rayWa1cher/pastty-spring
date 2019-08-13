package com.a6raywa1cher.pasttyspring.components.coderunner;

import lombok.Data;

@Data
public class CodeRunnerResponse {
	private final CodeRunnerRequest request;
	private final int exitCode;
	private final String stdout;
}
