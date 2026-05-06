package br.com.corestacks.agende360.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
	    UUID id,
	    String name,
	    String description,
	    BigDecimal price,
	    Integer durationMinutes,
	    Boolean active
    ) {
}