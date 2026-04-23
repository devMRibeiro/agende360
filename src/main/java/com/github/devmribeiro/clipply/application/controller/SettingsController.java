package com.github.devmribeiro.clipply.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.devmribeiro.clipply.application.dto.request.CompanySettingsRequest;
import com.github.devmribeiro.clipply.application.dto.request.SchedulingHorizonRequest;
import com.github.devmribeiro.clipply.application.dto.response.CompanySettingsResponse;
import com.github.devmribeiro.clipply.application.service.CompanyService;
import com.github.devmribeiro.clipply.application.service.CompanySettingsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/settings")
@PreAuthorize("hasRole('ADMIN')")
public class SettingsController {
	
	private final CompanySettingsService companySettingsService;
	private final CompanyService companyService;
	
	public SettingsController(
			CompanyService companyService,
			CompanySettingsService companySettingsService) {
		this.companySettingsService = companySettingsService;
		this.companyService = companyService;
	}
	
	@GetMapping
	public ResponseEntity<CompanySettingsResponse> getSettings() {
		return ResponseEntity.ok(companyService.getSettings());
	}
	
	@PutMapping
	public ResponseEntity<Void> updateSettings(@RequestBody @Valid CompanySettingsRequest request) {
		companyService.updateSettings(request);
		return ResponseEntity.ok().build();
	}
	
	@PutMapping("/scheduling-horizon")
	public ResponseEntity<Void> updateSchedulingHorizon(@RequestBody @Valid SchedulingHorizonRequest request) {
		companySettingsService.updateSchedulingHorizon(request);
		return ResponseEntity.ok().build();
	}
}