package com.a6raywa1cher.pasttyspring.components.coderunner.commandcompiler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.a6raywa1cher.pasttyspring.configs.ExecScriptsConfig.RunnerEnvironmentConfig;

public abstract class AbstractCommandCompiler implements CommandCompiler {

	public List<String[]> prepareCompile(RunnerEnvironmentConfig config,
	                                     Path sourcePath, Path compiledPath) {
		List<String> localCompile = new ArrayList<>();
		if (config.getCompile() == null) {
			localCompile.add(getDefaultCommand());
		} else {
			localCompile.addAll(config.getCompile());
		}
		return localCompile.stream().map(str -> str
				.replace("{1}", sourcePath.toAbsolutePath().toString())
				.replace("{2}", compiledPath.toAbsolutePath().toString())
				.replace("{3}", compiledPath.getParent().toAbsolutePath().toString()))
				.map(str -> str.startsWith("{!}") ? new String[]{str.substring(3)} :
						decorateCommand(str))
				.collect(Collectors.toList());
	}

	protected abstract String getDefaultCommand();

	protected abstract String[] decorateCommand(String cmd);

	public String prepareExec(RunnerEnvironmentConfig config, Path compiledPath) {
		return config.getExec()
				.replace("{1}", compiledPath.toAbsolutePath().toString());
	}

	public String prepareSourceFilename(RunnerEnvironmentConfig config, String file) {
		return config.getSourceFilename()
				.replace("{1}", file);
	}

	public String prepareCompiledFilename(RunnerEnvironmentConfig config, String file) {
		return (config.getCompiledFilename() != null ? config.getCompiledFilename() : config.getSourceFilename())
				.replace("{1}", file);
	}
}
