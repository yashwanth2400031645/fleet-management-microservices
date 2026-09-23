package com.commercial.fleet.telematics.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Populates the SecurityContext from a Bearer token. Invalid tokens are ignored
 * here so the entry point in SecurityConfig produces the 401 uniformly.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtService jwtService;

	@Override
	protected void doFilterInternal(HttpServletRequest request,
	                                HttpServletResponse response,
	                                FilterChain filterChain) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (header != null && header.startsWith(BEARER_PREFIX)
				&& SecurityContextHolder.getContext().getAuthentication() == null) {
			try {
				Claims claims = jwtService.parse(header.substring(BEARER_PREFIX.length()));
				String role = claims.get(JwtService.CLAIM_ROLE, String.class);

				var authentication = new UsernamePasswordAuthenticationToken(
					claims.getSubject(), null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
			catch (JwtException | IllegalArgumentException ex) {
				log.debug("Rejected JWT: {}", ex.getMessage());
			}
		}

		filterChain.doFilter(request, response);
	}

}
