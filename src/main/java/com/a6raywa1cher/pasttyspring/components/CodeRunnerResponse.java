package com.a6raywa1cher.pasttyspring.components;

import lombok.Data;

@Data
public class CodeRunnerResponse {
	private final CodeRunnerRequest request;
	private final int exitCode;
	private final String stdout;
}
