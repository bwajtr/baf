# AGENTS.md

Instructions for AI coding agents working in this repository.

## Git Worktree Setup

**IMPORTANT:** This repository uses Git worktrees. You are working on the **`opencode` branch**. When the user says "your branch", they mean `opencode`. For branch reset instructions, use the `/reset-branch` command.

## Build & Test Commands

### Quick Reference for AI Agents

**MOST IMPORTANT:** When verifying code changes, ALWAYS use the fast compilation command:

| Task                                     | Command                                                                                      |
|------------------------------------------|----------------------------------------------------------------------------------------------|
| **✅ Verify code compiles (RECOMMENDED)** | `mvn compile -DskipTests -Dvaadin.skip -Dkotlin.compiler.incremental=true`                   |
| Run tests (unit and integration)         | `mvn verify -Dkotlin.compiler.incremental=true`                                              |
| Full build (slow)                        | `mvn clean install`                                                                          |
| Run application                          | `mvn -pl sources/ui spring-boot:run`                                                         |

### Detailed Commands

```bash
# === FAST COMPILATION (USE THIS for verifying code changes) ===

# Compile to verify code changes - FASTEST method for AI agents
# Skips tests, Vaadin build, uses incremental Kotlin compilation
# ALWAYS USE THIS after making code changes to verify compilation
mvn compile -DskipTests -Dvaadin.skip -Dkotlin.compiler.incremental=true

# Clean build (removes incremental compilation cache, use when compilation issues occur)
mvn clean compile -DskipTests -Dvaadin.skip -Dkotlin.compiler.incremental=true

# === FULL BUILDS (slow, only when necessary) ===

# Build entire project (includes Vaadin frontend, tests, packaging)
mvn clean install

# === RUNNING THE APPLICATION ===

# Run the application (requires local dev environment)
mvn -pl sources/ui spring-boot:run

# === TESTING ===

# Run all tests (unit + integration)
mvn verify -Dkotlin.compiler.incremental=true

# Run only unit tests (*Test.kt, fast, no Testcontainers)
mvn test -Dkotlin.compiler.incremental=true

# Run a single unit test class
mvn test -pl sources/backend -Dtest=SecureRandomExtensionsTest -Dkotlin.compiler.incremental=true

# Run a single integration test class
mvn verify -pl sources/backend -Dit.test=MemberManagementServiceIT -Dkotlin.compiler.incremental=true

# === DATABASE ===

# Generate jOOQ code (requires database running)
cd sources/backend && mvn jooq-codegen:generate

# Flyway migrations
cd sources/backend && mvn flyway:migrate
cd sources/backend && mvn flyway:clean flyway:migrate  # Reset and re-run

# === LOCAL DEVELOPMENT ENVIRONMENT ===

# Start local dev environment (PostgreSQL + Keycloak)
cd deployment/baf-local-dev && docker compose up -d
```

## Project Structure

```
sources/
├── backend/          # Core business logic, services, repositories, DB
│   └── src/main/kotlin/com/wajtr/baf/
│       ├── core/           # Config, datasource, multi-tenancy, i18n
│       ├── user/           # User entity, repository, authentication
│       ├── authentication/ # Security services, OAuth2
│       ├── organization/   # Tenant/organization management
│       └── product/        # Domain modules (e.g., product)
├── api/              # REST API controllers, API security config
│   └── src/main/kotlin/com/wajtr/baf/api/
│       ├── product/        # Product API endpoints
│       └── user/           # User-related API endpoints (email verification, etc.)
├── ui/               # Vaadin 25 frontend: views, components
│   └── src/main/kotlin/com/wajtr/baf/ui/
│       ├── base/           # MainLayout, ViewToolbar
│       ├── components/     # Reusable UI components
│       ├── views/          # Vaadin routes by domain
│       └── vaadin/extensions/ # Kotlin extensions
```

**Module Dependencies:**
- `api` depends on `backend`
- `ui` depends on `backend` and `api`

## Technology Stack

