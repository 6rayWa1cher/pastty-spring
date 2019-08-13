package com.a6raywa1cher.pasttyspring.components.coderunner.commandcompiler;

import com.a6raywa1cher.pasttyspring.configs.ExecScriptsConfig;

import java.nio.file.Path;
import java.util.List;

public interface CommandCompiler {
	List<String[]> prepareCompile(ExecScriptsConfig.RunnerEnvironmentConfig config,
	                              Path sourcePath, Path compiledPath);

	String prepareExec(ExecScriptsConfig.RunnerEnvironmentConfig config, Path compiledPath);

	String prepareSourceFilename(ExecScriptsConfig.RunnerEnvironmentConfig config, String file);

	String prepareCompiledFilename(ExecScriptsConfig.RunnerEnvironmentConfig config, String file);
}
