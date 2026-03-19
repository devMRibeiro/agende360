package com.github.devmribeiro.clipply.application.dto.response;

import com.github.devmribeiro.clipply.application.type.UserRole;

public record UserResponse(
		String name,
		String email,
		String phone,
		boolean active,
		UserRole role
	) {
}