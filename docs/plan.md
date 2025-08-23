# Librarie Implementation Guide

## Task Index

### Core Infrastructure
- **CORE-001**: Docker Packaging & Compose Templates - Containerization and deployment infrastructure

### Authentication & Authorization
- **AUTH-001**: Authentication Providers and No-Auth Mode - Multiple authentication methods support
- **AUTH-002**: Accounts, Roles, and Permissions - Multi-user system with role-based access control

### Data Management
- **DATA-001**: Upload & Automated Ingest Pipeline - File upload and automated ingestion system
- **DATA-002**: Metadata Editing and External Providers - Metadata management with external sources
- **DATA-003**: Batch Edit & Delete - Bulk operations on multiple books

### File Processing
- **PROC-001**: EPUB Fixer - Automatic EPUB issue repair for compatibility
- **PROC-002**: E-book Conversion Service - Format conversion using Calibre integration
- **PROC-003**: Enforce Cover/Metadata Changes to Files - Write metadata back to files

### Content Delivery
- **DELIVERY-001**: OPDS 1.x/2.0 Catalog - OPDS protocol support for e-readers
- **DELIVERY-002**: Multi-Format In-Browser Readers - Browser-based readers for all formats
- **DELIVERY-003**: Send-to-Device via Email - Email books to reading devices

### User Features
- **USER-001**: User Shelves / Collections - Personal book organization system
- **USER-002**: Advanced Search & Filtering - Complex search across metadata fields

### Synchronization
- **SYNC-001**: Kobo / KOReader Sync - Reading progress synchronization

### Administration
- **ADMIN-001**: Stats & Operational Metrics Page - System health and usage statistics
- **ADMIN-002**: Update Notifications - Alert about available updates
- **ADMIN-003**: Internationalization (i18n) - Multi-language support

---

## Core Infrastructure

### CORE-001: Docker Packaging & Compose Templates
**Goal:** Provide official Docker images and compose examples for easy deployment.

**Implementation Details:**
- Create multi-stage Dockerfile for backend (Quarkus) and frontend (Angular)
- Define environment variables for database connection, storage paths, and auth configuration
- Create Docker Compose templates for common deployment scenarios
- Implement health check endpoints for container orchestration
- Configure volume mounts for persistent data (database, assets, configuration)

**Validation Criteria:**
- Docker image builds successfully in CI pipeline without errors
- Container health check endpoint returns 200 within 30 seconds of startup
- Docker Compose deployment successfully brings up all services (database, backend, frontend)
- Data persists across container restarts when volumes are properly mounted
- Environment variable overrides work correctly for all configurable parameters
- Container logs are accessible and properly formatted
- Resource limits are enforced and documented

---

## Authentication & Authorization

### AUTH-001: Authentication Providers and No-Auth Mode
**Goal:** Support multiple authentication methods including OIDC, reverse proxy, magic link, and no-auth mode for different deployment scenarios.

**Implementation Details:**

#### No Authentication Mode
- Implement bypass mechanism when `librarie.auth.disable=true`
- Auto-generate admin token for all requests
- Display warning banner in UI about security implications

#### OIDC Integration
- Implement Authorization Code flow with PKCE
- Handle token exchange between frontend and backend
- Map OIDC claims to internal user roles
- Support auto-provisioning of new users on first login
- Implement token refresh mechanism

#### Reverse Proxy Authentication
- Trust specified headers from whitelisted proxy IPs
- Map proxy-provided username to internal user
- Validate proxy source IP against whitelist

#### Magic Link Authentication
- Generate cryptographically secure one-time tokens
- Implement email delivery for magic links
- Enforce token expiration and single-use constraints
- Track token usage for security auditing

**Validation Criteria:**
- **No-auth mode:** When disabled, all requests receive admin token; warning banner displays
- **OIDC:** 
  - State parameter validates correctly preventing CSRF
  - Nonce validates in ID token
  - User created on first successful login
  - Token signature verification passes with test IdP
  - Refresh token flow maintains session
- **Proxy auth:**
  - Requests from whitelisted IPs with valid header auto-authenticate
  - Requests from non-whitelisted IPs return 401
  - Missing header from proxy returns 401
- **Magic link:**
  - Token expires after configured duration (returns 410)
  - Token works only once (second use returns 410)
  - Token tied to specific email (mismatch returns 403)
  - Email delivery confirmed via SMTP mock in tests
