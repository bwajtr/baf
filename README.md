# BAF - Business Application Framework

A production-ready, multi-tenant application framework for building SaaS business applications with Kotlin, Spring Boot 4, Vaadin 25, and PostgreSQL.

BAF provides the common infrastructure that every multi-tenant business application needs -- user management, organization management, authentication, role-based access control, email notifications, and a REST API with key-based authentication -- so you can focus on building your domain-specific features.

## Why BAF?

Building a multi-tenant SaaS application from scratch means solving the same set of foundational problems every time: user registration, login flows, organization management, member invitations, role-based access control, tenant data isolation, email notifications, API authentication, and more. These concerns are complex, security-sensitive, and time-consuming to implement correctly.

BAF solves these problems once, with carefully chosen technologies and architectural patterns, giving you a solid starting point for your next business application.

### When to Use BAF

BAF is a good fit if you are building:

- A **multi-tenant SaaS application** where each customer (organization) has isolated data
- A **business application** that needs user management, roles, and permissions out of the box
- An application that needs both a **web UI** and a **REST API**
- A **server-rendered** web application where you want to write UI logic in Kotlin (via Vaadin), not in a separate JavaScript/TypeScript frontend
- A project where you prefer **type-safe SQL** (jOOQ) over ORM abstractions (JPA/Hibernate)

### When NOT to Use BAF

BAF may not be the right choice if:

- You need a **single-tenant** application with no multi-tenancy requirements
- You want to build a **SPA frontend** with React, Angular, or Vue.js (BAF uses Vaadin for server-side UI rendering)
- You prefer **JPA/Hibernate** over jOOQ for data access
- You need to support databases other than **PostgreSQL** (the multi-tenancy implementation relies on PostgreSQL-specific features like Row Level Security and session parameters)
- You need a **microservices architecture** (BAF is a modular monolith)

## Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| **Kotlin** | 2.3.0 | Primary language (pure Kotlin project, no Java) |
| **Java** | 25 | Runtime platform |
| **Spring Boot** | 4.0.0 | Application framework |
| **Vaadin** | 25 | Web UI framework (server-side rendering) |
| **Karibu DSL** | 2.3.2 | Kotlin DSL for building Vaadin UIs |
| **PostgreSQL** | 18 | Database with Row Level Security for tenant isolation |
| **jOOQ** | 3.20.7 | Type-safe SQL query builder and code generator |
| **Flyway** | 11 | Database migration management |
| **Keycloak** | 26 | Identity provider for OAuth2/OIDC login (optional) |
| **Brevo** | - | Transactional email delivery (production) |
| **FreeMarker** | - | Email template engine |
| **Testcontainers** | 2.0.3 | Integration testing with real PostgreSQL |
| **Jackson** | 3.x | JSON serialization (Spring Boot 4 uses `tools.jackson` group) |

## Features

### Multi-Tenancy with PostgreSQL Row Level Security

Data isolation between tenants is enforced at the database level using PostgreSQL Row Level Security (RLS). Every tenant-scoped table has an RLS policy that automatically filters rows based on the current tenant context. This means:

- **No WHERE clauses needed** -- tenant filtering is automatic and cannot be bypassed by application code
- **Fail-safe** -- queries against tenant-scoped tables without a tenant context raise an error instead of returning unfiltered data
- **Framework-agnostic** -- works with jOOQ, raw JDBC, or any other data access method that uses the same DataSource

The tenant context is set via PostgreSQL session parameters on every database connection. A custom `TenantAwareDataSource` reads the current tenant from the Spring Security context and configures the connection before handing it to the application. When the connection returns to the pool, the context is cleared to prevent leakage between requests.

```
HTTP Request → Spring Security → Identity (tenant + user) → TenantAwareDataSource
    → SET session.tenant.id on connection → PostgreSQL RLS filters all queries
```

### Dual Database User Model

BAF uses two separate PostgreSQL users with different privilege levels:

- **`dbadmin`** -- Used exclusively by Flyway for schema migrations. Owns tables, can create RLS policies, and bypasses Row Level Security. Never used at runtime.
- **`dbuser`** -- Used by the application at runtime. Has restricted privileges and is subject to all RLS policies. Cannot modify schema or disable security policies.

