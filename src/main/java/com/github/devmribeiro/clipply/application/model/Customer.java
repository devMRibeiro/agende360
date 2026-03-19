package com.github.devmribeiro.clipply.application.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer", indexes = {
		@Index(name = "idx_customer_phone", columnList = "phone"),
		@Index(name = "idx_customer_email", columnList = "email", unique = true)
})
public class Customer extends BaseEntity {

	@Column(nullable = false)
	private String phone;

	@Column(nullable = false, unique = true)
	private String email;
	
	@Column(name = "company_id", nullable = false)
	private UUID companyId;

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UUID getCompanyId() {
		return companyId;
	}

	public void setCompanyId(UUID companyId) {
		this.companyId = companyId;
	}
}