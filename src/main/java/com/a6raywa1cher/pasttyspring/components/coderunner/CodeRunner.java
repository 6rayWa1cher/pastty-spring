package com.a6raywa1cher.pasttyspring.components.coderunner;

import java.util.concurrent.CompletableFuture;

public interface CodeRunner {
	CompletableFuture<CodeRunnerResponse> execTask(CodeRunnerRequest request);

	void registerTask(ExecutorTask task) throws IllegalStateException;

	void removeTask(ExecutorTask task);
}
