package com.github.devmribeiro.clipply.application.model;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Embeddable;

@Embeddable
public class UserProductId implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UUID userId;
	private UUID productId;

	public UUID getUserId() {
		return userId;
	}
	public void setUserId(UUID userId) {
		this.userId = userId;
	}
	public UUID getProductId() {
		return productId;
	}
	public void setProductId(UUID productId) {
		this.productId = productId;
	}
}