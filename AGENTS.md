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

# Run all tests (unit + integration)
cd sources/backend && mvn test verify

# Run only unit tests (*Test.kt, fast, no Testcontainers)
cd sources/backend && mvn test

# Run only integration tests (*IT.kt, slower, uses Testcontainers)
cd sources/backend && mvn verify -DskipTests

# Run a single unit test class
cd sources && mvn test -pl backend -Dtest=SecureRandomExtensionsTest

# Run a single integration test class
cd sources && mvn verify -pl backend -Dit.test=MemberManagementServiceIT

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
- **Code Generation**: Uses `KotlinGenerator` to generate Kotlin classes (not Java)
- Generated code in `sources/backend/src/gen/jooq/` - **DO NOT EDIT**
- Regenerate after schema changes: `mvn jooq-codegen:generate`
- Access tables: `Tables.TABLE_NAME` or use `com.wajtr.baf.db.jooq.tables.references.*`
- **IMPORTANT**: All generated fields are nullable (`UUID?`, `String?`, etc.) regardless of database NOT NULL constraints
  - When mapping to data classes, use `!!` for NOT NULL columns to respect proper nullability
  - Example from `UserRepository`:
    ```kotlin
    private val mapIntoUser: RecordMapper<AppUserRecord, User> = RecordMapper { record ->
        User(
            record.id!!,              // NOT NULL in DB, use !!
            record.name!!,            // NOT NULL in DB, use !!
            record.email!!,           // NOT NULL in DB, use !!
            record.emailVerified!!,   // NOT NULL in DB, use !!
            record.createdAt!!.toInstant(),
            record.emailVerificationToken  // nullable in DB, no !!
        )
    }
    ```

## Testing Patterns

### Unit Tests
- **Location**: `sources/backend/src/test/java/`
- **Naming**: `*Test.kt` (e.g., `SecureRandomExtensionsTest.kt`)
- **Characteristics**: Pure JUnit 5, no Spring context, fast execution
- **Use for**: Testing utility functions, extensions, pure logic without dependencies
- **Assertions**: Use AssertJ's `assertThat()` for fluent, readable assertions
- **Example**: `SecureRandomExtensionsTest.kt`

```kotlin
import org.assertj.core.api.Assertions.assertThat

class SecureRandomExtensionsTest {
    private val secureRandom = SecureRandom()
    
    @Test
    fun `generateRandomAlphanumeric should return string of correct length`() {
        val result = secureRandom.generateRandomAlphanumeric(10)
        
        assertThat(result)
            .hasSize(10)
            .`as`("Generated string should have exactly 10 characters")
    }
}
```

### Integration Tests
- **Location**: `sources/backend/src/test/java/`
- **Naming**: `*IT.kt` (e.g., `MemberManagementServiceIT.kt`)
- **Base Class**: Extend `BaseIntegrationTest`
- **Features**: Spring Boot context, Testcontainers (PostgreSQL 18), transactional rollback, default authentication as Owner
- **Use for**: Testing services, repositories, business logic with real database
- **Assertions**: Use AssertJ's `assertThat()` for fluent, readable assertions
- **Authentication**: Each test runs as Owner by default (via `@BeforeTransaction`), override with `authenticationTestHelper.loginAsXxx()` if needed

```kotlin
import org.assertj.core.api.Assertions.assertThat

class MyServiceIT : BaseIntegrationTest() {
    
    @Autowired
    private lateinit var myService: MyService
    
    @Test
    fun `test something as owner`() {
        // No authentication setup needed - logged in as Owner by default
        val result = myService.doSomething()
        
        assertThat(result).isEqualTo("xxx")
    }

     @Test
     fun `test something as admin`() {
        // Override default Owner authentication
        authenticationTestHelper.loginAsAdmin()
        val result = myService.doSomething()
        assertThat(result).isEqualTo("yyy")
     }
}
```

### Test Users (from integration-test-basic-database-content.sql)
- **Joe User**: USER role in Tenant 1
  - ID: `019b5aa6-97b6-7358-8ffe-bb68f70c8fc6`
  - Email: joe.user@acme.com
  - Password: test
- **Jane Admin**: ADMIN role in Tenant 1
  - ID: `019b5aa6-cd48-75f9-8b74-59878b0ea7d9`
  - Email: jane.admin@acme.com
  - Password: test
- **Josh Owner**: OWNER role in Tenant 1 (also BILLING_MANAGER)
  - ID: `019b5aa6-eae4-76f0-9077-571f50df349b`
  - Email: josh.owner@acme.com
  - Password: test
- **William Owner**: OWNER role in Tenant 2 (also BILLING_MANAGER)
  - ID: `019b5ab7-72c3-739d-b548-b13d1d59fe11`
  - Email: william.owner@acme.com
  - Password: test

### Test Tenants
- **Tenant 1**: `019b25f2-3cc6-761c-9e6e-1c0d279bfd30` (Development Tenant 1)
- **Tenant 2**: `019b25f2-6e55-7f32-bf82-9e2d116873ce` (Development Tenant 2)

### Test Helpers

#### DatabaseTestHelper
- `loadBasicTestData()` - Loads test users, tenants, and sample data
- `executeSqlScript(path)` - Execute custom SQL scripts
- `truncateAllTables()` - Manual cleanup (rarely needed due to @Transactional)

#### AuthenticationTestHelper
- `loginAsUser()` - Login as Joe (USER role, Tenant 1)
- `loginAsAdmin()` - Login as Jane (ADMIN role, Tenant 1)
- `loginAsOwner()` - Login as Josh (OWNER role, Tenant 1)
- `loginAsOwnerTenant2()` - Login as William (OWNER role, Tenant 2)
- `loginAs(userId, tenantId, roles)` - Custom login
- `logout()` - Clear security context (automatic in BaseIntegrationTest)

#### Constants for Test IDs
Available in `AuthenticationTestHelper` companion object:
- `JOE_USER_ID`, `JANE_ADMIN_ID`, `JOSH_OWNER_ID`, `WILLIAM_OWNER_ID`
- `TENANT_1_ID`, `TENANT_2_ID`

### Test Infrastructure
- **Testcontainers**: Shared PostgreSQL 18 container for all integration tests
- **Transactional Rollback**: Each test automatically rolls back database changes
- **Spring Security**: Real authentication context (no OAuth2 flow in tests)
- **Flyway**: Automatic migrations on container startup
- **OAuth2 Configuration**: OAuth2 client auto-configuration is excluded in tests via `@SpringBootApplication(exclude = [OAuth2ClientAutoConfiguration::class])` in `BackendTestApplication`. Tests use mock authentication via `AuthenticationTestHelper` instead of real OAuth2/Keycloak.
- **Timezone**: All tests run in UTC timezone with US locale for consistency

### Testing Best Practices
1. Tests run as Owner by default - no authentication setup needed unless testing with different roles
2. Override authentication with `authenticationTestHelper.loginAsXxx()` when testing other roles
3. Use descriptive test names with backticks: `` `test description` ``
4. Test both success and failure scenarios
5. Integration tests automatically roll back - no manual cleanup needed
6. Each test runs in its own transaction with automatic rollback after completion

## Common Gotchas

1. Always run `mvn jooq-codegen:generate` after changing database schema
2. Local dev DB requires Docker Compose to be running
3. Keycloak needs ~20 seconds after startup before users are created
4. Two DB users: `dbadmin` for migrations, `dbuser` for runtime
5. Vaadin uses Aura theme - never use Lumo CSS variables
