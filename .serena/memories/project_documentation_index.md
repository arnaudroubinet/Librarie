# Project Documentation Index

## Purpose
This memory provides Serena with references to the main project documentation and **when to consult each document** for optimal efficiency.

## 🎯 Start Here Flow

```
New to project? → llms.txt → PROJECT_OVERVIEW.md → GLOSSARY.md
Need a term? → quick_terminology (this memory) → GLOSSARY.md (if details needed)
Got an error? → quick_troubleshooting (this memory) → TROUBLESHOOTING.md (if not found)
Starting task? → priority_tasks (this memory) → tasks.md (full details)
Architecture question? → ARCHITECTURE.md
Contributing? → CONTRIBUTING.md
```

## 📚 Documentation Structure

### Entry Point for LLMs
- **`llms.txt`** (root) - Primary navigation hub
  - Links to all major documentation
  - Optimized for LLM parsing
  - **When**: First time reading project or orienting

---

### Core Documentation (Root Level)

#### 1. **`README.md`** - Quick Start
- **Content**: Concise overview, quick start (2 mins), tech stack table
- **When to Read**: 
  - First impression of project
  - Sharing with others
  - Quick reference to get running
- **Length**: ~150 lines (scannable)

#### 2. **`docs/PROJECT_OVERVIEW.md`** - Complete System Overview ⭐
- **Content**: Business context, tech stack details, architecture, domain model, user flows
- **When to Read**:
  - Understanding complete system
  - Before major architectural decisions
  - Learning business domain
  - Onboarding new developers
- **Length**: 600+ lines (comprehensive)
- **Priority**: HIGH - Most complete documentation

#### 3. **`docs/GLOSSARY.md`** - Authoritative Terminology ⭐
- **Content**: Domain concepts, technical terms, acronyms, frontend-backend mapping
- **When to Read**:
  - Learning vocabulary
  - Resolving naming conflicts
  - Writing documentation
  - Code review (checking consistency)
- **Length**: 300+ lines
- **Priority**: HIGH - Single source of truth for terms
- **Quick Access**: Use `quick_terminology` memory for common lookups

#### 4. **`docs/TROUBLESHOOTING.md`** - Problem Resolution ⭐
- **Content**: 20+ common issues with solutions, diagnostic commands, error patterns
- **When to Read**:
  - Encountering errors
  - Development environment problems
  - Build failures
  - Test failures
  - Performance issues
- **Length**: 400+ lines
- **Priority**: HIGH - First stop for errors
- **Quick Access**: Use `quick_troubleshooting` memory for top 10 issues

#### 5. **`ARCHITECTURE.md`** - Technical Architecture
- **Content**: C4 diagrams, hexagonal architecture, component structure, ADRs
- **When to Read**:
  - Understanding system design
  - Making architectural decisions
  - Adding new components/services
  - Reviewing architecture violations
- **Length**: Detailed with diagrams
- **Priority**: HIGH for technical work

#### 6. **`CONTRIBUTING.md`** - Development Workflow
- **Content**: CI/CD, coding standards, testing requirements, PR process, bundle monitoring
- **When to Read**:
  - Before first contribution
  - Setting up development environment
  - Before creating PR
  - Understanding quality gates
- **Length**: Detailed workflow guide

#### 7. **`audit_report.md`** - System Audit
- **Content**: Known issues, risk assessment, recommendations, roadmap
- **When to Read**:
  - Before reporting bugs (check if known)
  - Understanding project status
  - Planning improvements
  - Risk assessment
- **Last Updated**: Oct 18, 2025
- **Priority**: MEDIUM - Reference for known issues

#### 8. **`tasks.md`** - Task Backlog
- **Content**: 15 prioritized tasks with step-by-step implementation guides
- **When to Read**:
  - Planning what to work on
  - Implementing specific feature from backlog
  - Understanding acceptance criteria
  - Following implementation patterns
- **Length**: 1500+ lines
- **Priority**: HIGH for active development
- **Quick Access**: Use `priority_tasks` memory for top priorities

---

### Backend-Specific Documentation

Location: `backend/`

