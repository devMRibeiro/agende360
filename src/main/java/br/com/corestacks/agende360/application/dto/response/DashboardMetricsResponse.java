package br.com.corestacks.agende360.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardMetricsResponse(
		BigDecimal expectedRevenue,
		Long totalAppointments,
		Long confirmedAppointments,
		Trend revenueTrend,
		Trend appointmentsTrend,
		List<TrendPoint> chart,
		List<TopServiceItem> topServices,
		List<String> insights
	) {
}