package com.github.devmribeiro.clipply.application.dto.response;

import java.time.LocalTime;
import java.util.List;

public record AvailableSlotsResponse(
		List<LocalTime> slots
	) {
}