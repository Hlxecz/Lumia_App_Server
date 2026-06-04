# Repository Guidelines

## Project Structure & Module Organization
`src/main/java/com/ch4/lumia_backend` is the source of truth for the backend. Code is organized by layer: `controller`, `service`, `repository`, `entity`, `dto`, `config`, and `security`. Keep new classes in the matching package and mirror that package structure under `src/test/java`.

`src/main/resources` contains `application.properties` plus `static/` and `templates/` for any server-rendered or bundled assets. Treat `build/`, `.gradle/`, and `bin/` as generated output, not hand-edited source.

## Build, Test, and Development Commands
Use the Gradle wrapper from the repository root:

- `.\gradlew.bat bootRun` starts the Spring Boot server with local configuration.
- `.\gradlew.bat build` compiles the app, runs tests, and writes artifacts to `build/`.
- `.\gradlew.bat test` runs the JUnit 5 and Spring Security test suite only.
- `.\gradlew.bat clean` removes generated build output.

The project targets Java 17 and Spring Boot 3.4.x, so verify your JDK before debugging build issues.

## Coding Style & Naming Conventions
Follow standard Java formatting with 4-space indentation and one public class per file. Class names use `PascalCase`; methods, fields, and local variables use `camelCase`.

Match existing suffix patterns: `UserController`, `UserService`, `UserRepository`, and request/response DTOs ending in `Dto`. Prefer constructor injection with Lombok annotations such as `@RequiredArgsConstructor`. Keep REST routes under clear `/api/...` paths and avoid placing business logic in controllers.

## Testing Guidelines
Testing is set up with `spring-boot-starter-test`, JUnit 5, and `spring-security-test`. Name test classes `*Test` or `*Tests` and keep them in the corresponding package under `src/test/java`.

Add or update tests for new controller, service, repository, and security behavior. At minimum, cover the happy path plus validation, authorization, and error cases for any changed endpoint.

## Commit & Pull Request Guidelines
Recent history includes vague subjects like `commit` and `password commit`; do better. Use short, imperative commit messages such as `Add refresh token expiry check` or `Fix comment deletion authorization`.

PRs should include a clear summary, touched endpoints or entities, config/schema impact, and test evidence. For API changes, include sample request or response payloads. If you change `application.properties`, document any new required values and never commit real secrets.
