package com.github.devmribeiro.clipply.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.devmribeiro.clipply.application.dto.response.DashboardMetricsResponse;
import com.github.devmribeiro.clipply.application.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

	private final DashboardService dashboardService;
	
	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}
	
	@GetMapping("/metrics")
	public ResponseEntity<DashboardMetricsResponse> getMetrics() {
		return ResponseEntity.ok(dashboardService.getMetrics());
	}
}