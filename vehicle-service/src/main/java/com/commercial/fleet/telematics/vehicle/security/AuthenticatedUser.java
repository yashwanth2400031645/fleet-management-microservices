package com.commercial.fleet.telematics.vehicle.security;

/**
 * Identity forwarded by the API gateway after it verified the JWT.
 * Available in controllers via {@code @AuthenticationPrincipal AuthenticatedUser}.
 */
public record AuthenticatedUser(Long id, String username, String role) {
}