- All auth methods issue valid JWT with correct claims (iss, aud, exp, roles)

### AUTH-002: Accounts, Roles, and Permissions
**Goal:** Implement multi-user system with role-based access control and content filtering.

**Implementation Details:**

#### User Management
- Create user entity with unique email/username
- Implement password hashing with bcrypt/scrypt
- Support user activation/deactivation
- Track last login and session management

#### Role System
- Define three base roles: ADMIN, EDITOR, READER
- Create permission mappings for each role
- Support custom role creation (future enhancement)

#### Permission Framework
- Map permissions to API endpoints and actions
- Implement permission checking middleware/interceptor
- Create audit log for permission-denied events

#### Content Filtering
- Implement per-user hidden category lists
- Apply filters at query level for consistency
- Allow admins to bypass all filters
- Support group-based filter inheritance

#### Public Registration
- Toggle-able registration endpoint
- Optional email verification workflow
- Default role assignment for new users
- Rate limiting on registration endpoint

**Validation Criteria:**
- **Role enforcement:**
  - READER: GET requests succeed (200), write operations fail (403)
  - EDITOR: Can modify all book metadata (200), delete own uploads (200), cannot delete others' uploads (403)
  - ADMIN: All operations succeed (200)
- **Registration:**
  - When enabled: POST to register creates user (201)
  - With email verification: User created as inactive until verified
  - When disabled: Registration endpoint returns 404
  - Duplicate email/username returns 409
- **Content filtering:**
  - Hidden categories never appear in user's search/browse results
  - Filter applies to all list endpoints consistently
  - Admin sees all content regardless of filters
  - Filtering works with pagination (no items leak across pages)
- **Audit trail:**
  - All metadata changes log user, timestamp, and changes
  - Failed permission checks logged with details
  - Audit entries immutable once created

---

## Data Management

### DATA-001: Upload & Automated Ingest Pipeline
**Goal:** Handle file uploads and automated ingestion with validation and processing.

**Implementation Details:**

#### Upload Interface
- Implement multipart file upload endpoint
- Support drag-and-drop in UI
- Display upload progress with cancellation option
- Handle multiple simultaneous uploads

#### File System Watcher
- Monitor configured directory for new files
- Implement debouncing for file write completion
- Fall back to polling for network shares
- Support recursive directory watching

#### Intake Stage
- Calculate SHA256 hash for deduplication
- Check against existing assets
- Create temporary working copy

#### Validation Stage
- Verify file extension against whitelist
- Check file size limits
- Validate file structure (not corrupted)
- Scan for malicious content (optional)

#### Metadata Extraction
- Extract title, author, series from filename patterns
- Parse embedded metadata from files
- Extract cover images
- Identify language and publication info

#### Asset Storage
- Move processed file to permanent storage
- Create database entry with metadata
- Generate thumbnails for covers
- Update search indices

#### Backup Management
- Store original files before modifications
- Implement retention policies
- Compress backups if configured
- Schedule cleanup jobs

**Validation Criteria:**
- **Upload flow:**
  - File upload completes with progress tracking
  - Multiple simultaneous uploads don't interfere
  - Cancelled uploads clean up properly
  - Network interruption handles gracefully
- **Deduplication:**
  - Identical files (same SHA256) create single asset
  - Duplicate upload returns 200 with reference to existing
  - Metadata can differ for same file content
- **Validation:**
  - Blocked extensions return 415
  - Oversized files return 413
  - Corrupted files return 422 with clear error
- **File watcher:**
  - New file triggers import within 10 seconds
  - Partially written files don't trigger premature import
  - Network share polling works with 30-second interval
  - Subdirectories processed recursively if configured
- **Metadata:**
  - Standard patterns extract correctly (Author - Title.epub)
  - Embedded metadata takes precedence over filename
  - Missing metadata fields remain nullable
- **Backup:**
  - Original preserved before any modifications
  - Backup accessible via admin interface
  - Scheduled cleanup removes old backups
  - Compression reduces storage by >50% for text formats

### DATA-002: Metadata Editing and External Providers
**Goal:** Enable comprehensive metadata management with external data source integration.

**Implementation Details:**

#### Metadata Editor
- Create form UI for all metadata fields
- Support bulk field updates
- Implement field validation rules
- Track edit history with diffs

