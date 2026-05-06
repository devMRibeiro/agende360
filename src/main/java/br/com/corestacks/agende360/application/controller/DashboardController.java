package br.com.corestacks.agende360.application.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.corestacks.agende360.application.dto.response.DashboardMetricsResponse;
import br.com.corestacks.agende360.application.service.DashboardService;
import br.com.corestacks.agende360.application.type.PeriodFilter;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

	private final DashboardService dashboardService;
	
	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}
	
	@PostMapping("/metrics")
	public ResponseEntity<DashboardMetricsResponse> getMetrics(@RequestBody Map<String, PeriodFilter> request) {
		return ResponseEntity.ok(dashboardService.getMetrics(request.get("period")));
	}
}