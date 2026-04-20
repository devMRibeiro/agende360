package com.github.devmribeiro.clipply.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.devmribeiro.clipply.application.dto.request.RegisterCompanyRequest;
import com.github.devmribeiro.clipply.application.dto.response.CompanySettingsResponse;
import com.github.devmribeiro.clipply.application.dto.response.CompanySupportResponse;
import com.github.devmribeiro.clipply.application.dto.response.RegisterCompanyResponse;
import com.github.devmribeiro.clipply.application.exception.ConflictException;
import com.github.devmribeiro.clipply.application.exception.IllegalArgumentException;
import com.github.devmribeiro.clipply.application.model.Company;
import com.github.devmribeiro.clipply.application.model.User;
import com.github.devmribeiro.clipply.application.repository.CompanyRepository;
import com.github.devmribeiro.clipply.application.repository.UserRepository;
import com.github.devmribeiro.clipply.application.type.UserRole;
import com.github.devmribeiro.clipply.messaging.service.EmailService;
import com.github.devmribeiro.clipply.security.model.UserDetailsImpl;
import com.github.devmribeiro.clipply.security.util.SecurityUtils;

import jakarta.transaction.Transactional;

@Service
public class CompanyService {

	private final CompanyRepository companyRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder encoder;
	private final EmailService emailService;

	@Value("${clipply.base-url}")
	private String baseUrl;
	
	@Value("${clipply.default-password}")
	private String defaultPassword;

	public CompanyService(CompanyRepository companyRepository,
						  UserRepository userRepository,
						  PasswordEncoder encoder,
						  EmailService emailService) {
		this.userRepository = userRepository;
		this.companyRepository = companyRepository;
		this.encoder = encoder;
		this.emailService = emailService;
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
		companyRepository.save(company);
		
		User user = new User();
		user.setName(request.userName());
		user.setEmail(request.email());
		user.setPhone(request.phone());
		user.setPassword(encoder.encode(defaultPassword));
		user.setRole(UserRole.ADMIN);
		user.setCompanyId(company.getId());
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
		
		UserDetailsImpl user = SecurityUtils.getAuthenticatedUser();
		
		if (!user.getRole().equals(UserRole.ADMIN))
			return null;
		
		return companyRepository.getSettings(user.getId());
	}
}