package com.github.devmribeiro.clipply.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.devmribeiro.clipply.application.dto.response.CompanySettingsResponse;
import com.github.devmribeiro.clipply.application.service.CompanyService;

@RestController
@RequestMapping("/api/company")
public class CompanyController {
	
	private final CompanyService companyService;
	
	public CompanyController(CompanyService companyService) {
		this.companyService = companyService;
	}
	
	@GetMapping("/settings")
	public ResponseEntity<CompanySettingsResponse> getSettings() {
		return ResponseEntity.ok(companyService.getSettings());
	}
}