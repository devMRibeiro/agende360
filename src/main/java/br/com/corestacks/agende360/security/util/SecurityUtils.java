package br.com.corestacks.agende360.security.util;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.corestacks.agende360.application.exception.UnauthorizedException;
import br.com.corestacks.agende360.security.model.UserDetailsImpl;

public class SecurityUtils {

    private static UserDetailsImpl getPrincipal() {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null)
            throw new UnauthorizedException("User not authenticated");

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetailsImpl user))
            throw new UnauthorizedException("Invalid authentication");

        return user;
    }

    public static UserDetailsImpl getAuthenticatedUser() {
        return getPrincipal();
    }

    public static UUID getCompanyId() {
        return getPrincipal().getCompanyId();
    }
}