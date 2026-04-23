package com.github.devmribeiro.clipply.application.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.devmribeiro.clipply.application.dto.response.CompanySettingsResponse;
import com.github.devmribeiro.clipply.application.dto.response.CompanySupportResponse;
import com.github.devmribeiro.clipply.application.model.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

	@Query("select c from Company c where c.id = :id")
	Company findByCompanyId(UUID id);

	boolean existsByDocument(String document);
	
	boolean existsBySlug(String slug);
	
	Company findBySlug(String slug);
	
	@Query("""
		    SELECT new com.github.devmribeiro.clipply.application.dto.response.CompanySupportResponse(
		        c.name,
		        c.slug,
		        c.document,
		        c.active,
		        u.name,
		        u.phone,
		        u.email
		    )
		    FROM Company c
		    JOIN User u ON u.companyId = c.id
		    WHERE u.role = 'ADMIN'
		""")
	List<CompanySupportResponse> findAllWithUser();
	
	@Query("""
			SELECT new com.github.devmribeiro.clipply.application.dto.response.CompanySettingsResponse(
				c.name,
				u.phone,
				u.email
			)
			FROM User u
			INNER JOIN Company c ON c.id = u.companyId
			WHERE 
			u.id = :userId
		""")
	CompanySettingsResponse getSettings(UUID userId);
}