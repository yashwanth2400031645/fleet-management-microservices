package com.commercial.fleet.telematics.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateTripRequest(
	@NotNull @Positive Long vehicleId,
	@NotNull @Positive Long driverId,
	@NotBlank @Size(max = 255) String origin,
	@NotBlank @Size(max = 255) String destination
) {
}
