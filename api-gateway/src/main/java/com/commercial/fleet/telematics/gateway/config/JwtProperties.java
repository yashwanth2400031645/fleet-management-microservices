package com.commercial.fleet.telematics.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @param secret      Base64-encoded HMAC key; must match the auth-service value.
 * @param publicPaths Ant-style patterns that bypass token validation.
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, List<String> publicPaths) {
}
