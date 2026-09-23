package com.commercial.fleet.telematics.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateVehicleLocationRequest(@NotBlank @Size(max = 255) String location) {
}
