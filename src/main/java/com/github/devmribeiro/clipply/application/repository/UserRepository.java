package com.github.devmribeiro.clipply.application.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.devmribeiro.clipply.application.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

	@Query("select u from users u where u.id = :id")
	User findByUserId(UUID id);

	User findByEmail(String email);

	boolean existsByEmail(String email);
}