package br.com.corestacks.agende360.application.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.corestacks.agende360.application.model.CompanySettings;

@Repository
public interface CompanySettingsRepository extends JpaRepository<CompanySettings, UUID> {

	CompanySettings findByCompanyId(UUID companyId);
	
}