This separation ensures that even if the application has a SQL injection vulnerability, the attacker cannot alter the database schema or bypass tenant isolation.

### Authentication

BAF supports three authentication methods:

| Method | Use Case | Token Type |
|---|---|---|
| **Database login** (email + password) | Web UI login | `UsernamePasswordAuthenticationToken` |
| **OAuth2/OIDC** (Keycloak) | Web UI SSO login | `OAuth2TenantAuthenticationToken` |
| **API key** (X-API-Key header) | REST API access | `TenantApiKeyAuthenticationToken` |

Password verification happens inside the database using `pgcrypto`, so the application never sees stored password hashes. OAuth2 provides identity verification through the external IdP, while authorization (roles, tenant membership) is always resolved from the local database.

### User Registration and Onboarding

- **Self-service registration** with email, password, and organization name
- **Email verification** with tokenized confirmation links
- **Password reset** flow with secure, time-limited tokens
- **Auto-login** after successful registration

### Organization and Member Management

- **Multi-organization membership** -- users can belong to multiple organizations with different roles in each
- **Organization switching** -- users can switch between their organizations without logging out
- **Member invitation system** -- invite users by email with a specific role; invitations can be accepted, resent, or cancelled
- **Role-based access control** with four roles:
  - **Owner** -- full control, can manage all members and settings, can promote others to Owner
  - **Admin** -- can manage members (except Owners), invite new members
  - **Billing Manager** -- additional right that can be combined with other roles
  - **User** -- basic access with no management privileges
- **Owner protection** -- the system enforces that at least one Owner always exists per organization (cannot remove, demote, or allow the last Owner to leave)
- **Organization settings** -- rename, configure, or delete an organization (Owner only)
- **User settings** -- update profile, change password, delete account

### REST API with API Key Authentication

- Versioned REST API with `/api/v1` prefix
- Per-tenant API keys for programmatic access
- API key management UI (generate, copy, reset) for Owners and Admins
- Separate Spring Security filter chain for API endpoints
- Custom error handling with consistent JSON error responses

### Email System

A pluggable email system with two implementations:

- **Brevo (production)** -- transactional email delivery via the Brevo API, activated by configuration
- **Local file preview (development)** -- saves emails as HTML files to disk for local inspection (default)

All emails use FreeMarker templates with a shared base layout, supporting both HTML and plain text variants. The system sends emails for:

- Email verification
- Password reset
- Password change confirmation (security notification)
- Member invitation
- Account deletion confirmation
- Organization deletion notification

Email content is localized based on the recipient's preferred locale and timezone.

### Testing Infrastructure

BAF provides a comprehensive testing setup with three tiers:

- **Unit tests** (`*Test.kt`) -- pure JUnit 5, no Spring context, fast execution
- **Backend integration tests** (`*IT.kt`) -- full Spring context with Testcontainers PostgreSQL, automatic transactional rollback, pre-configured test users and tenants
- **API integration tests** (`*IT.kt`) -- full HTTP stack with `RestTestClient`, real database with data reload between tests, API key authentication

Test helpers provide convenient methods for authentication (`loginAsUser()`, `loginAsAdmin()`, `loginAsOwner()`), database operations (`loadBasicTestData()`, `truncateAllTables()`), and a fixed set of well-known test users and tenants with deterministic UUIDs.

### Internationalization (i18n)

- All user-facing strings externalized to `messages.properties`
- `i18n("key")` helper for accessing messages in Vaadin views
- Locale-aware date/time formatting via `toLocalizedString()` extensions
- User locale and timezone preferences stored in the database
- Email templates support locale-specific variants with automatic fallback

## Architecture

BAF is organized as a **modular monolith** with three Maven modules:

```
sources/
├── backend/     Core business logic, services, repositories, database access
├── api/         REST API controllers and API security configuration
└── ui/          Vaadin web UI: views, components, layout, frontend security
```

**Module dependencies:** `api` depends on `backend`. `ui` depends on `backend` and `api`. The `ui` module is the Spring Boot application entry point.

