package com.a6raywa1cher.pasttyspring;

import com.a6raywa1cher.pasttyspring.configs.ExecScriptsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;

@TestConfiguration
@Profile("cr_test")
public class ExecScriptsTestConfig {
	private ExecScriptsConfig execScriptsConfig;

	@Autowired
	public ExecScriptsTestConfig(ExecScriptsConfig execScriptsConfig) {
		this.execScriptsConfig = execScriptsConfig;
	}

	@PostConstruct
	public void execScriptsConfig() {
		execScriptsConfig.setBufferSize(1048576);
		execScriptsConfig.setMaxOutputSize(1048576);
		ExecScriptsConfig.RunnerEnvironmentConfig config = new ExecScriptsConfig.RunnerEnvironmentConfig();
		config.setName("java");
		config.setSourceFilename("{1}.java");
		config.setCompiledFilename("Main.class");
		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
		if (isWindows) {
			config.setCompile(Arrays.asList(
					"copy {1} {3}\\Main.java",
					"{!}javac -d {3} {3}\\Main.java"
			));
		} else {
			config.setCompile(Arrays.asList(
					"cp {1} {3}/Main.java",
					"javac -d {3} {3}/Main.java"
			));
		}
		config.setExec("java Main");
		execScriptsConfig.setEnvironments(Collections.singletonList(config));
	}
}
