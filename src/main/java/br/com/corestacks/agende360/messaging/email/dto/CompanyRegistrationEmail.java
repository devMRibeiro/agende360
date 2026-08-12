package br.com.corestacks.agende360.messaging.email.dto;

public record CompanyRegistrationEmail(
		String companyName,
		String publicLink,
		String companyDoc,
		String userName,
		String userEmail,
		String passwordTemp
	) {
}