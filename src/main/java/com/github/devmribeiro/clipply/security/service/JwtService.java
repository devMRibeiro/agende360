package com.github.devmribeiro.clipply.security.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import com.github.devmribeiro.clipply.application.exception.IllegalArgumentException;
import com.github.devmribeiro.clipply.application.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class JwtService {

	@Value("${security.jwt.secret}")
    private String secretKey;

	@Value("${security.jwt.expiration-ms}")
    private Long expirationToken;

	private final String COOKIE_TOKEN_NAME = "access_token";

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
		try {
	        return Jwts.parser()
	        		.verifyWith(key)
		            .build()
		            .parseSignedClaims(token)
		            .getPayload();
		} catch (ExpiredJwtException e) {
			throw new IllegalArgumentException("JWT token expired");
		} catch (UnsupportedJwtException e) {
			throw new IllegalArgumentException("Unsupported JWT");
		} catch (MalformedJwtException e) {
			throw new IllegalArgumentException("Malformed JWT");
		} catch (SignatureException e) {
			throw new IllegalArgumentException("Invalid signature");
		} catch (Exception e) {
			throw new IllegalArgumentException("Unexpected error");
		}
	}

	public String extractUserId(String token) {
		return parseClaims(token).getSubject();
	}

	public String extractEmail(String token) {
		return parseClaims(token).get("email", String.class);
	}

	public String extractUserRole(String token) {
		return parseClaims(token).get("role", String.class);
	}
	
	public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractEmail(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }
    
    public String getTokenFromCookie(HttpServletRequest request) {

	    if (request.getCookies() == null)
	        return null;

	    for (Cookie cookie : request.getCookies()) {
	        if (COOKIE_TOKEN_NAME.equals(cookie.getName()))
	            return cookie.getValue();
	    }
	    return null;
	}
}