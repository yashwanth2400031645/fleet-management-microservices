package com.commercial.fleet.telematics.vehicle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

@Entity
@Table(name = "vehicles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "vehicle_id")
	private Long id;

	@Column(name = "registration_number", nullable = false, unique = true, length = 30)
	private String registrationNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private VehicleType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private VehicleStatus status;

	/** Free-text last known location (depot name, city, or "lat,lng"). */
	@Column(length = 255)
	private String location;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

}
