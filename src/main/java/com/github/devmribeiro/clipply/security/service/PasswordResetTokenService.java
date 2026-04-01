package com.github.devmribeiro.clipply.security.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.devmribeiro.clipply.application.exception.IllegalArgumentException;
import com.github.devmribeiro.clipply.application.model.PasswordResetToken;
import com.github.devmribeiro.clipply.application.model.User;
import com.github.devmribeiro.clipply.application.repository.PasswordResetTokenRepository;
import com.github.devmribeiro.clipply.application.repository.UserRepository;
import com.github.devmribeiro.clipply.application.util.BaseUrlUtils;

@Service
public class PasswordResetTokenService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetTokenService.class);
	private final PasswordResetTokenRepository resetTokenRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder encoder;
	
	public PasswordResetTokenService(
			PasswordResetTokenRepository resetTokenRepository,
			UserRepository userRepository,
			PasswordEncoder encoder) {
		this.resetTokenRepository = resetTokenRepository;
		this.userRepository = userRepository;
		this.encoder = encoder;
	}
	
	public void save(String email) {
		User user = userRepository.findByEmail(email);
		
		if (user == null || !user.getActive())
			return;
		
		String rawToken = UUID.randomUUID().toString();

		PasswordResetToken prt = new PasswordResetToken();
		prt.setUserId(user.getId());
		prt.setTokenHash(hash(rawToken));
		prt.setExpiresAt(LocalDateTime.now().plusMinutes(5));
		resetTokenRepository.save(prt);
		
		LOGGER.info("http://localhost:9002/"+ BaseUrlUtils.RESET_PASSWORD_BY_TOKEN + "?token=" + rawToken);
	}
	
	public void resetPasswordFromToken(String token, String newPassword) {
		String tokenHash = hash(token);
		
		PasswordResetToken prt = resetTokenRepository.findValidToken(tokenHash, LocalDateTime.now());
		
		if (prt == null)
			throw new IllegalArgumentException("Invalid token");
		
		User user = userRepository.findByUserId(prt.getUserId());
		
		if (user == null)
			return;
		
		user.setPassword(encoder.encode(newPassword));
		user.setPasswordChangedAt(LocalDateTime.now());
		userRepository.save(user);
		
		prt.setUsed(true);
		resetTokenRepository.save(prt);
	}
	
	private String hash(String input) {
	    try {
	        MessageDigest md = MessageDigest.getInstance("SHA-256");
	        byte[] hashed = md.digest(input.getBytes(StandardCharsets.UTF_8));
	        return Base64.getEncoder().encodeToString(hashed);
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
}