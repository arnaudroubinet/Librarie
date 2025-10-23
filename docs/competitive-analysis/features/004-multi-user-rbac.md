# Feature: Multi-User with Role-Based Access Control (RBAC)

## Priority: CRITICAL (Tier 1)
**ROI Score: 90/100** (Very High Impact, Medium Effort)

**Found in:** Calibre-Web, Kavita, Komga, Audiobookshelf  
**Industry Standard:** Yes (essential for shared libraries)

## Why Implement This Feature

### User Value
- **Family Sharing**: Multiple family members can have separate accounts
- **Personal Libraries**: Each user has their own reading history, progress, collections
- **Privacy**: Users don't see each other's reading lists or notes
- **Permissions**: Parents can restrict children's access to content
- **Guest Access**: Ability to give limited access to friends

### Business Value
- **Market Expansion**: Opens to household/family use cases (70% of potential users)
- **Enterprise Potential**: Required for schools, libraries, organizations
- **Competitive Necessity**: Single-user is a dealbreaker for most users
- **Revenue Opportunity**: Enables tiered plans (per-user pricing)

### Technical Value
- **Security Foundation**: Proper auth/authz from the start
- **Audit Trail**: Track who did what
- **Resource Quotas**: Limit storage/bandwidth per user
- **Foundation**: Required for social features, sharing, recommendations

## Implementation Strategy

### Overview
Implement a comprehensive RBAC system with predefined roles (Admin, User, Guest) and granular permissions. Integrate with existing OIDC authentication.

### Technology Stack

**Backend (Quarkus/Java):**
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-security</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-security-jpa</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-oidc</artifactId>
</dependency>
```

### Architecture

#### 1. Data Model
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String displayName;
    
    private String avatarUrl;
    
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    // User preferences
    @OneToOne(cascade = CascadeType.ALL)
    private UserPreferences preferences;
    
    // Quotas
    @Column(name = "storage_quota_mb")
    private Long storageQuotaMb;
    
    @Column(name = "storage_used_mb")
    private Long storageUsedMb;
}

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    private String description;
    
    @Column(name = "is_system")
    private Boolean isSystem = false; // Prevents deletion of system roles
    
    @ManyToMany
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
}

@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name; // e.g., "books.read", "books.write", "users.manage"
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    private ResourceType resource; // BOOKS, USERS, SETTINGS, LIBRARY
    
    @Enumerated(EnumType.STRING)
    private Action action; // READ, WRITE, DELETE, MANAGE
}

// Predefined roles
public enum SystemRole {
    ADMIN("Administrator", "Full system access"),
    USER("User", "Standard user access"),
    GUEST("Guest", "Read-only access");
    
    private final String displayName;
    private final String description;
}

// Permission constants
public interface Permissions {
    // Books
    String BOOKS_READ = "books.read";
    String BOOKS_WRITE = "books.write";
    String BOOKS_DELETE = "books.delete";
    String BOOKS_UPLOAD = "books.upload";
    
    // Collections
    String COLLECTIONS_READ = "collections.read";
    String COLLECTIONS_WRITE = "collections.write";
    
    // Users
    String USERS_READ = "users.read";
    String USERS_MANAGE = "users.manage";
    
    // Library
    String LIBRARY_READ = "library.read";
    String LIBRARY_MANAGE = "library.manage";
    
    // Settings
    String SETTINGS_READ = "settings.read";
    String SETTINGS_MANAGE = "settings.manage";
}
```

