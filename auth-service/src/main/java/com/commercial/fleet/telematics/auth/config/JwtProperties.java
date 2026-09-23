package com.commercial.fleet.telematics.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param secret       Base64-encoded HMAC key, at least 256 bits. Must match the gateway.
 * @param expirationMs Token lifetime in milliseconds.
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, long expirationMs) {
}
