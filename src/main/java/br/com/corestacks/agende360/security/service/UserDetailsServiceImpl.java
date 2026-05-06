package br.com.corestacks.agende360.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.corestacks.agende360.application.model.User;
import br.com.corestacks.agende360.application.repository.UserRepository;
import br.com.corestacks.agende360.security.model.UserDetailsImpl;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;
	
	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email);

		if (user == null)
			throw new UsernameNotFoundException("User not found: " + email);

		return new UserDetailsImpl(
				user.getId(),
				user.getEmail(),
				user.getPassword(),
				user.getCompanyId(),
				user.getRole()
		);
	}
}