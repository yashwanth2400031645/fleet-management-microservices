package com.commercial.fleet.telematics.vehicle.service;

import com.commercial.fleet.telematics.vehicle.dto.UpdateVehicleLocationRequest;
import com.commercial.fleet.telematics.vehicle.dto.UpdateVehicleStatusRequest;
import com.commercial.fleet.telematics.vehicle.dto.VehicleRequest;
import com.commercial.fleet.telematics.vehicle.dto.VehicleResponse;
import com.commercial.fleet.telematics.vehicle.entity.Vehicle;
import com.commercial.fleet.telematics.vehicle.entity.VehicleStatus;
import com.commercial.fleet.telematics.vehicle.exception.DuplicateResourceException;
import com.commercial.fleet.telematics.vehicle.exception.InvalidStateTransitionException;
import com.commercial.fleet.telematics.vehicle.exception.ResourceNotFoundException;
import com.commercial.fleet.telematics.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleService {

	private final VehicleRepository vehicleRepository;

	@Transactional
	@PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
	public VehicleResponse create(VehicleRequest request) {
		if (vehicleRepository.existsByRegistrationNumber(request.registrationNumber())) {
			throw new DuplicateResourceException("Vehicle already registered: " + request.registrationNumber());
		}

		Vehicle vehicle = Vehicle.builder()
			.registrationNumber(request.registrationNumber())
			.type(request.type())
			.status(VehicleStatus.AVAILABLE)
			.location(request.location())
			.build();
		vehicle = vehicleRepository.save(vehicle);
		log.info("Created vehicle {} ({})", vehicle.getId(), vehicle.getRegistrationNumber());
		return VehicleResponse.from(vehicle);
	}

	@Transactional(readOnly = true)
	public List<VehicleResponse> findAll(VehicleStatus status) {
		List<Vehicle> vehicles = status == null ? vehicleRepository.findAll() : vehicleRepository.findByStatus(status);
		return vehicles.stream().map(VehicleResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public VehicleResponse findById(Long id) {
		return VehicleResponse.from(getOrThrow(id));
	}

	@Transactional
	@PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
	public VehicleResponse update(Long id, VehicleRequest request) {
		Vehicle vehicle = getOrThrow(id);

		if (!vehicle.getRegistrationNumber().equals(request.registrationNumber())
				&& vehicleRepository.existsByRegistrationNumber(request.registrationNumber())) {
			throw new DuplicateResourceException("Vehicle already registered: " + request.registrationNumber());
		}

		vehicle.setRegistrationNumber(request.registrationNumber());
		vehicle.setType(request.type());
		vehicle.setLocation(request.location());
		return VehicleResponse.from(vehicle);
	}

	/**
	 * Status changes are allowed for every role because the Trip service
	 * (driver starts/ends a trip) and Maintenance service call this on behalf
	 * of the original caller. The transition table guards consistency.
	 */
	@Transactional
	public VehicleResponse updateStatus(Long id, UpdateVehicleStatusRequest request) {
		Vehicle vehicle = getOrThrow(id);
		VehicleStatus from = vehicle.getStatus();
		VehicleStatus to = request.status();

		if (!from.canTransitionTo(to)) {
			throw new InvalidStateTransitionException(id, from, to);
		}

		vehicle.setStatus(to);
		log.info("Vehicle {} status {} -> {}", id, from, to);
		return VehicleResponse.from(vehicle);
	}

	@Transactional
	public VehicleResponse updateLocation(Long id, UpdateVehicleLocationRequest request) {
		Vehicle vehicle = getOrThrow(id);
		vehicle.setLocation(request.location());
		return VehicleResponse.from(vehicle);
	}

	@Transactional
	@PreAuthorize("hasRole('ADMIN')")
	public void delete(Long id) {
		Vehicle vehicle = getOrThrow(id);
		if (vehicle.getStatus() == VehicleStatus.ON_TRIP) {
			throw new InvalidStateTransitionException("Vehicle " + id + " is ON_TRIP and cannot be deleted");
		}
		vehicleRepository.delete(vehicle);
		log.info("Deleted vehicle {}", id);
	}

	private Vehicle getOrThrow(Long id) {
		return vehicleRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
	}

}
