package com.github.devmribeiro.clipply.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clipply")
public class ClipplyController {

	@PostMapping
	public ResponseEntity<?> registerCompany() {
		return ResponseEntity.ok().build();
	}
	
}