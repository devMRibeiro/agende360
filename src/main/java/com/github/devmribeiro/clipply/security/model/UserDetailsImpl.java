package com.github.devmribeiro.clipply.security.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.github.devmribeiro.clipply.application.type.UserRole;

public class UserDetailsImpl implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private UUID companyId;
	private String email;
	private String password;
	private UserRole role;
	private Collection<? extends GrantedAuthority> authorities;
	
	public UserDetailsImpl(String email, String password, UUID companyId, UserRole role) {
        this.email = email;
        this.password = password;
        this.companyId = companyId;
        this.role = role;

        List<SimpleGrantedAuthority> auths = new ArrayList<SimpleGrantedAuthority>();
        auths.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
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

	@Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

	public UUID getCompanyId() {
		return companyId;
	}

	public UserRole getRole() {
		return role;
	}
}