package br.com.corestacks.agende360.application.dto.response;

import java.time.LocalDateTime;

public record DateRange(LocalDateTime start, LocalDateTime end) { }