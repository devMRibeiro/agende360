package com.github.devmribeiro.clipply.application.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "product", indexes = {
		@Index(name = "idx_product_name", columnList = "name")
})
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
}