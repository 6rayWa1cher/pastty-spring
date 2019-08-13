package com.a6raywa1cher.pasttyspring.components.coderunner;

import com.a6raywa1cher.pasttyspring.configs.ExecScriptsConfig;
import com.a6raywa1cher.pasttyspring.models.Script;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;

@EqualsAndHashCode
public class ExecutorTaskImpl implements ExecutorTask {
	private static final Logger log = LoggerFactory.getLogger(CodeRunner.class);
	private final CodeRunner codeRunner;
	private final CodeRunnerRequest request;
	private final ExecScriptsConfig config;
	private final Path execDir;
	private final Path compiled;
	private Process process;

	ExecutorTaskImpl(CodeRunner codeRunner, CodeRunnerRequest request, ExecScriptsConfig config,
	                 Path execDir, Path compiled) {
		this.codeRunner = codeRunner;
		this.request = request;
		this.config = config;
		this.execDir = execDir;
		this.compiled = compiled;
	}

	@Override
	public CodeRunnerResponse call() throws Exception {
		Script script = request.getToExec();
		String preparedCommand = config.getEnvironment(script.getDialect())
				.prepareExec(compiled);
		try {
			process = Runtime.getRuntime().exec(preparedCommand, new String[]{}, execDir.toFile());
			codeRunner.registerTask(this);
		} catch (IllegalStateException e) {
			close();
			throw e;
		}
		log.info("Running process... command:{} info:{}", preparedCommand, process.info());
		try (BufferedReader processOutput = new BufferedReader(new InputStreamReader(new SequenceInputStream(process.getInputStream(), process.getErrorStream())),
				config.getBufferSize());
		     BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
			StringBuilder stdout = new StringBuilder(config.getMaxOutputSize());
			processInput.write(request.getStdin());
			processInput.flush();
			processInput.close();
			String line;
			while (process.isAlive() || processOutput.ready()) {
				line = processOutput.readLine();
				if (line != null) stdout.append(line);
				if (stdout.length() > config.getMaxOutputSize()) {
					break;
				}
			}
			return new CodeRunnerResponse(request, process.exitValue(), stdout.toString());
		} finally {
			System.out.println("CLOSING");
			process.destroy();
			codeRunner.removeTask(this);
		}
	}

	@Override
	public void close() throws InterruptedException {
		if (process != null) {
			process.destroyForcibly().waitFor();
			codeRunner.removeTask(this);
		}
	}
}
