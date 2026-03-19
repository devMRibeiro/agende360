package com.github.devmribeiro.clipply.security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;

@Service
public class CookieService {

	@Value("${cookie.secure:true}") // false when dev (HTTP), true when prod (HTTPS)
	private boolean secure;
	private final String COOKIE_NAME = "access_token"; 
	private final String REFRESH_COOKIE_NAME = "refresh_token"; 
	
	@Value("${security.jwt.expiration-ms}")
	private Long expirationToken;
	
	public Cookie createAccessTokenCookie(String token) {
		Cookie cookie = new Cookie(COOKIE_NAME, token);
		cookie.setHttpOnly(true);
		cookie.setSecure(secure);
		cookie.setPath("/");
		cookie.setMaxAge((int) (expirationToken / 1000));
		cookie.setAttribute("SameSite", "Strict"); // CSRF protection
		return cookie;
	}
	
	public Cookie createRefreshTokenCookie(String token) {
		Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, token);
		cookie.setHttpOnly(true);
		cookie.setSecure(secure);
		cookie.setPath("/api/auth"); // send to /auth/refresh and /auth/logout only.
		cookie.setMaxAge(60 * 60 * 24 * 7);
		return cookie;
	}
	
	public Cookie clearAccessTokenCookie() {
		Cookie cookie = new Cookie(COOKIE_NAME, "");
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		return cookie;
	}
	
	public Cookie clearRefreshTokenCookie() {
		Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, "");
		cookie.setHttpOnly(true);
		cookie.setPath("/api/auth");
		cookie.setMaxAge(0);
		return cookie;
	}
}