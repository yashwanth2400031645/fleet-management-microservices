package com.commercial.fleet.telematics.vehicle.dto;

import com.commercial.fleet.telematics.vehicle.entity.Vehicle;
import com.commercial.fleet.telematics.vehicle.entity.VehicleStatus;
import com.commercial.fleet.telematics.vehicle.entity.VehicleType;

import java.time.Instant;

public record VehicleResponse(
	Long id,
	String registrationNumber,
	VehicleType type,
	VehicleStatus status,
	String location,
	Instant createdAt,
	Instant updatedAt
) {
	public static VehicleResponse from(Vehicle v) {
		return new VehicleResponse(v.getId(), v.getRegistrationNumber(), v.getType(), v.getStatus(),
			v.getLocation(), v.getCreatedAt(), v.getUpdatedAt());
	}
}
