package com.commercial.fleet.telematics.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixed-window rate limiter, applied per client before routing. Protects the
 * downstream services from a single caller flooding them, and blunts brute-force
 * attempts against /api/auth/login.
 * <p>
 * Deliberately in-memory: this keeps the gateway free of a Redis dependency.
 * The trade-off is that the budget is per gateway instance, so a multi-instance
 * deployment would need the Redis-backed limiter to share one counter.
 * <p>
 * Runs AFTER {@link JwtAuthenticationFilter} on purpose: the username it keys
 * on is then one the gateway verified, not a header the caller supplied and
 * could rotate to slip past the limit. Anonymous traffic is keyed by IP.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

	private final Map<String, Window> windows = new ConcurrentHashMap<>();

	@Value("${gateway.rate-limit.requests-per-minute:120}")
	private int requestsPerMinute;

	@Value("${gateway.rate-limit.enabled:true}")
	private boolean enabled;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		if (!enabled) {
			return true;
		}
		// Never throttle the landing page polling its own health probes.
		String path = request.getRequestURI();
		return path.startsWith("/status/") || path.startsWith("/actuator/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
	                                HttpServletResponse response,
	                                FilterChain filterChain) throws ServletException, IOException {
		String key = clientKey(request);
		long minute = Instant.now().getEpochSecond() / 60;

		Window window = windows.compute(key, (k, existing) ->
			existing == null || existing.minute != minute ? new Window(minute) : existing);

		int used = window.count.incrementAndGet();
		int remaining = Math.max(0, requestsPerMinute - used);

		response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
		response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));

		if (used > requestsPerMinute) {
			log.warn("Rate limit exceeded by {} on {}", key, request.getRequestURI());
			response.setStatus(429);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setHeader("Retry-After", "60");
			response.getWriter().write("""
				{"timestamp":"%s","status":429,"error":"Too Many Requests",\
				"message":"Rate limit of %d requests per minute exceeded","path":"%s"}"""
				.formatted(Instant.now(), requestsPerMinute, request.getRequestURI()));
			return;
		}

		filterChain.doFilter(request, response);
	}

	/** Prefer the authenticated username; fall back to the remote address. */
	private String clientKey(HttpServletRequest request) {
		String user = request.getHeader(IdentityHeaders.USERNAME);
		return user != null ? "user:" + user : "ip:" + request.getRemoteAddr();
	}

	/** Keeps the map from growing without bound on a long-running gateway. */
	@org.springframework.scheduling.annotation.Scheduled(fixedDelay = 300_000)
	void evictStaleWindows() {
		long current = Instant.now().getEpochSecond() / 60;
		windows.entrySet().removeIf(e -> current - e.getValue().minute > 2);
	}

	private static final class Window {
		private final long minute;
		private final AtomicInteger count = new AtomicInteger();

		private Window(long minute) {
			this.minute = minute;
		}
	}

}
