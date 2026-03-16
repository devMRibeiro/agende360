package com.github.devmribeiro.clipply.security.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.devmribeiro.clipply.application.model.User;
import com.github.devmribeiro.clipply.security.model.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

	RefreshToken findByToken(String token);
	void deleteByUser(User user); // used when loggin out
}