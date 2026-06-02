package br.com.corestacks.agende360.security.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.corestacks.agende360.application.type.UserRole;
import br.com.corestacks.agende360.security.model.UserDetailsImpl;
import br.com.corestacks.agende360.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	private final JwtService jwtService;
	private final String COOKIE_TOKEN_NAME = "access_token";

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		LOGGER.info("REQUESTING ENDPOINT -> {} - FROM -> {}", request.getRequestURL(), getClientIp(request));
		
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

		UserRole role = jwtService.extractUserRole(token);
		UUID companyId = jwtService.extractCompanyId(token);

//		If any accessToken is present, then it will validate the token and then authenticate the request in security context
		if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = new UserDetailsImpl(jwtService.extractUserId(token), email, null, companyId, role);
			if (jwtService.isTokenValid(token, userDetails)) {
				UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			}
		}

		filterChain.doFilter(request, response);
	}
	
	public static String getClientIp(HttpServletRequest request) {

	    String xff = request.getHeader("X-Forwarded-For");

	    if (xff != null && !xff.isEmpty()) {
	        String ip = xff.split(",")[0].trim();
	        
	        if (ip != null && ip.startsWith("::ffff:"))
		        return ip.substring(7);
	    }

	    return request.getRemoteAddr();
	}
}