package com.github.devmribeiro.clipply.security.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.github.devmribeiro.clipply.application.model.User;

public class UserDetailsImpl implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String email;
	private String password;
	private Collection<? extends GrantedAuthority> authorities;
	
	public UserDetailsImpl(User user) {
        this.email = user.getEmail();
        this.password = user.getPassword();

        List<SimpleGrantedAuthority> auths = new ArrayList<SimpleGrantedAuthority>();
        auths.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        this.authorities = auths;
    }

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public @Nullable String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}
}