package com.github.devmribeiro.clipply.security.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.devmribeiro.clipply.application.exception.IllegalArgumentException;
import com.github.devmribeiro.clipply.application.exception.UnauthorizedException;
import com.github.devmribeiro.clipply.application.model.User;
import com.github.devmribeiro.clipply.security.model.RefreshToken;
import com.github.devmribeiro.clipply.security.repository.RefreshTokenRepository;

import jakarta.transaction.Transactional;

@Service
public class RefreshTokenService {

	@Value("${jwt.refresh-expiration}")
	private long refreshExpiration;
	
	private final RefreshTokenRepository refreshTokenRepository;
	
	public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
		this.refreshTokenRepository = refreshTokenRepository;
	}

	// Creates and persists a new refresh token for the user
	@Transactional
	public RefreshToken createRefreshToken(User user) {
		// Removes any previous refresh tokens for the user
		refreshTokenRepository.deleteByUser(user);

		RefreshToken rf = new RefreshToken();
		rf.setToken(UUID.randomUUID().toString());
		rf.setUser(user);
		rf.setExpiresAt(Instant.now().plusMillis(refreshExpiration));

		return refreshTokenRepository.save(rf);
	}

	// Validate: The token exists, has not been revoked and has not expired 
	public RefreshToken validateRefreshToken(String token) {
		RefreshToken rf = refreshTokenRepository.findByToken(token);

		if (rf == null)
			throw new IllegalArgumentException("Refresh token not found");

		if (rf.getExpiresAt().isBefore(Instant.now()))
			throw new UnauthorizedException("Refresh token expired. Log in again.");

		return rf;
	}

	// Revokes (invalidates) all of the user's refresh tokens - used during logout
	@Transactional
	public void revokeByUser(User user) {
		refreshTokenRepository.deleteByUser(user);
	}
	
	public RefreshToken findByToken(String token) {
		return refreshTokenRepository.findByToken(token);
	}
}