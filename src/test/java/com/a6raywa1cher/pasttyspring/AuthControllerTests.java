package com.a6raywa1cher.pasttyspring;

import com.a6raywa1cher.pasttyspring.configs.MainTestConfig;
import com.a6raywa1cher.pasttyspring.utils.AuthoritiesKeychain;
import com.a6raywa1cher.pasttyspring.utils.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"app.scripts-folder=testsScriptsFolder",
		"app.auth.access-token-duration=PT2S",
		"app.auth.refresh-token-duration=PT4S"
}, classes = MainTestConfig.class)
@AutoConfigureCache
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@ActiveProfiles("test")
public class AuthControllerTests {
	@Autowired
	private WebApplicationContext wac;

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
		keychain.check(mockMvc);
	}

	private void assertGoodAccessToken(String accessToken) throws Exception {
		this.mockMvc.perform(get("/auth/restricted_area")
				.header("jwt", accessToken))
				.andDo(print())
				.andExpect(status().is2xxSuccessful());
	}

	private void assertBadAccessToken(String accessToken) throws Exception {
		this.mockMvc.perform(get("/auth/restricted_area")
				.header("jwt", accessToken))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void expirationCheck() throws Exception {
		String username = "expirationCheck1";
		String password = "expirationCheck1";
		Pair<String, String> pair = TestUtils.registerUserBothTokens(mockMvc, username, password);
		String accessToken1 = pair.getFirst();
		String refreshToken = pair.getSecond();
		assertGoodAccessToken(accessToken1);
		String accessToken2 = TestUtils.getAccessToken(mockMvc, refreshToken);

		Thread.sleep(2500);

		assertBadAccessToken(accessToken1);
		String accessToken3 = TestUtils.getAccessToken(mockMvc, refreshToken);

		Thread.sleep(2500);

		assertBadAccessToken(accessToken1);
		assertBadAccessToken(accessToken2);
		assertBadAccessToken(accessToken3);

		this.mockMvc.perform(post("/auth/get_access")
				.content(objectMapper.createObjectNode()
						.put("refreshToken", refreshToken)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void revokeCheck() throws Exception {
		String username = "revokeCheck1";
		String password = "revokeCheck1";
		Pair<String, String> pair = TestUtils.registerUserBothTokens(mockMvc, username, password);
		String accessToken1 = pair.getFirst();
		String refreshToken = pair.getSecond();
		assertGoodAccessToken(accessToken1);
		String accessToken2 = TestUtils.getAccessToken(mockMvc, refreshToken);

		this.mockMvc.perform(post("/auth/full_logout")
				.header("jwt", accessToken1))
				.andDo(print())
				.andExpect(status().is2xxSuccessful());

		assertBadAccessToken(accessToken1);
		assertBadAccessToken(accessToken2);

		this.mockMvc.perform(post("/auth/get_access")
				.content(objectMapper.createObjectNode()
						.put("refreshToken", refreshToken)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}
}
