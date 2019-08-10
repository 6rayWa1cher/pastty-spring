package com.a6raywa1cher.pasttyspring.components;

import com.a6raywa1cher.pasttyspring.configs.AppConfig;
import com.a6raywa1cher.pasttyspring.configs.ExecScriptsConfig;
import com.a6raywa1cher.pasttyspring.models.Script;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class CodeRunner {
	private static final Logger log = LoggerFactory.getLogger(CodeRunner.class);
	private final AppConfig appConfig;
	private final ExecScriptsConfig config;
	private ExecutorService executorService;

	@Autowired
	public CodeRunner(AppConfig appConfig, ExecScriptsConfig config) {
		this.appConfig = appConfig;
		this.config = config;
		this.executorService = new ForkJoinPool();
	}

	public CompletableFuture<CodeRunnerResponse> execTask(CodeRunnerRequest request) {
		Script script = request.getToExec();
		ExecScriptsConfig.RunnerEnvironmentConfig runnerEnvironmentConfig = config.getEnvironment(script.getDialect());
		Path folderWithCompiled = Path.of(appConfig.getScriptsFolder(),
				"exec",
				request.getToExec().getPathToFile() + "_compiled");
		Path sources = Path.of(appConfig.getScriptsFolder(), request.getToExec().getPathToFile());
		String uniqueName = Path.of(sources.getFileName().toString()).getFileName().toString();
		Path compiled = Path.of(folderWithCompiled.toString(),
				runnerEnvironmentConfig.prepareCompiledFilename(uniqueName));
		log.info("script:{} folderCompiled:{} sources:{} compiled:{}",
				script.getId(), folderWithCompiled, sources, compiled);
		return CompletableFuture.supplyAsync(() -> null, executorService)
				.thenCompose(v -> {
					try {
						if (!folderWithCompiled.toFile().exists()) {
							Files.createDirectories(folderWithCompiled);
							String compileCommand = runnerEnvironmentConfig.prepareCompile(sources, compiled);
							return Runtime.getRuntime().exec(compileCommand, new String[]{},
									folderWithCompiled.toFile()).onExit();
						} else {
							return CompletableFuture.completedFuture(null);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})
				.thenAccept(process -> {
					if (process != null && process.exitValue() != 0) {
						throw new RuntimeException("Compilation not successful. " +
								new BufferedReader(new InputStreamReader(process.getErrorStream())).lines()
										.collect(Collectors.joining())
						);
					}
				})
				.exceptionally(throwable -> {
					try {
						FileUtils.deleteDirectory(folderWithCompiled.toFile());
						log.error("Compilation error", throwable);
						throw new RuntimeException("Compilation error. " + throwable.getMessage(), throwable);
					} catch (IOException e) {
						throw new RuntimeException(throwable.getMessage(), e);
					}
				})
				.thenApply(v -> {
					try (ExecutorTask task = new ExecutorTask(request, folderWithCompiled, compiled)) {
						Future<CodeRunnerResponse> responseFuture = executorService.invokeAll(
								Collections.singleton(task),
								request.getToExec().getMaxComputeTime(), TimeUnit.MILLISECONDS).get(0);
						return responseFuture.get();
					} catch (Exception e) {
						log.error("Run error", e);
						throw new RuntimeException(e);
					}
				});
	}

	final class ExecutorTask implements Callable<CodeRunnerResponse>, AutoCloseable {
		private final CodeRunnerRequest request;
		private final Path execDir;
		private final Path compiled;
		private Process process;

		ExecutorTask(CodeRunnerRequest request, Path execDir, Path compiled) {
			this.request = request;
			this.execDir = execDir;
			this.compiled = compiled;
		}

		@Override
		public CodeRunnerResponse call() throws Exception {
			Script script = request.getToExec();
			ExecScriptsConfig.RunnerEnvironmentConfig runnerEnvironmentConfig =
					config.getEnvironment(script.getDialect());
			String preparedCommand = runnerEnvironmentConfig
					.prepareExec(compiled);
			process = Runtime.getRuntime().exec(preparedCommand, new String[]{}, execDir.toFile());
			log.info("Running process... command:{} info:{}", preparedCommand, process.info());
			try (BufferedReader processOutput = new BufferedReader(new InputStreamReader(new SequenceInputStream(process.getInputStream(), process.getErrorStream())),
					1024 * 1024);
			     BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
				StringBuilder stdout = new StringBuilder();
				processInput.write(request.getStdin());
				processInput.flush();
				processInput.close();
				String line;
				while (process.isAlive() || processOutput.ready()) {
					line = processOutput.readLine();
					if (line != null) stdout.append(line);
					if (stdout.length() > 1024 * 1024) {
						break;
					}
				}
				return new CodeRunnerResponse(request, process.exitValue(), stdout.toString());
			} finally {
				System.out.println("CLOSING");
				process.destroy();
			}
		}

		@Override
		public void close() {
			if (process != null) process.destroyForcibly();
		}
	}
}
