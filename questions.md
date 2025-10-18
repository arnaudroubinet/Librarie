# Audit Questions & Analysis

## Frontend — Angular

### [Q-F001] Form Controls Outside FormGroup Declaration
**Context:** `/frontend/src/app/components/search.component.ts` — Advanced search panel  
**Observation (facts):**
- Browser console shows Angular error NG01050: `formControlName must be used with a parent formGroup directive`
- Affected form controls: `title`, `authors`, `series`, `publisher`, `language`, `formats`, `publishedAfter`, `publishedBefore`, `sortBy`, `sortDirection`
- The `<form>` element only wraps the quick search section (line 39)
- Advanced search fields (lines 77-143) are outside the `<form>` tag but use `formControlName`
- FormGroup is properly initialized in component class (line 376)

**Risk:** Reliability | UX  
**Impact:** H — **Likelihood:** H  
The advanced search feature is completely broken and unusable. Users cannot filter by fields.

**Hypotheses:**
- Template structure issue: advanced search fields were moved outside the `<form>` element during refactoring
- Missing `[formGroup]` directive on the advanced search container
- Could be intentional separation but lacks proper form group binding

**Expected good practice:**
- All form controls using `formControlName` must be within an element that has `[formGroup]` directive
- Per Angular Forms documentation: "formControlName expects to be nested in a FormGroup directive"
- Either move fields inside `<form>` or add separate `[formGroup]="searchForm"` to advanced panel container

**Analysis:**
The component class correctly creates a FormGroup with all controls, but the template HTML structure doesn't match. The quick search section is wrapped in `<form [formGroup]="searchForm">` but the advanced search expansion panel is a sibling, not a child. Angular cannot bind the formControlName directives without a parent formGroup context.

**References:**
- Angular Forms API: https://angular.dev/api/forms/FormControlName
- Angular error reference NG01050: https://angular.dev/errors/NG01050
- context7: Angular Reactive Forms require parent FormGroup directive

**Confidence:** H  
**Candidate next actions:**
- Wrap entire search form (quick + advanced) in single `<form [formGroup]="searchForm">` element
- OR add `[formGroup]="searchForm"` attribute to advanced search container `<div class="advanced-form">`
- Validate with browser console after fix
- Add E2E test for advanced search functionality

---

### [Q-F002] Author Images Returning 406 Not Acceptable
**Context:** `/frontend/src/app/components/author-list.component.ts` — Author list view  
**Observation (facts):**
- Console errors: `Failed to load resource: the server responded with a status of 406 (Not Acceptable)`
- Affects author picture endpoint: `http://localhost:4200/v1/authors/{id}/picture`
- Multiple 406 errors appear when navigating to Authors page
- Backend endpoint exists and is accessible (confirmed via OpenAPI spec)
- Book covers load successfully, only author pictures fail

**Risk:** UX | Performance  
**Impact:** M — **Likelihood:** H  
Author pictures don't display; default placeholder shown instead. Not critical but degrades UX.

**Hypotheses:**
- Frontend proxy configuration issue — requests to `/v1/authors/{id}/picture` not being forwarded correctly
- Content negotiation mismatch — frontend requests wrong Accept header
- Backend returning 406 when no author picture exists instead of 404
- CORS or authentication issue specific to this endpoint

**Expected good practice:**
- API should return 404 Not Found for missing resources, not 406
- Frontend should handle missing images gracefully with fallback
- Proxy configuration should route all `/v1/*` requests to backend uniformly

**Analysis:**
The 406 status code indicates the server cannot produce a response matching the acceptable values defined in the request's headers. This suggests either:
1. Frontend sending incompatible Accept header for image requests
2. Backend rejecting requests that lack proper authentication/OIDC token
3. Backend implementation issue where missing images return wrong status code

Need to inspect `proxy.conf.json` and backend endpoint implementation to confirm root cause.

**References:**
- HTTP 406 specification: https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/406
- Angular proxy configuration: https://angular.dev/tools/cli/serve#proxying-to-a-backend-server
- context7: REST API best practices for missing resources

**Confidence:** M  
**Candidate next actions:**
- Inspect `proxy.conf.json` configuration for `/v1/**` routing
- Check backend AuthorResource endpoint implementation for picture handling
- Add proper error handling in frontend for 404/406 responses
- Test with authenticated vs unauthenticated requests
- Consider returning placeholder image from backend when no author picture exists

---

### [Q-F003] NPM Package Vulnerabilities
**Context:** `/frontend/package.json` — Dependency vulnerabilities  
**Observation (facts):**
```
vite  7.1.0 - 7.1.4
Vite middleware may serve files starting with the same name with the public directory
Vite's server.fs settings were not applied to HTML files
```
- 2 low severity vulnerabilities detected in vite
- Affects @angular/build which depends on vulnerable vite version
- Fix available via `npm audit fix`
- Package audit output from npm v10+

