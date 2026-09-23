package com.commercial.fleet.telematics.maintenance.config;

import com.commercial.fleet.telematics.maintenance.exception.RemoteServiceException;
import com.commercial.fleet.telematics.maintenance.exception.ResourceNotFoundException;
import com.commercial.fleet.telematics.maintenance.exception.VehicleConflictException;
import com.commercial.fleet.telematics.maintenance.security.GatewayIdentityFilter;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Not annotated with @Configuration on purpose: referenced explicitly from
 * @FeignClient, so it must stay out of the global component scan.
 */
@Slf4j
public class FeignClientConfig {

	/** Propagates the caller's identity so vehicle-service does not answer 401. */
	@Bean
	public RequestInterceptor identityPropagationInterceptor() {
		return template -> {
			var attributes = RequestContextHolder.getRequestAttributes();
			if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
				return; // no inbound request to propagate from
			}

			var request = servletAttributes.getRequest();
			copyHeader(template, request.getHeader(GatewayIdentityFilter.HEADER_USER_ID), GatewayIdentityFilter.HEADER_USER_ID);
			copyHeader(template, request.getHeader(GatewayIdentityFilter.HEADER_USERNAME), GatewayIdentityFilter.HEADER_USERNAME);
			copyHeader(template, request.getHeader(GatewayIdentityFilter.HEADER_USER_ROLE), GatewayIdentityFilter.HEADER_USER_ROLE);
		};
	}

	private void copyHeader(RequestTemplate template, String value, String name) {
		if (value != null) {
			template.header(name, value);
		}
	}

	@Bean
	public ErrorDecoder errorDecoder() {
		return (methodKey, response) -> switch (response.status()) {
			case 404 -> new ResourceNotFoundException("Vehicle not found (via " + methodKey + ")");
			case 409 -> new VehicleConflictException("Vehicle rejected the requested status change");
			case 401, 403 -> new RemoteServiceException("Not permitted by vehicle-service: " + methodKey);
			default -> new RemoteServiceException("vehicle-service returned " + response.status() + " for " + methodKey);
		};
	}

}
