package com.commercial.fleet.telematics.trip.client;

import com.commercial.fleet.telematics.trip.client.dto.UpdateVehicleStatusRequest;
import com.commercial.fleet.telematics.trip.client.dto.VehicleResponse;
import com.commercial.fleet.telematics.trip.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The service id is resolved through Eureka + Spring Cloud LoadBalancer, so no
 * host or port appears anywhere. Paths mirror vehicle-service's controller.
 */
@FeignClient(name = "vehicle-service", configuration = FeignClientConfig.class,
	fallbackFactory = VehicleClientFallbackFactory.class)
public interface VehicleClient {

	@GetMapping("/api/vehicles/{id}")
	VehicleResponse getVehicle(@PathVariable("id") Long id);

	@PatchMapping("/api/vehicles/{id}/status")
	VehicleResponse updateStatus(@PathVariable("id") Long id, @RequestBody UpdateVehicleStatusRequest request);

}