#### 2. Security Service
```java
@ApplicationScoped
public class SecurityService {
    
    @Inject
    UserRepository userRepository;
    
    @Inject
    RoleRepository roleRepository;
    
    public boolean hasPermission(User user, String permissionName) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        
        return user.getRole().getPermissions().stream()
            .anyMatch(p -> p.getName().equals(permissionName));
    }
    
    public boolean hasAnyPermission(User user, String... permissionNames) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        
        Set<String> userPermissions = user.getRole().getPermissions().stream()
            .map(Permission::getName)
            .collect(Collectors.toSet());
        
        return Arrays.stream(permissionNames)
            .anyMatch(userPermissions::contains);
    }
    
    public boolean canAccessBook(User user, Book book) {
        // Admin can access everything
        if (hasPermission(user, Permissions.BOOKS_READ)) {
            return true;
        }
        
        // Check if book is in user's library
        return bookRepository.isBookAccessibleByUser(book.getId(), user.getId());
    }
    
    public void enforcePermission(User user, String permission) {
        if (!hasPermission(user, permission)) {
            throw new ForbiddenException("User does not have permission: " + permission);
        }
    }
}
```

#### 3. Security Annotations
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@InterceptorBinding
public @interface RequiresPermission {
    String value();
}

@Interceptor
@RequiresPermission("")
@Priority(Interceptor.Priority.PLATFORM_BEFORE)
public class PermissionInterceptor {
    
    @Inject
    SecurityService securityService;
    
    @Inject
    @Claim("sub")
    String userId;
    
    @AroundInvoke
    public Object enforcePermission(InvocationContext context) throws Exception {
        RequiresPermission annotation = context.getMethod()
            .getAnnotation(RequiresPermission.class);
        
        if (annotation == null) {
            annotation = context.getTarget().getClass()
                .getAnnotation(RequiresPermission.class);
        }
        
        if (annotation != null) {
            User user = userRepository.findByOidcSubject(userId);
            securityService.enforcePermission(user, annotation.value());
        }
        
        return context.proceed();
    }
}
```

#### 4. REST API
```java
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {
    
    @Inject
    UserService userService;
    
    @Inject
    SecurityService securityService;
    
    @GET
    @RequiresPermission(Permissions.USERS_READ)
    public Response listUsers(@QueryParam("page") @DefaultValue("0") int page,
                             @QueryParam("size") @DefaultValue("20") int size) {
        List<User> users = userService.listUsers(page, size);
        return Response.ok(users).build();
    }
    
    @POST
    @RequiresPermission(Permissions.USERS_MANAGE)
    public Response createUser(UserCreateRequest request) {
        User user = userService.createUser(request);
        return Response.status(Response.Status.CREATED).entity(user).build();
    }
    
    @PUT
    @Path("/{id}")
    @RequiresPermission(Permissions.USERS_MANAGE)
    public Response updateUser(@PathParam("id") Long id, UserUpdateRequest request) {
        User user = userService.updateUser(id, request);
        return Response.ok(user).build();
    }
    
    @PUT
    @Path("/{id}/role")
    @RequiresPermission(Permissions.USERS_MANAGE)
    public Response updateUserRole(@PathParam("id") Long id, @QueryParam("roleId") Long roleId) {
        userService.updateUserRole(id, roleId);
        return Response.ok().build();
    }
    
    @PUT
    @Path("/{id}/activate")
    @RequiresPermission(Permissions.USERS_MANAGE)
    public Response activateUser(@PathParam("id") Long id) {
        userService.activateUser(id);
        return Response.ok().build();
    }
    
    @PUT
    @Path("/{id}/deactivate")
    @RequiresPermission(Permissions.USERS_MANAGE)
    public Response deactivateUser(@PathParam("id") Long id) {
        userService.deactivateUser(id);
        return Response.ok().build();
    }
    
    @GET
    @Path("/me")
    public Response getCurrentUser(@Context SecurityContext securityContext) {
        User user = userService.getUserFromSecurityContext(securityContext);
        return Response.ok(user).build();
    }
    
    @PUT
    @Path("/me/preferences")
    public Response updatePreferences(@Context SecurityContext securityContext,
                                     UserPreferencesRequest request) {
        User user = userService.getUserFromSecurityContext(securityContext);
        userService.updatePreferences(user.getId(), request);
        return Response.ok().build();
    }
}

