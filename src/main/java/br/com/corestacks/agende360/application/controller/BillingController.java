package br.com.corestacks.agende360.application.controller;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.StripeException;

import br.com.corestacks.agende360.application.subscription.service.BillingService;
import br.com.corestacks.agende360.application.subscription.service.StripeWebhookService;
import br.com.corestacks.agende360.application.subscription.type.SubscriptionPlan;
import br.com.corestacks.agende360.security.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billingService;
    private final StripeWebhookService stripeWebhookService;

    public BillingController(BillingService billingService,
                             StripeWebhookService stripeWebhookService) {
        this.billingService = billingService;
        this.stripeWebhookService = stripeWebhookService;
    }

    // Autenticado — Admin cria a sessão de checkout
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> checkout(@RequestBody Map<String, String> body) throws StripeException {
        UUID companyId = SecurityUtils.getCompanyId();
        SubscriptionPlan plan = SubscriptionPlan.valueOf(body.get("plan"));
        String url = billingService.createCheckoutSession(companyId, plan);
        return ResponseEntity.ok(Map.of("url", url));
    }

    // Público — Stripe chama este endpoint
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(HttpServletRequest request, @RequestHeader("Stripe-Signature") String sigHeader) throws Exception {
        String payload = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        stripeWebhookService.handleEvent(payload, sigHeader);
        return ResponseEntity.ok().build();
    }
}