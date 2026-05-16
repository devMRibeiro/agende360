package br.com.corestacks.agende360.application.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.corestacks.agende360.application.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

	@Query("SELECT p FROM Product p WHERE p.id = :id")
	Product findByProductId(UUID id);
	
    boolean existsByNameAndCompanyId(String name, UUID companyId);

    @Query("""
    	    SELECT p
    	    FROM Product p
    	    WHERE p.companyId = :companyId
    	    AND (:active IS NULL OR p.active = :active)
    	""")
	List<Product> findByCompanyIdAndActive(@Param("companyId") UUID companyId, @Param("active") Boolean active);
}