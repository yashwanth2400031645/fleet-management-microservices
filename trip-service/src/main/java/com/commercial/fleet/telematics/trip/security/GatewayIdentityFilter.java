package com.commercial.fleet.telematics.trip.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Builds the SecurityContext from the identity headers the gateway injects.
 * The gateway overwrites any client-supplied copies, so this service must only
 * be reachable through it.
 */
@Component
@Slf4j
public class GatewayIdentityFilter extends OncePerRequestFilter {

	public static final String HEADER_USER_ID = "X-User-Id";
	public static final String HEADER_USERNAME = "X-Username";
	public static final String HEADER_USER_ROLE = "X-User-Role";

	@Override
	protected void doFilterInternal(HttpServletRequest request,
	                                HttpServletResponse response,
	                                FilterChain filterChain) throws ServletException, IOException {
		String userId = request.getHeader(HEADER_USER_ID);
		String username = request.getHeader(HEADER_USERNAME);
		String role = request.getHeader(HEADER_USER_ROLE);

		if (username != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			Long id = null;
			try {
				id = userId != null ? Long.valueOf(userId) : null;
			}
			catch (NumberFormatException ex) {
				log.warn("Non-numeric {} header: {}", HEADER_USER_ID, userId);
			}

			var principal = new AuthenticatedUser(id, username, role);
			var authentication = new UsernamePasswordAuthenticationToken(
				principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}

}
