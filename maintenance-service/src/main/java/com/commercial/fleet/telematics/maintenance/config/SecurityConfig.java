package com.commercial.fleet.telematics.maintenance.config;

import com.commercial.fleet.telematics.maintenance.security.GatewayIdentityFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final GatewayIdentityFilter gatewayIdentityFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/health", "/actuator/info").permitAll()
				// The gateway fetches these unauthenticated to build the aggregated Swagger UI.
				.requestMatchers("/v3/api-docs", "/v3/api-docs/**").permitAll()
				.anyRequest().authenticated())
			.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Missing gateway identity\"}");
			}))
			.addFilterBefore(gatewayIdentityFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

}
