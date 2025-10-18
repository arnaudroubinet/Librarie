# Branch Protection Configuration

This document describes how to configure branch protection rules for the Librarie repository to enforce CI/CD checks before merging pull requests.

## Overview

Branch protection rules ensure code quality by requiring all status checks to pass before pull requests can be merged. This prevents broken code from being merged into main branches.

## Required Status Checks

The following status checks should be configured as **required** before merging to `main` or `develop` branches:

### From `ci.yml` Workflow
- ✅ **Backend - Build & Test** - Ensures backend builds successfully and all tests pass
- ✅ **Frontend - Build & Test** - Ensures frontend builds successfully and all tests pass
- ✅ **Security - Dependency Scan** - Ensures no critical/high vulnerabilities in dependencies
- ✅ **CI Status Check** - Final gate ensuring all jobs completed successfully

### From `codeql.yml` Workflow
- ✅ **Analyze Code (java)** - CodeQL security analysis for Java backend
- ✅ **Analyze Code (javascript)** - CodeQL security analysis for JavaScript/TypeScript frontend

## Configuration Steps

### 1. Navigate to Repository Settings

1. Go to your repository: `https://github.com/arnaudroubinet/Librarie`
2. Click **Settings** tab
3. Click **Branches** in the left sidebar

### 2. Add Branch Protection Rule for `main`

1. Click **Add branch protection rule**
2. Set **Branch name pattern** to: `main`

#### Configure the following settings:

##### Protect matching branches
- [x] **Require a pull request before merging**
  - [x] Require approvals: `1` (at least one approval)
  - [x] Dismiss stale pull request approvals when new commits are pushed
  - [x] Require review from Code Owners (optional, if CODEOWNERS file exists)

- [x] **Require status checks to pass before merging**
  - [x] Require branches to be up to date before merging
  - **Status checks that are required:** (search and add each)
    - `Backend - Build & Test`
    - `Frontend - Build & Test`
    - `Security - Dependency Scan`
    - `CI Status Check`
    - `Analyze Code (java)`
    - `Analyze Code (javascript)`

- [x] **Require conversation resolution before merging**

- [x] **Require signed commits** (recommended for enhanced security)

- [x] **Include administrators** (enforce rules for repository admins)

- [x] **Restrict who can push to matching branches** (optional)
  - Add specific users or teams who can directly push

##### Rules applied to everyone including administrators
- [x] **Allow force pushes**: `Specify who can force push` → `nobody`
- [x] **Allow deletions**: `❌ Disabled`

3. Click **Create** or **Save changes**

### 3. Add Branch Protection Rule for `develop` (if used)

Repeat the same process for the `develop` branch with similar settings.

### 4. Configure Workflow Permissions

1. Go to **Settings** → **Actions** → **General**
2. Under **Workflow permissions**, select:
   - ✅ **Read and write permissions** (needed for CodeQL to upload results)
   - [x] **Allow GitHub Actions to create and approve pull requests**

### 5. Enable Security Features

1. Go to **Settings** → **Code security and analysis**
2. Enable the following:
   - [x] **Dependency graph**
   - [x] **Dependabot alerts**
   - [x] **Dependabot security updates**
   - [x] **Code scanning** (if using GitHub Advanced Security)
   - [x] **Secret scanning** (if using GitHub Advanced Security)

## Testing Branch Protection

### Method 1: Create a Draft PR

1. Create a new branch:
   ```bash
   git checkout -b test/branch-protection
   ```

2. Make a small change (e.g., update README)

3. Push and create a draft PR:
   ```bash
   git push origin test/branch-protection
   ```

4. On GitHub:
   - Create a **Draft Pull Request** targeting `main`
   - Verify all CI checks are triggered
   - Wait for all checks to complete
   - Verify you cannot merge until all checks pass
   - Mark as "Ready for review" when all checks pass
   - Verify merge button becomes available after approval

### Method 2: Test with Failing Checks

1. Create a branch with a failing test
2. Push and create PR
3. Verify merge is blocked while tests fail
4. Fix the tests
5. Push fix
6. Verify merge becomes available when tests pass

## Status Check Badges

You can add status badges to your README.md to show CI status:

```markdown
## Build Status

[![CI Pipeline](https://github.com/arnaudroubinet/Librarie/actions/workflows/ci.yml/badge.svg)](https://github.com/arnaudroubinet/Librarie/actions/workflows/ci.yml)
[![CodeQL](https://github.com/arnaudroubinet/Librarie/actions/workflows/codeql.yml/badge.svg)](https://github.com/arnaudroubinet/Librarie/actions/workflows/codeql.yml)
```

## Troubleshooting

### Status checks not appearing
- Ensure workflows have run at least once on the branch
- Check that workflow files are in the default branch (main)
- Verify workflow triggers include `pull_request`

### Cannot find status check to add
- Run the workflow at least once
- Wait a few minutes for GitHub to register the check
- Refresh the branch protection settings page

### Checks always failing
- Check workflow logs in the Actions tab
- Verify all required dependencies are installed
- Ensure test environment matches CI environment

### Force push needed
- Only repository admins can force push if protection is enabled
- Create a new branch instead of force pushing
- Use `git revert` instead of rewriting history

## Best Practices

1. **Always use Pull Requests** - Never push directly to protected branches
2. **Keep branches up to date** - Regularly merge main into feature branches
3. **Small, focused PRs** - Easier to review and faster to merge
4. **Run tests locally** - Before pushing, run the same tests that CI runs
5. **Fix broken builds immediately** - Don't let failing tests accumulate
6. **Review security alerts** - Address Dependabot and CodeQL findings promptly

## Automated Checks Summary

| Check | Purpose | Blocking |
|-------|---------|----------|
| Backend Build & Test | Verify Java code compiles and tests pass | Yes |
| Frontend Build & Test | Verify Angular app builds and tests pass | Yes |
| Dependency Scan | Check for vulnerable dependencies | Yes |
| CodeQL Java | Security analysis of Java code | Yes |
| CodeQL JavaScript | Security analysis of TypeScript/JavaScript | Yes |
| Checkstyle | Code style compliance (backend) | No (warning only) |
| Prettier | Code formatting (frontend) | No (warning only) |

## References

- [GitHub Branch Protection Documentation](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches)
- [GitHub Actions Status Checks](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/collaborating-on-repositories-with-code-quality-features/about-status-checks)
- [CONTRIBUTING.md](../CONTRIBUTING.md) - Full contribution guidelines
