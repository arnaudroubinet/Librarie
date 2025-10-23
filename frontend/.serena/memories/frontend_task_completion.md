# Frontend: What to Do When a Task is Completed

## Before Committing
1. **Format code**: `npx prettier --write "src/**/*.{ts,html,scss,css}"`
2. **Run tests**: `npm test -- --watch=false --browsers=ChromeHeadless`
3. **Build application**: `npm run build`
4. **Check bundle size**: `npm run size`
5. **Verify no TypeScript errors**: Check VS Code problems panel

## Code Formatting
### Prettier
Prettier is used for code formatting:
```powershell
# Format all files
cd c:\dev\gitRepository\Librarie\frontend
npx prettier --write "src/**/*.{ts,html,scss,css}"

# Check formatting without changes
npx prettier --check "src/**/*.{ts,html,scss,css}"

# Format specific file
npx prettier --write src/app/components/book-list.component.ts
```

### Automatic Formatting
- Configure VS Code to format on save
- Install Prettier extension for VS Code
- Settings: `"editor.formatOnSave": true`

## Testing
### Unit Tests
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Run tests once (CI mode)
npm test -- --watch=false --browsers=ChromeHeadless

# Run tests in watch mode (development)
npm test

# Run tests with coverage
npm test -- --watch=false --browsers=ChromeHeadless --code-coverage

# Run specific test file
npm test -- --include='**/book-list.component.spec.ts' --watch=false
```

### E2E Tests (Playwright)
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Run Playwright tests
npx playwright test

# Run in UI mode
npx playwright test --ui

# Run specific test
npx playwright test tests/book-list.spec.ts
```

### Test Coverage
- Coverage report generated in: `coverage/frontend/`
- Open `coverage/frontend/index.html` in browser
- Aim for >80% coverage on new code

## Building
### Development Build
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Build with development configuration
npm run build -- --configuration=development

# Watch mode (rebuilds on changes)
npm run watch
```

### Production Build
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Build for production
npm run build

# Output in: dist/frontend/
```

### Build Verification
- Check `dist/frontend/` directory exists
- Verify bundle sizes are within limits
- Test production build locally if possible

## Bundle Size Monitoring
### Check Bundle Size
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Build and check size
npm run build
npm run size
```

### Analyze Bundle
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Generate stats and analyze
npm run analyze

# Manually:
npm run build:stats
# Upload dist/frontend/stats.json to https://esbuild.github.io/analyze/
```

### Bundle Size Limits
- **Main Bundle**: 500 kB (compressed)
- **Component Styles**: 6 kB warning, 10 kB error

### If Bundle Size Exceeds Limits
1. **Identify large dependencies**: Use bundle analyzer
2. **Lazy load features**: Move to lazy-loaded routes
3. **Tree-shake unused code**: Remove unused imports
4. **Optimize images**: Compress and use appropriate formats
5. **Use dynamic imports**: For large libraries

## Type Checking
### TypeScript Compilation
```powershell
cd c:\dev\gitRepository\Librarie\frontend

# Check for TypeScript errors
npx tsc --noEmit

# Watch mode
npx tsc --noEmit --watch
```

### VS Code Integration
- Check "Problems" panel for TypeScript errors
- Fix all errors before committing
- Address warnings when reasonable

## Code Quality Checklist
Before committing, verify:
- [ ] All tests pass (`npm test`)
- [ ] Code is formatted (`npx prettier --check`)
- [ ] Application builds (`npm run build`)
- [ ] Bundle size within limits (`npm run size`)
- [ ] No TypeScript errors (`npx tsc --noEmit`)
- [ ] No console.log or debug statements
- [ ] Comments are meaningful and up-to-date
- [ ] New components have tests
- [ ] Accessibility considerations addressed

## Documentation Updates
### When to Update Documentation
- **New components**: Document purpose and usage
- **API changes**: Update service documentation
- **New features**: Update README.md
- **Breaking changes**: Document migration path

