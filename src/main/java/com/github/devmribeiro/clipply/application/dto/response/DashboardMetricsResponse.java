package com.github.devmribeiro.clipply.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardMetricsResponse(
		BigDecimal expectedRevenue,
		Integer totalAppointments,
		Integer confirmedAppointments,
		Integer occupancyRate,
		Trend revenueTrend,
		Trend appointmentsTrend,
		List<String> insights
	){
}