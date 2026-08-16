package com.jangada.RADAR.models.dtos;

public record AuthResponseDTO(
        String accessToken,
        String tokenType,
        long expiresIn,
        UsuarioDTO usuario
) {
}
