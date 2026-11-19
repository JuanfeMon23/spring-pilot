# Catálogo del núcleo (backend Spring)

| Elemento | Firma | Usado por | Para qué sirve |
|---|---|---|---|
| `error.AppException` | `(HttpStatus, code, message, details)` | modules.notes | Error de negocio con el contrato `{code, message, details}` |
| `error.NotFound` | `NotFound.of(resource)` | (ninguno aún) | 404 con código `<resource>_not_found` |
| `error.Conflict` | `Conflict.of(code, field, message)` | modules.notes | 409 con el campo en `details` |
| `error.ApiExceptionHandler` | `@RestControllerAdvice` | todos | AppException, 422 `request_validation`, errores MVC y 500 `internal_error` sin traza |
| `web.PageRequestDto` | `(page=1, size=20, q)` + `toPageable(Sort)` | modules.notes | Parámetros de paginación validados (page base 1, size máx. 100) |
| `web.PageDto<T>` | `(items, total, page, size)` + `from(Page, mapper)` | modules.notes | Respuesta paginada común |
| `config.CorsProperties` / `CorsConfig` | `app.cors.*` | todos | CORS explícito desde configuración, validado |
| `db.BaseEntity` | `@MappedSuperclass` (`id`, `createdAt`, `updatedAt`) | `modules.notes.Note` | Id uuidv7 y timestamps generados por la BD (`@Generated`); `core.db` expuesto como `@NamedInterface("db")` |
| SQL `set_updated_at()` | función trigger plpgsql | `trg_notes_set_updated_at` | Mantiene `updated_at` en `BEFORE UPDATE`; reutilizable por toda tabla con esa columna |
| `error`, `web` (`@NamedInterface`) | `package-info.java` | `modules.notes` | Exponen `AppException`/`Conflict`/`NotFound` y `PageDto`/`PageRequestDto` a los módulos (sin esto `verify()` falla) |
