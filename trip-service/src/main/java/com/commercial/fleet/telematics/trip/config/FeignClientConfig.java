package com.commercial.fleet.telematics.trip.config;

import com.commercial.fleet.telematics.trip.exception.RemoteServiceException;
import com.commercial.fleet.telematics.trip.exception.ResourceNotFoundException;
import com.commercial.fleet.telematics.trip.exception.VehicleUnavailableException;
import com.commercial.fleet.telematics.trip.security.GatewayIdentityFilter;
import feign.RequestInterceptor;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Not annotated with @Configuration on purpose: referenced explicitly from
 * @FeignClient, so it must stay out of the global component scan (otherwise it
 * would apply to every Feign client in the app).
 */
@Slf4j
public class FeignClientConfig {

	/**
	 * Propagates the caller's identity downstream. Without this the vehicle
	 * service sees an anonymous request and answers 401, because it trusts the
	 * gateway headers rather than the original token.
	 */
	@Bean
	public RequestInterceptor identityPropagationInterceptor() {
		return template -> {
			var attributes = RequestContextHolder.getRequestAttributes();
			if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
				return; // no inbound request (scheduled job, startup) - nothing to propagate
			}

			var request = servletAttributes.getRequest();
			copyHeader(template, request.getHeader(GatewayIdentityFilter.HEADER_USER_ID), GatewayIdentityFilter.HEADER_USER_ID);
			copyHeader(template, request.getHeader(GatewayIdentityFilter.HEADER_USERNAME), GatewayIdentityFilter.HEADER_USERNAME);
			copyHeader(template, request.getHeader(GatewayIdentityFilter.HEADER_USER_ROLE), GatewayIdentityFilter.HEADER_USER_ROLE);
		};
	}

	private void copyHeader(feign.RequestTemplate template, String value, String name) {
		if (value != null) {
			template.header(name, value);
		}
	}

	/** Turns downstream HTTP errors into this service's own exception types. */
	@Bean
	public ErrorDecoder errorDecoder() {
		return (methodKey, response) -> switch (response.status()) {
			case 404 -> new ResourceNotFoundException("Vehicle not found (via " + methodKey + ")");
			case 409 -> new VehicleUnavailableException(describe(methodKey, response));
			case 401, 403 -> new RemoteServiceException("Not permitted by vehicle-service: " + methodKey);
			default -> new RemoteServiceException("vehicle-service returned " + response.status() + " for " + methodKey);
		};
	}

	private String describe(String methodKey, Response response) {
		log.warn("Conflict from vehicle-service on {}: status {}", methodKey, response.status());
		return "Vehicle rejected the requested status change";
	}

}