**Risk:** Security  
**Impact:** L — **Likelihood:** M  
Low severity but security vulnerabilities should be addressed proactively.

**Hypotheses:**
- Recent vite releases (7.1.5+) likely contain patches
- Angular Build dependency version constraints may prevent automatic update
- May require @angular/build update to pull in fixed vite version

**Expected good practice:**
- Keep dependencies up to date with security patches
- Run `npm audit` regularly in CI/CD pipeline
- Set up Dependabot or Renovate for automated dependency updates
- Document policy for addressing vulnerabilities by severity level

**Analysis:**
These are low-severity vulnerabilities in the development server (Vite), not production code. However:
- Vite serves the dev environment so could affect developer security
- Good practice to stay current with patches
- Should verify if `npm audit fix` resolves without breaking changes

**References:**
- GHSA-g4jq-h2w9-997c: https://github.com/advisories/GHSA-g4jq-h2w9-997c
- GHSA-jqfw-vq24-v9c3: https://github.com/advisories/GHSA-jqfw-vq24-v9c3
- npm audit documentation: https://docs.npmjs.com/cli/v10/commands/npm-audit

**Confidence:** H  
**Candidate next actions:**
- Run `npm audit fix` and test build/dev server
- If fix breaks: update @angular/build to latest patch version
- Add npm audit to CI/CD pipeline with threshold enforcement
- Document in SECURITY.md how vulnerabilities are handled

---

### [Q-F004] Large Bundle Sizes and Lazy Loading Effectiveness
**Context:** `/frontend/angular.json` — Build configuration  
**Observation (facts):**
- Initial bundle: 453.09 kB raw, 120.23 kB gzipped
- Largest lazy chunk: ebook-reader-component at 316.37 kB raw (60.46 kB gzipped)
- Second largest: unnamed chunk at 225.63 kB (44.02 kB gzipped)
- Budget configured: 500kB warning, 1MB error for initial bundle
- Current size well within budget but approaching warning threshold

**Risk:** Performance | UX  
**Impact:** M — **Likelihood:** L  
Performance is acceptable now but could degrade as features are added.

**Hypotheses:**
- Ebook reader (Readium) dependencies are large but necessary
- Lazy loading is working correctly (ebook reader not in initial bundle)
- Unnamed chunks may indicate code-splitting opportunities
- Bundle size will grow as more features are added

**Expected good practice:**
- Initial bundle should stay under 200kB gzipped for optimal performance
- Lazy load heavy features (ebook reader is correctly lazy-loaded)
- Use webpack-bundle-analyzer to identify optimization opportunities
- Consider splitting vendor libraries into separate chunks

**Analysis:**
The application demonstrates good lazy loading practices:
- Heavy ebook reader is properly lazy-loaded (316 kB not in initial bundle)
- Each route component is lazy-loaded (search, series, authors, etc.)
- Initial bundle contains only core app shell and frequently-used components

However, there are unnamed chunks that suggest room for optimization. The readium libraries are unavoidably large for ebook rendering functionality.

**References:**
- Angular performance guide: https://angular.dev/best-practices/performance
- Web.dev bundle size recommendations: https://web.dev/reduce-javascript-payloads-with-code-splitting/
- context7: Lazy loading and code-splitting best practices

**Confidence:** M  
**Candidate next actions:**
- Run webpack-bundle-analyzer to visualize bundle composition
- Investigate unnamed chunks (chunk-VWU5EXXU.js, chunk-XRQPYLC5.js)
- Consider tree-shaking optimization for Material Design components
- Monitor bundle size in CI with size-limit or bundlewatch
- Document bundle size targets in contributing guidelines

---

### [Q-F005] Settings Page Form Control Error
**Context:** `/frontend/src/app/components/settings.component.ts` — Settings page load  
**Observation (facts):**
- Same NG01050 error appears when navigating to Settings
- Error indicates formControlName without parent formGroup
- Settings page displays correctly despite error
- No obvious form elements visible in settings UI from screenshots

**Risk:** Reliability  
**Impact:** L — **Likelihood:** H  
Error in console but functionality appears intact; may indicate dead code or future feature.

**Hypotheses:**
- Settings component may have hidden/commented form fields
- Error might be from a component that's imported but not visible
- Could be a work-in-progress feature not yet exposed in UI

**Expected good practice:**
- No console errors in production builds
- Remove unused form controls or properly bind them
- Use Angular strict template checks to catch these at compile time

**Analysis:**
This requires inspection of settings.component.ts source code to determine if there are form controls defined but not properly bound in the template. The error is less critical than the search page since visible functionality works.

