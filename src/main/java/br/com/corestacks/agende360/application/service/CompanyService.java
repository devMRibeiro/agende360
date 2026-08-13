package br.com.corestacks.agende360.application.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;

import br.com.corestacks.agende360.application.dto.request.CompanySettingsRequest;
import br.com.corestacks.agende360.application.dto.request.RegisterCompanyRequest;
import br.com.corestacks.agende360.application.dto.response.CompanyPublicResponse;
import br.com.corestacks.agende360.application.dto.response.CompanySettingsResponse;
import br.com.corestacks.agende360.application.dto.response.CompanySupportResponse;
import br.com.corestacks.agende360.application.dto.response.RegisterCompanyResponse;
import br.com.corestacks.agende360.application.exception.ConflictException;
import br.com.corestacks.agende360.application.exception.IllegalArgumentException;
import br.com.corestacks.agende360.application.model.Company;
import br.com.corestacks.agende360.application.model.CompanySettings;
import br.com.corestacks.agende360.application.model.User;
import br.com.corestacks.agende360.application.repository.CompanyRepository;
import br.com.corestacks.agende360.application.repository.CompanySettingsRepository;
import br.com.corestacks.agende360.application.repository.UserRepository;
import br.com.corestacks.agende360.application.type.SchedulingHorizon;
import br.com.corestacks.agende360.application.type.UserRole;
import br.com.corestacks.agende360.application.util.BaseUrlUtils;
import br.com.corestacks.agende360.messaging.email.dto.CompanyRegistrationEmail;
import br.com.corestacks.agende360.outbox.enums.AggregateType;
import br.com.corestacks.agende360.outbox.enums.OutboxEventType;
import br.com.corestacks.agende360.outbox.factory.OutboxEventFactory;
import br.com.corestacks.agende360.outbox.service.OutboxEventService;
import br.com.corestacks.agende360.security.model.UserDetailsImpl;
import br.com.corestacks.agende360.security.util.SecurityUtils;
import jakarta.transaction.Transactional;

