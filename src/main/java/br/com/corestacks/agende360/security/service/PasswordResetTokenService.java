package br.com.corestacks.agende360.security.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.corestacks.agende360.application.exception.IllegalArgumentException;
import br.com.corestacks.agende360.application.model.PasswordResetToken;
import br.com.corestacks.agende360.application.model.User;
import br.com.corestacks.agende360.application.repository.PasswordResetTokenRepository;
import br.com.corestacks.agende360.application.repository.UserRepository;
import br.com.corestacks.agende360.application.util.BaseUrlUtils;
import br.com.corestacks.agende360.messaging.service.EmailService;

@Service
public class PasswordResetTokenService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetTokenService.class);
	private final PasswordResetTokenRepository resetTokenRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder encoder;
	private final EmailService emailService;
	
	@Value("${SYSTEM.BASE-URL}")
	private String host;
	
	public PasswordResetTokenService(
			PasswordResetTokenRepository resetTokenRepository,
			UserRepository userRepository,
			PasswordEncoder encoder,
			EmailService emailService) {
		this.resetTokenRepository = resetTokenRepository;
		this.userRepository = userRepository;
		this.encoder = encoder;
		this.emailService = emailService;
	}
	
	public void forgotPassword(String email) {
		LOGGER.info("Password reset requested for email={}", email);

		User user = userRepository.findByEmail(email);
		
		if (user == null || !user.getActive())
			return;

		final String rawToken = UUID.randomUUID().toString();

		final LocalDateTime expirationToken = LocalDateTime.now().plusMinutes(5);
		
		PasswordResetToken prt = new PasswordResetToken();
		prt.setUserId(user.getId());
		prt.setTokenHash(hash(rawToken));
		prt.setExpiresAt(expirationToken);
		resetTokenRepository.save(prt);
		
		sendEmailResetPassword(user, expirationToken, rawToken);
	}
	
	private void sendEmailResetPassword(User user, LocalDateTime expirationToken, String rawToken) {
		
		String url = host + BaseUrlUtils.RESET_PASSWORD_BY_TOKEN + "?token=" + rawToken;
		
		Map<String, String> hmContentEmail = new HashMap<String, String>();
		hmContentEmail.put("CLIENT_NAME", user.getName());
		hmContentEmail.put("RESET_LINK", url);
		hmContentEmail.put("EXPIRATION_TIME", expirationToken.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")).toString());
		hmContentEmail.put("YEAR", String.valueOf(LocalDate.now().getYear()));

		emailService.sendResetPasswordEmail(user.getEmail(), hmContentEmail);
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
	
	public static String hash(String input) {
	    try {
	        MessageDigest md = MessageDigest.getInstance("SHA-256");
	        byte[] hashed = md.digest(input.getBytes(StandardCharsets.UTF_8));
	        return Base64.getEncoder().encodeToString(hashed);
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
}