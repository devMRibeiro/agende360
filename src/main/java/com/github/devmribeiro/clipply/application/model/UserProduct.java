package com.github.devmribeiro.clipply.application.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users_products")
public class UserProduct {

	@EmbeddedId
	private UserProductId id;

	public UserProductId getId() {
		return id;
	}

	public void setId(UserProductId id) {
		this.id = id;
	}
}