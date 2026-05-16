package br.com.corestacks.agende360.application.subscription.util;

import java.util.Map;

import br.com.corestacks.agende360.application.subscription.type.NotificationType;
import br.com.corestacks.agende360.application.subscription.type.SubscriptionPlan;

public final class PlanCatalog {

    private static final Map<SubscriptionPlan, PlanFeatures> PLANS =
        Map.of(

            SubscriptionPlan.TRIAL,
            new PlanFeatures(
                3,
                10,
                4,
                NotificationType.EMAIL,
                false,
                false
            ),

            SubscriptionPlan.ESSENTIAL,
            new PlanFeatures(
                3,
                10,
                4,
                NotificationType.EMAIL_SMS,
                false,
                false
            ),

            SubscriptionPlan.PRO,
            new PlanFeatures(
                7,
                20,
                9,
                NotificationType.EMAIL_SMS_WHATSAPP,
                true,
                true
            )
        );

    public static PlanFeatures get(SubscriptionPlan plan) {
        return PLANS.get(plan);
    }
    
    private PlanCatalog() { }
}