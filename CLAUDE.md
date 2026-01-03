# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build entire project
cd sources && mvn clean install

# Build without tests
cd sources && mvn clean install -DskipTests

# Run the application (from ui module)
cd sources/ui && mvn spring-boot:run

# Run a single test class
cd sources && mvn test -pl backend -Dtest=ClassName

# Run a single test method
cd sources && mvn test -pl backend -Dtest=ClassName#methodName

# Generate jOOQ code (requires database running)
cd sources/backend && mvn jooq-codegen:generate

# Run Flyway migrations manually
cd sources/backend && mvn flyway:migrate

# Clean Flyway and re-run all migrations
cd sources/backend && mvn flyway:clean flyway:migrate
```

## Local Development Setup

Start the local development environment with Docker Compose:
```bash
cd deployment/baf-local-dev && docker compose up -d
```

This starts:
- PostgreSQL 18 on port 5432
- Keycloak on port 9090 (admin/password) - you have to wait about 20 seconds before realm and users are automatically created inside Keycloak

Database credentials for local dev:
- `dbadmin/dbadmin` - for migrations and jOOQ generation
- `dbuser/dbuser` - for application runtime

## Architecture Overview

### Module Structure
- **sources/backend** - Spring Boot backend: REST API, services, repositories, database layer
- **sources/ui** - Vaadin 25 frontend: views, components, layouts (depends on backend)

### Technology Stack
- **Language**: Kotlin 2.3.0 / Java 25
- **Framework**: Spring Boot 4.0.0
- **UI**: Vaadin 25 with Karibu DSL (Kotlin DSL for Vaadin)
- **Database**: PostgreSQL with jOOQ (type-safe SQL) and Flyway (migrations)
- **Authentication**: Spring Security with form login and OAuth2/OIDC (Keycloak)

### Key Architectural Patterns

**Multi-Tenancy**: Core architectural pattern. Tenant context is set per transaction via PostgreSQL session parameters. Users can belong to multiple tenants with different roles. User can have multiple roles.

**Database Layer**:
- Migrations in `sources/backend/src/main/resources/db/migration/`
- jOOQ generated code in `sources/backend/src/gen/jooq/` (regenerate after schema changes)
- Two database users: `dbadmin` for DDL/migrations, `dbuser` for application queries

**Service/Repository Pattern**:
- Repositories use jOOQ DSLContext for type-safe queries
- Services handle business logic and transactions
- `@Transactional` annotations on service methods

**UI Structure**:
- `ApplicationPage` - base class for all routed pages with dynamic titles
- `MainLayout` - app shell with navigation drawer
- Views use `@Route` and `@Menu` annotations for navigation
- Karibu DSL for building Vaadin components in Kotlin

### Important Packages

Backend (`sources/backend/src/main/java/com/wajtr/baf/`):
- `core/` - configuration, datasource, multi-tenancy, i18n
- `user/` - user entity, repository, authentication
- `authentication/` - security services, OAuth2 integration
- `organization/member/` - organization membership management
- `product/` - example domain module

UI (`sources/ui/src/main/java/com/wajtr/baf/ui/`):
- `base/` - MainLayout, ViewToolbar
- `components/` - reusable UI components
- `views/` - Vaadin routes organized by domain
- `security/` - frontend security configuration
- `vaadin/extensions/` - Kotlin extension functions for Vaadin

### Configuration Files
- `sources/backend/src/main/resources/application.properties` - main config
- `sources/backend/src/main/resources/application-production.properties` - production overrides
- `sources/backend/src/main/resources/messages.properties` - i18n messages
