package com.github.devmribeiro.clipply.application.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.devmribeiro.clipply.application.dto.request.RegisterCompanyRequest;
import com.github.devmribeiro.clipply.application.dto.response.RegisterCompanyResponse;
import com.github.devmribeiro.clipply.application.exception.ConflictException;
import com.github.devmribeiro.clipply.application.exception.IllegalArgumentException;
import com.github.devmribeiro.clipply.application.model.Company;
import com.github.devmribeiro.clipply.application.model.User;
import com.github.devmribeiro.clipply.application.repository.CompanyRepository;
import com.github.devmribeiro.clipply.application.repository.UserRepository;
import com.github.devmribeiro.clipply.application.type.UserRole;

import jakarta.transaction.Transactional;

@Service
public class CompanyService {

	private final CompanyRepository companyRepository;
	private final UserRepository userRepository;

	private final PasswordEncoder encoder;

	@Value("${clipply.default-password}")
	private String defaultPassword;

	public CompanyService(CompanyRepository companyRepository,
						  UserRepository userRepository,
						  PasswordEncoder encoder) {
		this.userRepository = userRepository;
		this.companyRepository = companyRepository;
		this.encoder = encoder;
	}

	@Transactional
	public RegisterCompanyResponse registerCompany(RegisterCompanyRequest request) {

		if (companyRepository.existsByDocument(request.document()))
			throw new ConflictException("company already exists");

		User userExisting = userRepository.findByEmail(request.email());

		if (userExisting != null && request.email().equals(userExisting.getEmail()))
			throw new ConflictException("email already exists");

		Company company = new Company();
		company.setName(request.name());
		company.setDocument(request.document());
		company.setSlug(genSlug(request.name()));
		companyRepository.save(company);
		
		User user = new User();
		user.setEmail(request.email());
		user.setPhone(request.phone());
		user.setPassword(encoder.encode(defaultPassword));
		user.setRole(UserRole.ADMIN);
		user.setCompanyId(company.getId());
		userRepository.save(user);

		return new RegisterCompanyResponse(company.getName(), user.getEmail(), company.getSlug());
	}

	private String genSlug(String companyName) {
		String baseSlug = companyName.toLowerCase().replaceAll("[^a-z0-9\\s-]", "").replaceAll("\\s+", "-");
		String newSlug = baseSlug;

		int counter = 2;
		while (companyRepository.existsBySlug(newSlug))
			newSlug = baseSlug + "-" + counter++;

		return newSlug;
	}

	public void disable(UUID companyId) {

		Company company = companyRepository.findByCompanyId(companyId);

		if (company == null)
			throw new IllegalArgumentException("company not found");

		company.setActive(false);
		companyRepository.save(company);
	}
}