#### Field Management
- Title variants (original, sort, display)
- Author management (multiple, roles)
- Series tracking (name, position)
- Identifier systems (ISBN, ASIN, DOI)
- Tag hierarchies
- Custom columns

#### Provider Integration Framework
- Define provider interface contract
- Implement provider registry
- Create adapter for each provider
- Handle authentication/API keys

#### Provider Implementations
- Google Books API integration
- Open Library integration
- ISBNdb integration (if API key available)
- Custom provider plugin system

#### Merge Strategy
- Define field precedence rules
- Implement conflict resolution
- Support manual override flags
- Preview changes before applying

#### Cover Management
- Download and store cover images
- Support multiple cover versions
- Implement cover quality scoring
- Allow manual cover upload

**Validation Criteria:**
- **Editor functionality:**
  - All fields save correctly to database
  - Validation prevents invalid ISBN formats (400)
  - Bulk edit applies to all selected items
  - Edit history shows who/when/what changed
  - Concurrent edits handle via optimistic locking
- **Provider integration:**
  - ISBN search returns consistent results
  - Missing ISBN returns empty result, not error
  - Rate limiting triggers backoff (no 429 errors)
  - API key validation fails gracefully
  - Network timeout handled (5-second limit)
- **Merge behavior:**
  - User edits never overwritten without confirmation
  - Provider priority order respected
  - Empty fields filled, existing preserved
  - Merge preview shows exactly what will change
- **Cover handling:**
  - Downloaded covers stored with hash-based names
  - Duplicate covers deduplicated by content hash
  - GET returns proper ETag header
  - Conditional GET with If-None-Match returns 304
  - Multiple cover versions accessible
  - Cover quality metric calculated consistently

### DATA-003: Batch Edit & Delete
**Goal:** Enable efficient bulk operations on multiple books.

**Implementation Details:**

#### Selection Interface
- Implement checkbox selection in list views
- Add "select all" with pagination awareness
- Show selection count and actions
- Persist selection across page navigation

#### Batch Edit Operations
- Apply same change to multiple items
- Support incremental updates (add tag to existing)
- Preview changes before applying
- Execute as background job for large batches

#### Batch Delete Operations
- Confirm with item count
- Delete database entries and files
- Clean orphaned assets
- Generate deletion report

#### Transaction Management
- Process items individually within transaction
- Continue on individual failures
- Rollback option for critical errors
- Generate detailed result report

**Validation Criteria:**
- **Selection:**
  - Select all selects current page only
  - Selection persists during pagination
  - Clear selection works instantly
  - Selection count updates correctly
- **Batch edit:**
  - 50-item batch completes within 30 seconds
  - Failed items reported with reasons
  - Successful items committed despite failures
  - Concurrent edits detected via ETags
  - Result report shows success/failure per item
- **Batch delete:**
  - Confirmation requires explicit action
  - Files removed from storage
  - Database cascade deletes work
  - Orphan checker finds no residual data
  - Deletion reversible from backups if enabled
- **Performance:**
  - Operations chunk into manageable batches
  - UI remains responsive during operation
  - Progress indicator updates regularly
  - Cancel stops processing remaining items

---

## File Processing

### PROC-001: EPUB Fixer
**Goal:** Automatically repair common EPUB issues for compatibility.

**Implementation Details:**

#### EPUB Parser
- Unzip and parse container structure
- Validate against EPUB specification
- Identify common issues
- Generate fix recommendations

#### Fix Implementations
- Add missing UTF-8 declarations
- Repair broken NCX navigation
- Remove invalid empty img tags
- Fix malformed XHTML
- Correct content-type declarations
- Update spine references

#### Language Validation
- Check language tags against ISO-639
- Prompt for missing language
- Apply default from configuration
- Update all language references

#### Pipeline Integration
- Run as optional ingest step
- Support manual trigger
- Batch processing mode
- Skip if already valid

#### Safety Measures
- Always backup before fixing
- Validate result after fixes
- Rollback on corruption
- Log all changes made

**Validation Criteria:**
- **UTF-8 fixes:**
  - Missing declaration added to all XHTML files
  - Existing correct declarations unchanged
  - BOM removed if present
- **NCX repairs:**
  - Broken fragment identifiers corrected
  - Missing navMap entries added
  - playOrder sequence corrected
  - Result passes epubcheck validation
