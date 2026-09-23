package com.commercial.fleet.telematics.maintenance.dto;

import jakarta.validation.constraints.Size;

/** Notes are optional; the workshop may simply close the ticket. */
public record ResolveMaintenanceRequest(@Size(max = 500) String resolutionNotes) {
}
