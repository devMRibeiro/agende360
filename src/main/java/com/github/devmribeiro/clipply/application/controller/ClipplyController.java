package com.github.devmribeiro.clipply.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.devmribeiro.clipply.application.dto.request.RegisterCompanyRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clipply")
public class ClipplyController {

	@PostMapping
	public ResponseEntity<?> registerCompany(@RequestBody @Valid RegisterCompanyRequest request) {
		return ResponseEntity.ok().build();
	}
}