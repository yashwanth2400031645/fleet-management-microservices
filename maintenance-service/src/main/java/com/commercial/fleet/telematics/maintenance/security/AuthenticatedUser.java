package com.commercial.fleet.telematics.maintenance.security;

/** Identity forwarded by the API gateway after it verified the JWT. */
public record AuthenticatedUser(Long id, String username, String role) {
}
