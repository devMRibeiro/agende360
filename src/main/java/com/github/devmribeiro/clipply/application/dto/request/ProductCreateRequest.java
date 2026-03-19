package com.github.devmribeiro.clipply.application.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductCreateRequest(
		@NotBlank
		String name,

		String description,

		@NotNull(message = "Price is required")
		@DecimalMin(value = "0.01", message = "Price must be greater than zero")
	    @Digits(integer = 10, fraction = 2, message = "Price must have up to 10 digits and 2 decimal places")
		BigDecimal price,

		@NotNull
		@Positive
		Integer durationMinutes
	) {
}