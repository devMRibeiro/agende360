package com.github.devmribeiro.clipply.application.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.devmribeiro.clipply.application.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

	@Query("SELECT p FROM product p WHERE p.id = :id")
	Product findByProductId(UUID id);
	
    boolean existsByNameAndCompanyId(String name, UUID companyId);

    List<Product> findByCompanyId(UUID companyId);
}