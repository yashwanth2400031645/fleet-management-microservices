package com.commercial.fleet.telematics.maintenance.exception;

/** Downstream call failed for a reason this service cannot resolve. */
public class RemoteServiceException extends RuntimeException {

	public RemoteServiceException(String message) {
		super(message);
	}

}
