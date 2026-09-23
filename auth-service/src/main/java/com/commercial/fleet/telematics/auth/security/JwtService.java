package com.commercial.fleet.telematics.auth.security;

import com.commercial.fleet.telematics.auth.config.JwtProperties;
import com.commercial.fleet.telematics.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

/**
 * Issues and verifies HS256 tokens. Claim layout (also consumed by the gateway):
 * <pre>
 *   sub  - username
 *   uid  - numeric user id
 *   role - ADMIN | DISPATCHER | DRIVER
 * </pre>
 */
@Service
@RequiredArgsConstructor
public class JwtService {

	public static final String CLAIM_USER_ID = "uid";
	public static final String CLAIM_ROLE = "role";

	private final JwtProperties properties;
	private SecretKey key;

	@PostConstruct
	void init() {
		key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
	}

	public String generateToken(User user) {
		Instant now = Instant.now();
		return Jwts.builder()
			.subject(user.getUsername())
			.claim(CLAIM_USER_ID, user.getId())
			.claim(CLAIM_ROLE, user.getRole().name())
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plusMillis(properties.expirationMs())))
			.signWith(key)
			.compact();
	}

	/** Throws {@link io.jsonwebtoken.JwtException} on bad signature, expiry or malformed input. */
	public Claims parse(String token) {
		return Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	public long expirationMs() {
		return properties.expirationMs();
	}

}
