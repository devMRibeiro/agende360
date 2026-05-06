package br.com.corestacks.agende360.application.dto.response;

import java.math.BigDecimal;

public record TrendPoint(
		String label,
		BigDecimal value
	) {
}