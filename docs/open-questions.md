# Open Questions – Updated with Resolutions (Draft 2)

All previously listed questions reviewed. Items resolved per stakeholder directives; remaining clarifications explicitly noted.

## Resolved Decisions
| Topic | Resolution | Impact on Plan |
|-------|-----------|----------------|
| Schema Source | Do NOT chase Calibre DB compatibility; design a fresh, normalized domain (Calibre schema only inspirational) | Replace "Reverse Engineer Calibre Schema" with "Design New Domain Schema" tasks; no migration tooling from legacy DB |
| Calibre Plugins | Not supported / no plugin compatibility layer | Remove plugin/SPI parity tasks; new extension model optional later |
| Auth Accounts | OIDC only (OAuth2); no local user/password storage | Remove user persistence tasks; implement role/permission extraction from token claims |
| Roles & Permissions | Provided via OIDC claims; manage centrally (e.g. Keycloak / external IdP) | Introduce claim-to-RBAC mapping layer in API | 
| Conversion Engine | Re-implement minimal conversion pipeline in Java (no calibre sidecar) | Add scoped format support phases; higher effort risk flagged |
| Throttling | Needed later via Quartz-managed job rate control | Add deferred task for Quartz-based rate limiter/queue; deprioritize immediate implementation |
| File Watching | Use inotify (Linux) with manual trigger + scheduled scan fallback | Add watcher abstraction & polling fallback tasks |
| Error Handling | Any ingest/conversion failure = rollback (atomic) | Design staging directories + DB transactions + two-phase commit for file moves |
| Atomic Conversion | Entire pipeline (ingest -> convert -> metadata materialization) commits or fully rolls back | Introduce transactional service with temp workspace; compensate on failure |
| "Enforcement" Term | Renamed to "Metadata Materialization" (writing updated metadata & cover back into file artifacts) | Update documentation & task names |
| KOReader Sync | Keep capability; analyze existing Python logic for data model & security (still TO CLARIFY specifics) | Add task to dissect current implementation; then model in new domain |
| KOReader Auth | Will rely on bearer/OIDC tokens or signed short-lived tokens; no legacy headers | Add design task |
| Local Accounts | Eliminated | Simplifies schema; removes password hashing concerns |
| Rate Limiting Store | In-memory Redis-compatible (e.g. embedded) with option for external Redis later | Add abstraction around Redis client; configuration profiles |
| Reverse Proxy / WAF | Out of scope of application | Exclude from tasks; document deployment responsibility |
| External Metadata APIs | Not in scope initially (ISBNdb, Hardcover, GDrive, etc.) | Remove related provider & GDrive tasks; future enhancement backlog |
| Scheduler Framework | Use Quartz (Quarkus Quartz extension) instead of simple @Scheduled | Replace scheduler tasks accordingly |
| Time Zones | Always UTC; frontend handles presentation | Ensure DB timestamps stored in UTC; document contract |
| Observability | OpenTelemetry for traces/logs/metrics | Integrate OTel SDK + OTLP exporters; disable ad hoc stats |
| Metrics/Logs Export | Must integrate with Grafana stack (Mimir metrics, Loki logs, Tempo traces) | Provide config examples for OTLP + Loki appender |
| Scale Target | >100k books | Optimize schema, add indexes, use pagination & batch operations; defer full-text |
| Full-text Search | Not required initially | Remove search engine tasks; design query filter only |
| Deployment | Docker first; Kubernetes second | Provide Dockerfile + Helm chart later; prioritize container image |
| Runtime Mode | JVM only (no native image) | Remove native build tasks |
| Frontend | Angular SPA | Add tasks for Angular workspace, OIDC integration, UI modules |
| Frontend Metrics | Collect frontend telemetry (RUM) into Grafana stack | Add JS OTel instrumentation task |
| Data Migration | Initialize fresh DB; use Flyway for forward migrations only | Remove legacy import tasks; add bootstrap seeding task |
| Backups | No automatic backup logic | Remove backup service tasks |
| Rewrites | All logic rewritten; no code reuse requiring license review | Remove licensing review task |

