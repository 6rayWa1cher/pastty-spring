package com.a6raywa1cher.pasttyspring.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.nio.file.Path;
import java.util.List;

@Data
@PropertySource("classpath:appconfig.yml")
@ConfigurationProperties(prefix = "app.exec-script")
@Validated
public class ExecScriptsConfig {
	/**
	 * Environments for execution scripts.
	 */
	private List<RunnerEnvironmentConfig> environments;

	public RunnerEnvironmentConfig getEnvironment(String name) {
		return environments.stream().filter(rec -> rec.getName().equals(name)).findAny().orElseThrow();
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
		@NotBlank
		private String compile;

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

		public String prepareCompile(Path sourcePath, Path compiledPath) {
			return compile
					.replace("{1}", sourcePath.toAbsolutePath().toString())
					.replace("{2}", compiledPath.toAbsolutePath().toString())
					.replace("{3}", compiledPath.getParent().toAbsolutePath().toString());
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
