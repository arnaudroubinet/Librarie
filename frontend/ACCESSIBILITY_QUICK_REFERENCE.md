# Accessibility Quick Reference Card

Quick reference for common accessibility patterns in Angular/TypeScript.

## 🚀 Quick Checklist

### Before Committing Code
- [ ] All images have alt text
- [ ] All buttons have accessible names
- [ ] All form inputs have labels
- [ ] Icons are hidden or labeled
- [ ] Loading states announce
- [ ] Errors announce immediately
- [ ] Keyboard navigation works
- [ ] Focus indicators visible

## 📝 Common Patterns

### Images
```html
<!-- Book cover -->
<img [src]="coverUrl" 
     [alt]="book.title + ' cover'" 
     loading="lazy">

<!-- Decorative image -->
<img src="decoration.png" alt="" role="presentation">
```

### Buttons
```html
<!-- Icon only -->
<button mat-icon-button aria-label="Refresh books">
  <mat-icon aria-hidden="true">refresh</mat-icon>
</button>

<!-- Icon + text -->
<button mat-raised-button>
  <mat-icon aria-hidden="true">add</mat-icon>
  Add Book
</button>
```

### Icons (iconify-icon)
```html
<!-- Decorative (inside labeled element) -->
<iconify-icon icon="mdi:book" 
              role="img" 
              aria-hidden="true"></iconify-icon>

<!-- Standalone functional -->
<iconify-icon icon="mdi:close" 
              role="img" 
              aria-label="Close dialog"></iconify-icon>
```

### Loading States
```html
<div role="status" 
     aria-live="polite" 
     aria-label="Loading content">
  <mat-spinner aria-label="Loading"></mat-spinner>
  <p>Loading books...</p>
</div>
```

### Error Messages
```html
<div role="alert" class="error-message">
  <p>{{ errorMessage }}</p>
</div>
```

### Lists
```html
<div role="list" aria-label="Books">
  <div role="listitem" 
       [attr.aria-label]="'View ' + book.title">
    <!-- Card content -->
  </div>
</div>
```

### Forms
```html
<!-- Using Material -->
<mat-form-field>
  <mat-label>Book Title</mat-label>
  <input matInput>
</mat-form-field>

<!-- Standalone -->
<label for="author">Author</label>
<input id="author" type="text">

<!-- Without visible label -->
<input type="search" 
       aria-label="Search books" 
       placeholder="Search...">
```

### Navigation
```html
<nav role="navigation" aria-label="Main navigation">
  <a routerLink="/books" 
     aria-label="View all books">
    Books
  </a>
</nav>
```

### Landmarks
```html
<header role="banner">
  <!-- Site header -->
</header>

<nav role="navigation" aria-label="Main navigation">
  <!-- Navigation -->
</nav>

<main id="main-content" role="main" tabindex="-1">
  <!-- Main content -->
</main>

<footer role="contentinfo">
  <!-- Site footer -->
</footer>
```

### Dynamic Content
```html
<!-- Polite announcement (non-urgent) -->
<div aria-live="polite" aria-atomic="true">
  {{ searchResultCount }} results found
</div>

<!-- Assertive announcement (urgent) -->
<div aria-live="assertive" aria-atomic="true">
  {{ errorMessage }}
</div>
```

## 🎯 ARIA Attributes Quick Guide

### Common ARIA Attributes
- `aria-label`: Provides label when no visible text
- `aria-labelledby`: References element containing label
- `aria-describedby`: References element with description
- `aria-hidden`: Hides from screen readers
- `aria-live`: Announces dynamic changes (polite/assertive)
- `aria-expanded`: Indicates expand/collapse state
- `aria-pressed`: Indicates toggle button state
- `aria-current`: Marks current item in nav/list

### When to Use What
```typescript
// Element has no visible text
<button aria-label="Close">×</button>

// Element has visible text elsewhere
<input aria-labelledby="search-label">
<label id="search-label">Search</label>

// Element needs additional description
<input aria-describedby="password-hint">
<span id="password-hint">Must be 8+ characters</span>

// Hide decorative content
<span aria-hidden="true">★★★★★</span>

// Announce changes
<div aria-live="polite">{{ status }}</div>
```

