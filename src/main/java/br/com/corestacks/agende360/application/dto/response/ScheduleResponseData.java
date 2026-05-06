package br.com.corestacks.agende360.application.dto.response;

import java.time.LocalTime;
import java.util.UUID;

import br.com.corestacks.agende360.application.type.DayOfWeek;

public record ScheduleResponseData(
		UUID id,
	    DayOfWeek dayOfWeek,
	    LocalTime startTime,
	    LocalTime endTime
	){
}