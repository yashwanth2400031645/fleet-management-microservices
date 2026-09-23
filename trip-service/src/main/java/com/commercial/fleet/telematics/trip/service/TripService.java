package com.commercial.fleet.telematics.trip.service;

import com.commercial.fleet.telematics.trip.client.VehicleClient;
import com.commercial.fleet.telematics.trip.client.dto.UpdateVehicleStatusRequest;
import com.commercial.fleet.telematics.trip.client.dto.VehicleResponse;
import com.commercial.fleet.telematics.trip.client.dto.VehicleStatus;
import com.commercial.fleet.telematics.trip.dto.CreateTripRequest;
import com.commercial.fleet.telematics.trip.dto.TripResponse;
import com.commercial.fleet.telematics.trip.entity.Trip;
import com.commercial.fleet.telematics.trip.entity.TripStatus;
import com.commercial.fleet.telematics.trip.exception.InvalidTripTransitionException;
import com.commercial.fleet.telematics.trip.exception.ResourceNotFoundException;
import com.commercial.fleet.telematics.trip.exception.VehicleUnavailableException;
import com.commercial.fleet.telematics.trip.repository.TripRepository;
import com.commercial.fleet.telematics.trip.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

	private static final EnumSet<TripStatus> ACTIVE_STATUSES = EnumSet.of(TripStatus.SCHEDULED, TripStatus.IN_PROGRESS);

	private final TripRepository tripRepository;
	private final VehicleClient vehicleClient;

	@Transactional
	@PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
	public TripResponse schedule(CreateTripRequest request) {
		// Throws ResourceNotFoundException via the Feign ErrorDecoder if the vehicle does not exist.
		VehicleResponse vehicle = vehicleClient.getVehicle(request.vehicleId());

		if (vehicle.status() != VehicleStatus.AVAILABLE) {
			throw new VehicleUnavailableException(
				"Vehicle " + vehicle.id() + " is " + vehicle.status() + ", not AVAILABLE");
		}
		if (tripRepository.existsByVehicleIdAndTripStatusIn(request.vehicleId(), ACTIVE_STATUSES)) {
			throw new VehicleUnavailableException("Vehicle " + request.vehicleId() + " already has an active trip");
		}

		Trip trip = Trip.builder()
			.vehicleId(request.vehicleId())
			.driverId(request.driverId())
			.tripStatus(TripStatus.SCHEDULED)
			.origin(request.origin())
			.destination(request.destination())
			.build();
		trip = tripRepository.save(trip);
		log.info("Scheduled trip {} for vehicle {} / driver {}", trip.getId(), trip.getVehicleId(), trip.getDriverId());
		return TripResponse.from(trip);
	}

	@Transactional(readOnly = true)
	public List<TripResponse> findAll(TripStatus status, Long vehicleId, Long driverId) {
		List<Trip> trips;
		if (status != null) {
			trips = tripRepository.findByTripStatus(status);
		}
		else if (vehicleId != null) {
			trips = tripRepository.findByVehicleId(vehicleId);
		}
		else if (driverId != null) {
			trips = tripRepository.findByDriverId(driverId);
		}
		else {
			trips = tripRepository.findAll();
		}
		return trips.stream().map(TripResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public TripResponse findById(Long id) {
		return TripResponse.from(getOrThrow(id));
	}

	/**
	 * SCHEDULED -> IN_PROGRESS, and the vehicle moves to ON_TRIP.
	 * <p>
	 * The remote status change runs before the local commit so a rejection from
	 * vehicle-service rolls this transaction back. The reverse gap remains: if
	 * the commit fails after the remote call succeeded, the vehicle is left
	 * ON_TRIP. A production system would close that with an outbox or saga.
	 */
	@Transactional
	public TripResponse start(Long id, AuthenticatedUser caller) {
		Trip trip = getOrThrow(id);
		requireDriverOrDispatcher(trip, caller);
		transition(trip, TripStatus.IN_PROGRESS);

		vehicleClient.updateStatus(trip.getVehicleId(), new UpdateVehicleStatusRequest(VehicleStatus.ON_TRIP));

		trip.setStartedAt(Instant.now());
		log.info("Trip {} started by {}", id, caller.username());
		return TripResponse.from(trip);
	}

	/** IN_PROGRESS -> COMPLETED, and the vehicle returns to AVAILABLE. */
	@Transactional
	public TripResponse complete(Long id, AuthenticatedUser caller) {
		Trip trip = getOrThrow(id);
		requireDriverOrDispatcher(trip, caller);
		transition(trip, TripStatus.COMPLETED);

		vehicleClient.updateStatus(trip.getVehicleId(), new UpdateVehicleStatusRequest(VehicleStatus.AVAILABLE));

		trip.setEndedAt(Instant.now());
		log.info("Trip {} completed by {}", id, caller.username());
		return TripResponse.from(trip);
	}

	/**
	 * Cancels from either active state. The vehicle is only released when the
	 * trip had actually taken it (IN_PROGRESS); a SCHEDULED trip never moved it.
	 */
	@Transactional
	@PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
	public TripResponse cancel(Long id) {
		Trip trip = getOrThrow(id);
		boolean vehicleWasEngaged = trip.getTripStatus() == TripStatus.IN_PROGRESS;
		transition(trip, TripStatus.CANCELLED);

		if (vehicleWasEngaged) {
			vehicleClient.updateStatus(trip.getVehicleId(), new UpdateVehicleStatusRequest(VehicleStatus.AVAILABLE));
		}

		trip.setEndedAt(Instant.now());
		log.info("Trip {} cancelled", id);
		return TripResponse.from(trip);
	}

	private void transition(Trip trip, TripStatus target) {
		TripStatus from = trip.getTripStatus();
		if (!from.canTransitionTo(target)) {
			throw new InvalidTripTransitionException(trip.getId(), from, target);
		}
		trip.setTripStatus(target);
	}

	/** A driver may only drive their own trip; dispatchers and admins may act on any. */
	private void requireDriverOrDispatcher(Trip trip, AuthenticatedUser caller) {
		boolean privileged = "ADMIN".equals(caller.role()) || "DISPATCHER".equals(caller.role());
		boolean assignedDriver = caller.id() != null && caller.id().equals(trip.getDriverId());
		if (!privileged && !assignedDriver) {
			throw new AccessDeniedException("Trip " + trip.getId() + " is assigned to another driver");
		}
	}

	private Trip getOrThrow(Long id) {
		return tripRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + id));
	}

}
