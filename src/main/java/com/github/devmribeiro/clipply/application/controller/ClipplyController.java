package com.github.devmribeiro.clipply.application.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}