package br.com.corestacks.agende360.application.dto.response;

import java.math.BigDecimal;

public record RankingItemResponse(
	    String name,
	    BigDecimal value,
	    Long count
	) {
}