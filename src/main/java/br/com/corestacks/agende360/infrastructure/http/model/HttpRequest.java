package br.com.corestacks.agende360.infrastructure.http.model;

import java.util.Map;

/**
 * Representa uma requisição http
 * 
 * @author Michael Ribeiro
 */
public record HttpRequest(
		String url,
		Map<String, String> headers,
		Object body
	) {
}