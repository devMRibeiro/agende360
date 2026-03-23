package com.github.devmribeiro.clipply.application.dto.response;

import java.util.UUID;

public record CustomerResponse(
	    UUID id,
	    String name,
	    String phone
    ) {
}