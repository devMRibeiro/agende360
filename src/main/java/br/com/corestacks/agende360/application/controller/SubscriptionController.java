package br.com.corestacks.agende360.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.corestacks.agende360.application.subscription.dto.response.SubscriptionResponse;
import br.com.corestacks.agende360.application.subscription.service.SubscriptionService;
import br.com.corestacks.agende360.security.util.SecurityUtils;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/subscription")
public class SubscriptionController {

	private final SubscriptionService subscriptionService;
	
	public SubscriptionController(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}
	
	@GetMapping("/me")
	public ResponseEntity<SubscriptionResponse> me() {
		return ResponseEntity.ok(subscriptionService.me(SecurityUtils.getCompanyId()));
	}
}