package com.github.devmribeiro.clipply.application.dto.response;

public record RegisterCompanyResponse(
		String name,
		String email,
		String slug
	) {
}