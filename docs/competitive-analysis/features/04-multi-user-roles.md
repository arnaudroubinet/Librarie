# Feature Specification: Multi-User Roles & Permissions

**Feature ID:** F004  
**Priority:** High  
**Effort:** Medium (3-4 weeks)  
**Competitors:** All competitors

## Overview

Comprehensive user management with role-based access control (RBAC) and per-library permissions.

## User Stories

### US-001: Admin Creates Users
**As an** administrator  
**I want** to create user accounts with specific roles  
**So that** I can control access to my library

**Acceptance:**
- Create users with username/email
- Assign roles: Admin, User, Guest
- Set library access permissions
- Send invitation emails

### US-002: Per-Library Access
**As an** administrator  
**I want** to restrict users to specific libraries  
**So that** I can maintain content separation

**Acceptance:**
- Assign users to one or more libraries
- Users only see their assigned libraries
- Different permissions per library

### US-003: Guest Access
**As an** administrator  
**I want** to create guest accounts with limited access  
**So that** I can share specific content without full access

**Acceptance:**
- Guest users can only read
- No download or edit permissions
- Limited to specific collections

## Architecture

### Data Model

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    role VARCHAR(50) NOT NULL, -- ADMIN, USER, GUEST
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE libraries (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(1000) NOT NULL,
    owner_id UUID REFERENCES users(id)
);

CREATE TABLE library_permissions (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    library_id UUID REFERENCES libraries(id),
    can_read BOOLEAN DEFAULT true,
    can_download BOOLEAN DEFAULT true,
    can_edit BOOLEAN DEFAULT false,
    can_delete BOOLEAN DEFAULT false,
    UNIQUE(user_id, library_id)
);
```

### API Endpoints

```
POST /api/v1/users
GET /api/v1/users
GET /api/v1/users/{id}
PUT /api/v1/users/{id}
DELETE /api/v1/users/{id}

POST /api/v1/libraries
GET /api/v1/libraries (filtered by user access)
PUT /api/v1/libraries/{id}/permissions
```

## Implementation Tasks

1. **Backend Data Model** (1 week)
   - User, Role, Permission entities
   - Database migrations
   - Repository layer

2. **Authorization Logic** (1 week)
   - RBAC implementation
   - Permission checking interceptors
   - Library filtering

3. **Admin UI** (1.5 weeks)
   - User management screen
   - Role assignment
   - Permission matrix

4. **Testing** (0.5 weeks)
   - Permission tests
   - Integration tests
   - Security audits

## Success Metrics

- 70% of installations use multi-user
- Average 5 users per instance
- <1% unauthorized access attempts succeed

---
