package com.github.devmribeiro.clipply.application.util;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.github.devmribeiro.clipply.application.model.User;
import com.github.devmribeiro.clipply.application.repository.UserRepository;
import com.github.devmribeiro.clipply.application.type.UserRole;

@Component
public class BootstrapData implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder encoder;
	
	@Value("${clipply.support-email}")
	private String emailSupport;

	@Value("${clipply.support-password}")
	private String passwordSupport;

	@Value("${clipply.support-phone}")
	private String phoneSupport;
	
	public BootstrapData(
			UserRepository userRepository,
			PasswordEncoder encoder) {
		this.userRepository = userRepository;
		this.encoder = encoder;
	}
	
	@Override
	public void run(String... args) throws Exception {

		if (userRepository.existsByEmail(emailSupport))
			return;

		User user = new User();
		user.setEmail(emailSupport);
		user.setName("CoreStacks");
		user.setPassword(encoder.encode(passwordSupport));
		user.setPhone(phoneSupport);
		user.setRole(UserRole.SUPPORT);
		user.setPasswordChangedAt(LocalDateTime.now());
		userRepository.save(user);
	}
}