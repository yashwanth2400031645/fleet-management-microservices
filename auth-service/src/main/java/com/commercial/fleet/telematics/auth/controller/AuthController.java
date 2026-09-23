package com.commercial.fleet.telematics.auth.controller;

import com.commercial.fleet.telematics.auth.dto.AuthResponse;
import com.commercial.fleet.telematics.auth.dto.LoginRequest;
import com.commercial.fleet.telematics.auth.dto.RegisterRequest;
import com.commercial.fleet.telematics.auth.dto.UserResponse;
import com.commercial.fleet.telematics.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	/** Requires a valid Bearer token; the principal is the username set by JwtAuthenticationFilter. */
	@GetMapping("/me")
	public ResponseEntity<UserResponse> me(Authentication authentication) {
		return ResponseEntity.ok(authService.currentUser(authentication.getName()));
	}

}
