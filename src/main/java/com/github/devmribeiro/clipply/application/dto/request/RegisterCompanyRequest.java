package com.github.devmribeiro.clipply.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterCompanyRequest(

		@NotBlank(message = "companyName is required")
		String companyName,

		@NotBlank(message = "userName is required")
		String userName,

		@NotBlank
		@Email
		String email,

		@NotBlank
		String document,
		
		@NotBlank
		String phone
	) {
}