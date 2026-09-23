package com.commercial.fleet.telematics.maintenance.client.dto;

public record VehicleResponse(
	Long id,
	String registrationNumber,
	String type,
	VehicleStatus status,
	String location
) {
}
