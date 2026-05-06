package br.com.corestacks.agende360.application.dto.response;

public record CompanyPublicResponse(
		String name,
		String slug,
		Integer schedulingHorizon
	) {
}