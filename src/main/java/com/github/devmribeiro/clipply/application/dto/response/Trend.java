package com.github.devmribeiro.clipply.application.dto.response;

import java.math.BigDecimal;

public record Trend(
		BigDecimal value,
		boolean isPositive
	) {
}