#### **`backend/DEMO_DATA_IDEMPOTENCY.md`**
- **Content**: Demo data loading mechanism, idempotency implementation
- **When**: Understanding how demo data works, debugging data loading

#### **`backend/docs/HTTP_CACHING.md`**
- **Content**: HTTP caching strategy for static assets (covers, EPUBs)
- **When**: Implementing caching (T-013), performance optimization

#### **`.serena/memories/backend_*.md`** (4 files)
- **Content**: Backend technical overview, code style, commands, task completion
- **When**: Backend development, code review, following patterns

---

### Frontend-Specific Documentation

Location: `frontend/`

#### **`frontend/README.md`**
- **Content**: Frontend-specific setup, Angular details
- **When**: Frontend development setup

#### **`.serena/memories/frontend_*.md`** (4 files)
- **Content**: Frontend technical overview, code style, commands, task completion
- **When**: Frontend development, component creation, following patterns

---

### CI/CD Documentation

Location: `.github/`

#### **`.github/BRANCH_PROTECTION.md`**
- **Content**: Branch protection rules
- **When**: Setting up repository, understanding merge requirements

#### **`.github/workflows/*.yml`**
- **Content**: GitHub Actions workflow definitions
- **When**: Implementing T-012 (CI/CD), debugging workflows

---

## 🎯 Decision Tree: Which Doc to Read?

### "I need to understand..."

| Question | Primary Doc | Secondary | Memory Shortcut |
|----------|-------------|-----------|-----------------|
| **What is this project?** | PROJECT_OVERVIEW.md | README.md | project_global_context |
| **What does term X mean?** | GLOSSARY.md | - | quick_terminology ⚡ |
| **How do I fix error Y?** | TROUBLESHOOTING.md | - | quick_troubleshooting ⚡ |
| **What should I work on?** | tasks.md | audit_report.md | priority_tasks ⚡ |
| **How is this architected?** | ARCHITECTURE.md | PROJECT_OVERVIEW.md | - |
| **How do I contribute?** | CONTRIBUTING.md | - | - |
| **Why this design choice?** | ARCHITECTURE.md (ADRs) | - | - |
| **How do I add entity X?** | ARCHITECTURE.md | backend_code_style | - |
| **How do I add component Y?** | ARCHITECTURE.md | frontend_code_style | - |

### "I'm experiencing..."

| Situation | Start With | Then Read | Memory Shortcut |
|-----------|------------|-----------|-----------------|
| **Build error** | quick_troubleshooting ⚡ | TROUBLESHOOTING.md | ✅ |
| **Test failure** | quick_troubleshooting ⚡ | TROUBLESHOOTING.md | ✅ |
| **Runtime error** | quick_troubleshooting ⚡ | TROUBLESHOOTING.md | ✅ |
| **Unknown term** | quick_terminology ⚡ | GLOSSARY.md | ✅ |
| **Architecture violation** | ARCHITECTURE.md | backend_code_style | - |
| **Need to start task** | priority_tasks ⚡ | tasks.md | ✅ |

---

## ⚡ Serena Memory Shortcuts

For **ultra-fast access**, these memories cache the most commonly needed information:

1. **`quick_terminology`** → Top terms from GLOSSARY.md
2. **`quick_troubleshooting`** → Top 10 errors from TROUBLESHOOTING.md  
3. **`priority_tasks`** → Current sprint priorities from tasks.md
4. **`project_global_context`** → Project overview essentials
5. **`project_documentation_index`** → This file (navigation)

**When to use memories vs full docs:**
- ✅ **Use memory** for: Quick lookups, common terms, frequent errors, current priorities
- ✅ **Read full doc** for: Detailed explanations, complete context, learning, unusual cases

---

## 📋 Key Architectural Principles (Quick Reference)

From `ARCHITECTURE.md` and `PROJECT_OVERVIEW.md`:

### Backend Hexagonal Architecture
- **Layers**: Domain (core) → Application (use cases) → Infrastructure (adapters)
- **Dependency Rule**: Domain has NO framework dependencies (pure Java)
- **Testing**: ArchUnit enforces architecture rules automatically
- **Package Structure**:
  ```
  domain/core/model        → Entities
  domain/port/in          → Use case interfaces
  domain/port/out         → Repository interfaces
  application/service     → Use case implementations
  infrastructure/adapter/ → REST, DB, file I/O
  ```

