package com.commercial.fleet.telematics.vehicle.dto;

import com.commercial.fleet.telematics.vehicle.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Create / full-update payload. Status is managed through its own endpoint. */
public record VehicleRequest(
	@NotBlank @Size(max = 30) String registrationNumber,
	@NotNull VehicleType type,
	@Size(max = 255) String location
) {
}
