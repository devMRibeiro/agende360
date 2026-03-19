package com.github.devmribeiro.clipply.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.devmribeiro.clipply.application.dto.request.ChangePasswordRequest;
import com.github.devmribeiro.clipply.application.dto.request.RegisterProfessionalRequest;
import com.github.devmribeiro.clipply.application.dto.response.UserResponse;
import com.github.devmribeiro.clipply.application.exception.ConflictException;
import com.github.devmribeiro.clipply.application.exception.IllegalArgumentException;
import com.github.devmribeiro.clipply.application.model.User;
import com.github.devmribeiro.clipply.application.repository.UserRepository;
import com.github.devmribeiro.clipply.security.repository.RefreshTokenRepository;
import com.github.devmribeiro.clipply.security.util.PasswordUtil;
import com.github.devmribeiro.clipply.security.util.SecurityUtils;

import jakarta.transaction.Transactional;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder encoder;
	private final RefreshTokenRepository refreshTokenRepository;
	
	public UserService(
			UserRepository userRepository,
			PasswordEncoder encoder,
			RefreshTokenRepository refreshTokenRepository) {
		this.userRepository = userRepository;
		this.encoder = encoder;
		this.refreshTokenRepository = refreshTokenRepository;
	}

	public List<UserResponse> list(UUID companyId) {
		List<User> users = userRepository.findByCompanyId(companyId);
		List<UserResponse> result = new ArrayList<UserResponse>(users.size());
		for (User user : users)
			result.add(new UserResponse(user.getName(), user.getEmail(), user.getPhone(), user.getActive(), user.getRole()));
		
		return result;
	}
	
	public void create(RegisterProfessionalRequest request) {

		if (userRepository.existsByEmail(request.email()))
			throw new ConflictException("There is already user with that email");

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
}