package com.github.devmribeiro.clipply.application.dto.request;

import com.github.devmribeiro.clipply.application.type.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterProfessionalRequest(
		@NotBlank(message = "name is required")
		String name,

		@Email
		@NotBlank(message = "email is required")
		String email,
		
		@NotNull(message = "role is required")
		UserRole role,
		
		@NotBlank(message = "phone is required")
		String phone
	){
}