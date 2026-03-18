package com.github.devmribeiro.clipply.application.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.devmribeiro.clipply.application.dto.request.RegisterCompanyRequest;
import com.github.devmribeiro.clipply.application.dto.response.RegisterCompanyResponse;
import com.github.devmribeiro.clipply.application.service.CompanyService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clipply")
@PreAuthorize("hasRole('SUPPORT')")
public class ClipplyController {

	private final CompanyService companyService;

	public ClipplyController(CompanyService companyService) {
		this.companyService = companyService;
	}

	@PostMapping("/company")
	public ResponseEntity<RegisterCompanyResponse> registerCompany(@RequestBody @Valid RegisterCompanyRequest request) {
		return ResponseEntity
				.status(HttpStatus.CREATED.value())
				.body(companyService.registerCompany(request));
	}
	
	@PatchMapping("/company/{companyId}/disable")
	public ResponseEntity<Void> disableCompany(@PathVariable("companyId") UUID companyId) {
		companyService.disable(companyId);
		return ResponseEntity.ok().build();
	}
}