- **Content fixes:**
  - Empty img tags removed completely
  - Malformed tags closed properly
  - XHTML validates after fixes
- **Language handling:**
  - Invalid codes flagged for review
  - Default applied when configured
  - All language attributes updated consistently
- **Safety:**
  - Original file recoverable from backup
  - Failed fix doesn't corrupt file
  - Fix process idempotent
  - Second run makes no changes
- **Batch mode:**
  - Progress tracks items processed/remaining
  - Failures don't stop other items
  - Resume from interruption point
  - Summary report generated

### PROC-002: E-book Conversion Service
**Goal:** Convert between e-book formats using Calibre integration.

**Implementation Details:**

#### Calibre Integration
- Detect Calibre installation or container
- Validate ebook-convert availability
- Configure conversion parameters
- Handle binary execution safely

#### Job Queue System
- Queue conversion requests
- Track job status and progress
- Implement priority levels
- Support job cancellation

#### Format Support
- Define source/target format matrix
- Implement format-specific options
- Validate conversion feasibility
- Handle format limitations

#### Conversion Execution
- Create working directory
- Execute conversion with timeout
- Capture output and errors
- Validate result file

#### Result Management
- Store converted files as new assets
- Link to source book
- Update format availability
- Cache for repeated requests

#### Error Handling
- Timeout long-running conversions
- Capture stderr for diagnostics
- Retry transient failures
- Report permanent failures

**Validation Criteria:**
- **Calibre detection:**
  - Missing Calibre returns 503 with clear message
  - Version compatibility checked
  - Container connection verified
- **Conversion quality:**
  - MOBI to EPUB preserves text content
  - Images included in conversion
  - Metadata transferred correctly
  - Result passes format validation
- **Job management:**
  - Queue status accessible via API
  - Jobs process in priority order
  - Cancellation stops execution
  - Completed jobs cached
- **Performance:**
  - Conversion timeout enforced (5 minutes default)
  - Parallel conversions limited
  - CPU/memory limits enforced
  - Cache prevents redundant conversions
- **Error handling:**
  - Invalid input returns FAILED status
  - Stderr captured in job record
  - Partial files cleaned up
  - Retry succeeds for transient failures

### PROC-003: Enforce Cover/Metadata Changes to Files
**Goal:** Write metadata changes back to actual files for external compatibility.

**Implementation Details:**

#### Format-Specific Writers
- EPUB: OPF manifest updater
- PDF: XMP metadata writer
- MOBI/AZW3: Calibre-based updater
- CBZ: ComicInfo.xml writer

#### Metadata Mapping
- Map internal fields to format standards
- Handle format limitations
- Preserve format-specific metadata
- Generate required fields

#### Cover Embedding
- Extract current cover if exists
- Resize/optimize new cover
- Embed using format conventions
- Update manifest references

#### Backup Strategy
- Store original before changes
- Version multiple edits
- Implement restore function
- Clean old backups

#### Enforcement Triggers
- On-demand via UI action
- Batch enforcement tool
- Optional auto-enforcement
- Selective field updates

**Validation Criteria:**
- **EPUB updates:**
  - OPF dc:title matches database
  - Cover image in manifest
  - Cover referenced in guide/landmarks
  - UUID remains unchanged
  - Result validates with epubcheck
- **PDF updates:**
  - XMP metadata readable by PDF readers
  - Title/Author in document properties
  - Cover as first page (if configured)
  - File size increase < 10%
- **Backup integrity:**
  - Original recoverable after changes
  - SHA256 differs from modified
  - Restore returns exact original
  - Backup metadata preserved
- **Idempotency:**
  - Repeated enforcement produces same result
  - No corruption from multiple runs
  - Hash stable except for timestamps
- **Failure handling:**
  - Unsupported formats logged but don't fail
  - Partial writes rolled back
  - Original unchanged on error
  - Clear error messages for failures

---

## Content Delivery

### DELIVERY-001: OPDS 1.x/2.0 Catalog
**Goal:** Expose library via OPDS protocol for e-reader applications.

**Implementation Details:**

#### OPDS Feed Generation
- Root catalog with navigation links
- Acquisition feeds for books
- Navigation feeds for browsing
- OpenSearch description document

#### Feed Types
- New additions feed
- Popular books feed
- Author index and feeds
- Series index and feeds
- Tag/category feeds
- Search results feed

