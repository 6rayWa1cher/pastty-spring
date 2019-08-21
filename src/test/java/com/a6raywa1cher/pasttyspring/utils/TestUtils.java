package com.a6raywa1cher.pasttyspring.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.Assert;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class TestUtils {
	public static String registerUser(MockMvc mockMvc, String username) throws Exception {
		return registerUser(mockMvc, username, "password");
	}

	public static String registerUser(MockMvc mockMvc, String username, String password) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		mockMvc.perform(post("/user/reg")
				.content(objectMapper.createObjectNode()
						.put("username", username)
						.put("password", password)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.username").value(username));
		MvcResult result1 = mockMvc.perform(post("/auth/login")
				.content(objectMapper.createObjectNode()
						.put("username", username)
						.put("password", password)
						.toString())
				.contentType(MediaType.APPLICATION_JSON_UTF8))
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
