package com.commercial.fleet.telematics.auth.service;

import com.commercial.fleet.telematics.auth.dto.AuthResponse;
import com.commercial.fleet.telematics.auth.dto.LoginRequest;
import com.commercial.fleet.telematics.auth.dto.RegisterRequest;
import com.commercial.fleet.telematics.auth.dto.UserResponse;
import com.commercial.fleet.telematics.auth.entity.Role;
import com.commercial.fleet.telematics.auth.entity.User;
import com.commercial.fleet.telematics.auth.exception.ResourceNotFoundException;
import com.commercial.fleet.telematics.auth.exception.UserAlreadyExistsException;
import com.commercial.fleet.telematics.auth.repository.UserRepository;
import com.commercial.fleet.telematics.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByUsername(request.username())) {
			throw new UserAlreadyExistsException(request.username());
		}

		User user = User.builder()
			.username(request.username())
			.password(passwordEncoder.encode(request.password()))
			.role(request.role() != null ? request.role() : Role.DRIVER)
			.build();
		user = userRepository.save(user);
		log.info("Registered user {} with role {}", user.getUsername(), user.getRole());

		return issueToken(user);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		// Throws BadCredentialsException on failure; mapped to 401 by GlobalExceptionHandler.
		authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(request.username(), request.password()));

		User user = userRepository.findByUsername(request.username())
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.username()));
		return issueToken(user);
	}

	@Transactional(readOnly = true)
	public UserResponse currentUser(String username) {
		return userRepository.findByUsername(username)
			.map(UserResponse::from)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
	}

	private AuthResponse issueToken(User user) {
		return AuthResponse.bearer(jwtService.generateToken(user), jwtService.expirationMs(), UserResponse.from(user));
	}

}
