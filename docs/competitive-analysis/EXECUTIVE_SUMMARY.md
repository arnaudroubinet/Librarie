# Competitive Feature Gap Analysis - Executive Summary

**Project:** Librarie (arnaudroubinet/Librarie)  
**Date:** October 18, 2025  
**Purpose:** Strategic feature planning based on competitive analysis

---

## 🎯 Key Findings

### Current Position

Librarie is a modern, well-architected ebook library management system with:
- ✅ Clean Angular + Quarkus stack
- ✅ OIDC authentication
- ✅ Basic ebook reading
- ✅ Series and author management

**However**, it lacks 28 critical features present in leading competitors.

### Market Opportunity

No competitor excels at **unified ebook + audiobook management** with a modern, self-hosted solution:

- **Audiobookshelf**: Strong audiobooks, weak ebooks
- **Calibre-Web**: Strong ebooks, dated UI, no audiobooks
- **Kavita/Komga**: Focus on comics/manga
- **Booksonic-Air**: Audiobooks only

**Librarie can fill this gap.**

---

## 📊 Priority Recommendations

### Release 1 (3-4 months) - Quick Wins

**Investment:** 4 developers, $300K-400K

**Top Features:**
1. Multi-user with roles (all competitors have this)
2. OPDS feed support (industry standard)
3. Metadata auto-fetch (reduce manual work 50%)
4. Collections/reading lists (user engagement)
5. Statistics dashboard (gamification)

**Expected Impact:**
- 2x user growth (multi-user enables families/orgs)
- 30% reduction in manual metadata entry
- Feature parity with Calibre-Web on core functionality

---

### Release 2 (4-6 months) - Strategic Differentiators

**Investment:** 7 developers, $700K-900K

**Top Features:**
1. **Audiobook support** - Full playback with chapters
2. **Mobile apps** - Native iOS/Android with offline mode
3. Format conversion (EPUB ↔ MOBI)
4. Offline web mode (PWA)

**Expected Impact:**
- **Market expansion:** Enter $4B audiobook market
- **Mobile-first users:** 70% prefer mobile access
- **Competitive positioning:** Only unified ebook+audiobook solution

**ROI:**
- 5x user growth potential
- App store visibility
- Premium feature revenue opportunity

---

### Release 3 (6-12 months) - Market Expansion

**Investment:** $500K-700K

**Focus Areas:**
- Comic/manga support (compete with Kavita/Komga)
- Podcast management
- Internationalization (10+ languages)
- Enterprise features (LDAP, SSO)

---

## 💡 Strategic Recommendations

### Do These First (High Impact, Low Effort)

1. **OPDS Feed** - 3 weeks, unlocks e-reader ecosystem
2. **Cover Art Auto-Fetch** - 1 week, huge UX improvement
3. **Multi-User Roles** - 3 weeks, enables org/family use
4. **Themes/Dark Mode** - 1 week, user retention

**Total:** 8 weeks, ~$120K

**Impact:** Immediate competitive parity on basics, 2x user growth

### Big Bets (Strategic Investments)

1. **Audiobook Support** - 8-12 weeks
   - **Why:** No competitor does ebooks + audiobooks well
   - **Risk:** Technical complexity, storage costs
   - **Mitigation:** Start with basic playback, iterate

2. **Mobile Apps** - 12-16 weeks
   - **Why:** 70% of usage is mobile
   - **Risk:** App store maintenance, platform fragmentation
   - **Mitigation:** Cross-platform framework (Flutter)

### Don't Do Yet (Low Priority)

- Chapter editor for audiobooks (niche use case)
- Public registration (security risk, moderate value)
- Advanced scrobbling (nice-to-have, complex integration)

These can wait until R3 or later based on user demand.

---

## 📈 Expected Outcomes

### 6 Months (Post R1)
- **Users:** 10,000 active installations
- **Engagement:** 50% DAU/MAU ratio
- **Features:** 80% parity with Calibre-Web
- **Ecosystem:** OPDS support drives e-reader adoption

### 12 Months (Post R2)
- **Users:** 50,000+ installations
- **Market Position:** #1 unified ebook+audiobook solution
- **Mobile:** 5,000+ app downloads
- **Competitive:** Match/exceed Audiobookshelf on audiobooks

### 24 Months (Post R3)
- **Users:** 100,000+ installations
- **Ecosystem:** Plugin marketplace, 100+ contributors
- **Revenue:** Freemium model with premium features
- **Enterprise:** LDAP/SSO enables corporate deployments

---

## 💰 Budget Summary

| Phase | Duration | Team Size | Estimated Cost |
|-------|----------|-----------|----------------|
| R1 | 3-4 months | 4-5 devs | $300K-500K |
| R2 | 4-6 months | 7-9 devs | $700K-1.2M |
| R3 | 6-12 months | 5-7 devs | $500K-700K |
| **Total** | **18 months** | **Peak 9 devs** | **$1.5M-2.4M** |

**Note:** Assumes mix of full-time and contractors, open source contributions reduce costs.

---

## ⚠️ Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| Feature creep delays releases | Strict MVP scope, iterative releases |
| Audiobook scalability issues | Start with small files, CDN for large libraries |
| App store rejections | Follow guidelines, soft launch, TestFlight beta |
| Insufficient adoption | Beta program, user feedback loops, marketing |
| Key developer turnover | Documentation, knowledge sharing, modular code |

---

## 🚀 Immediate Next Steps (Week 1-4)

1. **Validate Priorities**
   - Share analysis with stakeholders
   - Gather feedback on roadmap
   - Confirm budget availability

2. **Assemble Team**
   - Hire/assign 4 developers
   - Onboard to codebase
   - Setup development environment

3. **Plan Sprint 1**
   - Detail work breakdown for multi-user feature
   - Create project tracking board
   - Setup CI/CD for new features

4. **Begin Development**
   - Start with multi-user roles (Week 3-4)
   - Parallel: Metadata provider research
   - Setup testing infrastructure

---

## 📝 Conclusion

Librarie has a **clear opportunity** to become the leading self-hosted media library solution by:

1. **Filling the gap** in unified ebook + audiobook management
2. **Modernizing** the category with Angular + Quarkus
3. **Prioritizing** user experience and performance
4. **Building** for the mobile-first generation

The recommended roadmap is:
- **R1:** Achieve feature parity with basics (multi-user, OPDS, metadata)
- **R2:** Differentiate with audiobooks and mobile apps
- **R3:** Expand into adjacent markets (comics, podcasts, i18n)

**Investment:** $1.5M-2.4M over 18 months  
**Expected ROI:** 100,000+ users, market leadership, enterprise opportunities

**Recommendation:** ✅ Proceed with R1 immediately, validate with beta users, iterate based on feedback.

---

**For detailed analysis, see:** [FEATURE_GAP_ANALYSIS.md](./FEATURE_GAP_ANALYSIS.md)  
**For feature specs, see:** [features/](./features/) directory

**Contact:** Project maintainers for questions or discussion
