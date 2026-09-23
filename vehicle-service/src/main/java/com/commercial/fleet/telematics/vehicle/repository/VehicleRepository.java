package com.commercial.fleet.telematics.vehicle.repository;

import com.commercial.fleet.telematics.vehicle.entity.Vehicle;
import com.commercial.fleet.telematics.vehicle.entity.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

	List<Vehicle> findByStatus(VehicleStatus status);

	boolean existsByRegistrationNumber(String registrationNumber);

}
