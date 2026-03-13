package com.github.devmribeiro.clipply.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record RegisterCompanyRequest(

		@NotNull
		String name,

		@NotNull
		String email,

		@NotNull
		String document,
		
		@NotNull
		String phone
	) {
}