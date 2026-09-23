package com.commercial.fleet.telematics.trip.dto;

import com.commercial.fleet.telematics.trip.entity.Trip;
import com.commercial.fleet.telematics.trip.entity.TripStatus;

import java.time.Instant;

public record TripResponse(
	Long id,
	Long vehicleId,
	Long driverId,
	TripStatus tripStatus,
	String origin,
	String destination,
	Instant startedAt,
	Instant endedAt,
	Instant createdAt,
	Instant updatedAt
) {
	public static TripResponse from(Trip t) {
		return new TripResponse(t.getId(), t.getVehicleId(), t.getDriverId(), t.getTripStatus(),
			t.getOrigin(), t.getDestination(), t.getStartedAt(), t.getEndedAt(),
			t.getCreatedAt(), t.getUpdatedAt());
	}
}