## Clarifications & Updates (Draft 3)
| ID | Status | Decision / Detail | Impact |
|----|--------|-------------------|--------|
| O1 | Resolved | Phase 1 = EPUB ONLY (ingest + metadata materialization). Architecture MUST be format-pluggable (design for future PDF, MOBI, AZW3, CBZ adapters). | Keep pipeline but implement only `EpubAdapter`; add extension SPI + registry now. Future formats = additive, no refactor. |
| O2 | Resolved | Preserve original cover image exactly. No resizing/format normalization initially. Thumbnails (if any) deferred. | Remove/skip image transformation step; simplify materialization service. Add backlog item for cover processing module. |
| O3 | Resolved | KOReader retention: default 90 days + 7 day grace (soft delete window) via Quartz purge job; configurable properties. | Implement schema + purge job (T38) + metrics; revisit after initial usage. |
| O4 | Resolved | No rate limiting in Phase 1. Provide dormant abstraction interface + config flag only. | Defer Redis/client integration & policies; remove initial enforcement tasks; keep placeholder service to avoid future API break. |
| O5 | Resolved | Support English (en) & French (fr) at launch. Use IETF BCP 47 tags. Scaffold i18n for future locales. | Angular: enable i18n extraction & translation pipeline; Backend: message keys (if any) kept English; locale negotiation via Accept-Language. |

## Explanation: Former Question 4 (Conversion Invocation Path Problem)
Original issue sought clarity on how Python triggered Calibre CLI. Since we are **not** using the legacy Calibre converters or sidecar, the problem becomes: reproducing essential conversion capabilities (at least normalization to EPUB) with acceptable fidelity, performance, and maintainability. Risk factors:
1. Calibre’s converters encapsulate numerous edge cases (CSS sanitization, encoding fixes, structural TOC generation). Re-implementing all is high effort.
2. Proposed mitigation: Limit Phase 1 to already-EPUB sources + simple metadata/cover injection; treat other formats as unsupported or queue for future extension.
3. Introduce a modular conversion pipeline: Detect -> Validate -> (Optional Transform) -> Materialize.

### Proposed Conversion Architecture & Calibre Reuse Strategy
Goal: Ship quickly with EPUB-only while laying groundwork for future multi-format support without locking into a monolithic Python dependency.

Pipeline (synchronous within transaction boundary until conversion stage grows):
1. Intake: Store raw upload in staging (content-addressed: SHA-256 path partitioning) + DB draft row.
2. Format Detection: Magic number + extension heuristics (Apache Tika optional later). For Phase 1 assert EPUB.
3. Validation: Run epubcheck (Java) optionally (config flag). On failure reject before persistence commit.
4. Metadata Extraction: For EPUB, parse container.xml + OPF (DOM or SAX) to extract title, creators, language, identifiers; populate DB draft (pending commit).
5. (Future) Transformation: For non-EPUB adapters call adapter-specific normalize -> output normalized EPUB.
6. Materialization: Inject authoritative metadata (DB canonical) back into OPF + cover file (if changed) atomically; recompute hash; move from staging to permanent store; commit DB.
7. Post-Commit Events: Emit domain events (BookCreated, BookUpdated) for search/index (future) or cache warming.

Java SPI Sketch:
```
interface FormatAdapter {
	Format id();                        // EPUB, PDF, MOBI, etc.
	boolean supportsDetection(byte[] header, String filename);
	DetectedFormat detect(InputStream in, String filename); // may return confidence score
	ExtractedMetadata extract(Path stagingFile) throws ValidationException;
	// For non-native formats: transform to normalized EPUB output path
	Optional<Path> normalize(Path stagingFile, NormalizationContext ctx) throws ConversionException;
	void materializeMetadata(Path epubFile, CanonicalMetadata md) throws MaterializationException; // EPUB adapter only Phase 1
}
```
Registry: `FormatAdapterRegistry` discovered via CDI; first positive detection wins (or highest confidence). EPUB adapter implemented now; others may initially proxy to a Python microservice.

Calibre Reuse Options (for Post-Phase 1):
| Option | Description | Pros | Cons | Recommendation |
|--------|-------------|------|------|----------------|
| A | No reuse; implement adapters in Java libs (epubcheck, pdfbox, tika, junrar) | Single stack, easier deployment | Slower format coverage | Combine with B (hybrid) |
| B | Sidecar microservice (Python + trimmed Calibre install) offering REST: /convert -> normalized EPUB | Rapid format breadth, battle-tested logic | Extra container, IPC latency, sandboxing & security surface | Use temporarily for complex formats (MOBI/AZW3) |
| C | Embed CPython in JVM (GraalPy / JEP) to call Calibre modules directly | Avoid network, unified lifecycle | Complexity, memory, version drift | Not initially |
| D | Batch offline pre-conversion using Calibre CLI prior to ingestion | Simple pipeline separation | Operational overhead, delays | Only for bulk migrations |

Recommended Path:
Phase 1: Implement Option A for EPUB-only (no conversion needed beyond materialization).
Phase 2 (when adding MOBI/AZW3): Introduce Option B microservice with a narrow, audited interface returning normalized EPUB + JSON metadata. Wrap it with a `CalibreSidecarAdapter` implementing `FormatAdapter`.
Phase 3: Incrementally replace high-traffic adapters with native Java implementations; retire sidecar when parity achieved.

