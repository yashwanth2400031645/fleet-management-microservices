package com.commercial.fleet.telematics.trip.repository;

import com.commercial.fleet.telematics.trip.entity.Trip;
import com.commercial.fleet.telematics.trip.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

	List<Trip> findByTripStatus(TripStatus tripStatus);

	List<Trip> findByVehicleId(Long vehicleId);

	List<Trip> findByDriverId(Long driverId);

	boolean existsByVehicleIdAndTripStatusIn(Long vehicleId, Collection<TripStatus> statuses);

}
