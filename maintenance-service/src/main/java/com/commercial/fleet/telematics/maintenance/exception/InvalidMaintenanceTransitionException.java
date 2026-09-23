package com.commercial.fleet.telematics.maintenance.exception;

import com.commercial.fleet.telematics.maintenance.entity.MaintenanceStatus;

public class InvalidMaintenanceTransitionException extends RuntimeException {

	public InvalidMaintenanceTransitionException(Long id, MaintenanceStatus from, MaintenanceStatus to) {
		super("Maintenance record " + id + " cannot move from " + from + " to " + to
			+ " (allowed: " + from.allowedTransitions() + ")");
	}

}