Security & Sandboxing for Sidecar (Option B):
- Run minimal Alpine-based image with pinned Calibre version.
- Resource limits: CPU quota, memory limit, tmpfs size cap.
- Timeout per conversion (Quartz job with watchdog cancels container if exceeded).
- Input sanitation: Restrict file size, disallow archives nesting depth > N.
- Output verification: Ensure produced EPUB passes epubcheck before acceptance.

Atomicity Strategy with External Sidecar:
- DB transaction kept open only for draft row & status (PENDING_CONVERT).
- Conversion occurs outside main transaction; upon success, materialization + final commit executed (two-step state machine: STAGED -> CONVERTED -> COMMITTED). If failure: mark FAILED with reason, leave staging file for inspection (retention N hours) and scheduled cleaner.

Metrics & Observability:
- Emit span attributes: format.original, format.normalized, adapter.name, conversion.ms, file.size.bytes.
- Count failures by adapter to decide which formats to re-implement natively.

Backlog Items to Add/Adjust:
- Task: Introduce `FormatAdapter` SPI & registry (before adding any additional formats).
- Task: Implement EPUB adapter (detection/extract/materialize) + tests.
- Task (Deferred): Calibre sidecar microservice spec + PoC (MOBI -> EPUB).
- Task (Deferred): Conversion watchdog & sandbox hardening guidelines.

Risk Mitigation Summary:
- Scope Control: Limit Phase 1 to metadata-safe operations (no reflow, no CSS rewriting).
- Progressive Hardening: Start without sidecar; add only when business need arises.
- Observability First: Metrics around conversion latency & failure surface gaps early.

Why Not Direct Calibre Embedding Now? Heavy dependency & Python/JVM integration cost outweighs benefit for single-format launch.

Result: We keep greenfield purity while enabling future calibrated reuse of Calibre where it buys fastest format expansion, then sunset that dependency.

---

### KOReader Sync Retention Design (Detail for O3 – Needs Confirmation)
Objective: Provide sync endpoints for KOReader (e.g., reading position, last opened, annotations summary pointers) with predictable storage growth and GDPR-friendly deletion.

Proposed Data Model:
```
table koreader_sync_events (
	id               bigserial primary key,
	user_id          uuid not null,
	book_id          bigint not null,
	event_type       smallint not null,            -- POSITION, BOOKMARK, ANNOTATION, DEVICE_PING
	payload_json     jsonb not null,
	created_utc      timestamptz not null default now(),
	expires_utc      timestamptz null,             -- set at insert (created_utc + retention)
	device_id        varchar(64) null,
	checksum_sha256  bytea not null,
	unique(user_id, book_id, event_type, checksum_sha256)
);
index idx_koreader_events_user_book on koreader_sync_events(user_id, book_id);
index idx_koreader_events_expires on koreader_sync_events(expires_utc);
```

Retention Policy:
1. Config property: `koreader.retention.days` (default 90). `0` or negative means keep indefinitely.
2. Soft Grace: Optional secondary property `koreader.retention.grace.days` (default 7) — rows eligible for purge move to a tombstone table for grace window before hard delete (allows recovery if accidental purge configuration change).
3. Quartz Job: Runs nightly (cron) -> selects up to N batches (e.g. 10k rows) ordered by expires_utc. Emits metrics: purged.count, remaining.count.
4. API Filtering: Endpoints never return expired events; queries filter by `expires_utc is null or expires_utc > now()`.
5. Privacy/Delete User: On user delete request -> immediate hard delete of rows for user_id bypassing grace.

Alternative (If Simpler Needed): Skip grace/tombstone initially; perform direct hard delete — confirm preference.

Open Points for Confirmation:
- Are annotations full-text stored or only references (e.g., highlight offsets)? (Affects payload size & retention pressure.)
- Desired default retention (stick with 90?)
- Need user-level override? (Per power user or per library settings.)

If above accepted, mark O3 Resolved and create tasks: schema migration, purge job, metrics, API endpoints, tests.

---

Please confirm KOReader retention specifics and whether to adopt the sidecar conversion Option B in backlog immediately or wait until first non-EPUB format requirement emerges.

## Terminology Update
- Enforcement -> Metadata Materialization: Persisting user edits (title, authors, tags, cover) directly inside the EPUB container (OPF, NCX, cover image) atomically alongside DB state.

## Action Items Derived
- Update migration plan tasks to reflect decisions & remove deprecated items.
- Add new tasks for: Domain schema design, Angular SPA scaffold, OIDC claim mapping, inotify watcher, conversion pipeline (phased), Quartz orchestration, OpenTelemetry integration, Redis-based rate limit (deferred), KOReader sync modeling, Flyway baseline.

---
Next: migration-plan.md will be revised to align with this resolution set.
