package com.github.devmribeiro.clipply.application.dto;

import java.math.BigDecimal;

public record DashboardMetricsDTO(
	    BigDecimal expectedRevenue,
	    Long totalAppointments
    ) {
}