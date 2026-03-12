package com.github.devmribeiro.clipply.application.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_products")
public class UserProduct {

	UserProductId id;

	public UserProductId getId() {
		return id;
	}

	public void setId(UserProductId id) {
		this.id = id;
	}
}