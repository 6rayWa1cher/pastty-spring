package com.a6raywa1cher.pasttyspring.configs;

import com.a6raywa1cher.pasttyspring.components.coderunner.commandcompiler.CommandCompiler;
import com.a6raywa1cher.pasttyspring.components.coderunner.commandcompiler.UnixCommandCompiler;
import com.a6raywa1cher.pasttyspring.components.coderunner.commandcompiler.WindowsCommandCompiler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CodeRunnerConfig {
	@Bean
	public CommandCompiler commandCompiler() {
		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
		return isWindows ? new WindowsCommandCompiler() : new UnixCommandCompiler();
	}
}
