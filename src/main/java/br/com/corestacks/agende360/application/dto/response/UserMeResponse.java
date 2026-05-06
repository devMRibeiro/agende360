package br.com.corestacks.agende360.application.dto.response;

import java.util.UUID;

import br.com.corestacks.agende360.application.type.UserRole;

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