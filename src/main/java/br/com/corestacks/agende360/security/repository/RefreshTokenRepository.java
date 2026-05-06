package br.com.corestacks.agende360.security.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.corestacks.agende360.application.model.User;
import br.com.corestacks.agende360.security.model.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

	RefreshToken findByToken(String token);
	void deleteByUser(User user); // used when loggin out
}