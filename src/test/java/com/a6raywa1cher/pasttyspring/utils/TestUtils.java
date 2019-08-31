package com.a6raywa1cher.pasttyspring.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.Assert;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestUtils {
	public static final String CODE = "codecodecode";
	public static final String DIALECT = "dialectlang";

	public static BaseMatcher<String> notContainsString(String st) {
		return new BaseMatcher<>() {
			@Override
			public boolean matches(Object item) {
				if (!(item instanceof String)) {
					return false;
				}
				return !((String) item).contains(st);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Not contains " + st);
			}
		};
	}

	public static String registerUser(MockMvc mockMvc, String username) throws Exception {
		return registerUser(mockMvc, username, "password");
	}

	public static String registerUser(MockMvc mockMvc, String username, String password) throws Exception {
		return registerUserBothTokens(mockMvc, username, password).getFirst();
	}

	public static Pair<String, String> registerUserBothTokens(MockMvc mockMvc, String username, String password)
			throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		mockMvc.perform(post("/user/reg")
				.content(objectMapper.createObjectNode()
						.put("username", username)
						.put("password", password)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.username").value(username));

		return login(mockMvc, username, password);
	}

	public static Pair<String, String> login(MockMvc mockMvc, String username, String password) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		MvcResult result1 = mockMvc.perform(post("/auth/login")
				.content(objectMapper.createObjectNode()
						.put("username", username)
						.put("password", password)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.accessToken").exists())
				.andExpect(jsonPath("$.refreshToken").exists())
				.andReturn();
		JsonNode response = objectMapper.readTree(result1.getResponse().getContentAsString());
		String accessToken = response.at("/accessToken").asText();
		String refreshToken = response.at("/refreshToken").asText();
		Assert.notNull(accessToken, "Access token not received");
		Assert.notNull(refreshToken, "Refresh token not received");
		// https://www.regexpal.com/105777
		Assert.isTrue(accessToken.matches("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$"),
				"Invalid access token: " + accessToken);
		Assert.isTrue(refreshToken.matches("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$"),
				"Invalid refresh token: " + accessToken);
		return Pair.of(accessToken, refreshToken);
	}


	public static String getAccessToken(MockMvc mockMvc, String refreshToken) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		String json = mockMvc.perform(post("/auth/get_access")
				.content(objectMapper.createObjectNode()
						.put("refreshToken", refreshToken)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.accessToken").exists())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(json).at("/accessToken").asText();
	}


	private static void invokeRequest(ThrowsFunction<String, ResultActions> req, String token, ThrowsConsumer<ResultActions> consumer) throws Exception {
		if (consumer != null) {
			consumer.accept(req.apply(token));
		}
	}

	public static void assertSecurity(ThrowsFunction<String, ResultActions> req,
	                                  String selfToken,
	                                  AuthoritiesKeychain keychain,
	                                  ThrowsConsumer<ResultActions> asSelf,
	                                  ThrowsConsumer<ResultActions> asModerator,
	                                  ThrowsConsumer<ResultActions> asOther,
	                                  ThrowsConsumer<ResultActions> asAnonymous
	) throws Exception {
		invokeRequest(req, selfToken, asSelf);
		invokeRequest(req, keychain.getModeratorToken(), asModerator);
		invokeRequest(req, keychain.getOtherToken(), asOther);
		invokeRequest(req, "", asAnonymous);
	}

	public static ResultActions uploadScript(MockMvc mockMvc, String token, String uploadName) throws Exception {
		return uploadScript(mockMvc, token, uploadName, CODE, DIALECT);
	}

	public static ResultActions uploadScript(MockMvc mockMvc, String token, String uploadName, String code, String dialect) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		return mockMvc.perform(post("/script/upload")
				.header("jwt", token)
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
	}
}
