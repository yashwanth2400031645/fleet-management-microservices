package com.commercial.fleet.telematics.auth.dto;

public record AuthResponse(
	String accessToken,
	String tokenType,
	long expiresInMs,
	UserResponse user
) {
	public static AuthResponse bearer(String token, long expiresInMs, UserResponse user) {
		return new AuthResponse(token, "Bearer", expiresInMs, user);
	}
}
