package com.github.devmribeiro.clipply.application.model;

import java.time.LocalDateTime;

import com.github.devmribeiro.clipply.application.type.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "users",
	indexes = {
		@Index(name = "idx_users_email", columnList = "email", unique = true),
		@Index(name = "idx_users_phone", columnList = "phone"),
})
public class User extends BaseEntity {

	@Column(nullable = false, length = 100)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String phone;

	@Column(nullable = false)
	private Boolean active = true;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserRole role;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id")
	private Company company;

	@Column(name = "password_chaged_at")
	private LocalDateTime passwordChangedAt;
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public LocalDateTime getPasswordChagendAt() {
		return passwordChangedAt;
	}

	public void setPasswordChagedAt(LocalDateTime passwordChangedAt) {
		this.passwordChangedAt = passwordChangedAt;
	}
}