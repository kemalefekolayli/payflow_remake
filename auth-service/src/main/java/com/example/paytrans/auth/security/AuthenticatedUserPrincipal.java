package com.example.paytrans.auth.security;

import org.springframework.security.core.GrantedAuthority;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public record AuthenticatedUserPrincipal(
        Long userId,
        String username,
        Collection<? extends GrantedAuthority> authorities
) implements Principal {

    public AuthenticatedUserPrincipal {
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(username, "username is required");
        authorities = List.copyOf(Objects.requireNonNull(authorities, "authorities are required"));
    }

    @Override
    public String getName() {
        return username;
    }
}
