---
applyTo: "**/backend/**"
---

# Golden Rules (ALWAYS follow)
1. Build fast: run `mvn quarkus:dev`, read the continous testing documentation at https://quarkus.io/guides/continuous-testing and use it at http://localhost:8080/q/dev-ui/continuous-testing
2. Before commit always do a `mvn clean install` into the /backend directory, build must be OK and don't have any error.
3. Before asking a review always do a `mvn clean install` into the /backend directory, build must be OK and don't have any error. Add any warning in a commentary.
4. As proof add the duration of build and the state at the end of the commit message
5. Always use `context7` first (MCP) for Quarkus documentation.
6. Always use `context7` first (MCP) for Java design pattern.
7. Minimize new dependencies: justify each new library (prefer Quarkus extensions already present); remove unused imports/config.
8. **NEVER use H2 database**: Always use PostgreSQL for all environments. H2 is prohibited.
9. **Backend Architecture Compliance**: ALWAYS ensure the `/backend` follows [hexagonal architecture principles](https://scalastic.io/en/hexagonal-architecture-domain) with proper separation of domain, application, and infrastructure layers.

## Configuration Overview
Files:
- `application.properties`: base config (port 8080, name, version).
- `application-dev.properties`: Only add overrides for development-specific settings (e.g., enabling debug logging).
- `application-prod.properties`: Enforce production-specific settings (e.g., disabling open api portail)

**Database Configuration - CRITICAL:**
- ALWAYS use PostgreSQL (`quarkus-jdbc-postgresql` dependency).
- NEVER use H2 database (`quarkus-jdbc-h2`) - it is prohibited in all environments.
- PostgreSQL configuration must be in prod profiles, not common application.properties and never defined in dev properties in order to use dev tools.