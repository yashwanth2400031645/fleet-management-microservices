package com.commercial.fleet.telematics.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS is configured once, here at the edge, rather than in four services.
 * <p>
 * Origins are an explicit allow-list from configuration - never a wildcard,
 * because these endpoints carry bearer tokens and a wildcard would let any site
 * a logged-in user visits call the API on their behalf.
 */
@Configuration
public class CorsConfig {

	@Value("${gateway.cors.allowed-origins:http://localhost:3000,http://localhost:4200,http://localhost:5173}")
	private List<String> allowedOrigins;

	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(allowedOrigins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
		config.setExposedHeaders(List.of("X-RateLimit-Limit", "X-RateLimit-Remaining", "Retry-After"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);

		var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", config);

		var registration = new FilterRegistrationBean<>(new CorsFilter(source));
		// Ahead of the JWT filter, so a browser preflight (which carries no token)
		// is answered instead of being rejected with 401.
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}

}