### Service and Repository Pattern

Business logic follows a clear separation between services and repositories:

- **`*Service` classes** encapsulate business logic, enforce rules, coordinate multiple operations, and return typed result objects
- **`*Repository` classes** handle database access via jOOQ, map records to domain objects, and provide reusable query methods

```kotlin
// Business operations return sealed classes instead of throwing exceptions
sealed class MemberOperationResult {
    data object Allowed : MemberOperationResult()
    data class Denied(val reason: DenialReason) : MemberOperationResult()
}

// Consumed with exhaustive when expressions
when (val result = memberService.canUserLeaveOrganization(userId, tenantId)) {
    is MemberOperationResult.Allowed -> { /* proceed */ }
    is MemberOperationResult.Denied -> { /* show denial reason */ }
}
```

### Multi-Tenant Table Template

Adding a new tenant-scoped table follows a standard pattern:

```sql
CREATE TABLE example (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    -- your columns here
    tenant_id UUID NOT NULL DEFAULT current_tenant()
        REFERENCES tenant (id) ON DELETE CASCADE
);
COMMENT ON TABLE example IS 'Description of the table';
SELECT apply_tenant_policy('public', 'example', 'tenant_id');
```

The `apply_tenant_policy()` function creates the RLS policy, enables RLS on the table, creates an index on the tenant column, and runs `ANALYZE`. After adding a migration, run `mvn jooq-codegen:generate` to regenerate the type-safe jOOQ code.

### Vaadin UI with Karibu DSL

Views are built using Karibu DSL, a Kotlin DSL for Vaadin that provides concise, readable UI construction:

```kotlin
@PermitAll
@Route("members", layout = MainLayout::class)
class MembersPage(
    private val memberService: MemberManagementService
) : MainLayoutPage() {
    init {
        verticalLayout {
            h2(i18n("members.page.header"))
            grid<MemberGridItem> {
                // grid configuration
            }
        }
    }
}
```

Access control uses standard Spring Security annotations: `@AnonymousAllowed` for public pages, `@PermitAll` for any authenticated user, and `@RolesAllowed` for role-restricted pages.

## Project Structure

```
baf/
├── sources/
│   ├── backend/                          # Core module
│   │   └── src/
│   │       ├── main/kotlin/com/wajtr/baf/
│   │       │   ├── core/                 # Infrastructure: datasource, email, i18n, caching
│   │       │   ├── authentication/       # Auth providers: database, OAuth2, API key
│   │       │   ├── organization/         # Tenant management: members, invitations, API keys
│   │       │   ├── user/                 # User management: registration, email verification, passwords
│   │       │   └── product/              # Example domain module
│   │       ├── main/resources/
│   │       │   ├── db/migration/         # Flyway SQL migrations
│   │       │   ├── templates/email/      # FreeMarker email templates
│   │       │   └── messages.properties   # i18n messages
│   │       └── gen/jooq/                 # Generated jOOQ code (committed to Git)
│   │
│   ├── api/                              # REST API module
│   │   └── src/main/kotlin/com/wajtr/baf/api/
│   │       ├── security/                 # API key authentication filter
│   │       └── product/                  # Example REST endpoints
│   │
│   └── ui/                               # Vaadin UI module (Spring Boot entry point)
│       └── src/main/kotlin/com/wajtr/baf/ui/
│           ├── base/                     # MainLayout, navigation menu
│           ├── components/               # Reusable UI components
│           ├── security/                 # Frontend security configuration
│           └── views/                    # Pages: login, registration, members, settings, etc.
│
└── deployment/
    └── baf-local-dev/                    # Docker Compose for local development
        ├── docker-compose.yml            # PostgreSQL 18 + Keycloak 26
        └── local-postgres-init/          # Database initialization (users, extensions)
```

## Getting Started

The primary way of using BAF is to **copy this repository and modify the classes directly** to fit your application's needs. This is intentional -- BAF is not distributed as a library or a dependency. You own all the code from day one, and you can change anything: rename packages, remove features you don't need, adjust the database schema, or rewrite the UI.

