package com.a6raywa1cher.pasttyspring.configs.security;

import com.a6raywa1cher.pasttyspring.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class TokenOncePerRequestFilter extends OncePerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(TokenOncePerRequestFilter.class);
	private final SecurityTokenService securityTokenService;

	public TokenOncePerRequestFilter(SecurityTokenService securityTokenService) {
		this.securityTokenService = securityTokenService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
	                                HttpServletResponse response,
	                                FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader("jwt");
		log.debug(header);
		if (header == null) {
			header = request.getHeader(HttpHeaders.AUTHORIZATION);
			log.debug(header);
			if (header == null) {
				filterChain.doFilter(request, response);        // If not valid, go to the next filter.
				return;
			}
			if (!header.startsWith("Bearer ")) {
				filterChain.doFilter(request, response);        // If not valid, go to the next filter.
				return;
			}
			header = header.replace("Bearer ", "").strip();
		}

		Optional<User> user = securityTokenService.checkToken(header);

		if (user.isEmpty()) {
			filterChain.doFilter(request, response);        // If not valid, go to the next filter.
			return;
		}

		TokenAuthentication tokenAuthentication = new TokenAuthentication(header, user.get());
//		tokenAuthentication.setAuthenticated(true);
		SecurityContextHolder.getContext().setAuthentication(tokenAuthentication);
		filterChain.doFilter(request, response);
	}
}
