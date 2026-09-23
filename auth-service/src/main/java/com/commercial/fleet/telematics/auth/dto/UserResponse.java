package com.commercial.fleet.telematics.auth.dto;

import com.commercial.fleet.telematics.auth.entity.Role;
import com.commercial.fleet.telematics.auth.entity.User;

import java.time.Instant;

public record UserResponse(Long id, String username, Role role, Instant createdAt) {

	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.getCreatedAt());
	}

}
