package br.com.corestacks.agende360.application.dto.response;

import java.util.List;

public record ScheduleResponse(
		Integer horizon,
		List<ScheduleResponseData> data
    ) {
}