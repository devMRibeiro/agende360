package br.com.corestacks.agende360.messaging.email.dto;

import java.time.LocalDateTime;

import br.com.corestacks.agende360.application.model.Endereco;

public record AppointmentConfirmationEmail(
		String companyName,
		String companySlug,
		String customerName,
		String customerEmail,
		String productName,
		String professionalName,
		String cancelURL,
		LocalDateTime appointmentStartTime,
		Endereco endereco
	) {
}