# Feature Map: Calibre vs Calibre-Web Automated (Initial Draft)

> Status: Draft 1 – High level derived from README, manifests, and initial code inspection. Further deep code scans required to reach COMPLETE coverage (see Open Questions).

## Legend
- Y = Present / implemented
- P = Partial / via plugin or limited scope
- N = Not present
- ? = Needs confirmation

## Feature Table

| Feature | Calibre (Desktop Suite) | Calibre-Web Automated (CWA) | Key Dependencies/Stacks | Notes |
|---------|-------------------------|-----------------------------|--------------------------|-------|
| Core Library Database | Y (SQLite, metadata.db, rich schema) | Y (Uses existing Calibre library, SQLite via SQLAlchemy ORM) | Python, SQLite, SQLAlchemy | CWA reuses Calibre DB rather than re‑modeling |
| Ebook Format Conversion | Y (Comprehensive converters) | Y (Automated Conversion Service, multi-format) | Calibre conversion CLI (external), Python services | CWA shells out to Calibre conversion tools (assumed) |
| Automatic Ingest | Built-in add books UI/CLI | Y (Automatic Ingest Service watching folder) | Python watchdog/? (TBD), file system | Need to locate watcher implementation |
| Metadata Editing | Y (GUI + bulk) | Y (Web UI + batch edit) | Calibre internal modules / CWA Flask views + SQLAlchemy | Enforcement layer difference |
| Metadata Fetch (internet sources) | Y (Many sources, plugins) | Y (Extended: ISBNdb, ibdb.dev, Hardcover + original CW providers) | Requests, external APIs | Rate limiting + API keys mgmt TBD |
| Cover Management | Y | Y (Automatic enforcement into files) | Calibre cover management vs CWA custom service | CWA writes back to underlying files |
| Device Sync (USB) | Y (Desktop device drivers) | N (Web context) | PyQt, device drivers | Out-of-scope for server; could map via APIs later |
| Send-to-Device (Email) | Y | Y (Email/Send-to-Kindle) | SMTP libraries | Confirm mail config handling |
| News Download (Recipe system) | Y (recipes/*.recipe) | N (Not in CWA) | Python recipe engine | Could become scheduled Quarkus jobs |
| OPDS Feed | P (Content server) | Y | Flask / OPDS blueprint | CWA offers OPDS endpoints |
| Web Content Server | Y (Built-in server) | Y (Primary interface) | Python custom server vs Flask + Tornado/Gevent | Migration target: Quarkus REST + Web UI |
| User Management / Auth | Basic (server mode) | Y (Extended: OAuth, LDAP, Proxy, Magic Link) | Flask-Login/Custom + OAuth libs, LDAP | Map to Quarkus Security + OIDC extensions |
| Role/Permission Granularity | Basic | Y (Per-user permissions, content hiding) | SQLAlchemy models | Need full permission matrix extraction |
| Batch Editing | Limited | Y | Custom Flask views | Identify endpoints |
| Scheduling / Automation | Desktop cron integration (Fetch news etc.) | Y (APScheduler for tasks: ingest, conversion, backups, stats) | APScheduler | Quarkus Scheduler replacement |
| EPUB Fixer | N (Implicit via conversion) | Y (Kindle EPUB Fix service) | Custom Python script (fork) | Identify algorithm steps for Java port |
| KOReader Sync (KOSync) | N | Y (Custom sync endpoint + plugin) | Flask route, storage in DB | Map to REST + WebSocket? |
| Backup Service | Manual/External | Y (Automated backup of originals) | File ops, compression libs | Strategy for object storage optional |
| Stats & Telemetry Page | Minimal | Y (Stats tracking & UI) | SQLAlchemy, templates | Map to metrics endpoints + frontend |
| Dark/Light Theme Toggle | Basic GUI theme | Y (Web toggle) | Bootstrap, JS | Could move to SPA or server-thymeleaf/Qute |
| Update Notification | Desktop auto-update | Y (Release check vs GitHub) | GitHub API requests | Move to scheduled HTTP client call |
| Plugin Architecture | Robust plugin system | Limited (metadata providers, ingest behaviors) | Dynamic imports | Define extension SPI in Quarkus |
| REST API (Structured) | Limited/Custom | Implicit via Flask views | Flask, JSON | Will formalize OpenAPI spec |
| OpenAPI / Swagger | N | N | — | Will add via smallrye-openapi |
| Health Checks | N | N | — | Add via smallrye-health |
| Metrics / Observability | Minimal logging | Basic logging | Logging, custom counters | Adopt Micrometer/OpenTelemetry |
| Internationalization (i18n) | Y (UI translations) | Y (Flask-Babel) | Babel, gettext | Use Quarkus i18n / resource bundles |
| Access Logging | Basic | Yes (through web stack) | Flask logging | Unify via Quarkus logging config |
| Rate Limiting | N | Y (Flask-Limiter) | Redis/memory backend? | Evaluate Quarkus rate limit (extension or custom) |
| Reverse Proxy Support | N/A | Y (reverseproxy.py) | Header parsing | Map to HTTP filters |
| GDrive Integration | N | Y (Google APIs + PyDrive2) | google-api-python-client | Re-implement or external microservice |
| Kobo Sync | Y (Device sync) | Y (Kobo sync endpoints) | HTTP endpoints, Calibre metadata | Different implementation style |
| LDAP Auth | N | Y | python-ldap | Use Quarkus LDAP security realm |
| OAuth (Google, GitHub etc.) | N | Y | Flask-Dance | Use Quarkus OIDC social providers |
| Pagination | Y | Y (Pagination helper) | Custom | Use Panache pagination or manual |
| Search (Full-text) | Y (Various indices) | Y (search.py, metadata search) | SQL queries + like | Consider Hibernate Search / Elasticsearch |
| File Format Support List | Extensive | Consumes Calibre converters | Calibre toolchain | Need inventory for migration strategy |
| CLI Utilities | Rich CLI | Limited CLI wrappers | Python scripts | Provide Quarkus Picocli integration |
| Containerization | Not core (community) | First-class (Docker) | Dockerfile, compose | Quarkus native/container build |
| Configuration Mechanism | Config files, CLI flags | env variables + config files (INI/DB) | Python config_sql | Move to application.properties + profile overrides |

## High-Level Architecture Notes
- Calibre is a monolithic desktop + optional content server (PyQt GUI, rich plugin ecosystem, direct filesystem & device I/O)
- Calibre-Web Automated is a headless/web augmentation layer over an existing Calibre library with automation services layered via APScheduler and various Flask blueprints.
- Shared asset: `metadata.db` schema from Calibre (SQLite) drives a large portion of functionality.
- Migration target suggests: Extract domain (Books, Authors, Tags, Series, Users, Permissions, Jobs, Stats) into Java entities; re-implement workflow/automation as Quarkus scheduled jobs; externalize conversion (call native Calibre CLI or re-implement subset in Java or via container sidecar).

## Initial Dependency / Integration Graph (Mermaid)
```mermaid
flowchart LR
  subgraph Calibre Desktop
    A[PyQt GUI]
    B[Conversion Engine]
    C[Metadata Fetch Plugins]
    D[Content Server]
  end
  subgraph Shared Library
    DB[(SQLite metadata.db)]
    Files[/Ebook Files/]
  end
  subgraph CWA
    W[Flask Web UI]
    Auth[Auth Modules\n(OAuth/LDAP)]
    Ingest[Ingest Service]
    Convert[Auto Conversion]
    Enforce[Cover & Metadata Enforcement]
    EPUBFix[EPUB Fixer]
    KO[KOReader Sync]
    Stats[Stats & Backups]
    Jobs[APScheduler]
  end
  B --> Files
  B --> DB
  D --> DB
  W --> DB
  W --> Files
  Ingest --> Files
  Ingest --> Convert
  Convert --> Files
  Enforce --> Files
  EPUBFix --> Files
  KO --> DB
  Stats --> DB
  Jobs --> Ingest
  Jobs --> Convert
  Jobs --> Enforce
  Jobs --> Backups[(Backups Store)]
  Auth --> W
  C --> W
```

## Migration Considerations (Revised)
- Fresh Domain Schema: Design new normalized model (inspiration only; no direct Calibre DB compatibility) targeting PostgreSQL.
- Conversion Scope Phase 1: Accept EPUB (and simple metadata injection) only; defer multi-format conversion; no Calibre sidecar.
- Scheduler: Use Quartz (not simple @Scheduled) for ingest scans & throttling.
- Auth: OIDC-only (no local users / LDAP); roles & permissions from token claims.
- Observability: OpenTelemetry (traces/logs/metrics) -> Grafana stack integrations.
- API Surfacing: REST + OpenAPI; Angular SPA consumes JSON; add KOReader sync endpoints.
- Metadata Materialization: (Former "Enforcement") atomic write of updated metadata/cover into EPUB container during pipeline.
- Extensibility: Future plugin/SPI optional; not required for MVP.

## Next Steps
1. Deep schema extraction from `db.py` and Calibre core to enumerate all tables/relations.
2. Identify all APScheduler jobs and their triggers.
3. Inventory external API calls (Google, ISBNdb, Goodreads, Kobo, etc.).
4. Classify security-sensitive operations (file writes, conversion subprocess calls).
5. Prioritize MVP scope for first Quarkus module (Library Core + Web API + Auth).

---
Generated: Initial draft. Will be iteratively refined.
