package br.com.corestacks.agende360.application.subscription.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.corestacks.agende360.application.subscription.type.SubscriptionPlan;
import br.com.corestacks.agende360.application.subscription.type.SubscriptionStatus;

public record SubscriptionCreate(
		UUID companyId,
		SubscriptionPlan plan,
		SubscriptionStatus status,
		LocalDateTime periodStart,
		LocalDateTime periodEnd
	) {
}