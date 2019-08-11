package com.a6raywa1cher.pasttyspring;

import com.a6raywa1cher.pasttyspring.components.CodeRunner;
import com.a6raywa1cher.pasttyspring.configs.AppConfig;
import com.a6raywa1cher.pasttyspring.configs.ExecScriptsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;

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
		config.setExec("java {1}");
		execScriptsConfig.setEnvironments(Collections.singletonList(config));
		return new CodeRunner(appConfig, execScriptsConfig);
	}
}