@Service
public class CompanyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompanyService.class);
	
	private final CompanyRepository companyRepository;
	private final CompanySettingsRepository companySettingsRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder encoder;
	private final CompanySettingsService companySettingsService;
	private final Cache<UUID, Company> companiesCache;
	private final Cache<String, UUID> companyIdsBySlugCache;
	private final Cache<UUID, Map<UUID, User>> usersCache;
	
	private final OutboxEventService outboxEventService;
	private final OutboxEventFactory outboxEventFactory; 

	@Value("${SYSTEM.DEFAULT-PASSWORD}")
	private String defaultPassword;

	public CompanyService(CompanyRepository companyRepository,
						  UserRepository userRepository,
						  PasswordEncoder encoder,
						  CompanySettingsRepository companySettingsRepository,
						  CompanySettingsService companySettingsService,
						  Cache<UUID, Company> companiesCache,
						  Cache<UUID, Map<UUID, User>> usersCache,
						  Cache<String, UUID> companyIdsBySlugCache,
						  OutboxEventFactory outboxEventFactory,
						  OutboxEventService outboxEventService) {
		this.userRepository = userRepository;
		this.companyRepository = companyRepository;
		this.encoder = encoder;
		this.companySettingsRepository = companySettingsRepository;
		this.companySettingsService = companySettingsService;
		this.companiesCache = companiesCache;
		this.companyIdsBySlugCache = companyIdsBySlugCache;
		this.usersCache = usersCache;
		this.outboxEventService = outboxEventService;
		this.outboxEventFactory = outboxEventFactory;
	}

	@Transactional
	public RegisterCompanyResponse registerCompany(RegisterCompanyRequest request) {

		if (companyRepository.existsByDocument(request.document()))
			throw new ConflictException("company already exists");

		if (userRepository.existsByEmail(request.email()))
			throw new ConflictException("email already exists");

		Company company = new Company();
		company.setName(request.companyName());
		company.setDocument(request.document());
		company.setSlug(genSlug(request.companyName()));
		company.setEndereco(request.endereco());
		companyRepository.save(company);

		CompanySettings companySettings = new CompanySettings();
		companySettings.setCompanyId(company.getId());
		companySettings.setSchedulingHorizon(SchedulingHorizon.SEM_LIMITE.getValue());
		companySettingsRepository.save(companySettings);
		
		User user = new User();
		user.setName(request.userName());
		user.setEmail(request.email());
		user.setPhone(request.phone());
		user.setPassword(encoder.encode(defaultPassword));
		user.setRole(UserRole.ADMIN);
		user.setCompanyId(company.getId());
		user.setIsProfessional(true);
		userRepository.save(user);

		outboxEventService.saveAll(List.of(outboxEventFactory.create(AggregateType.COMPANY, company.getId(), OutboxEventType.COMPANY_REGISTRATION_EMAIL, buildCompanyRegistrationEmailPayload(company, user), null)));
		
		return new RegisterCompanyResponse(company.getName(), company.getSlug(), user.getEmail(), user.getName());
	}
	
	private CompanyRegistrationEmail buildCompanyRegistrationEmailPayload(Company company, User user) {
		return new CompanyRegistrationEmail(
						company.getName(),
						BaseUrlUtils.BASE_URL_APPOINTMENT + "/"+ company.getSlug(),
						company.getDocument(),
		                user.getName(),
		                user.getEmail(),
		                defaultPassword
		);
	}
	
	private String genSlug(String companyName) {
		String baseSlug = companyName.toLowerCase().replaceAll("[^a-z0-9\\s-]", "").replaceAll("\\s+", "-");
		String newSlug = baseSlug;

		int counter = 2;
		while (companyRepository.existsBySlug(newSlug))
			newSlug = baseSlug + "-" + counter++;

		return newSlug;
	}

	public void disable(String slug) {

		Company company = companyRepository.findBySlug(slug);

		if (company == null)
			throw new IllegalArgumentException("company not found");

		company.setActive(false);
		companyRepository.save(company);
	}
	
	public List<CompanySupportResponse> list() {
	    return companyRepository.findAllWithUser();
	}
	
	public CompanySettingsResponse getSettings() {
		
		UserDetailsImpl userDetails = SecurityUtils.getAuthenticatedUser();
		
		if (!userDetails.getRole().equals(UserRole.ADMIN))
			return null;
		
		User user = usersCache.getIfPresent(userDetails.getCompanyId()).get(userDetails.getId());

		return new CompanySettingsResponse(user.getName(), user.getPhone(), user.getEmail());
	}

	@Transactional
	public void updateSettings(CompanySettingsRequest request) {

	    Company company = companyRepository.findByCompanyId(SecurityUtils.getCompanyId());

	    if (company == null)
	        throw new IllegalArgumentException("User does not belong to a company");

	    company.setName(request.companyName());

	    User user = userRepository.findByUserId(SecurityUtils.getAuthenticatedUser().getId());

	    user.setPhone(request.phone());

	    invalidateCompaniesCache(company);
	    usersCache.invalidate(user.getId());
	}
	
	public Company findByCompanyId(UUID companyId) {
		
		Company company = companiesCache.getIfPresent(companyId);
    	
    	if (company == null) {
    		LOGGER.info("COMPANY: não encontrada no cache. Consultando no banco.");
	    	company = companyRepository.findByCompanyId(companyId);

	    	if (company == null || !company.getActive())
	    		throw new IllegalArgumentException("Company not found");

	    	companiesCache.put(companyId, company);
	    	companyIdsBySlugCache.put(company.getSlug(), companyId);
    	}

    	return company;
	}
	
	public Company findByCompanySlug(String slug) {
		
		UUID companyId = companyIdsBySlugCache.getIfPresent(slug);
		
		Company company = companyId != null ? findByCompanyId(companyId) : null;
    	
    	if (company == null) {
    		LOGGER.info("COMPANY: não encontrada no cache. Consultando no banco.");
	    	company = companyRepository.findBySlug(slug);

	    	if (company == null || !company.getActive())
	    		throw new IllegalArgumentException("Company not found");
	    	
	    	companiesCache.put(company.getId(), company);
	    	companyIdsBySlugCache.put(company.getSlug(), company.getId());
    	}
		
		return company;
	}
	
	public CompanyPublicResponse getCompanyInfo(String slug) {
		
		UUID companyId = companyIdsBySlugCache.getIfPresent(slug);
		
		Company company = companyId != null ? companiesCache.getIfPresent(companyId) : null;
		
		if (company == null) {
			LOGGER.info("COMPANY: não encontrada no cache. Consultando no banco.");
			company = companyRepository.findBySlug(slug);

			if (company == null || !company.getActive())
				throw new IllegalArgumentException("Company not found");
			
			companiesCache.put(companyId, company);
		}
		
        int horizon = companySettingsService.getShcedulingHorizon(company.getId());
        
        return new CompanyPublicResponse(company.getName(), company.getSlug(), horizon);
	}
	
	private void invalidateCompaniesCache(Company company) {
	    companiesCache.invalidate(company.getId());
	    companyIdsBySlugCache.invalidate(company.getSlug());
	}
}