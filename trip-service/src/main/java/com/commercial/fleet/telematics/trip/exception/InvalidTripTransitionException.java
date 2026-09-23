package com.commercial.fleet.telematics.trip.exception;

import com.commercial.fleet.telematics.trip.entity.TripStatus;

public class InvalidTripTransitionException extends RuntimeException {

	public InvalidTripTransitionException(Long tripId, TripStatus from, TripStatus to) {
		super("Trip " + tripId + " cannot move from " + from + " to " + to
			+ " (allowed: " + from.allowedTransitions() + ")");
	}

}
