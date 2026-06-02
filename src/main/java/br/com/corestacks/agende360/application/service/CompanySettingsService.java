package br.com.corestacks.agende360.application.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;

import br.com.corestacks.agende360.application.dto.request.SchedulingHorizonRequest;
import br.com.corestacks.agende360.application.model.CompanySettings;
import br.com.corestacks.agende360.application.repository.CompanySettingsRepository;
import br.com.corestacks.agende360.security.util.SecurityUtils;
import jakarta.transaction.Transactional;

@Service
public class CompanySettingsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompanySettingsService.class);
	
	private final CompanySettingsRepository companySettingsRespository;
	private final Cache<UUID, CompanySettings> companysSettingsCache;
	
	public CompanySettingsService(
			CompanySettingsRepository companySettingsRespository,
			Cache<UUID, CompanySettings> companysSettingsCache) {
		this.companySettingsRespository = companySettingsRespository;
		this.companysSettingsCache = companysSettingsCache;
	}
	
	@Transactional
	public void updateSchedulingHorizon(SchedulingHorizonRequest request) {
		CompanySettings companySettings = companySettingsRespository.findByCompanyId(SecurityUtils.getCompanyId());
		companySettings.setSchedulingHorizon(request.horizon());
	}
	
	public int getShcedulingHorizon(UUID companyId) {
		
		CompanySettings companySettings = companysSettingsCache.getIfPresent(companyId);
		
		if (companySettings == null) {
			LOGGER.info("COMPANY_SETTINGS: não encontrada no cache. Consultando no banco.");
			companySettings = companySettingsRespository.findByCompanyId(companyId);
			companysSettingsCache.put(companyId, companySettings);
		}
		
		return companySettings.getSchedulingHorizon();
	}
}