package com.a6raywa1cher.pasttyspring.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Component
//@PropertySource("classpath:appconfig.yml")
@ConfigurationProperties(prefix = "app.exec-script")
@Validated
public class ExecScriptsConfig {
	/**
	 * Environments for execution scripts.
	 */
	private List<RunnerEnvironmentConfig> environments;

	/**
	 * Size of script's output buffer (BufferedReader).
	 */
	private int bufferSize;

	/**
	 * Maximum length of stdout.
	 */
	private int maxOutputSize;

	public RunnerEnvironmentConfig getEnvironment(String name) {
		return environments.stream().filter(rec -> rec.getName().equals(name)).findAny().orElseThrow();
	}

	public ExecScriptsConfig() {
		int a = 2;
	}

	@Validated
	@Data
	public static class RunnerEnvironmentConfig {
		/**
		 * Name of environment, unique.
		 */
		@NotBlank
		private String name;

		/**
		 * Shell command to compile script in separate directory.
		 * Have placeholder '{1}' for source absolute path, '{2}' for compiled absolute path
		 * and '{3}' for compiled directory.
		 * If not compilable, put 'cp {1} {2}' or 'copy {1} {2}'
		 */
		private List<String> compile;

		/**
		 * Shell command to execute script with '{1}' as placeholder for compiled absolute path.
		 */
		@NotBlank
		private String exec;
		/**
		 * Sources filename pattern with extension, where '{1}' - unique name of script
		 */
		@NotBlank
		private String sourceFilename;
		/**
		 * Compiled filename pattern with extension, where '{1}' - unique name of script
		 */
		private String compiledFilename;

		public List<String[]> prepareCompile(Path sourcePath, Path compiledPath) {
			List<String> localCompile = new ArrayList<>();
			boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
			if (compile == null) {
				if (isWindows) {
//					localCompile = "cmd.exe /c copy \"{1}\" \"{2}\"";
					localCompile.add("copy \"{1}\" \"{2}\"");
				} else {
//					localCompile = "sh -c \"cp \"{1}\" \"{2}\"\"";
					localCompile.add("cp {1} {2}");
				}
			} else {
				localCompile.addAll(compile);
			}
			return localCompile.stream().map(str -> str
					.replace("{1}", sourcePath.toAbsolutePath().toString())
					.replace("{2}", compiledPath.toAbsolutePath().toString())
					.replace("{3}", compiledPath.getParent().toAbsolutePath().toString()))
					.map(str -> str.startsWith("{!}") ? new String[]{str.substring(3)} :
							(isWindows ? new String[]{"cmd.exe", "/C", str} : new String[]{"sh", "-c", str}))
					.collect(Collectors.toList());
		}

		public String prepareExec(Path compiledPath) {
			return exec
					.replace("{1}", compiledPath.toAbsolutePath().toString());
		}

		public String prepareSourceFilename(String file) {
			return sourceFilename
					.replace("{1}", file);
		}

		public String prepareCompiledFilename(String file) {
			return (compiledFilename != null ? compiledFilename : sourceFilename)
					.replace("{1}", file);
		}
	}
}
