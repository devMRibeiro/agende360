package br.com.corestacks.agende360.messaging.whatsapp.dto;

import java.time.LocalDateTime;

public record AppointmentConfirmationMessage(
	    String customerPhone,
	    String customerName,
	    LocalDateTime appointmentDateTime,
	    String companyAddress,
	    String productName,
	    String professionalName,
	    String companyName,
	    String companySlug,
	    String appointmentToken
) {}