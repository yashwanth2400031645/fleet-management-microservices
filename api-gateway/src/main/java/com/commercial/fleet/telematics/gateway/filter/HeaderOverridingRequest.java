package com.commercial.fleet.telematics.gateway.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Servlet requests are read-only, so identity headers are layered on via a
 * wrapper. Overridden names shadow anything the client sent, which is what
 * prevents X-User-* spoofing. The gateway proxy copies headers from this
 * wrapper when it builds the downstream call.
 */
class HeaderOverridingRequest extends HttpServletRequestWrapper {

	private final Map<String, String> overrides = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	HeaderOverridingRequest(HttpServletRequest request, Map<String, String> overrides) {
		super(request);
		this.overrides.putAll(overrides);
	}

	@Override
	public String getHeader(String name) {
		String value = overrides.get(name);
		return value != null ? value : super.getHeader(name);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		String value = overrides.get(name);
		return value != null ? Collections.enumeration(List.of(value)) : super.getHeaders(name);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		Set<String> names = new LinkedHashSet<>(Collections.list(super.getHeaderNames()));
		names.addAll(overrides.keySet());
		return Collections.enumeration(names);
	}

}
