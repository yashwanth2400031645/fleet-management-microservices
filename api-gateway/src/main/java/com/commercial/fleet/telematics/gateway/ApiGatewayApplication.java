package com.commercial.fleet.telematics.gateway;

import com.commercial.fleet.telematics.gateway.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Edge service. Routes are declared in application.yml and resolved against
 * Eureka through Spring Cloud LoadBalancer (lb://{service-name}).
 * <p>
 * {@link com.commercial.fleet.telematics.gateway.filter.JwtAuthenticationFilter}
 * is the single JWT enforcement point: every route outside {@code jwt.public-paths}
 * requires a valid Bearer token, and the verified identity is forwarded to the
 * downstream service as X-User-Id / X-Username / X-User-Role headers.
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
@EnableScheduling
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
