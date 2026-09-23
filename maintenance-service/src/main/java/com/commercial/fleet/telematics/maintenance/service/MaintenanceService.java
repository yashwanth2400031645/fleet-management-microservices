package com.commercial.fleet.telematics.maintenance.service;

import com.commercial.fleet.telematics.maintenance.client.VehicleClient;
import com.commercial.fleet.telematics.maintenance.client.dto.UpdateVehicleStatusRequest;
import com.commercial.fleet.telematics.maintenance.client.dto.VehicleResponse;
import com.commercial.fleet.telematics.maintenance.client.dto.VehicleStatus;
import com.commercial.fleet.telematics.maintenance.dto.CreateMaintenanceRequest;
import com.commercial.fleet.telematics.maintenance.dto.MaintenanceResponse;
import com.commercial.fleet.telematics.maintenance.dto.ResolveMaintenanceRequest;
import com.commercial.fleet.telematics.maintenance.entity.Maintenance;
import com.commercial.fleet.telematics.maintenance.entity.MaintenanceStatus;
import com.commercial.fleet.telematics.maintenance.exception.InvalidMaintenanceTransitionException;
import com.commercial.fleet.telematics.maintenance.exception.ResourceNotFoundException;
import com.commercial.fleet.telematics.maintenance.exception.VehicleConflictException;
import com.commercial.fleet.telematics.maintenance.repository.MaintenanceRepository;
import com.commercial.fleet.telematics.maintenance.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceService {

	private static final EnumSet<MaintenanceStatus> OPEN_STATUSES =
		EnumSet.of(MaintenanceStatus.REPORTED, MaintenanceStatus.IN_REPAIR);

	private final MaintenanceRepository maintenanceRepository;
	private final VehicleClient vehicleClient;

	/**
	 * Anyone may report a fault - a driver noticing a problem on the road is the
	 * common case. The vehicle is not pulled from service yet; that happens when
	 * the workshop actually starts work.
	 */
	@Transactional
	public MaintenanceResponse report(CreateMaintenanceRequest request, AuthenticatedUser caller) {
		VehicleResponse vehicle = vehicleClient.getVehicle(request.vehicleId());

		if (maintenanceRepository.existsByVehicleIdAndStatusIn(request.vehicleId(), OPEN_STATUSES)) {
			throw new VehicleConflictException("Vehicle " + vehicle.id() + " already has an open maintenance record");
		}

		Maintenance record = Maintenance.builder()
			.vehicleId(request.vehicleId())
			.issue(request.issue())
			.status(MaintenanceStatus.REPORTED)
			.reportedByUserId(caller.id())
			.build();
		record = maintenanceRepository.save(record);
		log.info("Maintenance {} reported for vehicle {} by {}", record.getId(), record.getVehicleId(), caller.username());
		return MaintenanceResponse.from(record);
	}

	@Transactional(readOnly = true)
	public List<MaintenanceResponse> findAll(MaintenanceStatus status, Long vehicleId) {
		List<Maintenance> records;
		if (status != null) {
			records = maintenanceRepository.findByStatus(status);
		}
		else if (vehicleId != null) {
			records = maintenanceRepository.findByVehicleId(vehicleId);
		}
		else {
			records = maintenanceRepository.findAll();
		}
		return records.stream().map(MaintenanceResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public MaintenanceResponse findById(Long id) {
		return MaintenanceResponse.from(getOrThrow(id));
	}

	/**
	 * REPORTED -> IN_REPAIR, and the vehicle moves to UNDER_MAINTENANCE.
	 * <p>
	 * vehicle-service rejects the move with 409 if the vehicle is mid-trip,
	 * which surfaces here as VehicleConflictException and rolls the record back.
	 */
	@Transactional
	@PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
	public MaintenanceResponse startRepair(Long id) {
		Maintenance record = getOrThrow(id);
		transition(record, MaintenanceStatus.IN_REPAIR);

		vehicleClient.updateStatus(record.getVehicleId(),
			new UpdateVehicleStatusRequest(VehicleStatus.UNDER_MAINTENANCE));

		log.info("Maintenance {} moved to IN_REPAIR; vehicle {} UNDER_MAINTENANCE", id, record.getVehicleId());
		return MaintenanceResponse.from(record);
	}

	/** IN_REPAIR -> RESOLVED, and the vehicle returns to AVAILABLE. */
	@Transactional
	@PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
	public MaintenanceResponse resolve(Long id, ResolveMaintenanceRequest request) {
		Maintenance record = getOrThrow(id);
		transition(record, MaintenanceStatus.RESOLVED);

		vehicleClient.updateStatus(record.getVehicleId(), new UpdateVehicleStatusRequest(VehicleStatus.AVAILABLE));

		record.setResolutionNotes(request.resolutionNotes());
		record.setResolvedAt(Instant.now());
		log.info("Maintenance {} resolved; vehicle {} back to AVAILABLE", id, record.getVehicleId());
		return MaintenanceResponse.from(record);
	}

	/**
	 * Cancels a record raised in error. If the workshop had already pulled the
	 * vehicle (IN_REPAIR), it is released back to AVAILABLE.
	 */
	@Transactional
	@PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
	public MaintenanceResponse cancel(Long id) {
		Maintenance record = getOrThrow(id);
		boolean vehicleWasPulled = record.getStatus() == MaintenanceStatus.IN_REPAIR;
		transition(record, MaintenanceStatus.CANCELLED);

		if (vehicleWasPulled) {
			vehicleClient.updateStatus(record.getVehicleId(), new UpdateVehicleStatusRequest(VehicleStatus.AVAILABLE));
		}

		log.info("Maintenance {} cancelled", id);
		return MaintenanceResponse.from(record);
	}

	private void transition(Maintenance record, MaintenanceStatus target) {
		MaintenanceStatus from = record.getStatus();
		if (!from.canTransitionTo(target)) {
			throw new InvalidMaintenanceTransitionException(record.getId(), from, target);
		}
		record.setStatus(target);
	}

	private Maintenance getOrThrow(Long id) {
		return maintenanceRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Maintenance record not found: " + id));
	}

}
