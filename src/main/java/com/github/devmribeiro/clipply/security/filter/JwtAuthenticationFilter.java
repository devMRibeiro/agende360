package com.github.devmribeiro.clipply.security.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.devmribeiro.clipply.security.service.JwtService;
import com.github.devmribeiro.clipply.security.service.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserDetailsServiceImpl userDetailsService;
	private final String COOKIE_TOKEN_NAME = "access_token";

	public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsServiceImpl) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsServiceImpl;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String token = jwtService.getTokenFromCookie(request, COOKIE_TOKEN_NAME);

//      If the accessToken is null. It will pass the request to next filter in the chain.
//      Any login and signup requests will not have jwt token in their header, therefore they will be passed to next filter chain.
		if (token == null) {
			filterChain.doFilter(request, response);
			return;
		}

		String email = null;
		
		try {
			email = jwtService.extractEmail(token);
		} catch (Exception e) {
			// Invalid or expired token — proceeds without authentication
	        // The /auth/refresh endpoint will handle the refresh
			filterChain.doFilter(request, response);
			return;
		}

//		If any accessToken is present, then it will validate the token and then authenticate the request in security context
		if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = userDetailsService.loadUserByUsername(email);
			if (jwtService.isTokenValid(token, userDetails)) {
				UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			}
		}

		filterChain.doFilter(request, response);
	}
}