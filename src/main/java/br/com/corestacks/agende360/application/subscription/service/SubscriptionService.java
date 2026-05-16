package br.com.corestacks.agende360.application.subscription.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.corestacks.agende360.application.exception.IllegalArgumentException;
import br.com.corestacks.agende360.application.subscription.dto.request.SubscriptionCreate;
import br.com.corestacks.agende360.application.subscription.dto.response.SubscriptionResponse;
import br.com.corestacks.agende360.application.subscription.model.Subscription;
import br.com.corestacks.agende360.application.subscription.repository.SubscriptionRepository;
import br.com.corestacks.agende360.application.subscription.util.PlanCatalog;
import br.com.corestacks.agende360.application.subscription.util.PlanFeatures;

/**
 * Responsável por:
 * - buscar assinatura 
 * - verificar status
 * - retornar features do plano
 */
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription getByCompanyId(UUID companyId) {

    	Subscription subscription = subscriptionRepository.findByCompanyId(companyId);

    	if (subscription == null)
    		throw new IllegalArgumentException("Subscription not found");

    	return subscription;
    }

    public PlanFeatures getFeatures(UUID companyId) {
        return PlanCatalog.get(getByCompanyId(companyId).getPlan());
    }
    
    public void createSubscription(SubscriptionCreate request) {
		Subscription subscription = new Subscription();
		subscription.setCompanyId(request.companyId());
		subscription.setPlan(request.plan());
		subscription.setStatus(request.status());
		subscription.setCurrentPeriodStart(request.periodStart());
		subscription.setCurrentPeriodEnd(request.periodEnd());
		subscriptionRepository.save(subscription);
	}
    
    public SubscriptionResponse me(UUID companyId) {
    	Subscription subscription = getByCompanyId(companyId);
    	PlanFeatures planFeatures = getFeatures(companyId);
    	return new SubscriptionResponse(
    			subscription.getPlan(),
    			subscription.getStatus(),
    			subscription.getCurrentPeriodEnd(),
    			planFeatures);
    }
}