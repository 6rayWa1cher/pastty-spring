package com.a6raywa1cher.pasttyspring;

import com.a6raywa1cher.pasttyspring.configs.AppConfig;
import com.a6raywa1cher.pasttyspring.configs.MainTestConfig;
import com.a6raywa1cher.pasttyspring.dao.interfaces.ScriptService;
import com.a6raywa1cher.pasttyspring.dao.repository.CommentRepository;
import com.a6raywa1cher.pasttyspring.models.Comment;
import com.a6raywa1cher.pasttyspring.models.Script;
import com.a6raywa1cher.pasttyspring.models.enums.ScriptType;
import com.a6raywa1cher.pasttyspring.rest.dto.response.ExecutionScriptResponse;
import com.a6raywa1cher.pasttyspring.utils.AuthoritiesKeychain;
import com.a6raywa1cher.pasttyspring.utils.TestUtils;
import com.a6raywa1cher.pasttyspring.utils.ThrowsConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.hamcrest.BaseMatcher;
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
import java.nio.file.Path;
import java.util.Optional;

import static com.a6raywa1cher.pasttyspring.utils.TestUtils.registerUser;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
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
	private AuthoritiesKeychain keychain;

	@Autowired
	private ScriptService scriptService;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private AppConfig appConfig;

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;


	@Before
	public void setup() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
				.apply(springSecurity())
				.build();
		objectMapper = new ObjectMapper();
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
		BaseMatcher<String> notContainsName = TestUtils.notContainsString(name);
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
			// exec() returns CompletionStage of ExecutionScriptResponse (not JSON!), which requires special treatment
			// in case of MockMvc
			Assert.assertEquals(stdin,
					((ExecutionScriptResponse) ((ResponseEntity) act.andReturn().getAsyncResult()).getBody())
							.getStdout());
		};
		// Only moderator and admin can exec unauthorized script
		TestUtils.assertSecurity(token ->
						this.mockMvc.perform(post("/script/s/" + uploadName + "/exec")
								.header("jwt", token)
								.content(objectMapper.createObjectNode()
										.put("stdin", stdin)
										.toString())
								.contentType(MediaType.APPLICATION_JSON_UTF8))
								.andDo(print()),
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
				.andExpect(status().isForbidden()); // random people can't change type

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
				.andExpect(status().isForbidden()); // uploader can change type only on few directions (see changeType())

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
				anonymousAct -> anonymousAct.andExpect(status().isUnauthorized()) // anonymous can't exec scripts anyway
		);
	}

	@Test
	public void uploadAndPatchAnonymousScript() throws Exception {
		String uploadName = "whatis42";
		String code = "Answer to the Ultimate Question of Life, the Universe, and Everything";
		String dialect = "deepthoughtlang";
		this.mockMvc.perform(post("/script/upload")
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
		this.mockMvc.perform(patch("/script/s/" + uploadName)
				.content(objectMapper.createObjectNode()
						.put("dialect", "random")
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().is4xxClientError()); // after uploading, anonymous can't change anything
	}

	@Test
	public void patchScript() throws Exception {
		String uploaderUsername = "patchScript1";
		String uploaderToken = registerUser(mockMvc, uploaderUsername);
		String uploadName = "nyancat";
		String code = "Nya nya nya nya nya nya nya!";
		String dialect = "nyancatlang";
		String title = "Title.";
		this.mockMvc.perform(post("/script/upload")
				.header("jwt", uploaderToken)
				.content(objectMapper.createObjectNode()
						.put("code", code)
						.put("dialect", dialect)
						.put("name", uploadName)
						.put("title", title)
						.put("visible", true)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.name").value(uploadName))
				.andExpect(jsonPath("$.dialect").value(dialect));
		String newCode = "meow";
		String newDialect = "cat";
		String newName = "name";
		String newDescription = "meow meow meow!";
		TestUtils.assertSecurity(token -> this.mockMvc.perform(patch("/script/s/" + uploadName)
						.header("jwt", token)
						.content(objectMapper.createObjectNode()
								.put("code", newCode)
								.put("dialect", newDialect)
								.put("name", newName)
								.put("description", newDescription)
								.toString())
						.contentType(MediaType.APPLICATION_JSON_UTF8))
						.andDo(print()),
				uploaderToken, keychain,
				selfAct -> selfAct.andExpect(status().is2xxSuccessful())
						.andExpect(jsonPath("$.code").value(newCode))
						.andExpect(jsonPath("$.dialect").value(newDialect))
						.andExpect(jsonPath("$.name").value(newName))
						.andExpect(jsonPath("$.description").value(newDescription))
						.andExpect(jsonPath("$.title").value(title)),
				moderatorAct -> moderatorAct.andExpect(status().is4xxClientError()), // only uploader can change script
				otherAct -> otherAct.andExpect(status().is4xxClientError()),
				anonymousAct -> anonymousAct.andExpect(status().is4xxClientError()));
		this.mockMvc.perform(patch("/script/s/" + newName)
				.header("jwt", uploaderToken)
				.content(objectMapper.createObjectNode()
						.put("name", "@@#!@$!@@$")
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().isBadRequest()); // just validation check
		this.mockMvc.perform(get("/script/s/" + newName))
				.andDo(print())
				.andExpect(jsonPath("$.code").value(newCode))
				.andExpect(jsonPath("$.dialect").value(newDialect))
				.andExpect(jsonPath("$.name").value(newName))
				.andExpect(jsonPath("$.description").value(newDescription))
				.andExpect(jsonPath("$.title").value(title))
				.andExpect(jsonPath("$.visible").value(true));
	}

	@Test
	public void putScript() throws Exception {
		String uploaderUsername = "putScript1";
		String uploaderToken = registerUser(mockMvc, uploaderUsername);
		String uploadName = "ogwin";
		String code = "The International winners x2";
		String dialect = "esportslang";
		String title = "Title.";
		this.mockMvc.perform(post("/script/upload")
				.header("jwt", uploaderToken)
				.content(objectMapper.createObjectNode()
						.put("code", code)
						.put("dialect", dialect)
						.put("name", uploadName)
						.put("title", title)
						.put("visible", true)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.name").value(uploadName))
				.andExpect(jsonPath("$.dialect").value(dialect));
		String newCode = "2nd place in Six Invitational 2019";
		String newDialect = "esportslang";
		String newName = "te2nd";
		String newDescription = "2nd is good too!";
		this.mockMvc.perform(put("/script/s/" + uploadName)
				.header("jwt", uploaderToken)
				.content(objectMapper.createObjectNode()
						.put("dialect", newDialect)
						.put("name", newName)
						.put("description", newDescription)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().isBadRequest()); // "code" and "visible" isn't presented (put operation)
		TestUtils.assertSecurity(token -> this.mockMvc.perform(put("/script/s/" + uploadName)
						.header("jwt", token)
						.content(objectMapper.createObjectNode()
								.put("code", newCode)
								.put("dialect", newDialect)
								.put("name", newName)
								.put("description", newDescription)
								.put("visible", true)
								.toString())
						.contentType(MediaType.APPLICATION_JSON_UTF8))
						.andDo(print()),
				uploaderToken, keychain,
				selfAct -> selfAct.andExpect(status().is2xxSuccessful())
						.andExpect(jsonPath("$.code").value(newCode))
						.andExpect(jsonPath("$.dialect").value(newDialect))
						.andExpect(jsonPath("$.name").value(newName))
						.andExpect(jsonPath("$.description").value(newDescription))
						.andExpect(jsonPath("$.title").isEmpty()),
				moderatorAct -> moderatorAct.andExpect(status().is4xxClientError()),
				otherAct -> otherAct.andExpect(status().is4xxClientError()),
				anonymousAct -> anonymousAct.andExpect(status().is4xxClientError()));
		this.mockMvc.perform(get("/script/s/" + newName))
				.andDo(print())
				.andExpect(jsonPath("$.code").value(newCode))
				.andExpect(jsonPath("$.dialect").value(newDialect))
				.andExpect(jsonPath("$.name").value(newName))
				.andExpect(jsonPath("$.description").value(newDescription))
				.andExpect(jsonPath("$.title").isEmpty())
				.andExpect(jsonPath("$.visible").value(true));
	}

	@Test
	public void patchPostToExistingName() throws Exception {
		String uploaderUsername = "patchPostToEN1";
		String uploaderToken = registerUser(mockMvc, uploaderUsername);
		String uploadName1 = "ppten_1";
		TestUtils.uploadScript(mockMvc, uploaderToken, uploadName1);

		String uploadName2 = "ppten_2";
		TestUtils.uploadScript(mockMvc, uploaderToken, uploadName2);

		this.mockMvc.perform(patch("/script/s/" + uploadName2)
				.header("jwt", uploaderToken)
				.content(objectMapper.createObjectNode()
						.put("name", uploadName1)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(result -> Assert.assertEquals("Name is already taken",
						result.getResponse().getErrorMessage()));

		this.mockMvc.perform(put("/script/s/" + uploadName2)
				.header("jwt", uploaderToken)
				.content(objectMapper.createObjectNode()
						.put("code", TestUtils.CODE)
						.put("dialect", TestUtils.DIALECT)
						.put("name", uploadName1)
						.put("visible", true)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(result -> Assert.assertEquals("Name is already taken",
						result.getResponse().getErrorMessage()));
	}

	@Test
	public void deleteScript() throws Exception {
		String uploaderUsername = "deleteScript1";
		String uploaderToken = registerUser(mockMvc, uploaderUsername);
		String uploadName = "some_script";
		String code = "some_code";
		String dialect = "some_lang";
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
		Script script = scriptService.findByName(uploadName).orElseThrow();
		Path path = Path.of(appConfig.getScriptsFolder(), script.getPathToFile());
		Assert.assertTrue(path.toFile().exists());
		this.mockMvc.perform(delete("/script/s/" + uploadName))
				.andDo(print())
				.andExpect(status().isUnauthorized());
		this.mockMvc.perform(delete("/script/s/" + uploadName)
				.header("jwt", keychain.getOtherToken()))
				.andDo(print())
				.andExpect(status().isForbidden());
		this.mockMvc.perform(delete("/script/s/" + uploadName)
				.header("jwt", uploaderToken))
				.andDo(print())
				.andExpect(status().is2xxSuccessful());
		Assert.assertFalse(path.toFile().exists());
	}

	@Test
	public void commentTreeTest() throws Exception {
		String uploaderUsername = "commentTreeTest1";
		String uploaderToken = registerUser(mockMvc, uploaderUsername);
		String uploadName = "ctt_1";
		TestUtils.uploadScript(mockMvc, uploaderToken, uploadName);

		String comment1Body = "comment1";
		MvcResult mvcResult1 = this.mockMvc.perform(post("/comment/" + uploadName)
				.header("jwt", keychain.getOtherToken())
				.content(objectMapper.createObjectNode()
						.put("body", comment1Body)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.body").value(comment1Body))
				.andReturn();
		long id1 = objectMapper.readTree(mvcResult1.getResponse().getContentAsString()).get("id").asLong();

		String comment2Body = "comment2";
		MvcResult mvcResult2 = this.mockMvc.perform(post("/comment/" + uploadName)
				.header("jwt", keychain.getOtherToken())
				.content(objectMapper.createObjectNode()
						.put("body", comment2Body)
						.put("parentId", id1)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.body").value(comment2Body))
				.andExpect(jsonPath("$.upperLevel").exists())
				.andExpect(jsonPath("$.upperLevel.id").value(id1))
				.andReturn();
		long id2 = objectMapper.readTree(mvcResult2.getResponse().getContentAsString()).get("id").asLong();

		String comment3Body = "comment3";
		this.mockMvc.perform(post("/comment/" + uploadName)
				.header("jwt", keychain.getOtherToken())
				.content(objectMapper.createObjectNode()
						.put("body", comment3Body)
						.put("parentId", id2)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.body").value(comment3Body))
				.andExpect(jsonPath("$.upperLevel").exists())
				.andExpect(jsonPath("$.upperLevel.id").value(id2));

		String comment4Body = "comment4";
		this.mockMvc.perform(post("/comment/" + uploadName)
				.header("jwt", keychain.getOtherToken())
				.content(objectMapper.createObjectNode()
						.put("body", comment4Body)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.body").value(comment4Body));

		this.mockMvc.perform(get("/comment/" + uploadName))
				.andDo(print())
				.andExpect(content().string(StringContains.containsString(comment1Body)))
				.andExpect(content().string(StringContains.containsString(comment2Body)))
				.andExpect(content().string(StringContains.containsString(comment3Body)))
				.andExpect(content().string(StringContains.containsString(comment4Body)));

		this.mockMvc.perform(get("/comment/" + uploadName + "/" + id1 + "?lower=true"))
				.andDo(print())
				.andExpect(content().string(StringContains.containsString(comment1Body)))
				.andExpect(content().string(StringContains.containsString(comment2Body)))
				.andExpect(content().string(TestUtils.notContainsString(comment3Body)))
				.andExpect(content().string(TestUtils.notContainsString(comment4Body)));

		this.mockMvc.perform(get("/comment/" + uploadName + "/" + id1 + "?all_lower=true"))
				.andDo(print())
				.andExpect(content().string(StringContains.containsString(comment1Body)))
				.andExpect(content().string(StringContains.containsString(comment2Body)))
				.andExpect(content().string(StringContains.containsString(comment3Body)))
				.andExpect(content().string(TestUtils.notContainsString(comment4Body)));
	}

	@Test
	public void deleteScriptWithComments() throws Exception {
		String uploaderUsername = "deleteScriptWC1";
		String uploaderToken = registerUser(mockMvc, uploaderUsername);
		String uploadName = "dswc_1";
		TestUtils.uploadScript(mockMvc, uploaderToken, uploadName);

		String comment1Body = "comment1";
		MvcResult mvcResult1 = this.mockMvc.perform(post("/comment/" + uploadName)
				.header("jwt", keychain.getOtherToken())
				.content(objectMapper.createObjectNode()
						.put("body", comment1Body)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.body").value(comment1Body))
				.andReturn();
		long id1 = objectMapper.readTree(mvcResult1.getResponse().getContentAsString()).get("id").asLong();

		String comment2Body = "comment2";
		MvcResult mvcResult2 = this.mockMvc.perform(post("/comment/" + uploadName)
				.header("jwt", keychain.getOtherToken())
				.content(objectMapper.createObjectNode()
						.put("body", comment2Body)
						.put("parentId", id1)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.body").value(comment2Body))
				.andExpect(jsonPath("$.upperLevel").exists())
				.andExpect(jsonPath("$.upperLevel.id").value(id1))
				.andReturn();
		long id2 = objectMapper.readTree(mvcResult2.getResponse().getContentAsString()).get("id").asLong();

		this.mockMvc.perform(delete("/script/s/" + uploadName)
				.header("jwt", uploaderToken))
				.andDo(print())
				.andExpect(status().is2xxSuccessful());

		Optional<Comment> c1 = commentRepository.findById(id1);
		Optional<Comment> c2 = commentRepository.findById(id2);
		Assert.assertTrue(c1.isEmpty());
		Assert.assertTrue(c2.isEmpty());
	}

	@Test
	public void deleteCommentTree() throws Exception {
		String uploaderUsername = "deleteCommentT1";
		String uploaderToken = registerUser(mockMvc, uploaderUsername);
		String uploadName = "dct_1";
		TestUtils.uploadScript(mockMvc, uploaderToken, uploadName);

		String comment1Body = "comment1";
		MvcResult mvcResult1 = this.mockMvc.perform(post("/comment/" + uploadName)
				.header("jwt", keychain.getOtherToken())
				.content(objectMapper.createObjectNode()
						.put("body", comment1Body)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.body").value(comment1Body))
				.andReturn();
		long id1 = objectMapper.readTree(mvcResult1.getResponse().getContentAsString()).get("id").asLong();

		String comment2Body = "comment2";
		MvcResult mvcResult2 = this.mockMvc.perform(post("/comment/" + uploadName)
				.header("jwt", keychain.getOtherToken())
				.content(objectMapper.createObjectNode()
						.put("body", comment2Body)
						.put("parentId", id1)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.body").value(comment2Body))
				.andExpect(jsonPath("$.upperLevel").exists())
				.andExpect(jsonPath("$.upperLevel.id").value(id1))
				.andReturn();
		long id2 = objectMapper.readTree(mvcResult2.getResponse().getContentAsString()).get("id").asLong();

		String comment3Body = "comment3";
		MvcResult mvcResult3 = this.mockMvc.perform(post("/comment/" + uploadName)
				.header("jwt", keychain.getOtherToken())
				.content(objectMapper.createObjectNode()
						.put("body", comment3Body)
						.put("parentId", id2)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.body").value(comment3Body))
				.andExpect(jsonPath("$.upperLevel").exists())
				.andExpect(jsonPath("$.upperLevel.id").value(id2))
				.andReturn();
		long id3 = objectMapper.readTree(mvcResult3.getResponse().getContentAsString()).get("id").asLong();

		this.mockMvc.perform(delete("/comment/" + uploadName + "/" + id1)
				.header("jwt", keychain.getModeratorToken()))
				.andDo(print())
				.andExpect(status().is2xxSuccessful());

		Optional<Comment> c1 = commentRepository.findById(id1);
		Optional<Comment> c2 = commentRepository.findById(id2);
		Optional<Comment> c3 = commentRepository.findById(id3);
		Assert.assertTrue(c1.isEmpty());
		Assert.assertTrue(c2.isEmpty());
		Assert.assertTrue(c3.isEmpty());

		this.mockMvc.perform(get("/script/s/" + uploadName))
				.andDo(print())
				.andExpect(status().is2xxSuccessful());
	}

	@After
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(new File("testsScriptsFolder"));
	}
}
