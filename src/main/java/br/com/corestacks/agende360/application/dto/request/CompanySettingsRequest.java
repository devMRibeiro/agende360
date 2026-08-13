package br.com.corestacks.agende360.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CompanySettingsRequest (
		@NotBlank
		String companyName,
		
		@NotBlank
		String phone
	) {
}