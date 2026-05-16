package br.com.corestacks.agende360.application.subscription.model;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.corestacks.agende360.application.model.BaseEntity;
import br.com.corestacks.agende360.application.subscription.type.SubscriptionPlan;
import br.com.corestacks.agende360.application.subscription.type.SubscriptionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Subscription extends BaseEntity {

	@Column(name = "company_id", nullable = false, unique = true)
	private UUID companyId;
	
	@Column(nullable = false)
	private SubscriptionPlan plan;
	
	@Column(nullable = false)
	private SubscriptionStatus status;
	
	@Column(name = "stripe_subscription_id")
	private String stripeSubscriptionId;

	@Column(name = "stripe_customer_id")
	private String stripCustomerId;
	
	@Column(name = "current_period_start")
	private LocalDateTime currentPeriodStart;

	@Column(name = "current_period_end")
	private LocalDateTime currentPeriodEnd;

	@Column(name = "cancel_at_period_end", nullable = false)
	private boolean cancelAtPeriodEnd = false;

	public UUID getCompanyId() {
		return companyId;
	}
	public void setCompanyId(UUID companyId) {
		this.companyId = companyId;
	}
	public SubscriptionPlan getPlan() {
		return plan;
	}
	public void setPlan(SubscriptionPlan plan) {
		this.plan = plan;
	}
	public SubscriptionStatus getStatus() {
		return status;
	}
	public void setStatus(SubscriptionStatus status) {
		this.status = status;
	}
	public String getStripCustomerId() {
		return stripCustomerId;
	}
	public void setStripCustomerId(String stripCustomerId) {
		this.stripCustomerId = stripCustomerId;
	}
	public String getStripeSubscriptionId() {
		return stripeSubscriptionId;
	}
	public void setStripeSubscriptionId(String stripeSubscriptionId) {
		this.stripeSubscriptionId = stripeSubscriptionId;
	}
	public LocalDateTime getCurrentPeriodStart() {
		return currentPeriodStart;
	}
	public void setCurrentPeriodStart(LocalDateTime currentPeriodStart) {
		this.currentPeriodStart = currentPeriodStart;
	}
	public LocalDateTime getCurrentPeriodEnd() {
		return currentPeriodEnd;
	}
	public void setCurrentPeriodEnd(LocalDateTime currentPeriodEnd) {
		this.currentPeriodEnd = currentPeriodEnd;
	}
	public boolean isCancelAtPeriodEnd() {
		return cancelAtPeriodEnd;
	}
	public void setCancelAtPeriodEnd(boolean cancelAtPeriodEnd) {
		this.cancelAtPeriodEnd = cancelAtPeriodEnd;
	}
	
}