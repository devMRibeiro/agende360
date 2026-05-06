package br.com.corestacks.agende360.application.dto.response;

import java.math.BigDecimal;

public record TopServiceItem(
		String name,
		BigDecimal revenue,
		Long count
	) {
}