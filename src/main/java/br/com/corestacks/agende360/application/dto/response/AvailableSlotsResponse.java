package br.com.corestacks.agende360.application.dto.response;

import java.time.LocalTime;
import java.util.List;

public record AvailableSlotsResponse(
		List<LocalTime> slots
	) {
}