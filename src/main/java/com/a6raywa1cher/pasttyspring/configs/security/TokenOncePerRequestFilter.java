package com.a6raywa1cher.pasttyspring.configs.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
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
	private final JwtSecurityTokenService securityTokenService;
	private final AuthenticationProvider provider;

	public TokenOncePerRequestFilter(JwtSecurityTokenService securityTokenService, AuthenticationProvider provider) {
		this.securityTokenService = securityTokenService;
		this.provider = provider;
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

		Optional<TokenAuthentication> tokenAuthentication = securityTokenService.toAuthentication(header);

		if (tokenAuthentication.isEmpty()) {
			filterChain.doFilter(request, response);        // If not valid, go to the next filter.
			return;
		}
		try {
			provider.authenticate(tokenAuthentication.get());
			SecurityContextHolder.getContext().setAuthentication(tokenAuthentication.get());
			filterChain.doFilter(request, response);
		} catch (AuthenticationException ae) {
			SecurityContextHolder.clearContext();
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}