This approach is far simpler than a library-based framework. There is no framework API to learn, no extension points to discover, no version upgrades to manage, and no abstractions standing between you and your code. The result is a codebase that is straightforward to understand, debug, and customize.

### Prerequisites

- Java 25+
- Maven 3.9+
- Docker and Docker Compose (for local PostgreSQL and Keycloak)

### Local Development Setup

1. **Start the local infrastructure** (PostgreSQL and Keycloak):

   ```bash
   cd deployment/baf-local-dev
   docker compose up -d
   ```

   Wait ~20 seconds for Keycloak to fully initialize.

2. **Run database migrations:**

   ```bash
   cd sources/backend
   mvn flyway:migrate
   ```

3. **Generate jOOQ code** (if not already present):

   ```bash
   cd sources/backend
   mvn jooq-codegen:generate
   ```

4. **Run the application:**

   ```bash
   mvn -pl sources/ui spring-boot:run
   ```

   The application starts at `http://localhost:8080`.

### Running Tests

```bash
# All tests (unit + integration, requires Docker for Testcontainers)
mvn verify

# Unit tests only (fast, no Docker needed)
mvn test

# Single test class
mvn test -pl sources/backend -Dtest=SecureRandomExtensionsTest

# Single integration test class
mvn verify -pl sources/backend -Dit.test=MemberManagementServiceIT
```

### Build

```bash
# Quick compilation check (recommended during development)
mvn compile -DskipTests -Dvaadin.skip -Dkotlin.compiler.incremental=true

# Full production build
mvn clean install
```

## Design Decisions

### Why PostgreSQL Row Level Security (not application-level filtering)?

Tenant isolation at the database level means that a bug in application code cannot accidentally leak data between tenants. Every query is automatically filtered by the current tenant. There is no risk of a developer forgetting a `WHERE tenant_id = ?` clause. This is the strongest form of multi-tenancy short of separate databases per tenant.

### Why jOOQ (not JPA/Hibernate)?

jOOQ generates type-safe Kotlin code from the database schema, providing compile-time verification of queries. It does not hide SQL behind abstractions -- you write SQL, but with type safety. This makes complex queries, joins, and PostgreSQL-specific features straightforward. The generated code is committed to Git so that the project compiles without a running database.

### Why Vaadin (not React/Angular/Vue)?

Vaadin renders UI on the server, which means the entire application is written in Kotlin. There is no separate frontend project, no REST-to-UI data serialization, no client-side state management, and no TypeScript build pipeline. This is a significant productivity advantage for business applications where the UI is primarily forms, tables, and CRUD operations.

### Why sealed classes for results (not exceptions)?

Business operations return sealed class results (e.g., `Allowed` / `Denied(reason)`) instead of throwing exceptions. This forces callers to handle all outcomes via exhaustive `when` expressions, making error handling explicit and compiler-verified. Exceptions are reserved for truly exceptional situations (infrastructure failures, programming errors).

### Why dual database users?

Separating `dbadmin` (migration-only) from `dbuser` (runtime-only) follows the principle of least privilege. The runtime user cannot alter schema, create tables, or modify RLS policies. Even in a worst-case scenario where an attacker gains SQL access through the application, they cannot escalate to schema-level operations.

### Why Keycloak is optional?

BAF includes a built-in database authentication provider (email + password with `pgcrypto`). Keycloak integration is configured via Spring Boot properties and can be enabled or disabled without code changes. This means you can start with simple database auth and add SSO/OIDC later, or use a different OAuth2 provider entirely (e.g., Google SSO).

## Built with Agentic Development

This entire codebase has been developed using agentic development with [OpenCode](https://opencode.ai). From the very first commit, the project includes a carefully crafted `AGENTS.md` file that documents the architecture, design decisions, coding conventions, technology choices, and testing patterns.

This means that AI coding agents can start working on this project immediately. The `AGENTS.md` file gives them full context about the project structure, build commands, multi-tenancy patterns, service/repository architecture, UI conventions, and testing infrastructure -- so they follow the established patterns consistently, just as a new team member would after reading the project documentation.

## License

This project is licensed under the Apache License 2.0 -- see the [LICENSE](LICENSE) file for details.
