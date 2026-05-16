package br.com.corestacks.agende360.application.subscription.util;

import br.com.corestacks.agende360.application.subscription.type.NotificationType;

public record PlanFeatures(
	    int professionals,
	    int services,
	    int intervalsPerDay,
	    NotificationType notificationType,
	    boolean advancedDashboard,
	    boolean customPage
	) {
}