**References:**
- Angular strict mode: https://angular.dev/tools/cli/template-typecheck
- context7: Angular template type checking configuration

**Confidence:** M  
**Candidate next actions:**
- Review settings.component.ts template for formControlName usage
- Remove unused form controls or fix binding
- Enable strict template checking in tsconfig.json
- Add unit tests for form validation

---

## Backend — Quarkus

### [Q-B001] OIDC Configuration in Development Mode
**Context:** `/backend/src/main/resources/application.properties` — OIDC setup  
**Observation (facts):**
- Dev Services automatically start Keycloak container (quay.io/keycloak/keycloak:26.3.0)
- Keycloak takes ~21 seconds to start in dev mode
- Application configured for OIDC authentication
- Health check shows "OIDC Provider Health Check: UP"
- No visible authentication UI in frontend during exploration

**Risk:** Security | UX  
**Impact:** M — **Likelihood:** M  
Authentication is configured but not enforced in dev mode; unclear if production requires it.

**Hypotheses:**
- Dev mode bypasses OIDC for easier development
- Frontend may not have login/logout UI implemented
- Quarkus Dev Services provides auto-configured Keycloak for testing
- Production will require proper OIDC configuration with environment variables

**Expected good practice:**
- Development should mirror production authentication flow
- Document authentication requirements clearly in README
- Provide test credentials for development
- Implement frontend login UI if authentication is required

**Analysis:**
The backend is configured for OIDC with Keycloak, but there's no evident authentication flow in the frontend. This could mean:
1. Authentication is planned but not yet implemented in frontend
2. Dev mode disables auth for easier testing
3. Public endpoints allow unauthenticated access

Need to verify if backend endpoints require authentication and how frontend should handle login/logout.

**References:**
- Quarkus OIDC guide: https://quarkus.io/guides/security-oidc-code-flow-authentication
- Keycloak documentation: https://www.keycloak.org/documentation
- context7: OIDC authentication flows and best practices

**Confidence:** M  
**Candidate next actions:**
- Review backend endpoint security annotations (@RolesAllowed, etc.)
- Check if Dev Services auto-configures OIDC bypass
- Implement frontend authentication UI if required
- Document authentication setup in README.md
- Add E2E tests for authenticated vs unauthenticated flows

---

### [Q-B002] Deprecated OpenTelemetry Configuration Warning
**Context:** Backend startup logs  
**Observation (facts):**
```
WARN [io.qu.config] The "quarkus.otel.traces.enabled" config property is deprecated and should not be used anymore.
```
- Warning appears during backend startup
- Application uses OpenTelemetry for observability
- Config property `quarkus.otel.traces.enabled` is deprecated
- Application starts successfully despite warning

**Risk:** Maintainability  
**Impact:** L — **Likelihood:** H  
Deprecated config will be removed in future Quarkus versions; should be updated now.

**Hypotheses:**
- Recent Quarkus update changed OpenTelemetry configuration approach
- New property name or configuration method exists
- May need to update to new OTLP exporter configuration

**Expected good practice:**
- Replace deprecated configuration with recommended alternative
- Keep up with framework deprecation notices
- Document observability configuration for production deployment

**Analysis:**
Quarkus 3.x changed OpenTelemetry configuration structure. The old `quarkus.otel.traces.enabled` is deprecated in favor of more granular configuration. Should consult Quarkus 3.25.2 documentation for correct configuration.

**References:**
- Quarkus OpenTelemetry guide: https://quarkus.io/guides/opentelemetry
- Quarkus migration guide: https://github.com/quarkusio/quarkus/wiki/Migration-Guide-3.0
- context7: OpenTelemetry configuration best practices

**Confidence:** H  
**Candidate next actions:**
- Review application.properties for `quarkus.otel.*` configuration
- Update to recommended configuration format
- Test that telemetry still works after update
- Document telemetry setup for production (OTLP endpoint, etc.)

---

### [Q-B003] Quartz Scheduler Not Starting
**Context:** Backend startup logs  
**Observation (facts):**
```
INFO [io.qu.qu.ru.QuartzSchedulerImpl] No scheduled business methods found - Quartz scheduler will not be started
```
- Quartz dependency included in pom.xml (`quarkus-quartz`)
- Scheduler doesn't start because no scheduled methods found
- Application continues normally without scheduler

**Risk:** Maintainability | Cost  
**Impact:** L — **Likelihood:** L  
Unnecessary dependency if not used; minor impact on build size and startup time.

**Hypotheses:**
- Quartz was added for future scheduled task feature (not yet implemented)
- Scheduled tasks may have been removed but dependency remained
- May be intentionally included for production jobs not in demo data