@Path("/api/roles")
public class RoleResource {
    
    @Inject
    RoleService roleService;
    
    @GET
    @RequiresPermission(Permissions.USERS_MANAGE)
    public Response listRoles() {
        List<Role> roles = roleService.listRoles();
        return Response.ok(roles).build();
    }
    
    @POST
    @RequiresPermission(Permissions.USERS_MANAGE)
    public Response createRole(RoleCreateRequest request) {
        Role role = roleService.createRole(request);
        return Response.status(Response.Status.CREATED).entity(role).build();
    }
    
    @PUT
    @Path("/{id}/permissions")
    @RequiresPermission(Permissions.USERS_MANAGE)
    public Response updateRolePermissions(@PathParam("id") Long id,
                                         Set<Long> permissionIds) {
        roleService.updateRolePermissions(id, permissionIds);
        return Response.ok().build();
    }
}
```

### Database Schema

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    display_name VARCHAR(255),
    avatar_url TEXT,
    role_id BIGINT REFERENCES roles(id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    storage_quota_mb BIGINT DEFAULT 5000,
    storage_used_mb BIGINT DEFAULT 0,
    oidc_subject VARCHAR(255) UNIQUE -- Link to OIDC identity
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    is_system BOOLEAN DEFAULT FALSE
);

CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    resource VARCHAR(50),
    action VARCHAR(50)
);

CREATE TABLE role_permissions (
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    language VARCHAR(10) DEFAULT 'en',
    theme VARCHAR(50) DEFAULT 'light',
    books_per_page INTEGER DEFAULT 20,
    default_view VARCHAR(50) DEFAULT 'grid',
    email_notifications BOOLEAN DEFAULT TRUE
);

-- Audit log
CREATE TABLE user_activity_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(100),
    resource_type VARCHAR(50),
    resource_id BIGINT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_role ON users(role_id);
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_user_activity_log_user ON user_activity_log(user_id);
CREATE INDEX idx_user_activity_log_created ON user_activity_log(created_at DESC);

-- Initialize system roles and permissions
INSERT INTO roles (name, description, is_system) VALUES
('Administrator', 'Full system access', TRUE),
('User', 'Standard user access', TRUE),
('Guest', 'Read-only access', TRUE);

INSERT INTO permissions (name, description, resource, action) VALUES
('books.read', 'View books', 'BOOKS', 'READ'),
('books.write', 'Add/edit books', 'BOOKS', 'WRITE'),
('books.delete', 'Delete books', 'BOOKS', 'DELETE'),
('books.upload', 'Upload books', 'BOOKS', 'WRITE'),
('collections.read', 'View collections', 'COLLECTIONS', 'READ'),
('collections.write', 'Create/edit collections', 'COLLECTIONS', 'WRITE'),
('users.read', 'View users', 'USERS', 'READ'),
('users.manage', 'Manage users', 'USERS', 'MANAGE'),
('library.read', 'View library', 'LIBRARY', 'READ'),
('library.manage', 'Manage library settings', 'LIBRARY', 'MANAGE'),
('settings.read', 'View settings', 'SETTINGS', 'READ'),
('settings.manage', 'Manage settings', 'SETTINGS', 'MANAGE');

-- Assign permissions to roles
-- Admin gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.name = 'Administrator';

-- User gets standard permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'User' AND p.name IN (
    'books.read', 'books.upload', 'collections.read', 'collections.write', 'library.read'
);

-- Guest gets read-only
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'Guest' AND p.name IN ('books.read', 'library.read');
```

## Implementation Phases

### Phase 1: Core RBAC (Week 1-2)
- [ ] Database schema and entities
- [ ] Role and permission management
- [ ] Security service and interceptors
- [ ] Basic REST APIs

### Phase 2: User Management UI (Week 2-3)
- [ ] User list and detail pages
- [ ] Role assignment interface
- [ ] User activation/deactivation
- [ ] Permission matrix view