- **Language**: Kotlin 2.3.0 (pure Kotlin project)
- **Framework**: Spring Boot 4.0.0
- **UI**: Vaadin 25 with Karibu DSL
- **Database**: PostgreSQL 18, jOOQ 3.20.7, Flyway
- **Auth**: Spring Security + OAuth2/OIDC (Keycloak)
- **JSON**: Jackson 3.x (`tools.jackson` group ID)

### CRITICAL: Jackson 3.x (not 2.x)

Spring Boot 4 uses Jackson 3.x (`tools.jackson` group ID). Always use `tools.jackson.databind.ObjectMapper` — the old `com.fasterxml.jackson` 2.x artifacts are still on the classpath transitively, but Spring Boot does **not** register a bean for the 2.x `ObjectMapper` and injecting it will cause a `NoSuchBeanDefinitionException` at startup.

## REST API Patterns

### API Versioning

All REST API endpoints use version 1 with the prefix `/api/v1`. This is enforced by using a constant and class-level `@RequestMapping`.

**Constant Location**: `sources/api/src/main/kotlin/com/wajtr/baf/api/ApiConstants.kt`

```kotlin
const val API_V1_PREFIX = "/api/v1"
```

**Controller Pattern**:

```kotlin
import com.wajtr.baf.api.API_V1_PREFIX

@RestController
@RequestMapping(API_V1_PREFIX)
class MyApiController {
    
    @GetMapping("/myendpoint")  // Results in: GET /api/v1/myendpoint
    fun myEndpoint(): ResponseData {
        // ...
    }
}
```

**IMPORTANT**: Always use `@RequestMapping(API_V1_PREFIX)` at the class level for all new REST API controllers.

### Special Endpoints (Non-REST)

Some endpoints are not part of the REST API and should NOT use the `/api/v1` prefix:

- **Authentication callbacks**: Use `/auth/*` prefix (e.g., `/auth/confirm` for email verification)
- **OAuth2 endpoints**: Handled by Spring Security, no custom prefix needed

**Example**:

```kotlin
@Controller
class EmailVerificationController {
    
    @GetMapping("/auth/confirm")  // NOT /api/v1/auth/confirm
    fun confirmEmailAddress(@RequestParam key: String): String {
        // Special callback endpoint - not a REST API
    }
}
```

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
- Kotlin files are in `src/main/kotlin/` and `src/test/kotlin/`

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

### Service and Repository Architecture

**General Principle**: Business logic should be organized into `*Service` and `*Repository` classes following a clear separation of concerns.

#### Service Layer (`*Service` classes)
- **Purpose**: Encapsulate business logic and orchestrate multiple operations
- **When to create**: When you have complex business logic, multiple business methods, or need to coordinate multiple repositories
- **Responsibilities**:
  - Implement business rules and validation
  - Coordinate multiple repository calls
  - Call other services when needed
  - Return domain-specific result types (sealed classes, enums)
- **Database access**: 
  - Prefer delegating to `*Repository` classes
  - Simple queries can be added directly to Service if no suitable Repository exists
- **Example**: See `MemberManagementService`, `MemberInvitationService` in `sources/backend/src/main/kotlin/com/wajtr/baf/organization`

```kotlin
@Service
@Transactional
class MemberManagementService(
    private val userRoleTenantRepository: UserRoleTenantRepository,
    private val identity: Identity
) {
    // Business logic with multiple checks
    fun canUserLeaveOrganization(userId: UUID, tenantId: UUID): MemberOperationResult {
        if (userRoleTenantRepository.isUserLastOwnerInTenant(userId, tenantId)) {
            return MemberOperationResult.Denied(DenialReason.LAST_OWNER_CANNOT_LEAVE)
        }
        return MemberOperationResult.Allowed
    }
    
    // Orchestrating multiple operations
    fun setUserRolesForTenant(userId: UUID, tenantId: UUID, 
                              primaryRole: String, additionalRights: Set<String>): MemberOperationResult {
        // Validation logic
        val roleChangeResult = canUserRoleBeChanged(userId, tenantId, primaryRole)
        if (roleChangeResult is MemberOperationResult.Denied) {
            return roleChangeResult
        }
        // Coordinate repository operations
        userRoleTenantRepository.deleteAllRolesForUserInTenant(userId, tenantId)
        userRoleTenantRepository.insertRole(UserRoleTenant(userId, primaryRole, tenantId))
        additionalRights.forEach { role ->
            userRoleTenantRepository.insertRole(UserRoleTenant(userId, role, tenantId))
        }
        return MemberOperationResult.Allowed
    }
}
```

