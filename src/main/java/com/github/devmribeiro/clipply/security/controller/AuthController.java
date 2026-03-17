package com.github.devmribeiro.clipply.security.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.devmribeiro.clipply.application.dto.request.LoginRequest;
import com.github.devmribeiro.clipply.application.model.User;
import com.github.devmribeiro.clipply.application.repository.UserRepository;
import com.github.devmribeiro.clipply.security.model.RefreshToken;
import com.github.devmribeiro.clipply.security.service.CookieService;
import com.github.devmribeiro.clipply.security.service.JwtService;
import com.github.devmribeiro.clipply.security.service.RefreshTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final RefreshTokenService refreshTokenService;
	private final CookieService cookieService;
	private final String REFRESH_COOKIE_TOKEN_NAME = "refresh_token";

	public AuthController(
			AuthenticationManager authenticationManager,
			JwtService jwtService,
			UserRepository userRepository,
			RefreshTokenService refreshTokenService,
			CookieService cookieService) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.userRepository = userRepository;
		this.refreshTokenService = refreshTokenService;
		this.cookieService = cookieService;
	}
	
	@PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request, HttpServletResponse response) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email());

        // Generate access token (short-lived)
        String accessToken = jwtService.generateToken(user);

        // Generate a refresh token (long-lived, stored in the database)
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Set both as HttpOnly cookies - the body remains empty
        response.addCookie(cookieService.createAccessTokenCookie(accessToken));
        response.addCookie(cookieService.createRefreshTokenCookie(refreshToken.getToken()));

        return ResponseEntity.ok().build();
    }

	@PostMapping("/refresh")
	public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
		
		String refreshTokenValue = jwtService.getTokenFromCookie(request, REFRESH_COOKIE_TOKEN_NAME);
		
		if (refreshTokenValue == null)
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		
		// Valid - throws an exception if expired or revoked
		RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);
		User user = refreshToken.getUser();
		
		// Generate new access token
		response.addCookie(cookieService.createAccessTokenCookie(jwtService.generateToken(user)));
		
		return ResponseEntity.ok().build();
	}
	
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		
		String refreshTokenValue = jwtService.getTokenFromCookie(request, REFRESH_COOKIE_TOKEN_NAME);
		
		if (refreshTokenValue != null) {
			RefreshToken rf = refreshTokenService.findByToken(refreshTokenValue);
			if (rf != null)
				refreshTokenService.revokeByUser(rf.getUser());
		}
		
		// Remove both cookies from the browser (MaxAge = 0)
		response.addCookie(cookieService.clearAccessTokenCookie());
		response.addCookie(cookieService.clearRefreshTokenCookie());

		return ResponseEntity.ok().build();
	}
}