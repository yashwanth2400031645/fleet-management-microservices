package com.commercial.fleet.telematics.trip.exception;

import com.commercial.fleet.telematics.trip.dto.ErrorResponse;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
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

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
	}

	@ExceptionHandler({VehicleUnavailableException.class, InvalidTripTransitionException.class})
	public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest req) {
		return build(HttpStatus.CONFLICT, ex.getMessage(), req);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex, HttpServletRequest req) {
		return build(HttpStatus.FORBIDDEN, "Insufficient permissions", req);
	}

	/** vehicle-service is reachable but unhappy, or unreachable entirely. */
	@ExceptionHandler({RemoteServiceException.class, FeignException.class})
	public ResponseEntity<ErrorResponse> handleRemote(RuntimeException ex, HttpServletRequest req) {
		log.error("Downstream call failed on {} {}", req.getMethod(), req.getRequestURI(), ex);
		return build(HttpStatus.BAD_GATEWAY, "Vehicle service is unavailable, please retry", req);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, "Malformed request body", req);
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
