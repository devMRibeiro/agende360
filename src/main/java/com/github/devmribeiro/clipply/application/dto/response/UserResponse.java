package com.github.devmribeiro.clipply.application.dto.response;

import java.util.UUID;

import com.github.devmribeiro.clipply.application.type.UserRole;

public record UserResponse(
		UUID id,
		String name,
		String email,
		String phone,
		boolean active,
		UserRole role,
		Boolean isProfessional
	) {
}