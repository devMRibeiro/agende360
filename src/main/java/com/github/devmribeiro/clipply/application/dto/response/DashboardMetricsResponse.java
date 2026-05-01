package com.github.devmribeiro.clipply.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardMetricsResponse(
		BigDecimal expectedRevenue,
		Long totalAppointments,
		Long confirmedAppointments,
		Integer occupancyRate,
		Trend revenueTrend,
		Trend appointmentsTrend,
		List<String> insights
	){
}