package br.com.corestacks.agende360.application.subscription.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.checkout.Session;

import br.com.corestacks.agende360.application.subscription.model.Subscription;
import br.com.corestacks.agende360.application.subscription.repository.SubscriptionRepository;
import br.com.corestacks.agende360.application.subscription.type.SubscriptionStatus;
import jakarta.transaction.Transactional;

@Service
public class StripeWebhookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StripeWebhookService.class);

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final SubscriptionRepository subscriptionRepository;
    private final StripeClient stripeClient;

    public StripeWebhookService(
            SubscriptionRepository subscriptionRepository,
            StripeClient stripeClient) {
        this.subscriptionRepository = subscriptionRepository;
        this.stripeClient = stripeClient;
    }

    @Transactional
    public void handleEvent(String payload, String sigHeader) throws StripeException {

        Event event = stripeClient.constructEvent(payload, sigHeader, webhookSecret);
        String type = event.getType();

        LOGGER.info("[WEBHOOK] event received - type={}", type);

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

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;

        try {
            stripeObject = deserializer.deserializeUnsafe();
        } catch (Exception e) {
            LOGGER.error("[WEBHOOK] checkout.session.completed - deserialize failed: {}", e.getMessage());
            return;
        }

        Session session = (Session) stripeObject;

        if (session == null) {
            LOGGER.warn("[WEBHOOK] checkout.session.completed - session is null");
            return;
        }

        String companyIdStr = session.getMetadata().get("companyId");
        LOGGER.info("[WEBHOOK] checkout.session.completed - companyId={}", companyIdStr);

        UUID companyId = UUID.fromString(companyIdStr);
        Subscription subscription = subscriptionRepository.findByCompanyId(companyId);

        if (subscription == null) {
            LOGGER.warn("[WEBHOOK] subscription not found - companyId={}", companyId);
            return;
        }

        subscription.setStripCustomerId(session.getCustomer());
        subscription.setStripeSubscriptionId(session.getSubscription());
        subscriptionRepository.save(subscription);

        LOGGER.info("[WEBHOOK] subscription updated - companyId={}", companyId);
    }

    private void handleSubscriptionUpdated(Event event) {

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;

        try {
            stripeObject = deserializer.deserializeUnsafe();
        } catch (Exception e) {
            LOGGER.error("[WEBHOOK] customer.subscription.updated - deserialize failed: {}", e.getMessage());
            return;
        }

        com.stripe.model.Subscription stripeSub = (com.stripe.model.Subscription) stripeObject;

        if (stripeSub == null) {
            LOGGER.warn("[WEBHOOK] customer.subscription.updated - stripeSub is null");
            return;
        }

        Subscription subscription = subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId());

        if (subscription == null) {
            LOGGER.warn("[WEBHOOK] subscription not found - stripeSubId={}", stripeSub.getId());
            return;
        }

        subscription.setStatus(mapStatus(stripeSub.getStatus()));

        // currentPeriodStart/End agora ficam no SubscriptionItem (API 2025-03-31+)
        if (stripeSub.getItems() != null && stripeSub.getItems().getData() != null && !stripeSub.getItems().getData().isEmpty()) {

            SubscriptionItem item = stripeSub.getItems().getData().get(0);
            subscription.setCurrentPeriodStart(LocalDateTime.ofEpochSecond(item.getCurrentPeriodStart(), 0, ZoneOffset.UTC));
            subscription.setCurrentPeriodEnd(LocalDateTime.ofEpochSecond(item.getCurrentPeriodEnd(), 0, ZoneOffset.UTC));
        }

        subscriptionRepository.save(subscription);

        LOGGER.info("[WEBHOOK] subscription updated - stripeSubId={} status={}", stripeSub.getId(), stripeSub.getStatus());
    }

    private void handleSubscriptionDeleted(Event event) {

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;

        try {
            stripeObject = deserializer.deserializeUnsafe();
        } catch (Exception e) {
            LOGGER.error("[WEBHOOK] customer.subscription.deleted - deserialize failed: {}", e.getMessage());
            return;
        }

        com.stripe.model.Subscription stripeSub = (com.stripe.model.Subscription) stripeObject;

        if (stripeSub == null) {
            LOGGER.warn("[WEBHOOK] customer.subscription.deleted - stripeSub is null");
            return;
        }

        Subscription subscription = subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId());

        if (subscription == null) {
            LOGGER.warn("[WEBHOOK] subscription not found - stripeSubId={}", stripeSub.getId());
            return;
        }

        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscriptionRepository.save(subscription);

        LOGGER.info("[WEBHOOK] subscription canceled - stripeSubId={}", stripeSub.getId());
    }

    private void handlePaymentFailed(Event event) {

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;

        try {
            stripeObject = deserializer.deserializeUnsafe();
        } catch (Exception e) {
            LOGGER.error("[WEBHOOK] invoice.payment_failed - deserialize failed: {}", e.getMessage());
            return;
        }

        Invoice invoice = (Invoice) stripeObject;

        if (invoice == null) {
            LOGGER.warn("[WEBHOOK] invoice.payment_failed - invoice is null");
            return;
        }

        // Na v32+ o subscriptionId está em invoice.getParent().getSubscriptionDetails()
        String stripeSubId = null;

        if (invoice.getParent() != null && invoice.getParent().getSubscriptionDetails() != null)
            stripeSubId = invoice.getParent().getSubscriptionDetails().getSubscription();

        if (stripeSubId == null) {
            LOGGER.warn("[WEBHOOK] invoice.payment_failed - no subscriptionId found");
            return;
        }

        Subscription subscription = subscriptionRepository.findByStripeSubscriptionId(stripeSubId);

        if (subscription == null) {
            LOGGER.warn("[WEBHOOK] subscription not found - stripeSubId={}", stripeSubId);
            return;
        }

        subscription.setStatus(SubscriptionStatus.PAST_DUE);
        subscriptionRepository.save(subscription);

        LOGGER.info("[WEBHOOK] subscription past_due - stripeSubId={}", stripeSubId);
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