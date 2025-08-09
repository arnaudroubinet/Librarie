# Migration Plan to Quarkus 3 (Draft 2)

> Based on initial feature map (feature-map.md). Will expand after deeper code & schema inspection.

## Guiding Principles (Revised)
- Greenfield implementation: do not maintain runtime compatibility with legacy Calibre / CWA schemas or plugins.
- State-of-the-art domain-driven schema (PostgreSQL target; SQLite optional for dev) managed exclusively by Flyway forward migrations.
- OIDC-only security (no local users) with roles/permissions injected via token claims.
- Quartz (Quarkus Quartz extension) for all scheduled/throttled jobs (not simple @Scheduled).
- Conversion scope Phase 1: Accept EPUB ONLY; architecture provides pluggable `FormatAdapter` SPI for future formats (MOBI, PDF, AZW3, CBZ) without refactor.
- Atomic pipeline: ingest → validate → (optional convert) → metadata materialization → commit; rollback on any error.
- Angular SPA (separate module) consuming REST + OpenAPI; SPA handles time zones & i18n; backend stores timestamps in UTC.
- Observability: OpenTelemetry (traces/logs/metrics) exported to Grafana stack (Tempo, Loki, Mimir) + structured logging.
- JVM mode only (no native image builds in scope).
- Each task ≤ 4h where feasible; larger items decomposed.

## Proposed Architecture (High Level, Revised)
- Backend Modules (Quarkus): domain-core, persistence, auth-oidc, ingest, conversion, materialization (former enforcement), sync-koreader, quartz-jobs, observability.
- Frontend Module: angular-spa (OIDC login, library browsing, admin screens, telemetry instrumentation).
- Persistence: PostgreSQL (prod), SQLite only for local dev quick start.
- Scheduling & Rate Control: Quartz jobs + Quartz-based throttling decisions (Phase 2).
- Inter-service contracts: Single monolith for Phase 1; modular package boundaries enable future extraction.
- No Calibre sidecar; future advanced conversions may become external service (backlog).

## Task List

