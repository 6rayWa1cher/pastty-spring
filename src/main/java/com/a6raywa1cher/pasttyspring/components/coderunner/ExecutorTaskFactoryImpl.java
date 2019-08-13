package com.a6raywa1cher.pasttyspring.components.coderunner;

import com.a6raywa1cher.pasttyspring.configs.ExecScriptsConfig;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class ExecutorTaskFactoryImpl implements ExecutorTaskFactory {
	@Override
	public ExecutorTask createTask(CodeRunner codeRunner, CodeRunnerRequest request, ExecScriptsConfig config,
	                               Path execDir, Path exec) {
		return new ExecutorTaskImpl(codeRunner, request, config, execDir, exec);
	}
}