#### Repository Layer (`*Repository` classes)
- **Purpose**: Handle database access using jOOQ
- **When to create**: When you have reusable database queries or complex SQL operations
- **Responsibilities**:
  - Execute database queries via jOOQ `DSLContext`
  - Map database records to domain objects
  - Provide data access methods with clear, descriptive names
- **Best practices**:
  - Use `@Repository` or `@Service` annotation (both work, use `@Service` for consistency in this project)
  - Use `@Transactional` annotation
  - Use `RecordMapper` lambdas for mapping to domain objects
  - Return nullable types for single results, collections for multiple results
- **Example**: See `UserRoleTenantRepository`, `MemberInvitationRepository` in `sources/backend/src/main/kotlin/com/wajtr/baf/organization`

```kotlin
@Service
@Transactional
class UserRoleTenantRepository(private val dslContext: DSLContext) {
    
    fun getRolesForUserInTenant(userId: UUID, tenantId: UUID): List<String> {
        return dslContext.select(APP_USER_ROLE_TENANT.ROLE)
            .from(APP_USER_ROLE_TENANT)
            .where(APP_USER_ROLE_TENANT.USER_ID.eq(userId))
            .and(APP_USER_ROLE_TENANT.TENANT_ID.eq(tenantId))
            .fetchInto(String::class.java)
    }
    
    fun isUserLastOwnerInTenant(userId: UUID, tenantId: UUID): Boolean {
        val ownerCount = dslContext.selectCount()
            .from(APP_USER_ROLE_TENANT)
            .where(APP_USER_ROLE_TENANT.TENANT_ID.eq(tenantId))
            .and(APP_USER_ROLE_TENANT.ROLE.eq(UserRole.OWNER_ROLE))
            .fetchOne(0, Int::class.java) ?: 0
        
        return ownerCount == 1 && isUserOwnerInTenant(userId, tenantId)
    }
}
```

#### When to Use Which Pattern
1. **Create a `*Service`** when:
   - You have complex business logic or business rules to enforce
   - You need to coordinate multiple repository calls
   - You need to call other services
   - You have multiple related business methods

2. **Create a `*Repository`** when:
   - You have reusable database queries
   - Your SQL queries are complex
   - You want to centralize data access for a specific entity/domain

3. **Put simple queries in `*Service`** when:
   - The query is simple and used only in one place
   - No suitable repository exists and creating one would be overkill
   - The query is tightly coupled to the business logic

#### Naming Conventions
- Services: `*Service` (e.g., `MemberManagementService`, `MemberInvitationService`)
- Repositories: `*Repository` (e.g., `UserRepository`, `UserRoleTenantRepository`)
- Result types: Sealed classes or enums (e.g., `MemberOperationResult`, `InviteMembersResult`)

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

### Date and Time Formatting
- **ALWAYS use `toLocalizedString` extension functions** when displaying dates/times to users
- Import from `com.wajtr.baf.ui.l12n.toLocalizedString`
- Available variants:
  - `instant.toLocalizedString(FormatStyle.MEDIUM)` - Single style for both date and time
  - `instant.toLocalizedString(FormatStyle.MEDIUM, FormatStyle.SHORT)` - Separate styles for date and time
  - `instant.toLocalizedString(DateTimeFormatter.ofPattern("..."))` - Custom formatter
- These functions automatically:
  - Convert from server timezone (GMT) to user's timezone
  - Format according to user's locale
  - Handle VaadinSession context
- **DO NOT** use `DateTimeFormatter` directly or `ZoneId.systemDefault()` in UI code
- If you need to get ZoneId of the currently logged user in the UI module, use `UI.getCurrent().page.extendedClientDetails.timeZone`


