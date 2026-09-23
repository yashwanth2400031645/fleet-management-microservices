package com.commercial.fleet.telematics.trip.client.dto;

/** Subset of vehicle-service's response that this service actually reads. */
public record VehicleResponse(
	Long id,
	String registrationNumber,
	String type,
	VehicleStatus status,
	String location
) {
}
