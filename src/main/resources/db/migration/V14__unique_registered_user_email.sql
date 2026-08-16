CREATE UNIQUE INDEX uq_usuarios_email_registrado
    ON usuarios (LOWER(email))
    WHERE is_teste = FALSE AND email IS NOT NULL;
