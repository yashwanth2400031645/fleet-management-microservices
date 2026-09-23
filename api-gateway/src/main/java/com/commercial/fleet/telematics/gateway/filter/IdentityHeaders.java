package com.commercial.fleet.telematics.gateway.filter;

/**
 * Headers the gateway injects after verifying a token. Downstream services
 * read these instead of re-parsing the JWT. Any client-supplied values are
 * overwritten, so services can trust them as long as they are only reachable
 * through the gateway.
 */
public final class IdentityHeaders {

	public static final String USER_ID = "X-User-Id";
	public static final String USERNAME = "X-Username";
	public static final String USER_ROLE = "X-User-Role";

	private IdentityHeaders() {
	}

}
