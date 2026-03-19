package com.github.devmribeiro.clipply.application.dto.response;

public record RegisterCompanyResponse(
		String userName,
		String email,
		String slug
	) {
}