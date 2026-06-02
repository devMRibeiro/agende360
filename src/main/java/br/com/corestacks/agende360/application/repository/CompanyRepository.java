package br.com.corestacks.agende360.application.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.corestacks.agende360.application.dto.response.CompanySupportResponse;
import br.com.corestacks.agende360.application.model.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

	@Query("select c from Company c where c.id = :id")
	Company findByCompanyId(UUID id);

	Company findByIdOrSlug(UUID id, String slug);

	boolean existsByDocument(String document);
	
	boolean existsBySlug(String slug);
	
	Company findBySlug(String slug);
	
	@Query("""
		    SELECT new br.com.corestacks.agende360.application.dto.response.CompanySupportResponse(
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
}