### Frontend Modern Angular
- **Components**: Standalone only (NO NgModules)
- **State**: Angular signals for reactivity
- **Routing**: Lazy-loaded routes
- **Forms**: Reactive forms with FormGroup binding (see T-001)

### Common Patterns
- Dependency injection throughout
- Interface-based design (ports)
- Repository pattern for data access
- Service layer for business logic
- DTOs for API contracts

---

## 🔄 Documentation Maintenance

**When code changes affect documentation:**
1. Update relevant `.md` files
2. Update ADRs if architectural decisions change
3. Update `tasks.md` if completing or adding tasks
4. Update `.serena/memories/*` if coding standards change
5. Update **`GLOSSARY.md`** if new domain terms introduced
6. Update **`TROUBLESHOOTING.md`** if new common errors discovered

**Keep memories in sync:**
- `quick_terminology` ← GLOSSARY.md
- `quick_troubleshooting` ← TROUBLESHOOTING.md
- `priority_tasks` ← tasks.md
- `project_documentation_index` ← Overall doc structure

---

## 📊 Documentation Quality Metrics

| Document | Completeness | Freshness | Priority |
|----------|--------------|-----------|----------|
| llms.txt | ✅ 100% | ✅ Up to date | ⭐⭐⭐⭐⭐ |
| PROJECT_OVERVIEW.md | ✅ 100% | ✅ Up to date | ⭐⭐⭐⭐⭐ |
| GLOSSARY.md | ✅ 100% | ✅ Up to date | ⭐⭐⭐⭐⭐ |
| TROUBLESHOOTING.md | ✅ 100% | ✅ Up to date | ⭐⭐⭐⭐⭐ |
| README.md | ✅ 100% | ✅ Up to date | ⭐⭐⭐⭐ |
| ARCHITECTURE.md | 🔄 80% | ✅ Current | ⭐⭐⭐⭐ |
| tasks.md | ✅ 100% | ✅ Oct 18, 2025 | ⭐⭐⭐⭐ |
| CONTRIBUTING.md | 🔄 70% | ✅ Current | ⭐⭐⭐ |
| audit_report.md | ✅ 100% | ⚠️ Oct 18, 2025 | ⭐⭐⭐ |

Legend:
- ✅ Complete/Current
- 🔄 Needs enhancement
- ⚠️ Snapshot from specific date

---

## 🎯 Important Notes

1. **llms.txt** is the entry point - Always current with doc structure
2. **PROJECT_OVERVIEW.md** is most comprehensive - Go-to for system understanding
3. **GLOSSARY.md** is authoritative - Single source of truth for terminology
4. **TROUBLESHOOTING.md** resolves 80% of errors - First stop when stuck
5. **tasks.md** is actionable - Implementation details for planned work
6. **ARCHITECTURE.md** has diagrams - Visual understanding of system
7. **Memories accelerate common lookups** - Use for speed, docs for depth

---

## 🚀 Quick Start Paths

### For New AI Agent (Serena)
1. Read `project_global_context` memory
2. Read `quick_terminology` memory
3. Skim `PROJECT_OVERVIEW.md` sections relevant to task
4. Consult `ARCHITECTURE.md` for patterns
5. Use `quick_troubleshooting` when errors occur

### For New Developer
1. Read `llms.txt` → Navigate to `PROJECT_OVERVIEW.md`
2. Quick start from `README.md`
3. Learn vocabulary from `GLOSSARY.md`
4. Set up workflow from `CONTRIBUTING.md`
5. Check `audit_report.md` for known issues

### For Specific Task
1. Check `priority_tasks` memory → Read task in `tasks.md`
2. Review `ARCHITECTURE.md` for patterns
3. Follow steps in task
4. Use `quick_troubleshooting` if blocked
5. Follow `CONTRIBUTING.md` for PR

---

> **Optimization Tip**: Use memories for 90% of common queries. Read full docs for the 10% edge cases, detailed learning, or when writing documentation.
