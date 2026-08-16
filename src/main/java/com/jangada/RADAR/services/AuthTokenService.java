package com.jangada.RADAR.services;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import com.jangada.RADAR.models.dtos.AuthResponseDTO;
import com.jangada.RADAR.models.dtos.UsuarioDTO;

@Service
public class AuthTokenService {

    private final JwtEncoder encoder;
    private final Duration tokenTtl;

    public AuthTokenService(
            JwtEncoder encoder,
            @Value("${radar.auth.token-ttl:PT12H}") Duration tokenTtl) {
        this.encoder = encoder;
        this.tokenTtl = tokenTtl;
    }

    public AuthResponseDTO issue(UsuarioDTO usuario) {
        Instant issuedAt = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("radar")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(tokenTtl))
                .subject(usuario.getId().toString())
                .claim("test", Boolean.TRUE.equals(usuario.getIsTeste()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AuthResponseDTO(token, "Bearer", tokenTtl.toSeconds(), usuario);
    }
}
