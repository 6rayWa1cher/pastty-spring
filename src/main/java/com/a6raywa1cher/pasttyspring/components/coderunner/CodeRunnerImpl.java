package com.a6raywa1cher.pasttyspring.components.coderunner;

import com.a6raywa1cher.pasttyspring.components.coderunner.commandcompiler.CommandCompiler;
import com.a6raywa1cher.pasttyspring.configs.AppConfig;
import com.a6raywa1cher.pasttyspring.configs.ExecScriptsConfig;
import com.a6raywa1cher.pasttyspring.models.Script;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class CodeRunnerImpl implements CodeRunner {
	private static final Logger log = LoggerFactory.getLogger(CodeRunnerImpl.class);
	private final AppConfig appConfig;
	private final ExecScriptsConfig config;
	private final CommandCompiler commandCompiler;
	private final ExecutorTaskFactory taskFactory;
	private Set<ExecutorTask> tasks;
	private ExecutorService executorService;

	@Autowired
	public CodeRunnerImpl(AppConfig appConfig, ExecScriptsConfig config, CommandCompiler commandCompiler,
	                      ExecutorTaskFactory taskFactory) {
		this.appConfig = appConfig;
		this.config = config;
		this.commandCompiler = commandCompiler;
		this.taskFactory = taskFactory;
		this.executorService = new ForkJoinPool();
		this.tasks = new HashSet<>();
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
				commandCompiler.prepareCompiledFilename(runnerEnvironmentConfig, uniqueName));
		log.info("script:{} folderCompiled:{} sources:{} compiled:{}",
				script.getId(), folderWithCompiled, sources, compiled);
		CompletableFuture<Void> chain = CompletableFuture.supplyAsync(() -> null, executorService);
		boolean isCompiled = folderWithCompiled.toFile().exists();
		if (!isCompiled) {
			for (String[] preparedCompileCommand : commandCompiler.prepareCompile(runnerEnvironmentConfig, sources, compiled)) {
				chain = chain.thenCompose(v -> {
					try {
						Files.createDirectories(folderWithCompiled);
						log.warn(String.join(" ", preparedCompileCommand));
						if (preparedCompileCommand.length == 1) {
							return Runtime.getRuntime().exec(preparedCompileCommand[0], new String[]{},
									folderWithCompiled.toFile()).onExit();
						} else {
							return Runtime.getRuntime().exec(preparedCompileCommand, new String[]{},
									folderWithCompiled.toFile()).onExit();
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}).thenAccept(process -> {
					if (process != null && process.exitValue() != 0) {
						throw new RuntimeException("Compilation not successful. command array:" + String.join(" ", preparedCompileCommand) + "\n" +
								new BufferedReader(new InputStreamReader(process.getErrorStream())).lines()
										.collect(Collectors.joining("\n"))
						);
					}
				});
			}
		}
		return chain
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
					try (ExecutorTask task = taskFactory.createTask(this, request, config, folderWithCompiled, compiled)) {
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

	public void registerTask(ExecutorTask task) throws IllegalStateException {
		if (tasks == null) {
			throw new IllegalStateException("Destroy process");
		}
		tasks.add(task);
	}

	public void removeTask(ExecutorTask task) {
		if (tasks == null) {
			return;
		}
		tasks.remove(task);
	}

	@PreDestroy
	public void onDestroy() throws Exception {
		log.info("Going destroy {} processes", tasks.size());
		Set<ExecutorTask> clone = new HashSet<>(tasks);
		tasks = null;
		for (ExecutorTask p : clone) {
			p.close();
		}
		log.info("Destroy completed");
	}

}
