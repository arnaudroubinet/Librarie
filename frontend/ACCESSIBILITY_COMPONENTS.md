# Component Accessibility Improvement Guide

This guide provides specific instructions for improving accessibility in each component.

## Common Patterns to Apply

### 1. Iconify Icons
```typescript
// Decorative icons (within labeled elements)
<iconify-icon icon="..." role="img" aria-hidden="true"></iconify-icon>

// Standalone functional icons
<iconify-icon icon="..." role="img" aria-label="Descriptive label"></iconify-icon>
```

### 2. Loading States
```typescript
<div class="loading-section" role="status" aria-live="polite" aria-label="Loading content">
  <mat-spinner aria-label="Loading"></mat-spinner>
  <p>Loading message...</p>
</div>
```

### 3. Error Messages
```typescript
<div class="error-container" role="alert">
  <p>Error message here</p>
</div>
```

### 4. Interactive Lists
```typescript
<div class="items-grid" role="list" aria-label="Items">
  <div class="item-card" role="listitem" [attr.aria-label]="'View ' + item.name">
    ...
  </div>
</div>
```

### 5. Buttons
```typescript
// Icon-only buttons
<button mat-icon-button aria-label="Descriptive action">
  <iconify-icon icon="..." role="img" aria-hidden="true"></iconify-icon>
</button>

// Buttons with text and icon
<button mat-raised-button>
  <iconify-icon icon="..." role="img" aria-hidden="true"></iconify-icon>
  Button Text
</button>
```

### 6. Form Inputs
```typescript
// Using mat-form-field (automatic label association)
<mat-form-field>
  <mat-label>Field Label</mat-label>
  <input matInput />
</mat-form-field>

// Standalone inputs
<input type="text" aria-label="Descriptive label" />
```

### 7. Dynamic Content
```typescript
<div aria-live="polite" aria-atomic="true">
  {{ dynamicMessage }}
</div>
```

## Component-Specific Improvements

### book-list.component.ts
- [x] Add aria-label to sort dropdown
- [x] Add role="status" to loading section
- [x] Add role="alert" to error messages
- [x] Add role="list" to books grid
- [ ] Add role="listitem" to book cards
- [ ] Add aria-label to book cards with book title
- [ ] Add aria-label to action buttons (bookmark, share)
- [ ] Improve alt text for book cover images

### search.component.ts
- [ ] Add aria-label to search input
- [ ] Add aria-live region for search results count
- [ ] Add aria-label to filter buttons
- [ ] Add role="status" to loading states
- [ ] Add aria-label to clear search button
- [ ] Improve keyboard navigation for search suggestions

### book-detail.component.ts
- [ ] Add aria-label to back button
- [ ] Add aria-label to action buttons (read, download, etc.)
- [ ] Add alt text for book cover
- [ ] Add aria-label to tab controls if using tabs
- [ ] Ensure metadata is properly structured for screen readers

### series-list.component.ts
- [ ] Add role="list" to series grid
- [ ] Add role="listitem" to series cards
- [ ] Add aria-label to series cards
- [ ] Add aria-label to action buttons

### author-list.component.ts
- [ ] Add role="list" to authors grid
- [ ] Add role="listitem" to author cards
- [ ] Add aria-label to author cards
- [ ] Add alt text for author images if present

### settings.component.ts
- [ ] Ensure all form inputs have labels
- [ ] Add aria-describedby for help text
- [ ] Add role="alert" for validation messages
- [ ] Add aria-label to toggle switches
- [ ] Group related settings with fieldset/legend

### ebook-reader.component.ts
- [ ] Add aria-label to reader controls
- [ ] Add keyboard shortcuts
- [ ] Add aria-label to page navigation
- [ ] Add aria-live for page number announcements
- [ ] Ensure zoom controls are labeled
- [ ] Add skip links for navigation within book

## Testing Each Component

After making accessibility improvements to a component, test with:

1. **Keyboard Navigation**
   - Tab through all interactive elements
   - Ensure focus is visible
   - Test Enter and Space keys on buttons

2. **Screen Reader** (use browser dev tools or actual screen reader)
   - Listen to announced text
   - Verify labels make sense
   - Check landmarks are announced

3. **Automated Testing**
   ```bash
   npm run test:a11y
   ```

4. **Visual Inspection**
   - Check color contrast
   - Verify focus indicators
   - Test at 200% zoom

## Priority Order

1. **Critical (Must Fix)**
   - All interactive elements have accessible names
   - All images have alt text
   - All form inputs have labels
   - Loading states announce to screen readers

2. **High (Should Fix)**
   - Dynamic content has live regions
   - Error messages use role="alert"
   - Lists have proper structure
   - Keyboard shortcuts are documented

3. **Medium (Good to Have)**
   - Help text uses aria-describedby
   - Complex controls have aria-controls
   - Tooltips are accessible
   - Focus management in modals

4. **Low (Enhancement)**
   - Motion reduced for users who prefer it
   - High contrast mode support
   - Voice control optimization
   - Multilingual support

## Code Review Checklist

When reviewing accessibility changes, check:

- [ ] All new interactive elements have labels
- [ ] Icons are either hidden or have labels
- [ ] Loading states announce changes
- [ ] Error messages are announced immediately
- [ ] Keyboard navigation works
- [ ] Focus indicators are visible
- [ ] Color contrast meets WCAG AA
- [ ] Alt text is descriptive
- [ ] ARIA attributes are used correctly
- [ ] Automated tests pass

## Resources

- [ARIA Authoring Practices Guide](https://www.w3.org/WAI/ARIA/apg/)
- [WebAIM WCAG 2 Checklist](https://webaim.org/standards/wcag/checklist)
- [Angular Accessibility Guide](https://angular.dev/best-practices/a11y)
- [Material Design Accessibility](https://m2.material.io/design/usability/accessibility.html)
