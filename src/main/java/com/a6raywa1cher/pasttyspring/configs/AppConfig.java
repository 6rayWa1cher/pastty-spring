package com.a6raywa1cher.pasttyspring.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@PropertySource("classpath:appconfig.yml")
@ConfigurationProperties(prefix = "app")
@Validated
public class AppConfig {
	/**
	 * Folder, where all uploaded scripts permanently stored.
	 */
	@NotBlank
	private String scriptsFolder;
}
