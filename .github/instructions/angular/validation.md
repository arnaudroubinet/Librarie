---
applyTo: "**/frontend/**"
---

# Golden Rules (ALWAYS follow)
1. Build fast: run `mvn quarkus:dev`, read the continous testing documentation at https://quarkus.io/guides/continuous-testing and use it at http://localhost:8080/q/dev-ui/continuous-testing
2. Before commit always do a `mvn clean install` into the /backend directory, build must be OK and don't have any error.
3. Before asking a review always do a `mvn clean install` into the /backend directory, build must be OK and don't have any error. Add any warning in a commentary.
4. As proof add the duration of build and the state at the end of the commit message
5. Always use `context7` first (MCP) for Angular documentation.
6. Always use `context7` first (MCP) for UI/UX Design design pattern.
7. Always use `angular-cli` MCP server for angular interaction
8. Minimize new dependencies: justify each new library (prefer Quarkus extensions already present); remove unused imports/config.
7. **Frontend Style Guide Compliance**: ALWAYS ensure the `/frontend` directory and Angular project follows the [Angular Style Guide](https://angular.dev/style-guide) including naming conventions, file organization, and code structure.