### CRITICAL: Locale and Timezone in User-Facing Content
- **NEVER use system locale (`Locale.getDefault()`) or system timezone (`ZoneId.systemDefault()`)** when generating content for users (emails, notifications, formatted dates/times)
- Always pass the user's locale and timezone explicitly from the UI layer to backend services
- In Vaadin UI code:
  - Get user's locale: `VaadinSession.getCurrent().locale`
  - Get user's timezone: `UI.getCurrent().page.extendedClientDetails.timeZone`
- Backend services that generate user-facing content (e.g., email senders) should:
  - Accept `locale: Locale` and `zoneId: ZoneId` as **required parameters** (not optional with system defaults)
  - Use these parameters when formatting dates, times, and localized messages
- **For background tasks** (scheduled jobs, async processing) where UI context is not available:
  - Use user's stored preferences from the `user_account` table: `preferred_locale` and `preferred_timezone_id` columns
  - These values are persisted when the user logs in or updates their preferences
  - Fall back to sensible defaults (e.g., `Locale.ENGLISH`, `ZoneId.of("UTC")`) only if user preferences are null
- Example:
  ```kotlin
  // CORRECT: UI layer passes user's locale and timezone
  passwordChangeMailSender.sendPasswordChangedNotification(
      user.email,
      VaadinSession.getCurrent().locale,
      UI.getCurrent().page.extendedClientDetails.timeZone
  )
  
  // CORRECT: Background task uses stored user preferences
  val locale = user.preferredLocale ?: Locale.ENGLISH
  val zoneId = user.preferredTimezoneId?.let { ZoneId.of(it) } ?: ZoneId.of("UTC")
  scheduledNotificationSender.sendNotification(user.email, locale, zoneId)
  
  // WRONG: Using system defaults
  fun sendEmail(email: String, locale: Locale = Locale.getDefault()) // Don't do this!
  ```
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
- **Location**: `sources/backend/src/test/kotlin/`
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
- **Location**: `sources/backend/src/test/kotlin/`
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

## Build Optimization

### Maven Build Performance

This project uses several Maven flags to optimize build performance during development:

**Optimization Flags:**

- **`-Dvaadin.skip`**: Skips Vaadin frontend build (npm install, webpack, etc.)
  - The Vaadin frontend build is time-consuming and only needed when:
    - Building the final executable JAR
    - Running the application with `mvn spring-boot:run`
    - Making changes to frontend resources or TypeScript/JavaScript
  - **Always use** when you only need to verify Kotlin code compilation

- **`-Dkotlin.compiler.incremental=true`**: Enables Kotlin incremental compilation
  - Only recompiles changed files and their dependencies
  - Can reduce compilation time from minutes to seconds
  - **Always use** during iterative development

- **`-DskipTests`**: Skips running tests during compilation
  - Use when you only need to verify that code compiles
  - Run tests separately after compilation succeeds

**When to Use `mvn clean`:**

Use `mvn clean` to remove the `target/` directory and force a full rebuild when:
- Incremental compilation produces unexpected errors
- After major refactoring across many files
- After pulling significant changes from version control
- After changing build configuration (pom.xml)
- When you suspect stale compiled classes are causing issues

**Example workflow:**
```bash
# 1. Make code changes
# 2. Verify compilation (fast - seconds)
mvn compile -DskipTests -Dvaadin.skip -Dkotlin.compiler.incremental=true

# 3. If compilation succeeds, run tests for a final verification of your changes
mvn verify -Dkotlin.compiler.incremental=true

# 4. If you encounter strange compilation errors, clean and retry
mvn clean compile -DskipTests -Dvaadin.skip -Dkotlin.compiler.incremental=true
```

## Common Gotchas

1. Always run `mvn jooq-codegen:generate` after changing database schema
2. Local dev DB requires Docker Compose to be running
3. Keycloak needs ~20 seconds after startup before users are created
4. Two DB users: `dbadmin` for migrations, `dbuser` for runtime
5. Vaadin uses Aura theme - never use Lumo CSS variables
