package com.commercial.fleet.telematics.maintenance.entity;

import java.util.EnumSet;
import java.util.Set;

/**
 * REPORTED  - logged, vehicle not yet pulled from service
 * IN_REPAIR - workshop started; vehicle is UNDER_MAINTENANCE
 * RESOLVED  - fixed; vehicle returns to AVAILABLE
 * CANCELLED - raised in error / duplicate
 */
public enum MaintenanceStatus {
	REPORTED,
	IN_REPAIR,
	RESOLVED,
	CANCELLED;

	public Set<MaintenanceStatus> allowedTransitions() {
		return switch (this) {
			case REPORTED -> EnumSet.of(IN_REPAIR, CANCELLED);
			case IN_REPAIR -> EnumSet.of(RESOLVED, CANCELLED);
			case RESOLVED, CANCELLED -> EnumSet.noneOf(MaintenanceStatus.class); // terminal
		};
	}

	public boolean canTransitionTo(MaintenanceStatus target) {
		return allowedTransitions().contains(target);
	}
}
