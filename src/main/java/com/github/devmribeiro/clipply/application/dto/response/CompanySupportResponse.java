package com.github.devmribeiro.clipply.application.dto.response;

public record CompanySupportResponse(
		String name,
		String slug,
		String document,
		boolean active,
		String userAdmin,
		String userPhone,
		String userEmail
	) {
}