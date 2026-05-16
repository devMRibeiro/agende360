package br.com.corestacks.agende360.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.corestacks.agende360.application.dto.request.ChangePasswordRequest;
import br.com.corestacks.agende360.application.dto.request.RegisterProfessionalRequest;
import br.com.corestacks.agende360.application.dto.request.UpdateUserRequest;
import br.com.corestacks.agende360.application.dto.response.ProfessionalResponse;
import br.com.corestacks.agende360.application.dto.response.UserMeResponse;
import br.com.corestacks.agende360.application.dto.response.UserResponse;
import br.com.corestacks.agende360.application.exception.ConflictException;
import br.com.corestacks.agende360.application.exception.ForbiddenException;
import br.com.corestacks.agende360.application.exception.IllegalArgumentException;
import br.com.corestacks.agende360.application.model.Company;
import br.com.corestacks.agende360.application.model.User;
import br.com.corestacks.agende360.application.repository.CompanyRepository;
import br.com.corestacks.agende360.application.repository.UserRepository;
import br.com.corestacks.agende360.application.subscription.service.FeatureGateService;
import br.com.corestacks.agende360.application.type.UserRole;
import br.com.corestacks.agende360.security.model.UserDetailsImpl;
import br.com.corestacks.agende360.security.repository.RefreshTokenRepository;
import br.com.corestacks.agende360.security.util.PasswordUtil;
import br.com.corestacks.agende360.security.util.SecurityUtils;
import io.jsonwebtoken.lang.Collections;
import jakarta.transaction.Transactional;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder encoder;
	private final RefreshTokenRepository refreshTokenRepository;
	private final CompanyRepository companyRepository;
	private final FeatureGateService featureGateService;
	
	public UserService(
			UserRepository userRepository,
			PasswordEncoder encoder,
			RefreshTokenRepository refreshTokenRepository,
			CompanyRepository companyRepository,
			FeatureGateService featureGateService) {
		this.userRepository = userRepository;
		this.encoder = encoder;
		this.refreshTokenRepository = refreshTokenRepository;
		this.companyRepository = companyRepository;
		this.featureGateService = featureGateService;
	}

	public List<UserResponse> list(UUID companyId) {
		List<User> users = userRepository.findByCompanyId(companyId);
		List<UserResponse> result = new ArrayList<UserResponse>(users.size());
		for (User user : users) {
			result.add(new UserResponse(
					user.getId(),
					user.getName(),
					user.getEmail(),
					user.getPhone(),
					user.getActive(),
					user.getRole(),
					user.getIsProfessional())
			);
		}
		return result;
	}
	
	public void create(RegisterProfessionalRequest request) {

		if (userRepository.existsByEmail(request.email()))
			throw new ConflictException("There is already user with that email");
		
		featureGateService.checkProfessionalsLimit(SecurityUtils.getCompanyId(), userRepository.listProfessionals(SecurityUtils.getCompanyId(), null).size());

		User user = new User();
		user.setName(request.name());
		user.setEmail(request.email());
		user.setPassword(encoder.encode(PasswordUtil.getPassword(8)));
		user.setPhone(request.phone());
		user.setRole(request.role());
		user.setCompanyId(SecurityUtils.getCompanyId());
		userRepository.save(user);
	}
	
	@Transactional
	public void changePassword(ChangePasswordRequest request, String email) {

		User user = userRepository.findByEmailAndCompanyId(email, SecurityUtils.getCompanyId());

		if (user == null)
			throw new IllegalArgumentException("User not found");

		if (!encoder.matches(request.currentPassword(), user.getPassword()))
			throw new IllegalArgumentException("Bad credentials");

		if (encoder.matches(request.newPassword(), user.getPassword()))
			throw new IllegalArgumentException("New password must be different from current password");
		
		if (!request.newPassword().equals(request.confirmNewPassword()))
			throw new IllegalArgumentException("New password and confirm new password must be equals");
		
		user.setPassword(encoder.encode(request.newPassword()));
		user.setPasswordChangedAt(LocalDateTime.now());
		userRepository.save(user);
		refreshTokenRepository.deleteByUser(user);
	}
	
	@Transactional
	public UserMeResponse me() {
		User user = userRepository.findByUserId(SecurityUtils.getAuthenticatedUser().getId());
		
		String companyName = null;
		String companySlug = null;
		
		if (user.getCompanyId() != null) {
			Company company = companyRepository.findByCompanyId(user.getCompanyId());
			if (company != null) {
				companyName = company.getName();
				companySlug = company.getSlug();
			}
		}
		
		return new UserMeResponse(
				user.getId(),
				user.getEmail(),
				user.getCompanyId(),
				companyName,
				companySlug,
				user.getRole(),
				user.getPasswordChangedAt() == null
		);
	}
	
	public List<ProfessionalResponse> listProfessionalsActive(String slug) {

		List<User> users = userRepository.listProfessionals(null, slug);
        
        if (users == null || users.isEmpty())
        	return Collections.emptyList();

        List<ProfessionalResponse> professionalsResponse = new ArrayList<ProfessionalResponse>(users.size());
        
        for (User u : users)
        	professionalsResponse.add(new ProfessionalResponse(u.getId(), u.getName()));
        
        return professionalsResponse;
	}
	
	@Transactional
	public void updateUser(UpdateUserRequest request) {
	    UserDetailsImpl authUser = SecurityUtils.getAuthenticatedUser();
	    User target = userRepository.findByUserId(request.id());

	    if (!authUser.getRole().equals(UserRole.ADMIN) && !authUser.getId().equals(target.getId()))
	        throw new ForbiddenException("");

	    target.setName(request.name());
	    target.setPhone(request.phone());

	    // Regra de email
	    if (target.getRole() == UserRole.ADMIN && !target.getEmail().equals(request.email()))
	        throw new IllegalArgumentException("Admin não pode alterar email");

	    target.setEmail(request.email());
	}
	
	@Transactional
	public void toogleProfessionalUser(Boolean isProfessional) {
		
		User user = userRepository.findByUserId(SecurityUtils.getAuthenticatedUser().getId());
		
		if (!user.getRole().equals(UserRole.ADMIN))
			throw new ForbiddenException("");
		
		userRepository.toggleProfessionalUser(SecurityUtils.getAuthenticatedUser().getId(), isProfessional);
	}
}