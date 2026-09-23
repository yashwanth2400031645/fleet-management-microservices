package com.commercial.fleet.telematics.maintenance.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * The declared server is the API gateway, not this service's own port: the
 * gateway validates the JWT and injects the X-User-* headers this service
 * trusts. Calling port 8084 directly from Swagger would return 401.
 */
@Configuration
public class OpenApiConfig {

	@Value("${openapi.gateway-url:http://localhost:8080}")
	private String gatewayUrl;

	@Bean
	public OpenAPI maintenanceServiceOpenApi() {
		return new OpenAPI()
			.servers(List.of(new Server().url(gatewayUrl).description("API Gateway")))
			.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
			.info(new Info()
				.title("Maintenance Service API")
				.version("v1")
				.description("""
					Maintenance tickets: REPORTED -> IN_REPAIR -> RESOLVED.

					Any role may report a fault. Starting and resolving a repair moves
					the vehicle to UNDER_MAINTENANCE / AVAILABLE via OpenFeign."""))
			.components(new Components().addSecuritySchemes("bearerAuth",
				new SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")
					.description("Paste the accessToken returned by /api/auth/login")));
	}

}
