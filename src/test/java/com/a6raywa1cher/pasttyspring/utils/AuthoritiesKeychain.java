package com.a6raywa1cher.pasttyspring.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestComponent
public class AuthoritiesKeychain {
	public static final String OTHER_USERNAME = "dog";
	private String adminToken;
	private String moderatorToken;
	private String otherToken;

	public AuthoritiesKeychain() throws Exception {

	}

	public void check(MockMvc mockMvc) throws Exception {
		if (adminToken == null) {
			ObjectMapper objectMapper = new ObjectMapper();
			this.adminToken = TestUtils.registerUser(mockMvc, "admin", "admin");
			mockMvc.perform(get("/user/admin"))
					.andDo(print())
					.andExpect(jsonPath("$.role").value("ADMIN"));
			this.moderatorToken = TestUtils.registerUser(mockMvc, "moder", "moder");
			mockMvc.perform(post("/user/moder/role")
					.header("jwt", this.adminToken)
					.content(objectMapper.createObjectNode()
							.put("role", "MODERATOR")
							.toString())
					.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().is2xxSuccessful())
					.andExpect(jsonPath("$.role").value("MODERATOR"));
			this.otherToken = TestUtils.registerUser(mockMvc, OTHER_USERNAME, "dog");
		}
	}

	private void checkInitialization() {
		if (adminToken == null) {
			throw new RuntimeException("Keychain isn't initialized, use check(MockMvc) first");
		}
	}

	public String getAdminToken() {
		checkInitialization();
		return adminToken;
	}

	public String getModeratorToken() {
		checkInitialization();
		return moderatorToken;
	}

	public String getOtherToken() {
		checkInitialization();
		return otherToken;
	}
}
