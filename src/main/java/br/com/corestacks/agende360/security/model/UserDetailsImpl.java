package br.com.corestacks.agende360.security.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.corestacks.agende360.application.type.UserRole;

public class UserDetailsImpl implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final UUID id;
	private final UUID companyId;
	private final String email;
	private final String password;
	private final UserRole role;
	private final Collection<? extends GrantedAuthority> authorities;
	
	public UserDetailsImpl(UUID id, String email, String password, UUID companyId, UserRole role) {
		this.id = id;
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

	public UUID getId() {
		return id;
	}
}