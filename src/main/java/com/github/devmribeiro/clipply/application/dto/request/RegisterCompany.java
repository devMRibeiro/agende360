package com.github.devmribeiro.clipply.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record RegisterCompany(

		@NotNull
		String name,

		@NotNull
		String email,

		@NotNull
		String password,
		
		@NotNull
		String document,
		
		@NotNull
		String phone
	) {
}