package br.com.corestacks.agende360.application.dto.response;

import java.util.UUID;

public record CustomerResponse(
	    UUID id,
	    String name,
	    String phone
    ) {
}