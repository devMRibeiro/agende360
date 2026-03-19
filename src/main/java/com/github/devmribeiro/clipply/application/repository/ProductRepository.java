package com.github.devmribeiro.clipply.application.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.devmribeiro.clipply.application.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

	boolean existsByNameAndCompanyId(String name, UUID companyId);
}