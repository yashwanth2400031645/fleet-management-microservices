package com.commercial.fleet.telematics.maintenance.client.dto;

/** Local copy of vehicle-service's status enum; services stay independently deployable. */
public enum VehicleStatus {
	AVAILABLE,
	ON_TRIP,
	UNDER_MAINTENANCE,
	OUT_OF_SERVICE
}
