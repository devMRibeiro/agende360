package com.github.devmribeiro.clipply.application.dto.response;

import java.util.UUID;

import com.github.devmribeiro.clipply.application.type.UserRole;

public record UserMeResponse(
		UUID id,
		String email,
		UUID companyid,
		String companyName,
		String companySlug,
		UserRole role,
		boolean isFirstAccess
	) {
}