package br.com.corestacks.agende360.application.service;

import java.time.LocalDateTime;
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
import br.com.corestacks.agende360.messaging.email.service.EmailService;
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
	private final EmailService emailService;
	private final CompanySettingsService companySettingsService;
	private final Cache<String, Company> companiesCache;
	private final Cache<UUID, Map<UUID, User>> usersCache;

	@Value("${SYSTEM.BASE-URL}")
	private String baseUrl;
	
	@Value("${SYSTEM.DEFAULT-PASSWORD}")
	private String defaultPassword;

	public CompanyService(CompanyRepository companyRepository,
						  UserRepository userRepository,
						  PasswordEncoder encoder,
						  EmailService emailService,
						  CompanySettingsRepository companySettingsRepository,
						  CompanySettingsService companySettingsService,
						  Cache<String, Company> companiesCache,
						  Cache<UUID, Map<UUID, User>> usersCache) {
		this.userRepository = userRepository;
		this.companyRepository = companyRepository;
		this.encoder = encoder;
		this.emailService = emailService;
		this.companySettingsRepository = companySettingsRepository;
		this.companySettingsService = companySettingsService;
		this.companiesCache = companiesCache;
		this.usersCache = usersCache;
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

		sendAccessCreatedEmail(user, company);
		
		return new RegisterCompanyResponse(company.getName(), company.getSlug(), user.getEmail(), user.getName());
	}
	
	private void sendAccessCreatedEmail(User user, Company company) {
        Map<String, String> vars = Map.of(
                "COMPANY_NAME", company.getName(),
                "LINK_PUBLICO", baseUrl + "/"+ company.getSlug(),
                "COMPANY_DOCUMENT", company.getDocument(),
                "USER_NAME", user.getName(),
                "USER_EMAIL", user.getEmail(),
                "TEMP_PASSWORD", defaultPassword,
                "YEAR", String.valueOf(LocalDateTime.now().getYear())
        );

        emailService.sendUserAccessEmail(user.getEmail(), vars);
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

	    user.setEmail(request.email());
	    user.setPhone(request.phone());
	}
	
	public Company findByCompanyId(UUID companyId) {
		return companyRepository.findByCompanyId(companyId);
	}
	
	public CompanyPublicResponse getCompanyInfo(String slug) {
		
		Company company = companiesCache.getIfPresent(slug);
		
		if (company == null) {
			LOGGER.info("COMPANY: não encontrada no cache. Consultando no banco.");
			company = companyRepository.findBySlug(slug);

			if (company == null || !company.getActive())
				throw new IllegalArgumentException("Company not found");
			
			companiesCache.put(slug, company);
		}
		
        int horizon = companySettingsService.getShcedulingHorizon(company.getId());
        
        return new CompanyPublicResponse(company.getName(), company.getSlug(), horizon);
	}
}