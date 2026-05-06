package br.com.corestacks.agende360.application.exception;

import java.time.LocalDateTime;

public record ErrorResponse(int status, String message, LocalDateTime timestamp) {
	public ErrorResponse (int status, String message) {
		this(status, message, LocalDateTime.now());
	}
}