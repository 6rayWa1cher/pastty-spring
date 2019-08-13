package com.a6raywa1cher.pasttyspring;

import com.a6raywa1cher.pasttyspring.components.coderunner.CodeRunner;
import org.mockito.Mockito;
import org.springframework.context.annotation.*;

@Configuration
@Import(PasttySpringApplication.class)
@Profile("test")
public class MainTestConfig {
	@Bean
	@Primary
	public CodeRunner codeRunner() {
		return Mockito.mock(CodeRunner.class);
	}
}
