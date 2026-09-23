package com.commercial.fleet.telematics.trip.entity;

import java.util.EnumSet;
import java.util.Set;

public enum TripStatus {
	SCHEDULED,
	IN_PROGRESS,
	COMPLETED,
	CANCELLED;

	public Set<TripStatus> allowedTransitions() {
		return switch (this) {
			case SCHEDULED -> EnumSet.of(IN_PROGRESS, CANCELLED);
			case IN_PROGRESS -> EnumSet.of(COMPLETED, CANCELLED);
			case COMPLETED, CANCELLED -> EnumSet.noneOf(TripStatus.class); // terminal
		};
	}

	public boolean canTransitionTo(TripStatus target) {
		return allowedTransitions().contains(target);
	}
}
