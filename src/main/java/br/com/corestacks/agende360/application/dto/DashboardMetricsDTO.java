package br.com.corestacks.agende360.application.dto;

import java.math.BigDecimal;

public record DashboardMetricsDTO(
	    BigDecimal expectedRevenue,
	    Long totalAppointments
    ) {
}