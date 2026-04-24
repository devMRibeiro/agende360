package com.github.devmribeiro.clipply.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.devmribeiro.clipply.application.dto.request.SchedulingHorizonRequest;
import com.github.devmribeiro.clipply.application.model.CompanySettings;
import com.github.devmribeiro.clipply.application.repository.CompanySettingsRepository;
import com.github.devmribeiro.clipply.security.util.SecurityUtils;

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