package com.commercial.fleet.telematics.maintenance.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Hands the failure cause to the fallback, so it can tell a real business
 * response (404/409) apart from vehicle-service actually being down.
 */
@Component
public class VehicleClientFallbackFactory implements FallbackFactory<VehicleClient> {

	@Override
	public VehicleClient create(Throwable cause) {
		return new VehicleClientFallback(cause);
	}

}
