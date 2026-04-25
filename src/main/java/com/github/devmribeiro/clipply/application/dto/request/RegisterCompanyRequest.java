package com.github.devmribeiro.clipply.application.dto.request;

import com.github.devmribeiro.clipply.application.model.Endereco;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
		String phone,
		
		@NotNull
		Endereco endereco
	) {
}