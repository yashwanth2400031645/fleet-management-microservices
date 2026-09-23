package com.commercial.fleet.telematics.maintenance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * vehicleId references a row owned by vehicle-service; reportedByUserId a row
 * owned by auth-service. Cross-service ids are stored as plain columns.
 */
@Entity
@Table(name = "maintenance_records", indexes = {
	@Index(name = "idx_maintenance_vehicle", columnList = "vehicle_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Maintenance {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "maintenance_id")
	private Long id;

	@Column(name = "vehicle_id", nullable = false)
	private Long vehicleId;

	@Column(nullable = false, length = 500)
	private String issue;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MaintenanceStatus status;

	@Column(name = "reported_by_user_id")
	private Long reportedByUserId;

	@Column(name = "resolution_notes", length = 500)
	private String resolutionNotes;

	@Column(name = "resolved_at")
	private Instant resolvedAt;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

}
