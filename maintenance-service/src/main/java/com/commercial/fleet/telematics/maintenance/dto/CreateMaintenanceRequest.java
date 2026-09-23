package com.commercial.fleet.telematics.maintenance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateMaintenanceRequest(
	@NotNull @Positive Long vehicleId,
	@NotBlank @Size(max = 500) String issue
) {
}
