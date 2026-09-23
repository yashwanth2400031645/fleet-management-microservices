package com.commercial.fleet.telematics.maintenance.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
	Instant timestamp,
	int status,
	String error,
	String message,
	String path,
	Map<String, String> fieldErrors // null unless validation failed
) {
	public static ErrorResponse of(int status, String error, String message, String path) {
		return new ErrorResponse(Instant.now(), status, error, message, path, null);
	}
}
