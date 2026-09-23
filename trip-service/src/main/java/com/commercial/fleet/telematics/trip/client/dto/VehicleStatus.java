package com.commercial.fleet.telematics.trip.client.dto;

/**
 * Local copy of vehicle-service's status enum. Duplicated deliberately: sharing
 * a jar between services would couple their release cycles.
 */
public enum VehicleStatus {
	AVAILABLE,
	ON_TRIP,
	UNDER_MAINTENANCE,
	OUT_OF_SERVICE
}
