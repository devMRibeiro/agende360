package br.com.corestacks.agende360.messaging.whatsapp.dto;

import java.time.LocalDateTime;

public record WhatsAppAppointmentReminder(
	    String customerPhone,
	    String customerName,
	    LocalDateTime appointmentDateTime,
	    String companyAddress,
	    String productName,
	    String productDescription,
	    String professionalName,
	    String companyName,
	    String companySlug,
	    String appointmentToken
) {}