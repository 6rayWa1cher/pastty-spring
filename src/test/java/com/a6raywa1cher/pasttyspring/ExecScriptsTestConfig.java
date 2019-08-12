package com.a6raywa1cher.pasttyspring;

import com.a6raywa1cher.pasttyspring.components.CodeRunner;
import com.a6raywa1cher.pasttyspring.configs.AppConfig;
import com.a6raywa1cher.pasttyspring.configs.ExecScriptsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@Import(PasttySpringApplication.class)
@Profile("cr_test")
public class ExecScriptsTestConfig {

	@Autowired
	private AppConfig appConfig;

	@Bean
	@Primary
	public CodeRunner codeRunner() {
		ExecScriptsConfig execScriptsConfig = new ExecScriptsConfig();
		execScriptsConfig.setBufferSize(1048576);
		execScriptsConfig.setMaxOutputSize(1048576);
		ExecScriptsConfig.RunnerEnvironmentConfig config = new ExecScriptsConfig.RunnerEnvironmentConfig();
		config.setName("java");
		config.setSourceFilename("{1}.java");
		config.setCompiledFilename("Main.class");
		config.setCompile(Arrays.asList(
				"cp {1} {3}/Main.java",
				"javac -d {3} {3}/Main.java"
		));
		config.setExec("java Main");
		execScriptsConfig.setEnvironments(Collections.singletonList(config));
		return new CodeRunner(appConfig, execScriptsConfig);
	}
}
