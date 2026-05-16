package br.com.corestacks.agende360.application.subscription.dto.response;

import java.time.LocalDateTime;

import br.com.corestacks.agende360.application.subscription.type.SubscriptionPlan;
import br.com.corestacks.agende360.application.subscription.type.SubscriptionStatus;
import br.com.corestacks.agende360.application.subscription.util.PlanFeatures;

public record SubscriptionResponse(
		SubscriptionPlan plan,
		SubscriptionStatus status,
		LocalDateTime currentPeriodEnd,
		PlanFeatures features
	) {
}