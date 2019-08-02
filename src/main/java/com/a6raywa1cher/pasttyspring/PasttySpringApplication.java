package com.a6raywa1cher.pasttyspring;

import com.a6raywa1cher.pasttyspring.configs.AppConfig;
import com.a6raywa1cher.pasttyspring.configs.AuthConfig;
import com.a6raywa1cher.pasttyspring.configs.ExecScriptsConfig;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

@SpringBootApplication
@EnableConfigurationProperties({AppConfig.class, ExecScriptsConfig.class, AuthConfig.class})
public class PasttySpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(PasttySpringApplication.class, args);
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties() {
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
		yaml.setResources(new ClassPathResource("appconfig.yml"));
		configurer.setProperties(yaml.getObject());
		return configurer;
	}
}