#### OPDS 1.x Support
- Generate Atom XML feeds
- Include Dublin Core metadata
- Proper link relations
- Media type declarations

#### OPDS 2.0 Support
- Generate JSON feeds
- Include full metadata
- Support extensions
- Implement pagination

#### Authentication
- HTTP Basic auth option
- Bearer token support
- Public/private catalog modes
- Per-user content filtering

#### Acquisition Links
- Multiple format options
- Direct download links
- Format preference handling
- Bandwidth optimization

**Validation Criteria:**
- **Feed structure:**
  - Root feed validates against OPDS schema
  - All required elements present
  - Links use correct relations
  - Self links included
- **Content completeness:**
  - All books appear in appropriate feeds
  - Metadata fields populated
  - Cover images linked
  - Acquisition links for all formats
- **Pagination:**
  - Limit parameter respected (max 25 items)
  - Next/previous links correct
  - Total count accurate
  - Cursor stability maintained
- **Search:**
  - Results match query terms
  - Relevance ordering consistent
  - Special characters handled
  - Empty results valid feed
- **Authentication:**
  - Unauthenticated returns 401 with WWW-Authenticate
  - Valid credentials return 200
  - Content filtered per user permissions
  - Token expiry handled
- **Performance:**
  - Feed generation < 100ms for 25 items
  - Caching reduces database queries
  - ETags enable client caching
  - Compression supported

### DELIVERY-002: Multi-Format In-Browser Readers
**Goal:** Provide browser-based readers for all supported formats.

**Implementation Details:**

#### EPUB Reader
- Web-based EPUB renderer
- Generate reading manifest
- Serve individual resources
- Track reading position

#### PDF Viewer
- Integrate PDF.js library
- Support HTTP range requests
- Implement page navigation
- Enable text selection

#### CBZ/CBR Viewer
- Image sequence display
- Prefetch next pages
- Support zoom/pan
- Handle archive formats

#### Audio Player
- HTML5 audio integration
- Support for chapters
- Playback speed control
- Resume capability

#### Reader Features
- Bookmarking support
- Font size adjustment
- Theme selection
- Full-screen mode
- Progress tracking

#### Security
- Validate user permissions
- Prevent unauthorized access
- Secure resource serving
- Session management

**Validation Criteria:**
- **EPUB reader:**
  - Manifest endpoint returns valid JSON
  - Resources serve with correct MIME types
  - Navigation works between chapters
  - Style sheets apply correctly
  - Images display inline
- **PDF viewer:**
  - Range requests return 206 status
  - First page loads in < 3 seconds
  - Text selection works
  - Search functionality operates
  - Print function available
- **CBZ viewer:**
  - Images extract and display
  - Order maintains correctly
  - Prefetch reduces latency < 100ms
  - Zoom maintains quality
  - Navigation responsive
- **Audio player:**
  - Playback starts within 2 seconds
  - Chapter markers navigate correctly
  - Speed adjustment works (0.5x-2x)
  - Position saves and restores
- **Access control:**
  - Unauthorized access returns 403
  - Hidden content filtered
  - Session timeout handled
  - Permissions checked per resource

### DELIVERY-003: Send-to-Device via Email
**Goal:** Email books directly to reading devices.

**Implementation Details:**

#### Email Configuration
- SMTP server settings
- From address configuration
- DKIM signing support
- Rate limiting rules

#### Device Management
- User device registry
- Email address validation
- Format preferences
- Delivery history

#### Format Selection
- Check available formats
- Apply device preferences
- Trigger conversion if needed
- Optimize file size

#### Email Generation
- Create MIME message
- Attach book file
- Include metadata
- Add instructions

#### Delivery Pipeline
- Queue email jobs
- Execute SMTP delivery
- Handle failures/retries
- Track delivery status

#### Security
- Validate destinations
- Domain whitelisting
- User quotas
- Audit logging

**Validation Criteria:**
- **SMTP integration:**
  - Test SMTP receives correct message
  - Attachment MIME type correct
  - Filename includes title
  - Size within limits
- **Format selection:**
  - Preferred format selected when available
  - Conversion triggered for missing formats
  - Fallback to available format
  - Size optimization applied
- **Delivery:**
  - Success logged with message ID
  - Transient failures retried (4xx)
  - Permanent failures reported (5xx)
  - Retry uses exponential backoff
