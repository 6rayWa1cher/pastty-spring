package com.a6raywa1cher.pasttyspring.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
}
