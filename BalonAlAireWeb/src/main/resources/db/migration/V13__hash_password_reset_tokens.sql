-- Los tokens de recuperación ahora se guardan como hash SHA-256 (64 caracteres hex).
-- Los tokens vigentes se guardaron en texto plano y ya no son válidos: se eliminan.
UPDATE users
SET password_reset_token = NULL,
    password_reset_expires_at = NULL
WHERE password_reset_token IS NOT NULL;