## ⌨️ Keyboard Support

### Standard Controls
- **Links**: `Enter` activates
- **Buttons**: `Enter` or `Space` activates
- **Checkboxes**: `Space` toggles
- **Radio buttons**: `Arrow keys` to navigate group, `Space` selects
- **Dropdowns**: `Space/Enter` opens, `Arrow keys` navigate, `Enter` selects

### Custom Controls
```typescript
// Add keyboard support
@HostListener('keydown', ['$event'])
handleKeyboard(event: KeyboardEvent) {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault();
    this.activate();
  }
}
```

## 🎨 Color & Contrast

### Contrast Ratios (WCAG AA)
- Normal text: **4.5:1** minimum
- Large text (18pt+): **3:1** minimum
- UI components: **3:1** minimum
- Focus indicators: **3:1** minimum

### Testing
```bash
# Chrome DevTools
1. Open DevTools
2. Inspect element
3. Check "Accessibility" pane
4. View "Contrast" section

# axe DevTools Extension
1. Install extension
2. Click extension icon
3. Run "Scan All Page"
4. Review contrast issues
```

## 🧪 Testing Commands

```bash
# Run accessibility tests
npm run test:a11y

# Run with visible browser
npm run test:a11y:headed

# View test report
npm run test:a11y:report
```

## 🔍 Common Mistakes to Avoid

❌ **Don't**
```html
<!-- Missing alt -->
<img src="book.jpg">

<!-- Icon without label -->
<button><mat-icon>edit</mat-icon></button>

<!-- Input without label -->
<input type="text" placeholder="Name">

<!-- No announcement -->
<div *ngIf="loading">Loading...</div>

<!-- Non-semantic list -->
<div class="items">
  <div class="item">...</div>
</div>
```

✅ **Do**
```html
<!-- Proper alt -->
<img src="book.jpg" alt="Book title cover">

<!-- Icon with label -->
<button aria-label="Edit book">
  <mat-icon aria-hidden="true">edit</mat-icon>
</button>

<!-- Input with label -->
<mat-form-field>
  <mat-label>Name</mat-label>
  <input matInput>
</mat-form-field>

<!-- Announced loading -->
<div *ngIf="loading" 
     role="status" 
     aria-live="polite">
  Loading...
</div>

<!-- Semantic list -->
<div role="list">
  <div role="listitem">...</div>
</div>
```

## 📚 Resources

### Documentation
- [ACCESSIBILITY.md](./ACCESSIBILITY.md) - Full guidelines
- [ACCESSIBILITY_COMPONENTS.md](./ACCESSIBILITY_COMPONENTS.md) - Component patterns
- [KEYBOARD_NAVIGATION.md](./KEYBOARD_NAVIGATION.md) - Keyboard shortcuts

### External Resources
- [WCAG Quick Reference](https://www.w3.org/WAI/WCAG21/quickref/)
- [ARIA Authoring Practices](https://www.w3.org/WAI/ARIA/apg/)
- [MDN Accessibility](https://developer.mozilla.org/en-US/docs/Web/Accessibility)
- [WebAIM Articles](https://webaim.org/articles/)

### Tools
- [axe DevTools](https://www.deque.com/axe/devtools/)
- [WAVE Extension](https://wave.webaim.org/extension/)
- [Chrome Lighthouse](https://developers.google.com/web/tools/lighthouse)
- [Color Contrast Checker](https://webaim.org/resources/contrastchecker/)

## 💡 Pro Tips

1. **Test early, test often** - Don't wait until the end
2. **Use semantic HTML** - It's accessible by default
3. **Think keyboard-first** - If keyboard works, everything works
4. **Test with real users** - Automated tests catch ~30% of issues
5. **Document patterns** - Make it easy for team to follow
6. **Learn gradually** - Start with basics, improve over time

---

**Keep this card handy!** Bookmark it, print it, or pin it to your IDE.
