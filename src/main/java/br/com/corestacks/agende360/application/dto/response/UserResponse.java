package br.com.corestacks.agende360.application.dto.response;

import java.util.UUID;

import br.com.corestacks.agende360.application.type.UserRole;

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