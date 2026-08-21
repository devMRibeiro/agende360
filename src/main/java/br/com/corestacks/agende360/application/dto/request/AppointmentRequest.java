package br.com.corestacks.agende360.application.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AppointmentRequest(
	    @NotNull(message = "professionalId is required")
	    UUID professionalId,
	
	    @NotNull(message = "productId is required")
	    UUID productId,
	
	    @NotNull(message = "date is required")
	    LocalDate date,
	
	    @NotNull(message = "startTime is required")
	    LocalTime startTime,
	
	    @NotBlank(message = "customerName is required")
	    String customerName,
	
	    String customerEmail,
	    
	    @NotBlank(message = "customerPhone is required")
	    String customerPhone
    ) {
}