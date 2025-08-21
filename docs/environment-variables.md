# Environment Variables Reference

This document provides a comprehensive reference for all environment variables used by the Librarie application.

## Database Configuration

### DB_USERNAME
- **Purpose**: PostgreSQL database username for the application
- **Required**: Yes (production)
- **Default**: None (uses Quarkus DevServices in development)
- **Example**: `librarie_user`

### DB_PASSWORD
- **Purpose**: PostgreSQL database password
- **Required**: Yes (production)
- **Default**: None (uses Quarkus DevServices in development)
- **Example**: `secure_password_here`

### DB_URL
- **Purpose**: JDBC connection URL for PostgreSQL database
- **Required**: Yes (production)
- **Default**: None (uses Quarkus DevServices in development)
- **Format**: `jdbc:postgresql://host:port/database`
- **Example**: `jdbc:postgresql://db.company.com:5432/librarie`

## Authentication (OIDC)

### OIDC_AUTH_SERVER_URL
- **Purpose**: URL of the OIDC authentication server
- **Required**: Yes (production)
- **Default**: `https://auth.company.com/realms/prod-realm`
- **Example**: `https://keycloak.company.com/realms/production`

### OIDC_CLIENT_ID
- **Purpose**: Client identifier for OIDC authentication
- **Required**: Yes (production)
- **Default**: `librarie-prod`
- **Example**: `librarie-app`

### OIDC_CLIENT_SECRET
- **Purpose**: Client secret for OIDC authentication
- **Required**: Yes (production)
- **Default**: `prod-secret-placeholder`
- **Security**: ⚠️ Keep this secret secure and never commit to version control

## Application Configuration

### LIBRARIE_STORAGE_BASE_DIR
- **Property**: `librarie.storage.base-dir`
- **Purpose**: Base directory for file storage
- **Required**: No
- **Default**: `assets`
- **Example**: `/var/lib/librarie/storage`

### LIBRARIE_STORAGE_MAX_FILE_SIZE
- **Property**: `librarie.storage.max-file-size`
- **Purpose**: Maximum file upload size in bytes
- **Required**: No
- **Default**: `104857600` (100MB)
- **Example**: `524288000` (500MB)

### LIBRARIE_STORAGE_ALLOWED_BOOK_EXTENSIONS
- **Property**: `librarie.storage.allowed-book-extensions`
- **Purpose**: Comma-separated list of allowed book file extensions
- **Required**: No
- **Default**: `pdf,epub,mobi,azw,azw3,fb2,txt,rtf,doc,docx`
- **Example**: `pdf,epub,mobi`

### LIBRARIE_STORAGE_ALLOWED_IMAGE_EXTENSIONS
- **Property**: `librarie.storage.allowed-image-extensions`
- **Purpose**: Comma-separated list of allowed image file extensions
- **Required**: No
- **Default**: `jpg,jpeg,png,gif,webp,bmp`
- **Example**: `jpg,png,webp`

## Demo Mode Configuration

### LIBRARIE_DEMO_ENABLED
- **Property**: `librarie.demo.enabled`
- **Purpose**: Enable automatic demo data population
- **Required**: No
- **Default**: `true`
- **Values**: `true` or `false`

### LIBRARIE_DEMO_BOOK_COUNT
- **Property**: `librarie.demo.book-count`
- **Purpose**: Number of demo books to create
- **Required**: No
- **Default**: `100`
- **Example**: `50`

### LIBRARIE_DEMO_AUTHOR_COUNT
- **Property**: `librarie.demo.author-count`
- **Purpose**: Number of demo authors to create
- **Required**: No
- **Default**: `50`
- **Example**: `25`

### LIBRARIE_DEMO_SERIES_COUNT
- **Property**: `librarie.demo.series-count`
- **Purpose**: Number of demo series to create
- **Required**: No
- **Default**: `20`
- **Example**: `10`

## Security Configuration

### LIBRARIE_SECURITY_SANITIZATION_ENABLED
- **Property**: `librarie.security.sanitization-enabled`
- **Purpose**: Enable input sanitization for user data
- **Required**: No
- **Default**: `true`
- **Values**: `true` or `false`
- **Recommendation**: Keep enabled in production

### LIBRARIE_SECURITY_FILE_VALIDATION_ENABLED
- **Property**: `librarie.security.file-validation-enabled`
- **Purpose**: Enable file type validation for uploads
- **Required**: No
- **Default**: `true`
- **Values**: `true` or `false`
- **Recommendation**: Keep enabled in production

### LIBRARIE_SECURITY_MAX_REQUEST_SIZE
- **Property**: `librarie.security.max-request-size`
- **Purpose**: Maximum HTTP request size in bytes
- **Required**: No
- **Default**: `10485760` (10MB)
- **Example**: `52428800` (50MB)

## Standard Quarkus Environment Variables

The application also supports standard Quarkus configuration via environment variables:

### QUARKUS_HTTP_PORT
- **Purpose**: HTTP server port
- **Default**: `8080`
- **Example**: `9090`

### QUARKUS_LOG_LEVEL
- **Purpose**: Application log level
- **Default**: `INFO`
- **Values**: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`

### QUARKUS_HTTP_CORS_ORIGINS
- **Purpose**: Allowed CORS origins for frontend access
- **Default**: `http://localhost:4200`
- **Example**: `https://app.company.com,https://staging.company.com`

## Environment File Example

Create a `.env` file for local development (do not commit this file):

```bash
# Database (optional in dev - uses DevServices)
DB_USERNAME=dev_user
DB_PASSWORD=dev_password
DB_URL=jdbc:postgresql://localhost:5432/librarie_dev

# OIDC (optional in dev - uses DevServices)
OIDC_AUTH_SERVER_URL=http://localhost:8180/realms/librarie
OIDC_CLIENT_ID=librarie-dev
OIDC_CLIENT_SECRET=dev-secret

# Application settings
LIBRARIE_STORAGE_BASE_DIR=/tmp/librarie-storage
LIBRARIE_DEMO_ENABLED=true
LIBRARIE_DEMO_BOOK_COUNT=20

# Security
LIBRARIE_SECURITY_SANITIZATION_ENABLED=true
LIBRARIE_SECURITY_FILE_VALIDATION_ENABLED=true
```

## Docker Compose Example

```yaml
version: '3.8'
services:
  librarie:
    image: librarie:latest
    environment:
      DB_USERNAME: librarie_user
      DB_PASSWORD: secure_password
      DB_URL: jdbc:postgresql://postgres:5432/librarie
      OIDC_AUTH_SERVER_URL: http://keycloak:8080/realms/librarie
      OIDC_CLIENT_ID: librarie-app
      OIDC_CLIENT_SECRET: your-secret-here
      LIBRARIE_STORAGE_BASE_DIR: /app/storage
      LIBRARIE_DEMO_ENABLED: false
    volumes:
      - ./storage:/app/storage
    ports:
      - "8080:8080"
```

## Important Notes

1. **Security**: Never commit sensitive environment variables (passwords, secrets) to version control
2. **Production**: All database and OIDC variables are required in production
3. **Development**: Most variables are optional thanks to Quarkus DevServices
4. **Updates**: When adding new environment variables, update this documentation immediately