package com.a6raywa1cher.pasttyspring;

import com.a6raywa1cher.pasttyspring.components.coderunner.CodeRunner;
import com.a6raywa1cher.pasttyspring.components.coderunner.CodeRunnerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.core.StringContains;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"app.scripts-folder=testsScriptsFolder",
		"spring.main.allow-bean-definition-overriding=true"
})
@AutoConfigureCache
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@ActiveProfiles("test")
public class PasttySpringApplicationTests {

	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private CodeRunner codeRunner;

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;

	@Before
	public void setup() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
				.apply(springSecurity())
				.build();
		objectMapper = new ObjectMapper();
		Executor executor = Executors.newSingleThreadExecutor();
		Mockito.when(codeRunner.execTask(Mockito.any())).then(
				(Answer<CompletableFuture<CodeRunnerResponse>>) invocation ->
						CompletableFuture.supplyAsync(
								() -> new CodeRunnerResponse(invocation.getArgument(0), 0, "mock")
								, executor));
	}

	@Test
	public void contextLoads() {
	}

	private String registerUser(String username, String password) throws Exception {
		this.mockMvc.perform(post("/user/reg")
				.content(objectMapper.createObjectNode()
						.put("username", username)
						.put("password", password)
						.toString())
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(jsonPath("$.username").value(username));
		MvcResult result1 = this.mockMvc.perform(post("/auth/login")
				.content(objectMapper.createObjectNode()
						.put("username", username)
						.put("password", password)
						.toString())
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(jsonPath("$.token").exists())
				.andReturn();
		String token = objectMapper.readTree(result1.getResponse().getContentAsString())
				.at("/token").asText();
		Assert.notNull(token, "Token not received");
		// https://www.regexpal.com/105777
		Assert.isTrue(token.matches("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$"),
				"Invalid token");
		return token;
	}

	@Test
	public void authTest() throws Exception {
		this.mockMvc.perform(get("/auth/restricted_area")).andDo(print())
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());
		String username = "authtest1";
		String password = "password";
		String token = registerUser(username, password);
		this.mockMvc.perform(get("/auth/restricted_area").header("jwt", token)).andDo(print())
				.andExpect(MockMvcResultMatchers.content().string("Good!"));
		this.mockMvc.perform(post("/auth/login")
				.content(objectMapper.createObjectNode()
						.put("username", username)
						.put("password", password + "_")
						.toString())
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isForbidden());
		this.mockMvc.perform(post("/user/reg")
				.content(objectMapper.createObjectNode()
						.put("username", username)
						.put("password", password)
						.toString())
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	public void simpleScriptUpload() throws Exception {
		MvcResult result1 = this.mockMvc.perform(post("/script/upload")
				.content(objectMapper.createObjectNode()
						.put("dialect", "python")
						.put("code", "print(\"Hello world!\")")
						.toString())
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().is2xxSuccessful())
				.andReturn();
		String name = objectMapper.readTree(result1.getResponse().getContentAsString()).at("/name").asText();
		this.mockMvc.perform(get("/script/s/" + name))
				.andDo(print())
				.andExpect(jsonPath("$.code").value("print(\"Hello world!\")"));
	}

	@Test
	public void fullScriptUpload() throws Exception {
		String title = "Hello world on Python";
		String description = "Just hello world, nothing more.";
		String name = "helloworld_py";
		String code = "print(\"Hello world!\")";
		String dialect = "python";

		String username = "fullScriptUpload1";
		String token = registerUser(username, "password");

		this.mockMvc.perform(post("/script/upload")
				.header("jwt", token)
				.content(objectMapper.createObjectNode()
						.put("name", name)
						.put("title", title)
						.put("description", description)
						.put("visible", true)
						.put("dialect", dialect)
						.put("code", code)
						.toString())
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().is2xxSuccessful());
		this.mockMvc.perform(get("/script/s/" + name))
				.andDo(print())
				.andExpect(jsonPath("$.title").value(title))
				.andExpect(jsonPath("$.description").value(description))
				.andExpect(jsonPath("$.name").value(name))
				.andExpect(jsonPath("$.code").value(code))
				.andExpect(jsonPath("$.dialect").value(dialect))
				.andExpect(jsonPath("$.author.username").value(username));
	}

	@Test
	public void invisibleScripts() throws Exception {
		String uploaderUsername = "invisibleScripts1";
		String randomGuyUsername = "invisibleScripts2";
		String uploaderToken = registerUser(uploaderUsername, "password");
		String randomGuyToken = registerUser(randomGuyUsername, "password");
		String name = "simplemath";
		BaseMatcher<String> notContainsName = new BaseMatcher<>() {
			@Override
			public boolean matches(Object item) {
				if (!(item instanceof String)) {
					return false;
				}
				return !((String) item).contains(name);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Contains " + name);
			}
		};
		this.mockMvc.perform(post("/script/upload")
				.header("jwt", uploaderToken)
				.content(objectMapper.createObjectNode()
						.put("name", name)
						.put("code", "print(2 + 2)")
						.put("visible", false)
						.put("dialect", "python")
						.toString())
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().is2xxSuccessful());
		this.mockMvc.perform(get("/script"))
				.andDo(print())
				.andExpect(content().string(notContainsName));
		this.mockMvc.perform(get("/script")
				.header("jwt", randomGuyToken))
				.andDo(print())
				.andExpect(content().string(notContainsName));
		this.mockMvc.perform(get("/script")
				.header("jwt", uploaderToken))
				.andDo(print())
				.andExpect(content().string(StringContains.containsString(name)));

		this.mockMvc.perform(get("/script/u/" + uploaderUsername))
				.andDo(print())
				.andExpect(content().string(notContainsName));
		this.mockMvc.perform(get("/script/u/" + uploaderUsername)
				.header("jwt", randomGuyToken))
				.andDo(print())
				.andExpect(content().string(notContainsName));
		this.mockMvc.perform(get("/script/u/" + uploaderUsername)
				.header("jwt", uploaderToken))
				.andDo(print())
				.andExpect(content().string(StringContains.containsString(name)));

		this.mockMvc.perform(get("/script/s/" + name))
				.andDo(print())
				.andExpect(status().isForbidden());
		this.mockMvc.perform(get("/script/s/" + name)
				.header("jwt", randomGuyToken))
				.andDo(print())
				.andExpect(status().isForbidden());
		this.mockMvc.perform(get("/script/s/" + name)
				.header("jwt", uploaderToken))
				.andDo(print())
				.andExpect(jsonPath("$.name").value(name));
	}


	@After
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(new File("testsScriptsFolder"));
	}
}
