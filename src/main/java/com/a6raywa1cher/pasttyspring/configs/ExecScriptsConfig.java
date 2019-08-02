package com.a6raywa1cher.pasttyspring.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
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

	@Validated
	public static class RunnerEnvironmentConfig {
		/**
		 * Name of environment, unique.
		 */
		@NotBlank
		private String name;
		/**
		 * Shell command to execute script with '{1}' as placeholder for filename.
		 */
		@NotBlank
		private String exec;
		/**
		 * Filename pattern with extension, where '{1}' - unique name of script
		 */
		@NotBlank
		private String filename;

		@Override
		public String toString() {
			return "RunnerEnvironmentConfig{" +
					"name='" + name + '\'' +
					", exec='" + exec + '\'' +
					", filename='" + filename + '\'' +
					'}';
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getExec() {
			return exec;
		}

		public void setExec(String exec) {
			this.exec = exec;
		}

		public String getFilename() {
			return filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}
	}
}
