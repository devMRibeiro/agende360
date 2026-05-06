package br.com.corestacks.agende360.application.util;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.corestacks.agende360.application.model.User;
import br.com.corestacks.agende360.application.repository.UserRepository;
import br.com.corestacks.agende360.application.type.UserRole;

@Component
public class BootstrapData implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder encoder;
	
	@Value("${SYSTEM.SUPPORT-EMAIL}")
	private String emailSupport;

	@Value("${SYSTEM.SUPPORT-PASSWORD}")
	private String passwordSupport;

	@Value("${SYSTEM.SUPPORT-PHONE}")
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