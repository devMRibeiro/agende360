package br.com.corestacks.agende360.application.subscription.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import br.com.corestacks.agende360.application.subscription.model.Subscription;
import br.com.corestacks.agende360.application.subscription.repository.SubscriptionRepository;
import br.com.corestacks.agende360.application.subscription.type.SubscriptionStatus;

@Service
public class StripeWebhookService {

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final SubscriptionRepository subscriptionRepository;

    public StripeWebhookService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public void handleEvent(String payload, String sigHeader) throws StripeException {

        Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        String type = event.getType();

        if ("checkout.session.completed".equals(type)) {
            handleCheckoutCompleted(event);
        } else if ("customer.subscription.updated".equals(type)) {
            handleSubscriptionUpdated(event);
        } else if ("customer.subscription.deleted".equals(type)) {
            handleSubscriptionDeleted(event);
        } else if ("invoice.payment_failed".equals(type)) {
            handlePaymentFailed(event);
        }
    }

    private void handleCheckoutCompleted(Event event) {
        // Extrai a session do evento
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

        if (session == null)
        	return;

        UUID companyId = UUID.fromString(session.getMetadata().get("companyId"));
        Subscription subscription = subscriptionRepository.findByCompanyId(companyId);

        subscription.setStripCustomerId(session.getCustomer());
        subscription.setStripeSubscriptionId(session.getSubscription());
        subscriptionRepository.save(subscription);
    }

    private void handleSubscriptionUpdated(Event event) {
        com.stripe.model.Subscription stripeSub = (com.stripe.model.Subscription) event.getDataObjectDeserializer().getObject().orElse(null);

        if (stripeSub == null)
        	return;

        Subscription subscription = subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId());

        if (subscription == null)
        	return;

        subscription.setStatus(mapStatus(stripeSub.getStatus()));
        subscription.setCurrentPeriodStart(LocalDateTime.ofEpochSecond(stripeSub.getCurrentPeriodStart(), 0, ZoneOffset.UTC));
        subscription.setCurrentPeriodEnd(LocalDateTime.ofEpochSecond(stripeSub.getCurrentPeriodEnd(), 0, ZoneOffset.UTC));
        subscriptionRepository.save(subscription);
    }

    private void handleSubscriptionDeleted(Event event) {
        com.stripe.model.Subscription stripeSub = (com.stripe.model.Subscription) event.getDataObjectDeserializer().getObject().orElse(null);

        if (stripeSub == null)
        	return;

        Subscription subscription = subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId());

        if (subscription == null) return;

        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscriptionRepository.save(subscription);
    }

    private void handlePaymentFailed(Event event) {
        Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);

        if (invoice == null || invoice.getSubscription() == null)
        	return;

        Subscription subscription = subscriptionRepository.findByStripeSubscriptionId(invoice.getSubscription());

        if (subscription == null)
        	return;

        subscription.setStatus(SubscriptionStatus.PAST_DUE);
        subscriptionRepository.save(subscription);
    }

    private SubscriptionStatus mapStatus(String stripeStatus) {
        
    	if ("active".equals(stripeStatus))
        	return SubscriptionStatus.ACTIVE;
        
        if ("trialing".equals(stripeStatus))
        	return SubscriptionStatus.TRIALING;

        if ("past_due".equals(stripeStatus))
        	return SubscriptionStatus.PAST_DUE;
        
        if ("canceled".equals(stripeStatus))
        	return SubscriptionStatus.CANCELED;
        
        return SubscriptionStatus.EXPIRED;
    }
}