### TSDoc
- Add TSDoc comments to public methods
- Document parameters and return types
- Explain complex logic

## Performance Verification
### If You Added Resource-Intensive Features
- **Test with large datasets**: Verify performance with many items
- **Check memory usage**: Use browser DevTools profiler
- **Measure load time**: Use Lighthouse or similar tools
- **Virtual scrolling**: Consider for long lists

### Performance Checklist
- [ ] `OnPush` change detection where appropriate
- [ ] TrackBy functions for `@for` loops
- [ ] Lazy loading for routes
- [ ] Proper unsubscription from observables
- [ ] Optimized images and assets

## Accessibility Verification
### Accessibility Checklist
- [ ] Semantic HTML elements used
- [ ] ARIA labels where needed
- [ ] Keyboard navigation works
- [ ] Focus management for modals
- [ ] Color contrast meets WCAG standards
- [ ] Screen reader compatible

### Testing Accessibility
```powershell
# Use browser DevTools Lighthouse
# Check "Accessibility" score
# Address any issues found
```

## Security Considerations
### Security Checklist
- [ ] User inputs are sanitized (backend handles this)
- [ ] XSS protection in templates
- [ ] No sensitive data in console logs
- [ ] Secure communication (HTTPS)
- [ ] Dependencies are up-to-date

## Git Commit
### Commit Message Format
Follow conventional commits:
```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### Examples
```
feat(books): add advanced search with filters
fix(reader): correct page navigation in EPUB reader
refactor(services): simplify book service API calls
test(books): add unit tests for book list component
style(global): format code with Prettier
docs(readme): update installation instructions
```

### Commit Checklist
- [ ] All code quality checks pass
- [ ] Commit message follows conventions
- [ ] Changes are focused and atomic
- [ ] No large files committed
- [ ] No sensitive data in commits

## Commands Summary
```powershell
# Windows PowerShell commands for frontend

# Format code
cd c:\dev\gitRepository\Librarie\frontend
npx prettier --write "src/**/*.{ts,html,scss,css}"

# Run tests
npm test -- --watch=false --browsers=ChromeHeadless

# Build application
npm run build

# Check bundle size
npm run size

# Analyze bundle
npm run analyze

# Type checking
npx tsc --noEmit

# Start dev server
npm start
```

## CI/CD Considerations
The project uses GitHub Actions for CI/CD:
1. **Local verification**: Run all checks before pushing
2. **Bundle size check**: CI monitors bundle sizes
3. **Test results**: Uploaded as artifacts
4. **Build artifacts**: Available for 7 days

### CI Workflow
When you push:
1. **Code formatting check**: Prettier validation
2. **Tests**: Unit tests with coverage
3. **Build**: Production build
4. **Bundle size**: Compared with main branch
5. **Comment on PR**: Bundle size results

## Troubleshooting
### Common Issues
1. **Tests fail**: Check for breaking changes, mock dependencies
2. **Build fails**: Check for TypeScript errors, missing imports
3. **Bundle too large**: Analyze bundle, remove unused code
4. **Formatting fails**: Run Prettier manually, check config

### Getting Help
- Check Angular documentation: https://angular.dev
- Check Angular Material docs: https://material.angular.io
- Check Readium docs: https://github.com/readium/
- Review existing components for patterns

## Post-Commit
After committing:
1. **Push to remote**: `git push origin <branch-name>`
2. **Monitor CI**: Check GitHub Actions for build status
3. **Review PR checks**: Ensure all checks pass
4. **Address feedback**: Respond to code review comments

## Production Deployment
Before deploying to production:
1. **Full test suite**: All tests must pass
2. **Production build**: Verify build succeeds
3. **Bundle size**: Must be within limits
4. **Accessibility**: Lighthouse score >90
5. **Performance**: Load time <3 seconds
6. **Browser testing**: Test in Chrome, Firefox, Safari, Edge
