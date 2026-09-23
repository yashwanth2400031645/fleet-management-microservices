package com.commercial.fleet.telematics.vehicle.controller;

import com.commercial.fleet.telematics.vehicle.dto.UpdateVehicleLocationRequest;
import com.commercial.fleet.telematics.vehicle.dto.UpdateVehicleStatusRequest;
import com.commercial.fleet.telematics.vehicle.dto.VehicleRequest;
import com.commercial.fleet.telematics.vehicle.dto.VehicleResponse;
import com.commercial.fleet.telematics.vehicle.entity.VehicleStatus;
import com.commercial.fleet.telematics.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

	private final VehicleService vehicleService;

	@PostMapping
	public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(request));
	}

	/** Optional filter, e.g. {@code GET /api/vehicles?status=AVAILABLE}. */
	@GetMapping
	public ResponseEntity<List<VehicleResponse>> findAll(@RequestParam(required = false) VehicleStatus status) {
		return ResponseEntity.ok(vehicleService.findAll(status));
	}

	@GetMapping("/{id}")
	public ResponseEntity<VehicleResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(vehicleService.findById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<VehicleResponse> update(@PathVariable Long id, @Valid @RequestBody VehicleRequest request) {
		return ResponseEntity.ok(vehicleService.update(id, request));
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<VehicleResponse> updateStatus(@PathVariable Long id,
	                                                    @Valid @RequestBody UpdateVehicleStatusRequest request) {
		return ResponseEntity.ok(vehicleService.updateStatus(id, request));
	}

	@PatchMapping("/{id}/location")
	public ResponseEntity<VehicleResponse> updateLocation(@PathVariable Long id,
	                                                      @Valid @RequestBody UpdateVehicleLocationRequest request) {
		return ResponseEntity.ok(vehicleService.updateLocation(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		vehicleService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
