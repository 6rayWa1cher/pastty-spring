package com.a6raywa1cher.pasttyspring.configs;

import com.a6raywa1cher.pasttyspring.PasttySpringApplication;
import com.a6raywa1cher.pasttyspring.components.coderunner.CodeRunner;
import com.a6raywa1cher.pasttyspring.components.coderunner.CodeRunnerRequest;
import com.a6raywa1cher.pasttyspring.components.coderunner.CodeRunnerResponse;
import com.a6raywa1cher.pasttyspring.utils.AuthoritiesKeychain;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.*;

@TestConfiguration
@Import(PasttySpringApplication.class)
@Profile("test")
public class MainTestConfig {
	private AuthoritiesKeychain keychain = new AuthoritiesKeychain();

	public MainTestConfig() throws Exception {
	}

	@Bean
	@Primary
	public CodeRunner codeRunner() {
		CodeRunner mock = mock(CodeRunner.class);
		Executor executor = Executors.newSingleThreadExecutor();
		when(mock.execTask(any(CodeRunnerRequest.class))).then(invocation -> CompletableFuture.supplyAsync(
				() -> {
					CodeRunnerRequest request = invocation.getArgument(0);
					return new CodeRunnerResponse(request, 0,
							request.getStdin());
				}, executor));
		return mock;
	}

	@Bean
	public AuthoritiesKeychain authoritiesKeychain() {
		return keychain;
	}
}
