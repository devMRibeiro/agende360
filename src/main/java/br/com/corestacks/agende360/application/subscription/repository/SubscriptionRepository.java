package br.com.corestacks.agende360.application.subscription.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.corestacks.agende360.application.subscription.model.Subscription;

//@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
	Subscription findByCompanyId(UUID companyId);
	
	Subscription findByStripeSubscriptionId(String stripeSubscriptionId);
}