| ID | Title | Initial State | Precise Change | Acceptance Criteria | Est (h) | Depends |
|----|-------|---------------|----------------|---------------------|---------|---------|
| T1 | Initialize Quarkus + Angular Workspace | None | Create backend Quarkus project + Angular SPA folder structure | Both build & run (Quarkus dev + ng serve) | 3 | |
| T2 | Add Core Quarkus Dependencies | Skeleton | Add: rest, hibernate-orm, flyway, oidc, quartz, openapi, health, micrometer, otel, config-yaml (optional) | App starts; /q/health UP | 1 | T1 |
| T3 | Base Configuration Profiles | None | application.properties (dev/prod), OIDC placeholders, Swagger dev only, UTC settings | Config loads; Swagger in dev | 1 | T2 |
| T4 | Domain Schema Design (ERD) | Not defined | Produce ER diagram (Books, Formats, Authors, Tags, Series, BookTag, BookAuthor, UserExternalRef (optional), SyncState) | ERD doc committed | 2 | T3 |
| T5 | Flyway Baseline (V1) | No DB objects | Create V1__baseline.sql reflecting designed schema (Postgres) + SQLite variant if needed | Flyway migrate succeeds | 2 | T4 |
| T6 | JPA Entities & Mappings | None | Implement entities + relationships + indexes annotations | Hibernate validates schema | 3 | T5 |
| T7 | Repository Layer & DTO Mappers | None | Implement repositories + MapStruct or manual mappers | CRUD tests pass | 3 | T6 |
| T8 | OIDC Integration & Claim Mapping | None | Configure dev Keycloak; extract roles/permissions from token claims | Protected endpoint returns 401/200 appropriately | 3 | T3 |
| T9 | RBAC Enforcement | None | Role annotations + custom PermissionFilter for fine-grained checks | Tests validate access matrix | 2 | T8 |
| T10 | REST: Books CRUD (Read only phase) | None | /api/books list + detail with pagination & filters | OpenAPI docs show endpoints; tests pass | 3 | T7 |
| T11 | REST: Reference Data (Authors/Tags/Series) | None | /api/authors, /api/tags, /api/series endpoints | Tests pass | 2 | T10 |
| T12 | File Staging Abstraction | Direct FS TBD | Implement staging + permanent storage dirs, hashing, temp workspace | Unit tests for move/rollback | 3 | T10 |
| T13 | Inotify Watcher Service | Not present | Implement Linux watcher + fallback Quartz scan job | New files detected in tests | 3 | T12 |
| T14 | Quartz Job: Scheduled Scan | Not present | Implement job that enumerates ingest dir and enqueues candidates | Job visible in logs | 1 | T13 |
| T15 | Ingest Pipeline Orchestrator | None | Service orchestrating staging -> validation -> atomic commit | Transactional commit tests pass | 4 | T12 |
| T16 | FormatAdapter SPI + EPUB Adapter | None | Introduce `FormatAdapter` interface + implement EPUB adapter (detect, extract, materialize) | Detection & extraction tests green; EPUB only | 4 | T15 |
| T17 | Metadata Materialization Service | None | Write updated metadata & cover into EPUB (OPF edit, cover replace) using EPUB adapter | Modified EPUB passes validation | 4 | T16 |
| T18 | KOReader Sync Model Extraction | Legacy ref only | Define SyncState entity (bookId, userId/subject, position, updatedAt) | Entity & tests | 2 | T6 |
| T19 | KOReader Sync REST Endpoints | None | POST progress, GET progress collection (auth required) | Plugin simulation passes | 3 | T18 |
| T20 | Atomic Pipeline Transaction Support | Partial | Wrap ingest+conversion+materialization in single service with rollback | Failure leaves no partial artifacts | 3 | T17 |
| T21 | Observability: OpenTelemetry Setup | None | Configure OTLP exporters (traces, metrics) + Loki logging appender pattern | Data visible in local collector mock | 3 | T2 |
| T22 | Metrics & Structured Logging | Basic | Add timers, counters (ingests, conversions), structured JSON logs | /q/metrics shows counters | 2 | T21 |
| T23 | Angular SPA OIDC Auth Flow | None | Implement login redirect, token storage, refresh | User can login & call protected API | 4 | T1 |
| T24 | Angular Library Browsing Views | None | Implement book list, detail, pagination, filters | UI e2e test passes | 4 | T23 |
| T25 | Angular Telemetry (Frontend OTel) | None | JS OTel instrumentation to export traces/metrics to backend OTLP | Traces show frontend spans | 3 | T23 |
| T26 | Flyway Migration Pipeline (Future Changes) | Baseline only | Add example V2 adding index; doc process | V2 migration applies cleanly | 1 | T5 |
| T27 | Rate Limiting Abstraction (Deferred) | None | Interface & placeholder using in-memory bucket; Redis config stub | Unit test simulates throttling | 3 | T22 |
| T28 | Pagination & Query Optimization (>100k) | Basic | Add indexes, limit/offset strategy, keyset pagination PoC | Load test within SLA | 3 | T10 |
| T29 | Performance Smoke (k6/Gatling) | None | Create script hitting list & ingest endpoints | Baseline report stored | 2 | T22 |
| T30 | CI Pipeline (Build + Test) | None | GitHub Actions: Java build, Angular build, unit tests | Pipeline green | 2 | T1 |
| T31 | Docker Image (Backend + Nginx for SPA) | None | Multi-stage Dockerfile building backend & Angular dist | Container runs end-to-end | 3 | T30 |
| T32 | Helm Chart (Kubernetes) | None | Basic Helm chart (deployment, service, configmap, ingress) | helm install succeeds locally | 4 | T31 |
| T33 | Security Hardening (Headers, CORS) | Default | Add CSP, CORS config, security headers | OWASP baseline scan passes | 2 | T31 |
| T34 | Documentation (Dev + Ops) | Minimal | Write architecture overview, setup, ERD, pipeline docs | Docs reviewed | 3 | T32 |
| T35 | Open Questions Follow-up | Draft answers | Update open-questions.md with new clarifications (O1-O5) | File updated | 1 | T21 |
| T36 | Cleanup & Backlog Grooming | Pending | Remove deprecated tasks; log future enhancements (advanced conversions, search) | Backlog doc present | 1 | T34 |
| T37 | Calibre Sidecar Adapter (Deferred) | None | Define sidecar REST contract & placeholder adapter (no runtime call) | Spec doc + stub bean present | 2 | T16 |
| T38 | KOReader Retention Purge Job | None | Quartz job purging expired sync events + metrics + config defaults (90d + 7d grace) | Job runs in dev; metrics exposed | 2 | T19 |

## Quarkus / Related Docs References
- REST JSON, Hibernate ORM, Flyway, OIDC, Quartz, OpenAPI & Swagger UI, Micrometer + OpenTelemetry, Config Reference, Angular OIDC best practices (external).

## Risks & Mitigations
| Risk | Mitigation |
|------|------------|
| Re-implementing conversion complexity | Limit Phase 1 to EPUB normalization; defer other formats |
| Large dataset performance (>100k) | Early indexing + pagination benchmarks (T28, T29) |
| OIDC role granularity mismatch | Introduce claim mapping & fallback permission config |
| Inotify portability (non-Linux devs) | Provide Quartz scan fallback | 
| Atomic pipeline file rollback edge cases | Use staging directory + checksum verification before commit |
| Telemetry overhead | Sampling + batching; configurable exporters |

## Next Iteration Targets
- Format support expansion roadmap (MOBI/AZW3 via sidecar Option B) (T37 spec).
- Advanced search/full-text backlog (off-scope initially).
- Specify rate limit policies & Redis externalization (post T27 decision).
- Angular i18n enablement (en, fr initial; translation workflow).
- KOReader retention tuning & metrics review (post T38).

---
Revised draft reflecting clarified requirements.
