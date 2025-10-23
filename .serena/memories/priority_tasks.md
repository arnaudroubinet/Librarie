# Priority Tasks Quick Reference

## Purpose
Quick access to the top priority tasks from tasks.md for immediate work planning. Always consult the full [tasks.md](../../tasks.md) for implementation details.

## Current Sprint Priorities

### 🔥 CRITICAL (Do First)

#### T-001: Fix Search FormGroup Binding
- **Status**: ❌ Critical Bug
- **Impact**: Search feature broken (NG01050 errors)
- **File**: `frontend/src/app/components/search.component.ts`
- **Issue**: Advanced search fields not wrapped in FormGroup
- **Fix**: Move `<form>` tag to wrap entire search or add FormGroup wrapper
- **Effort**: 2h
- **Tests**: Unit + Manual + E2E

#### T-003: Fix Author Picture HTTP 406 Errors
- **Status**: ❌ Critical Bug
- **Impact**: Author pictures fail to load
- **Files**: 
  - `backend/infrastructure/adapter/in/rest/AuthorResource.java`
  - `frontend/src/app/components/author-card.component.ts`
- **Issue**: Content negotiation failing (406 Not Acceptable)
- **Fix**: Configure proper MediaType in JAX-RS endpoint
- **Effort**: 2h

#### T-009: Remove Demo Data from Production
- **Status**: ⚠️ Security Risk
- **Impact**: Production safety
- **Files**: `backend/src/main/resources/application.properties`
- **Issue**: Demo data could load in prod if not disabled
- **Fix**: Profile-specific configuration, validation at startup
- **Effort**: 3h

---

### 🚀 HIGH (Do Next)

#### T-012: Implement CI/CD Pipeline
- **Status**: ❌ Missing
- **Impact**: No automated testing/deployment
- **Files**: `.github/workflows/*.yml`
- **Deliverables**:
  - Backend build + test workflow
  - Frontend build + test workflow
  - Bundle size monitoring
  - Automated PR checks
- **Effort**: 8h

#### T-013: Implement HTTP Caching
- **Status**: ❌ Missing
- **Impact**: Performance (static assets not cached)
- **Files**: Backend JAX-RS resources, frontend interceptors
- **Strategy**: See `backend/docs/HTTP_CACHING.md`
- **Effort**: 5h

#### T-015: Add E2E Test Suite
- **Status**: ❌ Missing
- **Impact**: No integration testing
- **Tool**: Playwright
- **Coverage**: 
  - Search (T-001)
  - Reading progress (T-004)
  - Author pictures (T-003)
- **Effort**: 12h

---

### ⚙️ MEDIUM (Backlog)

#### T-004: Persist Reading Progress Across Sessions
- **Issue**: Progress lost on browser refresh
- **Fix**: Save to backend on position change, restore on load
- **Effort**: 6h

#### T-005: Add Loading States
- **Issue**: No user feedback during async operations
- **Fix**: Spinners, skeleton screens, progress bars
- **Effort**: 4h

#### T-006: Optimize TestContainers
- **Issue**: Slow test startup
- **Fix**: Enable container reuse
- **Effort**: 2h

#### T-007: Fix Search Results Pagination
- **Issue**: Pagination broken (shows all results)
- **Fix**: Implement proper Angular Material paginator
- **Effort**: 3h

#### T-008: Add Book Deletion Feature
- **Issue**: No way to remove books
- **Fix**: Delete button + confirmation dialog
- **Effort**: 4h

---

### 📝 LOW (Nice to Have)

#### T-010: Add Drag-and-Drop EPUB Upload
- **Enhancement**: Better UX for adding books
- **Effort**: 4h

#### T-011: Add Dark Mode
- **Enhancement**: User preference
- **Effort**: 6h

#### T-014: Add Export/Import Features
- **Enhancement**: Backup and migration
- **Effort**: 8h

---

## Task Implementation Pattern

When starting a task:

1. **Read full details** in [tasks.md](../../tasks.md)
2. **Create branch**: `feature/T-XXX-description`
3. **Follow prerequisites** listed in task
4. **Apply proposed changes** exactly as specified
5. **Run validation tests** (unit, manual, E2E)
6. **Check acceptance criteria** (DoD checklist)
7. **Update documentation** if needed
8. **Create PR** following [CONTRIBUTING.md](../../CONTRIBUTING.md)

## Task Status Legend

- ❌ **Not Started** - No work done
- 🔄 **In Progress** - Currently being worked on
- ⚠️ **Blocked** - Waiting on external dependency
- ✅ **Complete** - Merged to main

## Dependencies Between Tasks

```
T-001 (Search fix) ← T-015 (E2E tests) - Can't test search until fixed
T-003 (Author pics) ← T-015 (E2E tests) - Can't test until fixed
T-012 (CI/CD) → T-015 (E2E tests) - CI should run E2E tests
T-009 (Demo data) → T-012 (CI/CD) - Deploy safety before automation
```

## Quick Commands for Common Tasks

### Start New Task
```powershell
git checkout -b feature/T-XXX-description
# Make changes
./mvnw test                    # Backend
npm test                       # Frontend
git commit -m "feat: [T-XXX] Description"
git push origin feature/T-XXX-description
```

### Validate Task Completion
```powershell
# Backend
./mvnw clean verify            # All tests + quality

# Frontend  
npm test                       # Unit tests
npm run build                  # Production build
npm run size                   # Bundle size check
```

---

## When to Update This Memory

Update when:
- Task priorities change
- Tasks are completed
- New critical tasks are added
- Dependencies change

Always keep in sync with [tasks.md](../../tasks.md) master list.

---

> **Current Focus**: Fix T-001 (Search) and T-003 (Author pics) before implementing T-012 (CI/CD) to ensure clean baseline.
