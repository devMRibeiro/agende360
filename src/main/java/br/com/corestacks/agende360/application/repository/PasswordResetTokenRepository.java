package br.com.corestacks.agende360.application.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.corestacks.agende360.application.model.PasswordResetToken;

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