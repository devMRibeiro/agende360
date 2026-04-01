package com.github.devmribeiro.clipply.application.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.devmribeiro.clipply.application.model.PasswordResetToken;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
	@Query("""
			SELECT t FROM PasswordResetToken t
			WHERE t.tokenHash = :hash
			AND t.expiresAt > :now
			AND t.used = false
	""")
	PasswordResetToken findValidToken(String hash, LocalDateTime now);
}