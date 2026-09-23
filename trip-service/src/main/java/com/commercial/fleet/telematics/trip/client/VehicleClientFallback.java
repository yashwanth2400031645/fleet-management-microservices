package com.commercial.fleet.telematics.trip.client;

import com.commercial.fleet.telematics.trip.client.dto.UpdateVehicleStatusRequest;
import com.commercial.fleet.telematics.trip.client.dto.VehicleResponse;
import com.commercial.fleet.telematics.trip.exception.RemoteServiceException;
import com.commercial.fleet.telematics.trip.exception.ResourceNotFoundException;
import com.commercial.fleet.telematics.trip.exception.VehicleUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Runs when the circuit to vehicle-service is open, or a call fails outright.
 * {@code cause} carries the original failure, so a genuine 404/409 from the
 * remote service is still reported as such rather than being masked as an outage.
 * <p>
 * Reads degrade to a clear error; writes must never be faked, because reporting
 * a vehicle as ON_TRIP when the update never landed would corrupt the fleet state.
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
		if (cause instanceof ResourceNotFoundException || cause instanceof VehicleUnavailableException) {
			// A real business answer from the remote service - pass it straight through.
			return (RuntimeException) cause;
		}
		log.warn("Circuit open or call failed, could not {}: {}", attempted,
			cause != null ? cause.toString() : "unknown cause");
		return new RemoteServiceException(
			"Vehicle service is not responding, could not " + attempted + ". Please retry shortly.");
	}

}
