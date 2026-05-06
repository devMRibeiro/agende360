package br.com.corestacks.agende360.application.dto.response;

import java.util.UUID;

public record ProfessionalResponse(
        UUID id,
        String name
) {}