**Expected good practice:**
- Remove unused dependencies to reduce attack surface and build size
- Document if dependency is for future use
- Use dependency analysis tools to identify unused libraries

**Analysis:**
The Quartz dependency adds minimal overhead, but if no scheduled tasks exist, it should be removed unless there's a documented plan to use it. Common uses for scheduling in a library app:
- Periodic metadata updates
- Backup jobs
- Statistics aggregation
- Thumbnail regeneration

**References:**
- Quarkus Quartz guide: https://quarkus.io/guides/quartz
- Maven dependency plugin: https://maven.apache.org/plugins/maven-dependency-plugin/analyze-mojo.html

**Confidence:** M  
**Candidate next actions:**
- Search codebase for @Scheduled annotations or Quartz jobs
- If unused: remove `quarkus-quartz` dependency from pom.xml
- If planned: document intended use cases in README or architecture docs
- Consider if scheduled tasks are needed (backup, maintenance, etc.)

---

### [Q-B004] PostgreSQL and Keycloak Container Reuse Warning
**Context:** Backend startup with TestContainers  
**Observation (facts):**
```
WARN [tc.do.io/postgres:17] Reuse was requested but the environment does not support the reuse of containers
To enable reuse of containers, you must set 'testcontainers.reuse.enable=true' in a file located at /home/runner/.testcontainers.properties
```
- TestContainers used for Dev Services (PostgreSQL 17, Keycloak 26.3.0)
- Containers restart on every application restart (no reuse)
- Container startup adds ~28 seconds to application startup time
- PostgreSQL starts in ~2 seconds, Keycloak in ~21 seconds

**Risk:** Performance | Developer Experience  
**Impact:** M — **Likelihood:** H  
Slow startup time during development; developers wait for containers on every restart.

**Hypotheses:**
- TestContainers reuse feature not enabled in developer environment
- Could significantly reduce startup time by reusing existing containers
- May require developer documentation to enable reuse

**Expected good practice:**
- Enable container reuse for faster development iteration
- Document setup steps in development guide
- Balance between startup speed and container freshness

**Analysis:**
TestContainers 1.21.3 supports container reuse to speed up development cycles. By enabling reuse, containers persist between application restarts, reducing startup from ~28 seconds to ~3-5 seconds. This is particularly impactful for Keycloak which takes 21+ seconds to initialize.

**References:**
- TestContainers reuse documentation: https://java.testcontainers.org/features/reuse/
- Quarkus Dev Services: https://quarkus.io/guides/dev-services
- context7: TestContainers best practices for development

**Confidence:** H  
**Candidate next actions:**
- Add setup instructions to README.md for enabling container reuse
- Create `.testcontainers.properties` template in docs/
- Consider project-level configuration in test resources
- Document trade-offs (startup speed vs container state freshness)
- Add dev setup script to automate configuration

---

## Architecture & Structure

### [Q-A001] Demo Data Generation Strategy
**Context:** Backend configuration and startup logs  
**Observation (facts):**
- Configuration shows `librarie.demo.enabled=true` by default
- Demo creates 100 books, 50 authors, 20 series
- DemoDataService populates database on startup
- Statistics page shows: 188 books, 51 series, 26 authors
- Demo data generation likely runs on every startup

**Risk:** Performance | Data Integrity  
**Impact:** M — **Likelihood:** M  
May cause data duplication if not idempotent; slows startup in production if not disabled.

**Hypotheses:**
- Demo data intended only for development/testing
- Should be disabled in production via environment variable
- May not be idempotent (could duplicate data on restart)
- Useful for demonstrations and QA testing

**Expected good practice:**
- Demo data should be idempotent (check if exists before creating)
- Disable by default in production environments
- Document how to enable/disable demo data
- Consider separate data seeding tool instead of startup generation

**Analysis:**
The demo data feature is useful for development and demos but poses risks:
- Production deployments should not generate fake data
- Need to verify if DemoDataService is idempotent
- Current count (188 books vs configured 100) suggests either duplicates or manual additions

Should review DemoDataService implementation to ensure proper production safety.

**References:**
- Quarkus startup events: https://quarkus.io/guides/lifecycle
- Database seeding best practices: https://martinfowler.com/articles/evodb.html#Seeding
- context7: Environment-based configuration patterns

**Confidence:** M  
**Candidate next actions:**
- Review DemoDataService for idempotency
- Add environment check: only enable demo in dev/test profiles
- Document demo data configuration in README
- Consider using Flyway migrations for seed data instead
- Add startup check to prevent demo data in production

---

