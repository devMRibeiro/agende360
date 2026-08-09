package br.com.corestacks.agende360.application.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.corestacks.agende360.application.dto.request.ChangePasswordRequest;
import br.com.corestacks.agende360.application.dto.request.RegisterProfessionalRequest;
import br.com.corestacks.agende360.application.dto.request.UpdateUserRequest;
import br.com.corestacks.agende360.application.dto.response.UserResponse;
import br.com.corestacks.agende360.application.service.UserService;
import br.com.corestacks.agende360.security.util.SecurityUtils;
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
		return ResponseEntity.ok(userService.list());
	}
	
	@PatchMapping("/change-password")
	@PreAuthorize("hasAnyRole('ADMIN', 'PROFESSIONAL')")
	public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
		userService.changePassword(request, SecurityUtils.getAuthenticatedUser().getUsername());
		return ResponseEntity.ok().build();
	}
	
	@PutMapping("/user")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> updateUserAdmin(@RequestBody UpdateUserRequest request) {
		userService.updateUser(request);
		return ResponseEntity.ok().build();
	}
	
	@PatchMapping("/user/professional")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> toggleProfessionalUser(@RequestBody Map<String, Boolean> request) {
		userService.toggleProfessionalUser(request.get("isProfessional"));
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/user/active")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> toggleActiveUser(@RequestBody Map<String, String> request) {
		userService.toggleActiveUser(UUID.fromString(request.get("id")), Boolean.valueOf(request.get("active")));
		return ResponseEntity.ok().build();
	}
}