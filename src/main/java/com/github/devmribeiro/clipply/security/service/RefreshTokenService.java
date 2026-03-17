package com.github.devmribeiro.clipply.security.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.devmribeiro.clipply.application.model.User;
import com.github.devmribeiro.clipply.security.model.RefreshToken;
import com.github.devmribeiro.clipply.security.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

	@Value("${jwt.refresh-expiration}")
	private long refreshExpiration;
	
	private final RefreshTokenRepository refreshTokenRepository;
	
	public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
		this.refreshTokenRepository = refreshTokenRepository;
	}

	// Creates and persists a new refresh token for the user
	public RefreshToken createRefreshToken(User user) {
		// Removes any previous refresh tokens for the user
		refreshTokenRepository.deleteByUser(user);

		RefreshToken rf = new RefreshToken();
		rf.setToken(UUID.randomUUID().toString());
		rf.setUser(user);
		rf.setExpiresAt(Instant.now().plusMillis(refreshExpiration));
		rf.setRevoked(false);

		return refreshTokenRepository.save(rf);
	}

	// Validate: The token exists, has not been revoked and has not expired 
	public RefreshToken validateRefreshToken(String token) {
		RefreshToken rf = refreshTokenRepository.findByToken(token);

		if (rf == null)
			throw new RuntimeException("Refresh token not found");

		if (rf.isRevoked())
			throw new RuntimeException("The refresh token has been revoked");

		if (rf.getExpiresAt().isBefore(Instant.now()))
			throw new RuntimeException("Refresh token expired. Log in again.");

		return rf;
	}

	// Revokes (invalidates) all of the user's refresh tokens - used during logout
	public void revokeByUser(User user) {
		refreshTokenRepository.deleteByUser(user);
	}
}