# BalonAlAire
Página de Básquet (sitio público + panel de administración). Backend en `BalonAlAireWeb/` (Spring Boot, PostgreSQL).

## Entornos y configuración

- **Desarrollo:** `SPRING_PROFILES_ACTIVE=dev`. Trae valores por defecto locales (BD en `localhost`, `APP_PUBLIC_URL=http://localhost:8080`, CORS para puertos típicos de front). Las variables también pueden ir en `BalonAlAireWeb/.env`.
- **Producción:** `SPRING_PROFILES_ACTIVE=prod` (cookie de sesión `Secure` y logs JSON). No hay URLs ni credenciales por defecto: si falta una variable obligatoria el arranque falla indicando cuál.

| Variable | Obligatoria | Descripción |
|---|---|---|
| `PGHOST`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` (`PGPORT`=5432) | sí (prod) | Conexión a PostgreSQL |
| `APP_PUBLIC_URL` | sí (prod) | URL pública (links de emails, CORS por defecto) |
| `ALLOWED_ORIGINS` | no | Orígenes CORS separados por coma. Por defecto solo `APP_PUBLIC_URL` |
| `ADMIN_USERNAME`, `ADMIN_PASSWORD` (≥12), `ADMIN_EMAIL` | recomendada | Crea el admin inicial y reemplaza la contraseña sembrada por la migración V8 |
| `SESSION_TIMEOUT`, `SESSION_COOKIE_MAX_AGE` | no | Duraciones con unidad válida (`8h`, `30m`); por defecto `8h` |
| `STORAGE_ENDPOINT`, `STORAGE_PUBLIC_URL`, `STORAGE_BUCKET`, `STORAGE_ACCESS_KEY`, `STORAGE_SECRET_KEY`, `STORAGE_REGION`, `STORAGE_PATH_STYLE` | no | Storage S3 compatible (R2, S3, MinIO). Sin `STORAGE_ENDPOINT` se guarda en disco local (`uploads/`, solo desarrollo) |
| `TINYMCE_API_KEY`, `MAIL_*` | no | Editor y SMTP para recuperar contraseñas |

## API

- `/api/public/**`: solo lectura, solo contenido publicado/activo, con DTOs públicos.
- `/api/admin/**`: administración. Roles: `ADMIN` (todo) y `EDITOR` (noticias, subidas y lectura de categorías).
- `/api/auth/forgot-password` y `/api/auth/reset-password` son públicos (con límite de intentos); el resto de `/api/auth/**` es solo `ADMIN`, salvo `change-password` (cualquier usuario del panel).
- `/actuator/health` expone el estado (sin detalles).

## Subidas de archivos

El panel sube directo al storage con URL prefirmada (`POST /api/admin/uploads/presign` y luego `PUT`). Si el storage es local, o la subida directa falla, usa `POST /api/admin/uploads` (a través del servidor).

Para que funcione la subida directa, el bucket necesita CORS que permita el origen del panel:

```json
[{
  "AllowedOrigins": ["https://<tu-dominio>"],
  "AllowedMethods": ["PUT"],
  "AllowedHeaders": ["*"]
}]
```

## Caché

Las categorías, enlaces sociales y ajustes usan Caffeine local (TTL de 10 min). Con más de una réplica, un cambio puede tardar hasta ese TTL en verse en las demás (consistencia eventual). Si hiciera falta consistencia inmediata, habría que pasar a una caché compartida (Redis).

## Pruebas

`./mvnw test`. Las pruebas de integración usan Testcontainers (PostgreSQL real) y requieren Docker; sin Docker se omiten.
