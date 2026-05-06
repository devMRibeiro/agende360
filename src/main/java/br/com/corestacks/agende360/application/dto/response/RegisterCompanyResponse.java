package br.com.corestacks.agende360.application.dto.response;

public record RegisterCompanyResponse(
		String companyName,
		String slug,
		String email,
		String userName
	) {
}