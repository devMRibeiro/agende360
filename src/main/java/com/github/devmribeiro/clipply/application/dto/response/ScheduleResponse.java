package com.github.devmribeiro.clipply.application.dto.response;

import java.time.LocalTime;
import java.util.UUID;

import com.github.devmribeiro.clipply.application.type.DayOfWeek;

public record ScheduleResponse(
	    UUID id,
	    DayOfWeek dayOfWeek,
	    LocalTime startTime,
	    LocalTime endTime
    ) {
}