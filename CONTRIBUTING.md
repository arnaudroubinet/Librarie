# Contributing to Librarie

Thank you for your interest in contributing to Librarie! This document provides guidelines and information for contributors.

## Table of Contents

- [Development Setup](#development-setup)
- [Code Quality](#code-quality)
- [Bundle Size Monitoring](#bundle-size-monitoring)
- [Pull Request Process](#pull-request-process)

## Development Setup

### Prerequisites

- Node.js 20.x or later
- npm (comes with Node.js)

### Frontend Setup

1. Clone the repository
2. Navigate to the frontend directory: `cd frontend`
3. Install dependencies: `npm ci`
4. Start the development server: `npm start`
5. Open your browser to `http://localhost:4200`

### Backend Setup

Please refer to the backend-specific documentation in the `backend` directory.

## Code Quality

### Testing

Run tests before submitting your pull request:

```bash
cd frontend
npm test
```

### Code Formatting

We use Prettier for code formatting. The CI pipeline will check your code formatting.

```bash
cd frontend
npx prettier --check "src/**/*.{ts,html,scss,css}"
```

To automatically fix formatting issues:

```bash
npx prettier --write "src/**/*.{ts,html,scss,css}"
```

## Bundle Size Monitoring

We monitor bundle sizes to ensure the application remains performant and fast to load.

### Checking Bundle Size Locally

Before submitting a pull request, you can check the bundle size:

```bash
cd frontend
npm run build
npm run size
```

The `size-limit` tool will report:
- Current size of each bundle
- Whether the size exceeds configured limits
- Loading time estimates on slow networks
- Running time estimates on slower devices

### Bundle Size Limits

Current limit is configured in `frontend/package.json` under the `size-limit` section:

- **Application Bundle**: 500 kB (compressed size of all files loaded on initial page load)

This limit includes the main application code, polyfills, Angular Material components, and all other code that loads when the application first opens. Lazy-loaded routes and components are not included in this measurement.

### What Happens in CI

When you submit a pull request affecting the frontend:

1. The GitHub Actions workflow builds your branch
2. Bundle sizes are checked against configured limits
3. Sizes are compared with the main branch
4. A comment is posted to your PR with the results
5. **The PR build will fail if any bundle exceeds its limit**

### If Your Bundle Exceeds the Limit

If the bundle size check fails, you have several options:

1. **Optimize your code** (preferred):
   - Remove unnecessary imports
   - Use lazy loading for routes and components
   - Tree-shake unused code
   - Optimize dependencies

2. **Analyze the bundle** to identify large dependencies:
   ```bash
   cd frontend
   npm run build
   npm run analyze
   ```
   This will open an interactive visualization of your bundle composition.

3. **Update size limits** (only if justified):
   - If the size increase is necessary and justified
   - Update the limits in `frontend/package.json`
   - Document the reason in your PR description

### Bundle Analysis

To analyze what's in your bundle:

```bash
cd frontend
npm run analyze
```

This will:
1. Build your application with stats generation
2. Create a `dist/frontend/stats.json` file
3. Display a link to the esbuild analyzer

Open https://esbuild.github.io/analyze/ in your browser and upload the generated `dist/frontend/stats.json` file to see:
- Size of each module
- What's taking up space in your bundles
- Dependencies and their sizes
- Opportunities for optimization

## Pull Request Process

1. Fork the repository
2. Create a feature branch from `main`
3. Make your changes
4. Run tests and bundle size checks locally
5. Commit your changes with clear, descriptive messages
6. Push to your fork
7. Submit a pull request to the main repository

### PR Requirements

- All tests must pass
- Code must be properly formatted
- Bundle sizes must not exceed limits (or have documented justification)
- Changes should be described clearly in the PR description

### Review Process

- Maintainers will review your PR
- Address any feedback or requested changes
- Once approved, a maintainer will merge your PR

## Questions?

If you have questions or need help, please:
- Open an issue on GitHub
- Reach out to the maintainers

Thank you for contributing to Librarie!
