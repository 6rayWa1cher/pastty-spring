package com.a6raywa1cher.pasttyspring.rest.dto.response;

import lombok.Data;

@Data
public class ExecutionScriptResponse {
	private int exitCode;
	private String stdout;
}
