package com.github.devmribeiro.clipply.application.dto.response;

import java.util.UUID;

public record ProfessionalResponse(
        UUID id,
        String name
) {}