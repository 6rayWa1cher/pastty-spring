package com.a6raywa1cher.pasttyspring.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.util.Pair;
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
	private String adminRefreshToken;
	private String moderatorToken;
	private String moderatorRefreshToken;
	private String otherToken;
	private String otherRefreshToken;

	public AuthoritiesKeychain() throws Exception {

	}

	public void check(MockMvc mockMvc) throws Exception {
		if (adminToken == null) {
			ObjectMapper objectMapper = new ObjectMapper();
			Pair<String, String> admin = TestUtils.registerUserBothTokens(mockMvc, "admin", "admin");
			this.adminToken = admin.getFirst();
			this.adminRefreshToken = admin.getSecond();
			mockMvc.perform(get("/user/admin"))
					.andDo(print())
					.andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
			TestUtils.registerUser(mockMvc, "moder", "moder");
			mockMvc.perform(post("/user/moder/role")
					.header("jwt", this.adminToken)
					.content(objectMapper.createObjectNode()
							.put("role", "ROLE_MODERATOR")
							.toString())
					.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().is2xxSuccessful())
					.andExpect(jsonPath("$.role").value("ROLE_MODERATOR"));
			Pair<String, String> moderator = TestUtils.login(mockMvc, "moder", "moder");
			this.moderatorToken = moderator.getFirst();
			this.moderatorRefreshToken = moderator.getSecond();
			Pair<String, String> other = TestUtils.registerUserBothTokens(mockMvc, OTHER_USERNAME, "dog");
			this.otherToken = other.getFirst();
			this.otherRefreshToken = other.getSecond();
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

	public String getAdminRefreshToken() {
		checkInitialization();
		return adminRefreshToken;
	}

	public String getModeratorRefreshToken() {
		checkInitialization();
		return moderatorRefreshToken;
	}

	public String getOtherRefreshToken() {
		checkInitialization();
		return otherRefreshToken;
	}

	public void setAdminToken(String adminToken) {
		this.adminToken = adminToken;
	}

	public void setModeratorToken(String moderatorToken) {
		this.moderatorToken = moderatorToken;
	}

	public void setOtherToken(String otherToken) {
		this.otherToken = otherToken;
	}
}
