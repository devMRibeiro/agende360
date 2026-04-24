package com.github.devmribeiro.clipply.application.dto.response;

public record CompanyPublicResponse(
		String name,
		String slug,
		Integer schedulingHorizon
	) {
}