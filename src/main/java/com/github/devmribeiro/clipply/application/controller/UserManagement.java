package com.github.devmribeiro.clipply.application.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.devmribeiro.clipply.application.dto.request.ChangePasswordRequest;
import com.github.devmribeiro.clipply.application.dto.request.RegisterProfessionalRequest;
import com.github.devmribeiro.clipply.application.dto.response.UserResponse;
import com.github.devmribeiro.clipply.application.service.UserService;
import com.github.devmribeiro.clipply.security.util.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/management")
public class UserManagement {

	private final UserService userService;
	
	public UserManagement(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/user")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> registerUser(@RequestBody @Valid RegisterProfessionalRequest request) {
		userService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<UserResponse>> list() {
		return ResponseEntity.ok(userService.list(SecurityUtils.getCompanyId()));
	}
	
	@PatchMapping("/change-password")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
		userService.changePassword(request, SecurityUtils.getAuthenticatedUser().getUsername());
		return ResponseEntity.ok().build();
	}
}