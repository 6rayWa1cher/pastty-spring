package com.a6raywa1cher.pasttyspring.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Configuration
@PropertySource("classpath:appconfig.yml")
@ConfigurationProperties(prefix = "app")
@Validated
public class AppConfig {

	/**
	 * Folder, where all uploaded scripts permanently stored.
	 */
	@NotBlank
	private String scriptsFolder;

	public String getScriptsFolder() {
		return scriptsFolder;
	}

	public void setScriptsFolder(String scriptsFolder) {
		this.scriptsFolder = scriptsFolder;
	}
}
