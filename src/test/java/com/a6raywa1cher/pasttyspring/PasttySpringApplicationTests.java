package com.a6raywa1cher.pasttyspring;

import com.a6raywa1cher.pasttyspring.components.coderunner.CodeRunner;
import com.a6raywa1cher.pasttyspring.configs.MainTestConfig;
import com.a6raywa1cher.pasttyspring.models.enums.ScriptType;
import com.a6raywa1cher.pasttyspring.rest.dto.response.ExecutionScriptResponse;
import com.a6raywa1cher.pasttyspring.utils.AuthoritiesKeychain;
import com.a6raywa1cher.pasttyspring.utils.TestUtils;
import com.a6raywa1cher.pasttyspring.utils.ThrowsConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.core.StringContains;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;

import static com.a6raywa1cher.pasttyspring.utils.TestUtils.registerUser;
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
}, classes = MainTestConfig.class)
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

	@Autowired
	private AuthoritiesKeychain keychain;

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;


	@Before
	public void setup() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
				.apply(springSecurity())
				.build();
		objectMapper = new ObjectMapper();
//		Executor executor = Executors.newSingleThreadExecutor();
//		Mockito.when(codeRunner.execTask(Mockito.any())).then(
//				(Answer<CompletableFuture<CodeRunnerResponse>>) invocation ->
//						CompletableFuture.supplyAsync(
//								() -> new CodeRunnerResponse(invocation.getArgument(0), 0, "mock")
//								, executor));
		keychain.check(mockMvc);
	}

	@Test
	public void contextLoads() {
	}

	@Test
	public void authTest() throws Exception {
		this.mockMvc.perform(get("/auth/restricted_area")).andDo(print())
				.andExpect(MockMvcResultMatchers.status().isUnauthorized());
		String username = "authtest1";
		String password = "password";
		String token = registerUser(mockMvc, username, password);
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
		String token = registerUser(mockMvc, username);

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
		String uploaderToken = registerUser(mockMvc, uploaderUsername);
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
		TestUtils.assertSecurity(token -> this.mockMvc.perform(get("/script")
						.header("jwt", token))
						.andDo(print()),
				uploaderToken, keychain,
				selfAct -> selfAct.andExpect(content().string(StringContains.containsString(name))),
				null,
				otherAct -> otherAct.andExpect(content().string(notContainsName)),
				anonymousAct -> anonymousAct.andExpect(content().string(notContainsName)));

		TestUtils.assertSecurity(token -> this.mockMvc.perform(get("/script/u/" + uploaderUsername)
						.header("jwt", token))
						.andDo(print()),
				uploaderToken, keychain,
				selfAct -> selfAct.andExpect(content().string(StringContains.containsString(name))),
				null,
				otherAct -> otherAct.andExpect(content().string(notContainsName)),
				anonymousAct -> anonymousAct.andExpect(content().string(notContainsName)));

		TestUtils.assertSecurity(token -> this.mockMvc.perform(get("/script/s/" + name)
						.header("jwt", token))
						.andDo(print()),
				uploaderToken, keychain,
				selfAct -> selfAct.andExpect(jsonPath("$.name").value(name)),
				null,
				otherAct -> otherAct.andExpect(status().isForbidden()),
				anonymousAct -> anonymousAct.andExpect(status().isForbidden()));
	}

	@Test
	public void execScript() throws Exception {
		String uploaderUsername = "execScript1";
		String uploaderToken = registerUser(mockMvc, uploaderUsername);
		String uploadName = "echo";
		String code = "blablabla!";
		String dialect = "dracula";
		this.mockMvc.perform(post("/script/upload")
				.header("jwt", uploaderToken)
				.content(objectMapper.createObjectNode()
						.put("code", code)
						.put("dialect", dialect)
						.put("name", uploadName)
						.put("visible", true)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.name").value(uploadName))
				.andExpect(jsonPath("$.dialect").value(dialect));

		String stdin = "echo, echo, echo";
		ThrowsConsumer<ResultActions> checkStdout = act -> {
			act.andExpect(status().is2xxSuccessful());
			Assert.assertEquals(stdin,
					((ExecutionScriptResponse) ((ResponseEntity) act.andReturn().getAsyncResult()).getBody()).getStdout());
		};
		TestUtils.assertSecurity(token ->
						this.mockMvc.perform(post("/script/s/" + uploadName + "/exec")
								.header("jwt", token)
								.content(objectMapper.createObjectNode()
										.put("stdin", stdin)
										.toString())
								.contentType(MediaType.APPLICATION_JSON_UTF8))
								.andDo(print()),
//										.andExpect(request().asyncStarted()
				uploaderToken, keychain,
				selfAct -> selfAct.andExpect(status().isBadRequest()),
				checkStdout,
				otherAct -> otherAct.andExpect(status().isBadRequest()),
				anonymousAct -> anonymousAct.andExpect(status().isUnauthorized())
		);

		this.mockMvc.perform(post("/script/s/" + uploadName + "/change_type")
				.header("jwt", keychain.getOtherToken())
				.content(objectMapper.createObjectNode()
						.put("type", ScriptType.REQUESTED_MODERATION.name())
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().isForbidden());

		this.mockMvc.perform(post("/script/s/" + uploadName + "/change_type")
				.header("jwt", uploaderToken)
				.content(objectMapper.createObjectNode()
						.put("type", ScriptType.REQUESTED_MODERATION.name())
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.type").value(ScriptType.REQUESTED_MODERATION.name()));

		this.mockMvc.perform(post("/script/s/" + uploadName + "/change_type")
				.header("jwt", uploaderToken)
				.content(objectMapper.createObjectNode()
						.put("type", ScriptType.EXEC_SCRIPT.name())
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().isForbidden());

		this.mockMvc.perform(post("/script/s/" + uploadName + "/change_type")
				.header("jwt", keychain.getModeratorToken())
				.content(objectMapper.createObjectNode()
						.put("type", ScriptType.EXEC_SCRIPT.name())
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.type").value(ScriptType.EXEC_SCRIPT.name()));

		TestUtils.assertSecurity(token -> this.mockMvc.perform(post("/script/s/" + uploadName + "/exec")
						.header("jwt", token)
						.content(objectMapper.createObjectNode()
								.put("stdin", stdin)
								.toString())
						.contentType(MediaType.APPLICATION_JSON_UTF8))
						.andDo(print()),
				uploaderToken, keychain,
				checkStdout,
				checkStdout,
				checkStdout,
				anonymousAct -> anonymousAct.andExpect(status().isUnauthorized())
		);
	}

	@After
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(new File("testsScriptsFolder"));
	}
}