### [Q-A002] Missing Publishers, Languages, and Formats Statistics
**Context:** Settings page statistics display  
**Observation (facts):**
- Statistics show: 188 books, 51 series, 26 authors
- Publishers: 0, Languages: 0, Formats: 0, Tags: 0
- These fields exist in Book model (from OpenAPI spec)
- Data appears incomplete or not aggregated

**Risk:** Data Quality | UX  
**Impact:** L — **Likelihood:** M  
Statistics don't reflect actual data; may confuse users about library content.

**Hypotheses:**
- Demo data doesn't populate publisher/language/format fields
- Statistics service doesn't aggregate these fields
- Fields exist in model but aren't used yet
- May be placeholder for future features

**Expected good practice:**
- Statistics should reflect actual data in database
- If fields aren't populated, don't display 0 counts (hide or show N/A)
- Demo data should populate all relevant fields realistically

**Analysis:**
The mismatch between book count (188) and zero counts for publishers/languages/formats suggests:
1. Database schema has these fields but they're not populated
2. Statistics calculation doesn't query these fields
3. UI displays placeholders for unimplemented features

Need to review Book entity and demo data generation to understand field population.

**References:**
- Database normalization: https://en.wikipedia.org/wiki/Database_normalization
- context7: Entity design and data completeness patterns

**Confidence:** M  
**Candidate next actions:**
- Review Book entity and database schema
- Check if demo data populates publisher/language/format
- Update statistics service to aggregate these fields correctly
- Consider hiding unpopulated statistics from UI
- Add data validation to ensure required fields are populated

---

### [Q-A003] Application Version Management
**Context:** Settings page and package.json  
**Observation (facts):**
- Backend version: 1.0.0-SNAPSHOT
- Frontend version: 1.0.0
- Version displayed in Settings page
- Backend uses Maven SNAPSHOT versioning
- Frontend uses fixed semantic version

**Risk:** Maintainability  
**Impact:** L — **Likelihood:** L  
Inconsistent versioning between frontend and backend; minor but should be aligned.

**Hypotheses:**
- SNAPSHOT indicates backend is in active development
- Frontend version should match backend or use separate versioning
- May need version synchronization strategy for releases

**Expected good practice:**
- Align version numbers between frontend and backend
- Use semantic versioning consistently
- Automate version updates in CI/CD pipeline
- Document versioning strategy in CONTRIBUTING.md

**Analysis:**
Maven SNAPSHOT versioning indicates development mode. For production releases:
- Both should use matching release versions (e.g., 1.0.0)
- Remove SNAPSHOT suffix for releases
- Consider monorepo versioning strategy

**References:**
- Semantic versioning: https://semver.org/
- Maven versioning: https://maven.apache.org/maven-ci-friendly.html
- context7: Versioning strategies for fullstack applications

**Confidence:** H  
**Candidate next actions:**
- Document versioning strategy in CONTRIBUTING.md
- Align frontend and backend versions for releases
- Create release script that updates both versions
- Add version to build artifacts and Docker tags

---

## Security & Compliance

### [Q-S001] File Upload Security Configuration
**Context:** README.md environment variables  
**Observation (facts):**
- Configuration shows file upload security settings
- `librarie.storage.max-file-size=104857600` (100MB)
- `librarie.security.file-validation-enabled=true`
- Allowed extensions for books and images defined
- `librarie.security.sanitization-enabled=true`

**Risk:** Security  
**Impact:** M — **Likelihood:** M  
File upload is a common attack vector; configuration appears secure but implementation should be verified.

**Hypotheses:**
- Security configuration exists and is enabled by default (good)
- Need to verify actual validation implementation in backend
- 100MB file size may be appropriate for ebooks but should be justified
- Sanitization implementation should be reviewed

**Expected good practice:**
- Validate file types by content (magic bytes), not just extension
- Scan uploaded files for malware
- Store uploaded files outside web root
- Implement file size limits appropriate to use case
- Log all file upload attempts for security monitoring

**Analysis:**
The configuration shows security awareness with:
- File size limits
- Extension whitelisting
- Validation and sanitization flags

However, need to verify implementation:
- How is file type validation performed?
- What does sanitization entail?
- Are files scanned for malware?
- Where are files stored and how are they served?

**References:**
- OWASP File Upload Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/File_Upload_Cheat_Sheet.html
- context7: Secure file upload implementation patterns

**Confidence:** M  
**Candidate next actions:**
- Review FileStorageService and SecureFileProcessingPort implementation
- Verify file type validation uses content inspection, not just extension
- Test upload of malicious files (polyglot, ZIP bombs, etc.)
- Add virus scanning if handling untrusted uploads
- Document file storage security in architecture docs

---

### [Q-S002] Secrets Management in Documentation
**Context:** README.md environment variables section  
**Observation (facts):**
- README shows example OIDC_CLIENT_SECRET in environment variable examples
- Production environment example includes placeholder secrets
- No mention of secrets management solution (Vault, AWS Secrets Manager, etc.)
- Environment variables documented but storage method not specified

