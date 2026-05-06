package br.com.corestacks.agende360.application.dto.request;

import java.time.LocalTime;

import br.com.corestacks.agende360.application.type.DayOfWeek;
import jakarta.validation.constraints.NotNull;

public record ScheduleRequest(
	    @NotNull(message = "dayOfWeek is required")
	    DayOfWeek dayOfWeek,
	
	    @NotNull(message = "startTime is required")
	    LocalTime startTime,
	
	    @NotNull(message = "endTime is required")
	    LocalTime endTime
	){
}