# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Spring Boot 4.0.4 REST backend for a veterinary clinic (`clinica`), built with Java 21, Spring Data JPA, and MySQL. Manages `Cliente` (customers), `Mascota` (pets), and `Raza` (breeds).

## Commands

Uses the Maven wrapper (`mvnw` / `mvnw.cmd`) — no need for a local Maven install.

```bash
./mvnw compile              # Compile
./mvnw test                 # Run all tests
./mvnw test -Dtest=ClinicaApplicationTests#contextLoads   # Run a single test
./mvnw spring-boot:run       # Run the app locally (requires MySQL running, see below)
./mvnw clean package         # Build the jar
```

The app runs on port `8080` with context path `/clinica/v1` (e.g. `http://localhost:8080/clinica/v1/cliente/listar-todos`).

## Database

- Active profile is `dev` (`spring.profiles.active=dev`, set in `application.properties`). `application-dev.properties` and `application-prod.properties` are currently identical.
- Requires a local MySQL instance at `jdbc:mysql://localhost:3306/clinica`, credentials `root`/`1234` (see `application-dev.properties`). No schema/migration files exist in the repo — the `clinica` schema (tables `cliente`, `mascota`, `raza`) must already exist in the target MySQL instance.
- `spring.jpa.show-sql=true` — SQL statements are logged to stdout.

## Architecture

Each domain (Cliente, Mascota, Clinica-generic) follows the same layered pattern; when adding a new domain, replicate it across all five layers:

1. **`api/`** — interface defining the REST contract: `@RequestMapping` base path, `@GetMapping`/`@PostMapping` methods with request/response types. `@CrossOrigin(origins = "*")` is set per-interface.
2. **`apicontroller/`** — `@RestController` implementing the corresponding `api` interface, delegating directly to a `service`.
3. **`service/`** — interface declaring business operations.
4. **`serviceimpl/`** — `@Service` implementation containing all validation and business logic, using `@Autowired` field injection into `repository` interfaces.
5. **`repository/`** — `JpaRepository` interfaces (Spring Data), with derived query methods (e.g. `findAllByRaza`, `findAllByCliente`) where needed.

Request/response bodies that aren't entities live in `models/` (e.g. `MascotaRq` for pet create/update, `MiRespuestaRS` as a generic `{status, message}` response envelope). JPA entities live in `entity/` and use Lombok `@Data`.

### Error handling and security

- Use `com.uniminuto.clinica.exception.BadRequestException` (400), `NotFoundException` (404), `ConflictException` (409) and `UnauthorizedException` (401); `GlobalExceptionHandler` maps them (and `AccessDeniedException` → 403) to `ErrorResponse`.
- Auth is stateless JWT (`/auth/login`); roles ADMIN, SECRETARIA, MEDICO, DESPACHO_MEDICO. The URL matrix is in `config/SecurityConfig`; `UsuarioActual` gives the authenticated user for auditing. See `PLAN_BACKEND.md` section 7 for design decisions and test users (password `Clinica2026*`).
- DB changes go in `sql/V0N__*.sql` AND `schema.sql` (Docker loads it). Swagger: `/clinica/v1/swagger-ui.html`.

## Testing

`ClinicaApplicationTests` needs the DB running; `SeguridadWebTest` (MockMvc slice) and `ReglasCriticasTest` (Mockito) do not.
