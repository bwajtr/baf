# AGENTS.md

Instructions for AI coding agents working in this repository.

## Build & Test Commands

```bash
# Build entire project
cd sources && mvn clean install

# Build without tests
cd sources && mvn clean install -DskipTests

# Run the application (requires local dev environment)
cd sources/ui && mvn spring-boot:run

# Run a single test class
cd sources && mvn test -pl backend -Dtest=ClassName

# Run a single test method
cd sources && mvn test -pl backend -Dtest=ClassName#methodName

# Run UI module tests
cd sources && mvn test -pl ui -Dtest=ClassName

# Generate jOOQ code (requires database running)
cd sources/backend && mvn jooq-codegen:generate

# Flyway migrations
cd sources/backend && mvn flyway:migrate
cd sources/backend && mvn flyway:clean flyway:migrate  # Reset and re-run

# Start local dev environment (PostgreSQL + Keycloak)
cd deployment/baf-local-dev && docker compose up -d
```

## Project Structure

```
sources/
├── backend/          # Spring Boot: REST API, services, repositories, DB
│   └── src/main/java/com/wajtr/baf/
│       ├── core/           # Config, datasource, multi-tenancy, i18n
│       ├── user/           # User entity, repository, authentication
│       ├── authentication/ # Security services, OAuth2
│       ├── organization/   # Tenant/organization management
│       └── features/       # Domain modules (e.g., product/)
├── ui/               # Vaadin 25 frontend: views, components
│   └── src/main/java/com/wajtr/baf/ui/
│       ├── base/           # MainLayout, ViewToolbar
│       ├── components/     # Reusable UI components
│       ├── views/          # Vaadin routes by domain
│       └── vaadin/extensions/ # Kotlin extensions
```

## Technology Stack

- **Language**: Kotlin 2.3.0 / Java 25
- **Framework**: Spring Boot 4.0.0
- **UI**: Vaadin 25 with Karibu DSL
- **Database**: PostgreSQL 18, jOOQ 3.20.7, Flyway
- **Auth**: Spring Security + OAuth2/OIDC (Keycloak)

## Code Style Guidelines

### Naming Conventions
- Classes: `PascalCase` (e.g., `UserRepository`, `MemberManagementService`)
- Functions/methods: `camelCase` (e.g., `findById`, `loadMembers`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `MEMBERS_PAGE`, `LOGIN_PATH`)
- Properties: `camelCase` (e.g., `authenticatedUser`, `grantedRoles`)
- Packages: `lowercase` (e.g., `com.wajtr.baf.organization.member`)

### File Organization
- One primary class per file
- Related data classes, enums, sealed classes may be in same file
- Result types (sealed classes/enums) at end of file
- Kotlin files are in `src/main/java/` (not `src/main/kotlin/`)

### Import Order
1. Java/Kotlin stdlib
2. Third-party libraries
3. Project packages
- Wildcard imports OK for Karibu DSL: `import com.github.mvysny.karibudsl.v10.*`

### Kotlin Idioms
- Use `data class` for DTOs and entities
- Use `sealed class` for result types with multiple states
- Use `object` for singletons
- Use `?.` and `?:` for null safety
- Use `when` expressions for type dispatch
- Use trailing lambdas for DSL-style code

### Formatting
- 4-space indentation
- Opening braces on same line
- Blank lines between functions/classes

## Architectural Patterns

### Repository Pattern
```kotlin
@Repository
@Transactional
class UserRepository(private val create: DSLContext) {
    fun findById(id: UUID): User? {
        return create.selectFrom(APP_USER)
            .where(APP_USER.ID.eq(id))
            .fetchOne(mapIntoUser)
    }
}
```
- Use jOOQ `DSLContext` for type-safe SQL
- `RecordMapper` lambdas for mapping to domain objects
- Return nullable types for single results

### Service Pattern
```kotlin
@Service
@Transactional
class MemberManagementService(
    private val userRoleTenantService: UserRoleTenantService,
    private val identity: Identity
) {
    fun canUserLeaveOrganization(userId: UUID, tenantId: UUID): MemberOperationResult
}
```
- Constructor injection for dependencies
- Return sealed class results for operations that can fail

### Error Handling
- Use `sealed class` for operation results (Allowed/Denied patterns)
- Use enums for simple success/failure scenarios
- Throw exceptions only for truly exceptional cases
- UI: `showErrorNotification()` and `showSuccessNotification()`

### Multi-Tenancy
- Tenant context set per transaction via PostgreSQL session parameters
- Tables use `tenant_id` column with Row Level Security
- Access current tenant via `Identity.authenticatedTenant`

## UI Patterns (Vaadin)

### Page Structure
```kotlin
@PermitAll
@Route("members", layout = MainLayout::class)
class MembersPage(
    private val identity: Identity,
    private val userRepository: UserRepository
) : MainLayoutPage() {
    init {
        // Build UI in init block using Karibu DSL
    }
}
```

### CRITICAL: Theming (Aura, NOT Lumo)
- Uses **Aura theme** - Do NOT use `--lumo-*` variables
- Use `--vaadin-*` variables: `--vaadin-padding-m`, `--vaadin-text-color-secondary`
- Use `--aura-*` for accents: `--aura-red`, `--aura-green`
- Buttons: `ButtonVariant.AURA_PRIMARY` (not `LUMO_PRIMARY`)

### i18n
- Messages in `messages.properties`
- Access via `i18n("key")` or `i18n("key", arg1, arg2)`
- Key format: `domain.component.property` (e.g., `members.page.header`)

## Database Patterns

### Migration Files
- Location: `sources/backend/src/main/resources/db/migration/`
- Sequential: `V{number}__{description}.sql` (two underscores)
- Repeatable: `R__{number}__{description}.sql`

### Multi-tenant Table Template
```sql
CREATE TABLE example (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    -- columns
    tenant_id UUID NOT NULL DEFAULT current_tenant() 
        REFERENCES tenant (id) ON DELETE CASCADE
);
COMMENT ON TABLE example IS 'Description';
SELECT apply_tenant_policy('public', 'example', 'tenant_id');
```

### jOOQ
- Generated code in `sources/backend/src/gen/jooq/` - **DO NOT EDIT**
- Regenerate after schema changes: `mvn jooq-codegen:generate`
- Access tables: `Tables.TABLE_NAME`

## Common Gotchas

1. Always run `mvn jooq-codegen:generate` after changing database schema
2. Local dev DB requires Docker Compose to be running
3. Keycloak needs ~20 seconds after startup before users are created
4. Two DB users: `dbadmin` for migrations, `dbuser` for runtime
5. Vaadin uses Aura theme - never use Lumo CSS variables
