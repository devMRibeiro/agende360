package br.com.corestacks.agende360.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.corestacks.agende360.application.type.AppointmentStatus;

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