- **Rate limiting:**
  - Daily limit enforced per user
  - Exceeding limit returns 429
  - Counter resets at midnight
  - Admin bypass available
- **Security:**
  - Only whitelisted domains accepted
  - Invalid addresses rejected
  - Audit log captures all sends
  - User can view their history

---

## User Features

### USER-001: User Shelves / Collections
**Goal:** Personal book organization through custom collections.

**Implementation Details:**

#### Shelf Management
- Create named shelves
- Set visibility levels
- Configure sort order
- Add descriptions

#### Book Organization
- Add/remove books
- Bulk operations
- Drag-drop support
- Quick-add buttons

#### Visibility Levels
- Private (owner only)
- Shared (specific users)
- Public (all users)
- Inherited permissions

#### Sharing Features
- Share shelf links
- Collaborate on shared shelves
- Follow other users' shelves
- Export shelf lists

#### UI Integration
- Shelf browser
- Book count display
- Recent additions
- Quick filters

**Validation Criteria:**
- **CRUD operations:**
  - Create shelf returns 201 with ID
  - Duplicate names return 409
  - Rename updates successfully
  - Delete removes shelf and links
- **Book management:**
  - Add book idempotent (no duplicates)
  - Remove book immediate
  - Bulk add processes all items
  - Order preserved if configured
- **Visibility:**
  - Private shelves hidden from others
  - Shared shelves visible to authorized
  - Public shelves in discovery
  - Permissions cascade properly
- **Data integrity:**
  - User deletion preserves public shelves
  - Book deletion removes from shelves
  - No orphaned shelf-book links
  - Referential integrity maintained
- **Performance:**
  - Large shelf (1000+ books) paginated
  - Add/remove operations < 100ms
  - List shelves cached appropriately

### USER-002: Advanced Search & Filtering
**Goal:** Complex search capabilities across all metadata fields.

**Implementation Details:**

#### Search Interface
- Query syntax parser
- Advanced form builder
- Filter combinations
- Search history

#### Filter Types
- Text fields (title, description)
- Authors (exact and partial)
- Series membership
- Tags/categories
- Format availability
- Date ranges
- Identifiers (ISBN, ASIN)
- Language
- Custom columns

#### Query Processing
- Parse query syntax
- Build database query
- Apply security filters
- Execute with pagination

#### Search Features
- Boolean operators (AND, OR, NOT)
- Phrase searching
- Wildcard support
- Fuzzy matching
- Regular expressions

#### Results Handling
- Relevance scoring
- Multiple sort options
- Faceted results
- Export capabilities

#### Performance
- Query optimization
- Index utilization
- Result caching
- Progressive loading

**Validation Criteria:**
- **Query accuracy:**
  - Exact matches rank first
  - Boolean operators work correctly
  - Phrase search preserves order
  - Case-insensitive by default
- **Filter combinations:**
  - AND narrows results
  - OR expands results
  - NOT excludes correctly
  - Nested conditions work
- **Range queries:**
  - Date ranges inclusive
  - Open-ended ranges supported
  - Invalid dates rejected
  - Timezone handled correctly
- **Sorting:**
  - All sort fields work
  - Direction toggles correctly
  - Null values handled
  - Secondary sort stable
- **Performance:**
  - 100k record search < 150ms
  - Indexes used (explain plan)
  - Pagination doesn't re-query
  - Facet counts accurate
- **Security:**
  - Hidden categories filtered
  - User permissions applied
  - No data leakage via search

---

## Synchronization

### SYNC-001: Kobo / KOReader Sync
**Goal:** Synchronize reading progress across devices.

**Implementation Details:**

#### KOReader Protocol
- Implement KOSync endpoints
- User authentication
- Progress data structure
- Conflict resolution

#### Data Model
- User-book-device tracking
- Position/percentage storage
- Timestamp management
- Statistics collection

#### Sync Operations
- Upload progress
- Download progress
- Merge conflicts
- Reset progress

#### Device Management
- Register devices
- Device nicknames
- Last sync tracking
- Device removal

#### Kobo Integration
- Research available APIs
- Implement compatible endpoints
- Handle Kobo-specific format
- Test with devices

**Validation Criteria:**
- **KOReader sync:**
  - Progress upload returns 200
  - Download returns latest position
  - Device-specific progress maintained
  - Timestamps prevent data loss
