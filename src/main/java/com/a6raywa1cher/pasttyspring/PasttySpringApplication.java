package com.a6raywa1cher.pasttyspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableConfigurationProperties({AppConfig.class, ExecScriptsConfig.class, AuthConfig.class})
//@PropertySource("classpath:application.properties")
//@PropertySource(value = "classpath:appconfig.yml", factory = YamlPropertySourceFactory.class)
public class PasttySpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(PasttySpringApplication.class, args);
	}

//	@Bean
//	public static PropertySourcesPlaceholderConfigurer properties() {
//		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
//		YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
//		yaml.setResources(new ClassPathResource("appconfig.yml"));
//		configurer.setProperties(yaml.getObject());
//		return configurer;
//	}
}
