package br.com.corestacks.agende360.application.dto.response;

import br.com.corestacks.agende360.application.type.AppointmentStatus;

public record AppointmentStatusCountResponse(
	    AppointmentStatus status,
	    Long count
	) {
}