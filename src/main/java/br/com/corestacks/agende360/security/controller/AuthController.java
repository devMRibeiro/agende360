package br.com.corestacks.agende360.security.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.corestacks.agende360.application.dto.request.ForgotPasswordRequest;
import br.com.corestacks.agende360.application.dto.request.LoginRequest;
import br.com.corestacks.agende360.application.dto.request.NewPasswordRequest;
import br.com.corestacks.agende360.application.dto.response.UserMeResponse;
import br.com.corestacks.agende360.application.exception.ForbiddenException;
import br.com.corestacks.agende360.application.model.Company;
import br.com.corestacks.agende360.application.model.User;
import br.com.corestacks.agende360.application.repository.CompanyRepository;
import br.com.corestacks.agende360.application.repository.UserRepository;
import br.com.corestacks.agende360.application.util.BaseUrlUtils;
import br.com.corestacks.agende360.security.model.RefreshToken;
import br.com.corestacks.agende360.security.model.UserDetailsImpl;
import br.com.corestacks.agende360.security.service.CookieService;
import br.com.corestacks.agende360.security.service.JwtService;
import br.com.corestacks.agende360.security.service.PasswordResetTokenService;
import br.com.corestacks.agende360.security.service.RefreshTokenService;
import br.com.corestacks.agende360.security.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final CompanyRepository companyRepository;
	private final RefreshTokenService refreshTokenService;
	private final CookieService cookieService;
	private final String REFRESH_COOKIE_TOKEN_NAME = "refresh_token";
	private final PasswordResetTokenService passwordResetTokenService;

	public AuthController(
			AuthenticationManager authenticationManager,
			JwtService jwtService,
			UserRepository userRepository,
			CompanyRepository companyRepository,
			RefreshTokenService refreshTokenService,
			CookieService cookieService,
			PasswordResetTokenService passwordResetTokenService) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.userRepository = userRepository;
		this.companyRepository = companyRepository;
		this.refreshTokenService = refreshTokenService;
		this.cookieService = cookieService;
		this.passwordResetTokenService = passwordResetTokenService;
	}

	@PostMapping("/login")
	public ResponseEntity<Void> login(@RequestBody LoginRequest request, HttpServletResponse response) {

		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

		User user = userRepository.findByEmail(request.email());
		
		Company company = companyRepository.findByCompanyId(user.getCompanyId());
		
		if (company != null && !company.getActive())
			throw new ForbiddenException("Ops.. Não perca seus agendamentos, entre em contato com o suporte para regularizar.");

		String accessToken = jwtService.generateToken(user);
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

		response.addCookie(cookieService.createAccessTokenCookie(accessToken));
		response.addCookie(cookieService.createRefreshTokenCookie(refreshToken.getToken()));

		return ResponseEntity.ok().build();
	}

	@PostMapping("/refresh")
	public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {

		String refreshTokenValue = jwtService.getTokenFromCookie(request, REFRESH_COOKIE_TOKEN_NAME);

		if (refreshTokenValue == null)
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

		RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);
		User user = refreshToken.getUser();

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

		response.addCookie(cookieService.clearAccessTokenCookie());
		response.addCookie(cookieService.clearRefreshTokenCookie());

		return ResponseEntity.ok().build();
	}

	@GetMapping("/me")
	public ResponseEntity<UserMeResponse> me() {
		UserDetailsImpl principal = SecurityUtils.getAuthenticatedUser();

		User user = userRepository.findByUserId(principal.getId());
		boolean firstAccess = user.getPasswordChangedAt() == null;

		String companyName = null;
		String companySlug = null;

		if (user.getCompanyId() != null) {
			Company company = companyRepository.findByCompanyId(user.getCompanyId());
			if (company != null) {
				companyName = company.getName();
				companySlug = company.getSlug();
			}
		}

		return ResponseEntity.ok(new UserMeResponse(
				user.getId(),
				user.getEmail(),
				user.getCompanyId(),
				companyName,
				companySlug,
				user.getRole(),
				firstAccess
		));
	}
	
	@PostMapping("/forgot-password")
	public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
		passwordResetTokenService.forgotPassword(request.email());
		return ResponseEntity.ok().build();
	}
	
	@PostMapping("/" + BaseUrlUtils.RESET_PASSWORD_BY_TOKEN)
	public ResponseEntity<Void> resetPasswordByToken(@RequestBody @Valid NewPasswordRequest request) {
		passwordResetTokenService.resetPasswordFromToken(request.token(), request.newPassword());
		return ResponseEntity.ok().build();
	}
}