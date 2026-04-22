package com.github.devmribeiro.clipply.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.devmribeiro.clipply.application.dto.request.CompanySettingsRequest;
import com.github.devmribeiro.clipply.application.dto.response.CompanySettingsResponse;
import com.github.devmribeiro.clipply.application.service.CompanyService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/company")
@PreAuthorize("hasRole('ADMIN')")
public class CompanyController {
	
	private final CompanyService companyService;
	
	public CompanyController(CompanyService companyService) {
		this.companyService = companyService;
	}
	
	@GetMapping("/settings")
	public ResponseEntity<CompanySettingsResponse> getSettings() {
		return ResponseEntity.ok(companyService.getSettings());
	}
	
	@PutMapping("/settings")
	public ResponseEntity<Void> updateSettings(@RequestBody @Valid CompanySettingsRequest request) {
		companyService.updateSettings(request);
		return ResponseEntity.ok().build();
	}
}