package com.github.devmribeiro.clipply.security.util;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;

import com.github.devmribeiro.clipply.application.exception.UnauthorizedException;
import com.github.devmribeiro.clipply.application.type.UserRole;
import com.github.devmribeiro.clipply.security.model.UserDetailsImpl;

public class SecurityUtils {

    private static UserDetailsImpl getPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

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