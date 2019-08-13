package com.a6raywa1cher.pasttyspring.components.coderunner;

import com.a6raywa1cher.pasttyspring.configs.ExecScriptsConfig;

import java.nio.file.Path;

public interface ExecutorTaskFactory {
	ExecutorTask createTask(CodeRunner codeRunner, CodeRunnerRequest request, ExecScriptsConfig config,
	                        Path execDir, Path exec);
}
