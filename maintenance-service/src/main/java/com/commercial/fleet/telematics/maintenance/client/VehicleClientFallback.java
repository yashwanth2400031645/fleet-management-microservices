package com.commercial.fleet.telematics.maintenance.client;

import com.commercial.fleet.telematics.maintenance.client.dto.UpdateVehicleStatusRequest;
import com.commercial.fleet.telematics.maintenance.client.dto.VehicleResponse;
import com.commercial.fleet.telematics.maintenance.exception.RemoteServiceException;
import com.commercial.fleet.telematics.maintenance.exception.ResourceNotFoundException;
import com.commercial.fleet.telematics.maintenance.exception.VehicleConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Runs when the circuit to vehicle-service is open, or a call fails outright.
 * {@code cause} carries the original failure, so a genuine 404/409 from the
 * remote service is still reported as such rather than being masked as an outage.
 * <p>
 * Writes are never faked: claiming a vehicle is UNDER_MAINTENANCE when the
 * update never landed would leave the two services permanently inconsistent.
 */
@RequiredArgsConstructor
@Slf4j
public class VehicleClientFallback implements VehicleClient {

	private final Throwable cause;

	@Override
	public VehicleResponse getVehicle(Long id) {
		throw translate("read vehicle " + id);
	}

	@Override
	public VehicleResponse updateStatus(Long id, UpdateVehicleStatusRequest request) {
		throw translate("update vehicle " + id + " to " + request.status());
	}

	private RuntimeException translate(String attempted) {
		if (cause instanceof ResourceNotFoundException || cause instanceof VehicleConflictException) {
			// A real business answer from the remote service - pass it straight through.
			return (RuntimeException) cause;
		}
		log.warn("Circuit open or call failed, could not {}: {}", attempted,
			cause != null ? cause.toString() : "unknown cause");
		return new RemoteServiceException(
			"Vehicle service is not responding, could not " + attempted + ". Please retry shortly.");
	}

}
