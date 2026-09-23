package com.commercial.fleet.telematics.trip.entity;

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
 * vehicleId and driverId are plain foreign keys to rows owned by other services
 * (vehicle-service and auth-service). Each service keeps its own schema, so
 * there is no JPA relationship across the boundary - only the id is stored.
 */
@Entity
@Table(name = "trips", indexes = {
	@Index(name = "idx_trip_vehicle", columnList = "vehicle_id"),
	@Index(name = "idx_trip_driver", columnList = "driver_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trip {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "trip_id")
	private Long id;

	@Column(name = "vehicle_id", nullable = false)
	private Long vehicleId;

	@Column(name = "driver_id", nullable = false)
	private Long driverId;

	@Enumerated(EnumType.STRING)
	@Column(name = "trip_status", nullable = false, length = 20)
	private TripStatus tripStatus;

	@Column(nullable = false, length = 255)
	private String origin;

	@Column(nullable = false, length = 255)
	private String destination;

	@Column(name = "started_at")
	private Instant startedAt;

	@Column(name = "ended_at")
	private Instant endedAt;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

}
