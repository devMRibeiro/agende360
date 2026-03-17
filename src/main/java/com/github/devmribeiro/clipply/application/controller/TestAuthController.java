package com.github.devmribeiro.clipply.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestAuthController {

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> test() {
		return ResponseEntity.ok("Entrou");
	}
}