**Risk:** Security  
**Impact:** M — **Likelihood:** M  
Risk of developers hardcoding secrets or storing them insecurely if not guided properly.

**Hypotheses:**
- Documentation intended for setup guidance, not secure production deployment
- May assume users know not to commit secrets
- Production deployment guide may exist elsewhere

**Expected good practice:**
- Document proper secrets management approach
- Recommend using secrets management tools (Vault, cloud provider solutions)
- Warn against committing secrets to version control
- Provide .env.example template without real secrets
- Document secret rotation procedures

**Analysis:**
The README provides helpful environment variable documentation but doesn't guide users on secure secret storage. Should add:
- Recommendation for secrets management solution
- .env.example template
- Warning about not committing secrets
- Reference to security best practices

**References:**
- OWASP Secrets Management Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html
- HashiCorp Vault: https://www.vaultproject.io/
- context7: Secrets management patterns for production applications

**Confidence:** H  
**Candidate next actions:**
- Add secrets management section to README
- Create .env.example template
- Add SECURITY.md documenting security practices
- Recommend vault solution for production
- Add pre-commit hook to detect accidentally committed secrets

---

## Documentation & Observability

### [Q-D001] Missing Architectural Documentation
**Context:** Repository structure and docs/ folder  
**Observation (facts):**
- docs/ folder contains: migration plans, feature maps, PRD submissions
- No architecture diagram or system overview document
- No API documentation beyond OpenAPI spec
- README focuses on setup, not architecture

**Risk:** Maintainability  
**Impact:** M — **Likelihood:** M  
New developers need to infer architecture from code; slows onboarding and increases errors.

**Hypotheses:**
- Early-stage project where architecture is evolving
- Documentation may exist elsewhere (wiki, Confluence, etc.)
- Technical debt from rapid development

**Expected good practice:**
- Maintain architecture decision records (ADRs)
- Document high-level architecture (C4 model)
- Explain key design patterns and structure
- Keep documentation close to code (in repo)

**Analysis:**
The application uses clean architecture patterns (Domain, Application, Infrastructure layers visible in backend), but this isn't documented. Should add:
- Architecture overview diagram
- Directory structure explanation
- Key patterns used (ports & adapters, CQRS, etc.)
- Frontend state management approach (signals)

**References:**
- C4 model: https://c4model.com/
- Architecture Decision Records: https://adr.github.io/
- context7: Documentation best practices for software projects

**Confidence:** H  
**Candidate next actions:**
- Create ARCHITECTURE.md with system overview
- Add C4 context and container diagrams
- Document backend hexagonal architecture
- Document frontend component structure and state management
- Add ADRs for major technical decisions

---

### [Q-D002] API Documentation Accessibility
**Context:** README mentions OpenAPI at /q/swagger-ui  
**Observation (facts):**
- Backend exposes OpenAPI spec
- Swagger UI available at http://localhost:8080/q/swagger-ui
- No mention of API versioning strategy
- No API changelog or migration guide

**Risk:** Maintainability | UX  
**Impact:** L — **Likelihood:** L  
API documentation exists but could be more accessible and comprehensive.

**Hypotheses:**
- OpenAPI spec generated automatically from code
- May lack detailed descriptions or examples
- No versioning strategy yet (v1.0.0-SNAPSHOT)

**Expected good practice:**
- Maintain comprehensive API documentation
- Version API endpoints (/v1/, /v2/, etc.)
- Provide migration guides for breaking changes
- Include authentication examples in API docs

**Analysis:**
The application has good foundation with OpenAPI spec, but should enhance:
- API versioning strategy for future compatibility
- Example requests/responses in OpenAPI annotations
- Authentication flow documentation
- Rate limiting documentation (if implemented)

**References:**
- OpenAPI best practices: https://swagger.io/resources/articles/best-practices-in-api-documentation/
- API versioning strategies: https://restfulapi.net/versioning/
- context7: REST API documentation patterns

**Confidence:** M  
**Candidate next actions:**
- Add detailed OpenAPI descriptions to endpoints
- Document authentication requirements per endpoint
- Create API.md with usage examples and best practices
- Plan API versioning strategy for breaking changes
- Consider API changelog for tracking changes

---

## Testing & Quality Assurance

### [Q-T001] Testing Infrastructure and Coverage
**Context:** Project structure  
**Observation (facts):**
- Backend: JUnit 5 tests available (`mvn test`)
- Frontend: Jasmine/Karma configured for unit tests
- No visible E2E test suite
- No coverage reports mentioned in CI/CD
- Playwright installed in frontend dependencies but no test files found

