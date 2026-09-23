package com.commercial.fleet.telematics.maintenance.repository;

import com.commercial.fleet.telematics.maintenance.entity.Maintenance;
import com.commercial.fleet.telematics.maintenance.entity.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

	List<Maintenance> findByStatus(MaintenanceStatus status);

	List<Maintenance> findByVehicleId(Long vehicleId);

	boolean existsByVehicleIdAndStatusIn(Long vehicleId, Collection<MaintenanceStatus> statuses);

}
