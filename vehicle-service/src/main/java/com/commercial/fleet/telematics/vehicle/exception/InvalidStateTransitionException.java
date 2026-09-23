package com.commercial.fleet.telematics.vehicle.exception;

import com.commercial.fleet.telematics.vehicle.entity.VehicleStatus;

public class InvalidStateTransitionException extends RuntimeException {

	public InvalidStateTransitionException(String message) {
		super(message);
	}

	public InvalidStateTransitionException(Long vehicleId, VehicleStatus from, VehicleStatus to) {
		super("Vehicle " + vehicleId + " cannot move from " + from + " to " + to
			+ " (allowed: " + from.allowedTransitions() + ")");
	}

}
