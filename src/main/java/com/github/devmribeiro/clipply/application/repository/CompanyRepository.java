package com.github.devmribeiro.clipply.application.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.devmribeiro.clipply.application.model.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

	@Query("select c from company c where c.id = :id")
	Company findByCompanyId(UUID id);

	boolean existsByDocument(String document);
	
	boolean existsBySlug(String slug);
}