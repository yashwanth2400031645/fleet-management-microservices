package com.commercial.fleet.telematics.vehicle.entity;

import java.util.EnumSet;
import java.util.Set;

/**
 * Lifecycle of a vehicle. Transitions are driven by the Trip service
 * (AVAILABLE <-> ON_TRIP) and the Maintenance service (-> UNDER_MAINTENANCE -> AVAILABLE).
 */
public enum VehicleStatus {
	AVAILABLE,
	ON_TRIP,
	UNDER_MAINTENANCE,
	OUT_OF_SERVICE;

	public Set<VehicleStatus> allowedTransitions() {
		return switch (this) {
			case AVAILABLE -> EnumSet.of(ON_TRIP, UNDER_MAINTENANCE, OUT_OF_SERVICE);
			case ON_TRIP -> EnumSet.of(AVAILABLE, UNDER_MAINTENANCE);
			case UNDER_MAINTENANCE -> EnumSet.of(AVAILABLE, OUT_OF_SERVICE);
			case OUT_OF_SERVICE -> EnumSet.of(AVAILABLE, UNDER_MAINTENANCE);
		};
	}

	public boolean canTransitionTo(VehicleStatus target) {
		return this == target || allowedTransitions().contains(target);
	}
}
