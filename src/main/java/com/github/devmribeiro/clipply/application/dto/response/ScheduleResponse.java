package com.github.devmribeiro.clipply.application.dto.response;

import java.util.List;

public record ScheduleResponse(
		Integer horizon,
		List<ScheduleResponseData> data
    ) {
}