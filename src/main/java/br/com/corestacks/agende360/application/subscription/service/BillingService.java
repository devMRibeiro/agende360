package br.com.corestacks.agende360.application.subscription.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import br.com.corestacks.agende360.application.subscription.model.Subscription;
import br.com.corestacks.agende360.application.subscription.repository.SubscriptionRepository;
import br.com.corestacks.agende360.application.subscription.type.SubscriptionPlan;

@Service
public class BillingService {

    @Value("${stripe.price.essential}")
    private String priceEssential;

    @Value("${stripe.price.pro}")
    private String pricePro;

    @Value("${SYSTEM.BASE-URL}")
    private String baseUrl;

    private final SubscriptionRepository subscriptionRepository;

    public BillingService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public String createCheckoutSession(UUID companyId, SubscriptionPlan plan) throws StripeException {

        Subscription subscription = subscriptionRepository.findByCompanyId(companyId);

        String priceId = plan == SubscriptionPlan.PRO ? pricePro : priceEssential;

        SessionCreateParams.Builder params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setSuccessUrl(baseUrl + "/subscription/success?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(baseUrl + "/subscription/cancel")
            .addLineItem(SessionCreateParams.LineItem.builder()
                .setPrice(priceId)
                .setQuantity(1L)
                .build())
            .putMetadata("companyId", companyId.toString());

        // Se já existe customer na Stripe, reutiliza
        if (subscription.getStripCustomerId() != null)
            params.setCustomer(subscription.getStripCustomerId().toString());

        Session session = Session.create(params.build());
        return session.getUrl();
    }
}