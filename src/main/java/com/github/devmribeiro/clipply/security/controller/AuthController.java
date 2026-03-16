package com.github.devmribeiro.clipply.security.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.devmribeiro.clipply.application.dto.request.LoginRequest;
import com.github.devmribeiro.clipply.application.dto.response.LoginResponse;
import com.github.devmribeiro.clipply.application.model.User;
import com.github.devmribeiro.clipply.security.service.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	
	public AuthController(
			AuthenticationManager authenticationManager,
			JwtService jwtService) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}
	
	@PostMapping("/signin")
	public ResponseEntity<LoginResponse> signIn(@RequestBody @Valid LoginRequest request) {
	    User user = (User) authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password())).getPrincipal();
	    return ResponseEntity.ok(new LoginResponse(jwtService.generateToken(user)));
	}
}