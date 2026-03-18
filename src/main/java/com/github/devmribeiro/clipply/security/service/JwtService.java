package com.github.devmribeiro.clipply.security.service;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.github.devmribeiro.clipply.application.exception.IllegalArgumentException;
import com.github.devmribeiro.clipply.application.model.User;
import com.github.devmribeiro.clipply.application.type.UserRole;

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

@Service
public class JwtService {

	@Value("${security.jwt.secret}")
    private String secretKey;

	@Value("${security.jwt.expiration-ms}")
    private Long expirationToken;

	public String generateToken(User user) {
        return Jwts.builder()
        		.subject(user.getEmail())
                .claim("role", user.getRole().name())
                .claim("companyId", user.getCompanyId())
                .claim("active", user.getActive())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationToken))
                .signWith(getKey())
                .compact();
	}

	public Claims parseClaims(String token) {
		try {
	        return Jwts.parser()
	        		.verifyWith(getKey())
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

	private SecretKey getKey() {
	    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
	}

	public String extractUserId(String token) {
		return parseClaims(token).getSubject();
	}

	public String extractEmail(String token) {
		return parseClaims(token).get("email", String.class);
	}

	public UserRole extractUserRole(String token) {
		String role = parseClaims(token).get("role", String.class);
		return role != null ? UserRole.valueOf(role) : null;
	}
	
	public UUID extractCompanyId(String token) {
	    String companyId = parseClaims(token).get("companyId", String.class);

	    if (companyId == null)
	        return null;

	    return UUID.fromString(companyId);
	}
	
	public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractEmail(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }
    
    public String getTokenFromCookie(HttpServletRequest request, String cookieName) {

	    if (request.getCookies() == null)
	        return null;

	    for (Cookie cookie : request.getCookies()) {
	        if (cookieName.equals(cookie.getName()))
	            return cookie.getValue();
	    }
	    return null;
	}
}