- **Authentication:**
  - Missing auth returns 401
  - Invalid credentials rejected
  - User isolation enforced
  - Sessions managed properly
- **Conflict resolution:**
  - Latest timestamp wins
  - No progress lost
  - Explicit conflicts reported
  - Manual resolution available
- **Data persistence:**
  - Progress survives restart
  - User deletion cascades
  - Book deletion preserves history
  - Device limit enforced
- **Performance:**
  - Sync request < 100ms
  - Batch sync supported
  - 50 updates/second sustained
  - Database optimized for queries

---

## Administration

### ADMIN-001: Stats & Operational Metrics Page
**Goal:** Display system health and usage statistics.

**Implementation Details:**

#### Metrics Collection
- Count import operations
- Track conversions
- Monitor fixes applied
- Record send operations
- Calculate storage usage

#### Dashboard Components
- Real-time counters
- Historical graphs
- Error summaries
- Performance metrics

#### Data Aggregation
- Daily summaries
- Monthly trends
- User statistics
- Format distribution

#### Prometheus Integration
- Export metrics endpoint
- Label dimensions
- Counter/gauge types
- Histogram buckets

#### Access Control
- Admin-only access
- Read-only view option
- API authentication
- Audit access logs

**Validation Criteria:**
- **Counter accuracy:**
  - Import count matches operations
  - Conversion tallies correct
  - Daily reset works
  - Historical data preserved
- **Metrics endpoint:**
  - Prometheus format valid
  - All metrics present
  - Labels consistent
  - Values update real-time
- **Performance:**
  - Dashboard loads < 1 second
  - Queries optimized
  - Caching where appropriate
  - No impact on operations
- **Access control:**
  - Non-admin users blocked
  - API key required
  - Audit trails complete
  - No sensitive data exposed

### ADMIN-002: Update Notifications
**Goal:** Alert administrators about available system updates.

**Implementation Details:**

#### Version Check System
- Query upstream releases
- Compare with current version
- Parse semantic versioning
- Check compatibility matrix

#### Notification Delivery
- In-app notifications
- Email alerts option
- Webhook notifications
- RSS/Atom feed

#### Update Information
- Release notes display
- Breaking changes highlight
- Migration requirements
- Security advisories

#### Configuration
- Check frequency settings
- Notification preferences
- Update channels (stable/beta)
- Automatic vs manual

#### Security
- Verify release signatures
- Use secure channels only
- Rate limit checks
- Validate responses

**Validation Criteria:**
- **Version detection:**
  - Current version compared correctly
  - Semantic versioning parsed
  - Pre-release versions handled
  - Downgrade scenarios detected
- **Notification timing:**
  - Updates checked on schedule
  - Immediate notification for critical
  - No spam from repeated checks
  - Manual check triggers immediate
- **Content accuracy:**
  - Release notes displayed correctly
  - Breaking changes highlighted
  - Migration steps included
  - Links functional
- **Security validation:**
  - Signatures verified
  - HTTPS enforced
  - Responses validated
  - Error handling secure

### ADMIN-003: Internationalization (i18n)
**Goal:** Support multiple languages for global deployment.

**Implementation Details:**

#### Frontend Internationalization
- Angular i18n integration
- Translation key management
- Locale-specific formatting
- Dynamic language switching

#### Backend Internationalization
- Resource bundle management
- Database message storage
- API response localization
- Error message translation

#### Content Localization
- Book metadata fields
- User interface elements
- Email templates
- Documentation

#### Language Management
- Add/remove languages
- Translation completion tracking
- Fallback to default language
- Professional translation integration

#### Cultural Considerations
- Date/time formatting
- Number formatting
- Currency display
- Text direction (RTL)

**Validation Criteria:**
- **Language switching:**
  - UI updates immediately
  - All text elements translate
  - Formatting applies correctly
  - No untranslated content
- **Translation completeness:**
  - All keys have translations
  - Placeholders work correctly
  - Pluralization handles correctly
  - Context-sensitive translations
- **Fallback behavior:**
  - Missing translations use default
  - No broken interface elements
  - Graceful degradation
  - Error logging for missing keys
- **Performance:**
  - Language switch < 200ms
  - Translation loading cached
  - No impact on operations
  - Minimal bundle size increase