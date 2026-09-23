package com.commercial.fleet.telematics.vehicle.dto;

import com.commercial.fleet.telematics.vehicle.entity.VehicleStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateVehicleStatusRequest(@NotNull VehicleStatus status) {
}
