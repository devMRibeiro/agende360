package com.github.devmribeiro.clipply.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.devmribeiro.clipply.application.type.AppointmentStatus;

public record AppointmentResponse(
	    UUID id,
	    String customerName,
	    String customerPhone,
	    String productName,
	    String professionalName,
	    LocalDateTime startTime,
	    LocalDateTime endTime,
	    AppointmentStatus status
    ) {
}