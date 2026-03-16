package com.github.devmribeiro.clipply.security.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import com.github.devmribeiro.clipply.application.model.User;
import com.github.devmribeiro.clipply.application.type.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

public class JwtService {

	@Value("${security.jwt.secret}")
    private String secretKey;

	@Value("${security.jwt.expiration-ms}")
    private Long expirationToken;

	public String generateToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .claim("email", user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationToken))
                .signWith(key)
                .compact();
	}

	public Claims parseClaims(String token) {
		SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
        		.verifyWith(key)
	            .build()
	            .parseSignedClaims(token)
	            .getPayload();
	}

	public String extractUserId(String token) {
		return parseClaims(token).getSubject();
	}

	public String extractEmail(String token) {
		return parseClaims(token).get("email", String.class);
	}

	public UserRole extractUserRole(String token) {
		return parseClaims(token).get("role", UserRole.class);
	}
	
	public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractEmail(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }
}