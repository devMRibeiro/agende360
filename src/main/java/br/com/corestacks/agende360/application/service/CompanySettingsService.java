package br.com.corestacks.agende360.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.corestacks.agende360.application.dto.request.SchedulingHorizonRequest;
import br.com.corestacks.agende360.application.model.CompanySettings;
import br.com.corestacks.agende360.application.repository.CompanySettingsRepository;
import br.com.corestacks.agende360.security.util.SecurityUtils;
import jakarta.transaction.Transactional;

@Service
public class CompanySettingsService {

	private final CompanySettingsRepository companySettingsRespository;
	
	public CompanySettingsService(CompanySettingsRepository companySettingsRespository) {
		this.companySettingsRespository = companySettingsRespository;
	}
	
	@Transactional
	public void updateSchedulingHorizon(SchedulingHorizonRequest request) {
		CompanySettings companySettings = companySettingsRespository.findByCompanyId(SecurityUtils.getCompanyId());
		companySettings.setSchedulingHorizon(request.horizon());
	}
	
	public int getShcedulingHorizon(UUID companyId) {
		return companySettingsRespository.findByCompanyId(companyId).getSchedulingHorizon();
	}
}