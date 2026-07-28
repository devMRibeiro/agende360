package br.com.corestacks.agende360.application.subscription.service;

import java.util.UUID;

import br.com.corestacks.agende360.application.exception.ForbiddenException;
import br.com.corestacks.agende360.application.subscription.util.PlanFeatures;

/**
 * Responsável por:
 * - validar acesso
 * - validar limite
 */
//@Service
public class FeatureGateService {

    private final SubscriptionService subscriptionService;

    public FeatureGateService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }
    
    public void checkAdvancedDashboard(UUID companyId) {
        if (!subscriptionService.getFeatures(companyId).advancedDashboard())
            throw new ForbiddenException("Advanced dashboard is not available for your current plan");
    }
    
    public void checkProfessionalsLimit(UUID companyId, int currentTotal) {

	    PlanFeatures features = subscriptionService.getFeatures(companyId);

	    if (currentTotal >= features.professionals())
	        throw new ForbiddenException("Professionals limit reached");
	}
    
    public void checkServicesLimit(UUID companyId, int currentTotal) {
	    PlanFeatures features = subscriptionService.getFeatures(companyId);

	    if (currentTotal >= features.professionals())
	        throw new ForbiddenException("Services limit reached");
    }

    public void checkIntervalsPerDayLimit(UUID companyId, int currentTotal) {
    	PlanFeatures features = subscriptionService.getFeatures(companyId);
    	
    	if (currentTotal >= features.intervalsPerDay())
    		throw new ForbiddenException("Intervals per day limit reached");
    }
}