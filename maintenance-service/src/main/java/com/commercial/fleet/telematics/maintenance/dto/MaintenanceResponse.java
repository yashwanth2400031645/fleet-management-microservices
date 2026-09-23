package com.commercial.fleet.telematics.maintenance.dto;

import com.commercial.fleet.telematics.maintenance.entity.Maintenance;
import com.commercial.fleet.telematics.maintenance.entity.MaintenanceStatus;

import java.time.Instant;

public record MaintenanceResponse(
	Long id,
	Long vehicleId,
	String issue,
	MaintenanceStatus status,
	Long reportedByUserId,
	String resolutionNotes,
	Instant resolvedAt,
	Instant createdAt,
	Instant updatedAt
) {
	public static MaintenanceResponse from(Maintenance m) {
		return new MaintenanceResponse(m.getId(), m.getVehicleId(), m.getIssue(), m.getStatus(),
			m.getReportedByUserId(), m.getResolutionNotes(), m.getResolvedAt(),
			m.getCreatedAt(), m.getUpdatedAt());
	}
}