### Phase 3: Integration (Week 3-4)
- [ ] Integrate with existing OIDC
- [ ] Add permission checks to all endpoints
- [ ] User preferences
- [ ] Activity logging

### Phase 4: Advanced Features (Week 4-5)
- [ ] Custom roles
- [ ] Resource-level permissions (per-library, per-collection)
- [ ] Quotas and limits
- [ ] Audit reporting

## Acceptance Tests

### Unit Tests
```java
@QuarkusTest
public class SecurityServiceTest {
    
    @Inject
    SecurityService securityService;
    
    @Test
    public void testAdminHasAllPermissions() {
        User admin = createUserWithRole(SystemRole.ADMIN);
        
        assertTrue(securityService.hasPermission(admin, Permissions.BOOKS_READ));
        assertTrue(securityService.hasPermission(admin, Permissions.BOOKS_WRITE));
        assertTrue(securityService.hasPermission(admin, Permissions.USERS_MANAGE));
    }
    
    @Test
    public void testGuestHasReadOnlyPermissions() {
        User guest = createUserWithRole(SystemRole.GUEST);
        
        assertTrue(securityService.hasPermission(guest, Permissions.BOOKS_READ));
        assertFalse(securityService.hasPermission(guest, Permissions.BOOKS_WRITE));
        assertFalse(securityService.hasPermission(guest, Permissions.USERS_MANAGE));
    }
    
    @Test
    public void testEnforcePermissionThrowsForUnauthorized() {
        User guest = createUserWithRole(SystemRole.GUEST);
        
        assertThrows(ForbiddenException.class, () -> {
            securityService.enforcePermission(guest, Permissions.BOOKS_DELETE);
        });
    }
}
```

### Integration Tests
```java
@QuarkusTest
public class UserResourceTest {
    
    @Test
    public void testListUsersRequiresPermission() {
        given()
            .auth().oauth2(getGuestToken())
        .when()
            .get("/api/users")
        .then()
            .statusCode(403);
    }
    
    @Test
    public void testAdminCanListUsers() {
        given()
            .auth().oauth2(getAdminToken())
        .when()
            .get("/api/users")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0));
    }
    
    @Test
    public void testUserCannotDeleteBooks() {
        Long bookId = createTestBook().getId();
        
        given()
            .auth().oauth2(getUserToken())
        .when()
            .delete("/api/books/" + bookId)
        .then()
            .statusCode(403);
    }
}
```

### End-to-End Tests
```gherkin
Feature: Multi-User RBAC

Scenario: Admin creates new user
    Given I am logged in as admin
    When I navigate to "Users"
    And I click "Add User"
    And I enter username "newuser"
    And I enter email "newuser@example.com"
    And I select role "User"
    And I click "Create"
    Then the user should appear in the user list
    And should have role "User"
    
Scenario: Guest cannot upload books
    Given I am logged in as guest
    When I navigate to "Library"
    Then I should not see "Upload" button
    When I try to POST to "/api/books/upload"
    Then I should receive a 403 Forbidden response
    
Scenario: User can manage own collections but not others
    Given I am logged in as regular user
    And another user has a collection "Private List"
    When I try to view their collection
    Then I should receive a 403 Forbidden response
    When I create my own collection "My List"
    Then I should be able to view and edit it
```

## Success Metrics

### User Metrics
- 80%+ of installations have multiple users within 30 days
- Average 2.5 users per installation
- Guest users convert to full users at 40% rate

### Technical Metrics
- Permission checks add < 5ms latency
- Zero security vulnerabilities in RBAC system

## Estimated Effort

**Total: 5 weeks (1 developer)**

## Related Features
- **Foundation For**: Sharing, social features, enterprise features
- **Depends On**: OIDC authentication (already present)
- **Enables**: Per-user collections, progress tracking, recommendations
