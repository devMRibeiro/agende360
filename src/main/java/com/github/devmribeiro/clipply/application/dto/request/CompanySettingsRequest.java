package com.github.devmribeiro.clipply.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CompanySettingsRequest (
		@NotBlank
		String companyName,
		
		@NotBlank
		String phone,
		
		@NotBlank
		String email
	) {
}