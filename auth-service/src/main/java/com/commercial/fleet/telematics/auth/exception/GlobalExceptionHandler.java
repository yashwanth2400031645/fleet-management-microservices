package com.commercial.fleet.telematics.auth.exception;

import com.commercial.fleet.telematics.auth.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleConflict(UserAlreadyExistsException ex, HttpServletRequest req) {
		return build(HttpStatus.CONFLICT, ex.getMessage(), req);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
	}

	@ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
	public ResponseEntity<ErrorResponse> handleBadCredentials(RuntimeException ex, HttpServletRequest req) {
		// Deliberately vague: never reveal whether the username or the password was wrong.
		return build(HttpStatus.UNAUTHORIZED, "Invalid username or password", req);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
			fieldErrors.put(fe.getField(), fe.getDefaultMessage());
		}
		ErrorResponse body = new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
			HttpStatus.BAD_REQUEST.getReasonPhrase(), "Validation failed", req.getRequestURI(), fieldErrors);
		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
		log.error("Unhandled exception on {} {}", req.getMethod(), req.getRequestURI(), ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", req);
	}

	private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
		return ResponseEntity.status(status)
			.body(ErrorResponse.of(status.value(), status.getReasonPhrase(), message, req.getRequestURI()));
	}

}