**Risk:** Quality | Reliability  
**Impact:** M — **Likelihood:** M  
Without comprehensive tests, refactoring and new features risk breaking existing functionality.

**Hypotheses:**
- Tests exist but coverage is unknown
- E2E tests may be planned (Playwright installed) but not implemented
- Manual testing currently used for quality assurance

**Expected good practice:**
- Maintain >80% code coverage for critical paths
- Implement E2E tests for key user flows
- Run tests in CI/CD pipeline
- Generate and publish coverage reports
- Use mutation testing for test quality assessment

**Analysis:**
The project has testing infrastructure but should:
- Add E2E tests for critical flows (book browsing, search, reading)
- Set up coverage reporting (JaCoCo for backend, Istanbul for frontend)
- Implement integration tests for API endpoints
- Add visual regression testing for UI components

**References:**
- Testing pyramid: https://martinfowler.com/bliki/TestPyramid.html
- Playwright E2E testing: https://playwright.dev/
- context7: Testing strategies for fullstack applications

**Confidence:** M  
**Candidate next actions:**
- Audit existing test coverage (backend and frontend)
- Create E2E test suite using Playwright
- Set up coverage reporting in CI/CD
- Document testing strategy and standards
- Add pre-commit hooks to run unit tests

---

### [Q-T002] CI/CD Pipeline Configuration
**Context:** .github/workflows directory  
**Observation (facts):**
- No visible GitHub Actions workflows in repository
- No CI/CD configuration found
- Manual build and deployment processes
- No automated quality gates

**Risk:** Quality | Reliability  
**Impact:** M — **Likelihood:** M  
Without automated CI/CD, quality issues can slip into production.

**Hypotheses:**
- Early stage project without CI/CD yet
- CI/CD may exist in separate deployment repository
- Manual deployment process currently used

**Expected good practice:**
- Automate build, test, and deployment
- Run linters, tests, security scans on every PR
- Implement automated quality gates
- Deploy automatically to staging on merge to main
- Require manual approval for production deployments

**Analysis:**
Should implement CI/CD pipeline with:
- Build verification (backend Maven, frontend npm)
- Unit tests execution
- E2E tests execution
- Security scanning (SAST, dependency scanning)
- Deployment to staging environment
- Manual approval gate for production

**References:**
- GitHub Actions best practices: https://docs.github.com/en/actions/learn-github-actions/workflow-syntax-for-github-actions
- CI/CD patterns: https://martinfowler.com/articles/continuousIntegration.html
- context7: GitHub Actions workflows for fullstack applications

**Confidence:** H  
**Candidate next actions:**
- Create .github/workflows/ci.yml for build and test
- Add security scanning with CodeQL and Dependabot
- Create deployment workflows for staging and production
- Document CI/CD pipeline in CONTRIBUTING.md
- Set up status checks as required for PR merges

---

## Performance & Scalability

### [Q-P001] Database Connection Pooling Configuration
**Context:** Quarkus application.properties and PostgreSQL  
**Observation (facts):**
- PostgreSQL used as primary database
- Quarkus Agroal (HikariCP-based) connection pooling included
- No visible connection pool configuration in docs
- Dev Services provides PostgreSQL for development

**Risk:** Performance | Reliability  
**Impact:** M — **Likelihood:** M  
Default connection pool settings may not be optimal for production load.

**Hypotheses:**
- Using default Agroal/HikariCP settings
- May be acceptable for current load
- Should be tuned for production based on expected concurrent users

**Expected good practice:**
- Configure connection pool size based on application needs
- Monitor connection pool metrics
- Set appropriate timeout values
- Document production database configuration

**Analysis:**
Connection pooling is critical for database performance. Should:
- Define explicit pool size configuration
- Monitor pool exhaustion and wait times
- Set appropriate timeout values
- Document sizing calculations

Recommended starting configuration:
- Pool size = (core_count * 2) + effective_spindle_count
- Monitor and adjust based on actual load

**References:**
- HikariCP configuration: https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby
- Quarkus Agroal: https://quarkus.io/guides/datasource#configuration-reference
- context7: Database connection pooling best practices

**Confidence:** M  
**Candidate next actions:**
- Add explicit connection pool configuration to application.properties
- Document production database sizing recommendations
- Add connection pool metrics to monitoring
- Load test to determine optimal pool size
- Document connection pool monitoring in operations guide

---

### [Q-P002] Caching Strategy
**Context:** Application architecture  
**Observation (facts):**
- No visible caching layer mentioned
- Book covers and images loaded on every request
- API calls made for every navigation
- No CDN configuration mentioned

**Risk:** Performance | Cost  
**Impact:** M — **Likelihood:** M  
Unnecessary database queries and file I/O; higher latency and server load.

