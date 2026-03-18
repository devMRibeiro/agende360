package com.github.devmribeiro.clipply.application.model;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "product",
	indexes = {
			@Index(name = "idx_product_name", columnList = "name")
	},
	uniqueConstraints = {
			@UniqueConstraint(name = "uk_product_name_company", columnNames = {"name", "company_id"})
    }
)
public class Product extends BaseEntity {

	@Column(nullable = false, length = 100)
	private String name;

	private String description;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;
	
	@Column(name = "duration_minutes", nullable = false)
	private Integer durationMinutes;

	@Column(nullable = false)
	private Boolean active = true;
	
	@Column(name = "company_id", nullable = false)
	private UUID companyId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Integer getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(Integer durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public UUID getCompany() {
		return companyId;
	}

	public void setCompany(UUID companyId) {
		this.companyId = companyId;
	}
}