package com.github.devmribeiro.clipply.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.github.devmribeiro.clipply.application.type.UserRole;
import com.github.devmribeiro.clipply.security.filter.ApiKeyFilter;
import com.github.devmribeiro.clipply.security.filter.AuthEntryPointJwt;
import com.github.devmribeiro.clipply.security.filter.JwtAuthenticationFilter;
import com.github.devmribeiro.clipply.security.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // enable @PreAuthorize in the controllers
public class SecurityConfig {

	private final JwtAuthenticationFilter authenticationFilter;
	private final UserDetailsServiceImpl userDetailsService;
	private final ApiKeyFilter apiKeyFilter;
	private final AuthEntryPointJwt unauthorizedHandler;
	
	public SecurityConfig(
			JwtAuthenticationFilter authenticationFilter,
			UserDetailsServiceImpl userDetailsService,
			ApiKeyFilter apiKeyFilter,
			AuthEntryPointJwt unauthorizedHandler) {
		this.authenticationFilter = authenticationFilter;
		this.userDetailsService = userDetailsService;
		this.apiKeyFilter = apiKeyFilter;
		this.unauthorizedHandler = unauthorizedHandler;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        .csrf(csrf -> csrf.disable())
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/api/auth/**").permitAll()
 	            .requestMatchers("/api/clipply/**").hasRole(UserRole.SUPPORT.name())
	            .anyRequest().authenticated()
	        )
	        .authenticationProvider(authenticationProvider())
	        .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
	        .addFilterBefore(apiKeyFilter, authenticationFilter.getClass());

	    return http.build();
	}

	@Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(encoder());
        return provider;
    }

	@Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}
}