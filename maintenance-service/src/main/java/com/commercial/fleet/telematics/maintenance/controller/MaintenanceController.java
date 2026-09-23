package com.commercial.fleet.telematics.maintenance.controller;

import com.commercial.fleet.telematics.maintenance.dto.CreateMaintenanceRequest;
import com.commercial.fleet.telematics.maintenance.dto.MaintenanceResponse;
import com.commercial.fleet.telematics.maintenance.dto.ResolveMaintenanceRequest;
import com.commercial.fleet.telematics.maintenance.entity.MaintenanceStatus;
import com.commercial.fleet.telematics.maintenance.security.AuthenticatedUser;
import com.commercial.fleet.telematics.maintenance.service.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

	private final MaintenanceService maintenanceService;

	@PostMapping
	public ResponseEntity<MaintenanceResponse> report(@Valid @RequestBody CreateMaintenanceRequest request,
	                                                  @AuthenticationPrincipal AuthenticatedUser caller) {
		return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceService.report(request, caller));
	}

	/** Filters are mutually exclusive; status wins over vehicleId. */
	@GetMapping
	public ResponseEntity<List<MaintenanceResponse>> findAll(@RequestParam(required = false) MaintenanceStatus status,
	                                                         @RequestParam(required = false) Long vehicleId) {
		return ResponseEntity.ok(maintenanceService.findAll(status, vehicleId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<MaintenanceResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(maintenanceService.findById(id));
	}

	@PatchMapping("/{id}/start")
	public ResponseEntity<MaintenanceResponse> startRepair(@PathVariable Long id) {
		return ResponseEntity.ok(maintenanceService.startRepair(id));
	}

	@PatchMapping("/{id}/resolve")
	public ResponseEntity<MaintenanceResponse> resolve(@PathVariable Long id,
	                                                   @Valid @RequestBody ResolveMaintenanceRequest request) {
		return ResponseEntity.ok(maintenanceService.resolve(id, request));
	}

	@PatchMapping("/{id}/cancel")
	public ResponseEntity<MaintenanceResponse> cancel(@PathVariable Long id) {
		return ResponseEntity.ok(maintenanceService.cancel(id));
	}

}
