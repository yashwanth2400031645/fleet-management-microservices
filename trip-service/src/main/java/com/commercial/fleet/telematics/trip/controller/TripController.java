package com.commercial.fleet.telematics.trip.controller;

import com.commercial.fleet.telematics.trip.dto.CreateTripRequest;
import com.commercial.fleet.telematics.trip.dto.TripResponse;
import com.commercial.fleet.telematics.trip.entity.TripStatus;
import com.commercial.fleet.telematics.trip.security.AuthenticatedUser;
import com.commercial.fleet.telematics.trip.service.TripService;
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
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

	private final TripService tripService;

	@PostMapping
	public ResponseEntity<TripResponse> schedule(@Valid @RequestBody CreateTripRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(tripService.schedule(request));
	}

	/** Filters are mutually exclusive; status wins, then vehicleId, then driverId. */
	@GetMapping
	public ResponseEntity<List<TripResponse>> findAll(@RequestParam(required = false) TripStatus status,
	                                                  @RequestParam(required = false) Long vehicleId,
	                                                  @RequestParam(required = false) Long driverId) {
		return ResponseEntity.ok(tripService.findAll(status, vehicleId, driverId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<TripResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(tripService.findById(id));
	}

	@PatchMapping("/{id}/start")
	public ResponseEntity<TripResponse> start(@PathVariable Long id,
	                                          @AuthenticationPrincipal AuthenticatedUser caller) {
		return ResponseEntity.ok(tripService.start(id, caller));
	}

	@PatchMapping("/{id}/complete")
	public ResponseEntity<TripResponse> complete(@PathVariable Long id,
	                                             @AuthenticationPrincipal AuthenticatedUser caller) {
		return ResponseEntity.ok(tripService.complete(id, caller));
	}

	@PatchMapping("/{id}/cancel")
	public ResponseEntity<TripResponse> cancel(@PathVariable Long id) {
		return ResponseEntity.ok(tripService.cancel(id));
	}

}
