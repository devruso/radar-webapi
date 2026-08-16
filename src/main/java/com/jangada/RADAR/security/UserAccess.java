package com.jangada.RADAR.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userAccess")
public class UserAccess {

    public boolean canAccess(Long usuarioId, Authentication authentication) {
        if (usuarioId == null || authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return usuarioId.toString().equals(authentication.getName());
    }
}
