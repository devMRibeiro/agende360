package com.github.devmribeiro.clipply.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewPasswordRequest(
		
		@NotBlank
		String token,
		
		@NotBlank
		@Size(min = 6, max = 16, message = "newPassword must be between 6 and 16")
		String newPassword
	) {
}