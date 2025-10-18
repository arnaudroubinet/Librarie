# Audit Artifacts

This directory contains artifacts from the application audit conducted on October 18, 2025.

## Directory Structure

```
artifacts/
└── 2025-10-18/
    ├── screenshots/     # UI screenshots from Playwright exploration
    ├── logs/           # Application logs (gitignored)
    └── traces/         # Playwright traces (gitignored)
```

## Screenshots

### 01-homepage-initial.png
Initial homepage load showing the MotsPassants library interface with navigation sidebar and empty main content area.

**Key Observations:**
- Clean Material Design interface
- Navigation structure visible (Books, Series, Authors, Search, Settings)
- Version 1.0.0 displayed
- No console errors on initial load

---

### 02-books-library.png
Books library page displaying the book collection grid with covers and metadata.

**Key Observations:**
- Demo data successfully loaded (188 books visible)
- Book covers loading correctly
- Sorting dropdown working (Recently Updated)
- Grid layout responsive
- Bookmark and share buttons present

---

### 03-book-detail.png
Individual book detail page showing "Inheritance" by Christopher Paolini.

**Key Observations:**
- Book metadata displayed: publication date, language, binding, pages
- Cover image loaded successfully
- "Read Book" and "Download" buttons functional
- File size information (2.07 MB)
- Added/Updated timestamps shown

---

### 04-authors-list.png
Authors library page with alphabetically grouped author cards.

**Key Observations:**
- Alphabetical grouping working (A, C, D, E, G, H, I sections visible)
- Author metadata displayed (birth/death dates, bio snippets)
- **ISSUE**: Multiple HTTP 406 errors for author pictures in console
- Placeholder icons shown when pictures fail to load
- 26 total authors in demo data

---

### 05-search-page-error.png
Search page showing the form interface with visible console errors.

**Key Observations:**
- Quick search input field visible
- Advanced search panel present
- **CRITICAL ISSUE**: Angular NG01050 errors in console
  - `formControlName must be used with a parent formGroup directive`
  - Affects: title, authors, series form controls
- Search interface renders but form binding is broken

---

### 06-settings-page.png
Settings page displaying system information, health status, and library statistics.

**Key Observations:**
- Backend version: 1.0.0-SNAPSHOT
- Frontend version: 1.0.0
- Health checks: Liveness and Readiness both OK
- OIDC Provider: UP
- Database: UP
- Library statistics: 188 books, 51 series, 26 authors
- 9 supported formats displayed
- **MINOR ISSUE**: NG01050 error also present on this page

---

## How to Use These Artifacts

### For Bug Reports
Reference specific screenshots when reporting issues:
- Search functionality bug → `05-search-page-error.png`
- Author picture loading → `04-authors-list.png`

### For Documentation
These screenshots can be used in:
- User guides and tutorials
- Architecture documentation
- Development setup guides
- Issue tracking and bug fixes

### For Comparison
After fixes are implemented, capture new screenshots to show before/after:
```bash
# Example: After fixing search FormGroup issue
playwright screenshot http://localhost:4200/search --full-page artifacts/2025-10-18/screenshots/05-search-page-FIXED.png
```

## Screenshot Metadata

| Screenshot | Resolution | File Size | Capture Tool | Date |
|------------|------------|-----------|--------------|------|
| 01-homepage-initial.png | Full page | 150 KB | Playwright | 2025-10-18 |
| 02-books-library.png | Full page | 150 KB | Playwright | 2025-10-18 |
| 03-book-detail.png | Full page | 137 KB | Playwright | 2025-10-18 |
| 04-authors-list.png | Full page | 33 KB | Playwright | 2025-10-18 |
| 05-search-page-error.png | Full page | 31 KB | Playwright | 2025-10-18 |
| 06-settings-page.png | Full page | 82 KB | Playwright | 2025-10-18 |

## Related Documentation

- **Audit Report**: `../audit_report.md` - Executive summary and findings
- **Questions**: `../questions.md` - Detailed analysis of each issue
- **Tasks**: `../tasks.md` - Actionable improvement tasks

## Notes

- All screenshots were captured using Playwright browser automation
- Application running in development mode (backend on :8080, frontend on :4200)
- Demo data was active during capture (188 books, 26 authors, 51 series)
- No authentication was required (dev mode with OIDC dev services)
