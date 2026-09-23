package com.commercial.fleet.telematics.gateway.filter;

import com.commercial.fleet.telematics.gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Rejects requests without a valid Bearer token before they reach any route,
 * except for paths listed under {@code jwt.public-paths}. On success the
 * verified claims are forwarded as {@link IdentityHeaders}.
 * <p>
 * Runs as a plain servlet filter (no Spring Security on the gateway) so it sits
 * in front of the Gateway MVC handler with nothing else in the way.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String CLAIM_USER_ID = "uid";
	private static final String CLAIM_ROLE = "role";

	private final JwtProperties properties;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();
	private SecretKey key;

	@PostConstruct
	void init() {
		key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return properties.publicPaths().stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
	                                HttpServletResponse response,
	                                FilterChain filterChain) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith(BEARER_PREFIX)) {
			reject(response, request, "Missing or malformed Authorization header");
			return;
		}

		Claims claims;
		try {
			claims = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(header.substring(BEARER_PREFIX.length()))
				.getPayload();
		}
		catch (ExpiredJwtException ex) {
			reject(response, request, "Token has expired");
			return;
		}
		catch (JwtException | IllegalArgumentException ex) {
			log.debug("Rejected JWT on {}: {}", request.getRequestURI(), ex.getMessage());
			reject(response, request, "Invalid token");
			return;
		}

		Map<String, String> identity = Map.of(
			IdentityHeaders.USER_ID, String.valueOf(claims.get(CLAIM_USER_ID)),
			IdentityHeaders.USERNAME, claims.getSubject(),
			IdentityHeaders.USER_ROLE, String.valueOf(claims.get(CLAIM_ROLE)));

		filterChain.doFilter(new HeaderOverridingRequest(request, identity), response);
	}

	private void reject(HttpServletResponse response, HttpServletRequest request, String message) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write("""
			{"timestamp":"%s","status":401,"error":"Unauthorized","message":"%s","path":"%s"}"""
			.formatted(Instant.now(), message, request.getRequestURI()));
	}

}