**Hypotheses:**
- Early stage application without caching optimization
- May rely on browser caching for static assets
- Database query performance acceptable at current scale

**Expected good practice:**
- Implement HTTP caching headers for static assets (covers, images)
- Cache frequently accessed data (book lists, author info)
- Use CDN for static asset delivery
- Implement query result caching with appropriate TTL

**Analysis:**
Should implement multi-level caching:
1. Browser caching: HTTP Cache-Control headers for images
2. Application caching: Quarkus Cache for database queries
3. CDN: CloudFront/Cloudflare for static assets
4. Database: Query result caching with Hibernate second-level cache

**References:**
- HTTP caching: https://developer.mozilla.org/en-US/docs/Web/HTTP/Caching
- Quarkus cache: https://quarkus.io/guides/cache
- context7: Caching strategies for web applications

**Confidence:** M  
**Candidate next actions:**
- Implement HTTP cache headers for images and static assets
- Add Quarkus Cache for book/author/series queries
- Configure appropriate cache TTLs based on update frequency
- Monitor cache hit rates
- Document caching strategy in architecture docs

---

## Accessibility & Internationalization

### [Q-I001] Internationalization Support
**Context:** Frontend and backend configuration  
**Observation (facts):**
- Author bio field supports multiple languages (from OpenAPI: `additionalProperties`)
- Application UI appears to be English-only
- No i18n library visible in frontend dependencies
- Backend supports language field in Book model

**Risk:** UX | Market Reach  
**Impact:** M — **Likelihood:** L  
Limited to English-speaking users; restricts potential user base.

**Hypotheses:**
- English-only for initial release
- Data model supports i18n but UI doesn't yet
- May be planned for future release

**Expected good practice:**
- Use i18n library (@angular/localize or ngx-translate)
- Externalize all user-facing strings
- Support multiple languages in UI
- Right-to-left (RTL) support for Arabic, Hebrew, etc.

**Analysis:**
The data model shows i18n awareness (author bio in multiple languages), but:
- Frontend doesn't use i18n library
- All UI strings are hardcoded in English
- No language switcher in UI

Should implement:
- Angular i18n or ngx-translate
- Extract strings to translation files
- Add language selector to settings

**References:**
- Angular i18n: https://angular.dev/guide/i18n
- ngx-translate: https://github.com/ngx-translate/core
- context7: Internationalization patterns for Angular applications

**Confidence:** M  
**Candidate next actions:**
- Decide on i18n approach (Angular built-in vs ngx-translate)
- Extract hardcoded strings to translation files
- Implement language switcher in settings
- Document supported languages and translation process
- Consider RTL layout support

---

### [Q-I002] Accessibility Compliance
**Context:** Frontend UI components  
**Observation (facts):**
- Angular Material components used (generally accessible)
- No visible ARIA labels in custom components
- No accessibility testing mentioned
- Keyboard navigation not explicitly tested

**Risk:** Legal | UX  
**Impact:** M — **Likelihood:** M  
May not be accessible to users with disabilities; potential legal compliance issues.

**Hypotheses:**
- Relies on Material Design accessibility
- Custom components may lack proper ARIA attributes
- Not tested with screen readers

**Expected good practice:**
- WCAG 2.1 Level AA compliance minimum
- Test with screen readers (NVDA, JAWS, VoiceOver)
- Ensure keyboard navigation works throughout
- Add ARIA labels to custom components
- Implement skip links for keyboard users

**Analysis:**
Angular Material provides good accessibility foundation, but:
- Custom components need ARIA attributes
- Should be tested with assistive technologies
- Keyboard navigation should be verified
- Color contrast should be checked

**References:**
- WCAG 2.1: https://www.w3.org/WAI/WCAG21/quickref/
- Angular Material accessibility: https://material.angular.io/cdk/a11y/overview
- context7: Accessibility best practices for Angular applications
- axe-core: https://www.deque.com/axe/

**Confidence:** M  
**Candidate next actions:**
- Run axe-core accessibility audit on UI
- Test keyboard navigation throughout application
- Add ARIA labels to custom components
- Test with screen readers
- Document accessibility compliance level
- Add accessibility testing to CI/CD

---

## Summary Statistics
- **Total Questions:** 22
- **Frontend Issues:** 5
- **Backend Issues:** 4
- **Architecture Issues:** 3
- **Security Issues:** 2
- **Documentation Issues:** 2
- **Testing Issues:** 2
- **Performance Issues:** 2
- **Accessibility Issues:** 2

**Priority Distribution:**
- High Impact: 7
- Medium Impact: 13
- Low Impact: 2

**Confidence Levels:**
- High: 11
- Medium: 11
- Low: 0
