# Product Requirements Document: Librarie

## 1. Executive Summary

Librarie is a comprehensive e-library management system designed to organize, maintain, and provide access to digital book collections. The application allows users to catalog books, authors, and series, providing robust search capabilities and detailed metadata management. Built with modern technologies (Java 21, Quarkus 3 backend, Angular 20 frontend), Librarie offers a responsive web interface for library management tasks.

## 2. Problem Statement

Digital book collections often lack proper organization systems, making it difficult for users to:
- Catalog and organize large digital book libraries
- Track book metadata including authors, publishers, and series
- Search across their collection using various criteria
- Manage book covers and author images
- Track reading progress and personal ratings

Librarie addresses these challenges by providing a centralized management system with rich metadata support and modern user interfaces.

## 3. User Personas

### 3.1. Digital Library Owner
- Maintains a personal collection of digital books
- Needs to organize books by author, series, and genre
- Wants to track reading progress and ratings

### 3.2. Collection Manager
- Manages a shared digital library
- Requires robust cataloging and search features
- Needs to maintain consistent metadata across all items

### 3.3. Reader
- Primarily interested in finding and accessing books
- Relies on search functionality and metadata
- Tracks personal reading progress

## 4. Core Features

### 4.1. Book Management
- Create, view, update, and delete book entries
- Store comprehensive book metadata (title, ISBN, page count, publication date)
- Associate books with authors and series
- Support for multiple formats per book
- Cover image management
- Reading progress tracking
- E-book file management and storage
- Custom metadata fields and tagging system
- Book format conversion (inspired by Calibre)
- Annotations and notes for books

### 4.2. Author Management
- Create, view, update, and delete author profiles
- Store author metadata (name, biography, birth/death dates)
- Link authors to their works
- Author picture management
- Support for multilingual author biographies

### 4.3. Series Management
- Create and maintain book series
- Track books within series with ordering
- Series cover image management
- Completion status tracking

### 4.4. Search Functionality
- Unified search across books, authors, and series
- Advanced search with multiple criteria
- Sort and filter capabilities
- Pagination for large result sets
- Full-text search within book content

### 4.5. Library Statistics
- Track total books, authors, and series
- Monitor library growth over time

### 4.6. Device Integration
- Connect and manage e-reader devices
- Send books to devices
- Track which books are on which devices
- Support for multiple device types

### 4.7. Book Acquisition
- Import books from external sources
- Bulk import functionality
- Duplicate detection and handling
- Automatic metadata retrieval from online sources

### 4.8. Content Management
- View e-books directly in the application
- Edit e-book metadata and content
- Extract and edit book covers
- Automatic library organization

## 5. User Experience

### 5.1. Navigation and Interface
- Modern, responsive web interface
- Sidebar navigation for main sections:
  - Books
  - Series
  - Authors
  - Search
  - Settings
- Consistent design language across all pages

### 5.2. Book Views
- Grid/list toggle for book browsing
- Detailed book view with all metadata
- Cover image display
- Related books by same author or series

### 5.3. Author Views
- List view of all authors
- Author detail page with biography and bibliography
- Author image display

### 5.4. Series Views
- List view of all series
- Series detail page with book list
- Series cover image display
- Reading order indicators

## 6. Technical Architecture

### 6.1. Backend (Java/Quarkus)
- Java 21 with Quarkus 3.25.2
- RESTful API design with Jackson for JSON processing
- Hexagonal architecture with domain-driven design
- Core domain models:
  - Book
  - Author
  - Series
  - Format
  - Tag
  - Rating
  - ReadingProgress
- PostgreSQL database with Flyway migrations
- Security via OIDC (OAuth2/OpenID Connect)
- Observability via Micrometer (metrics), SmallRye Health, and OpenTelemetry
- Background processing with Quartz scheduler
- API documentation with SmallRye OpenAPI
- Testing with JUnit 5, REST-assured, Mockito, and ArchUnit
- Asset management for covers and images

### 6.2. Frontend (Angular)
- Component-based architecture
- Responsive design using Material UI
- Type-safe models matching backend DTOs
- Service layer for API communication
- Client-side caching for performance

## 7. Data Model

### 7.1. Core Entities
- **Book**: Digital or physical book with metadata
- **Author**: Creator or contributor to books
- **Series**: Collection of related books
- **Format**: File format information
- **Tag**: Categorization tags
- **ReadingProgress**: User reading status
- **Rating**: User ratings for books

### 7.2. Key Relationships
- Books can have multiple authors with different roles
- Books can belong to multiple series
- Books can exist in multiple formats
- Series contain multiple books in a specific order
- Books can have multiple tags

## 8. Non-Functional Requirements

### 8.1. Performance
- Fast search and filtering
- Efficient loading of book lists
- Image caching for covers and author pictures

### 8.2. Scalability
- Support for large libraries (10,000+ books)
- Efficient pagination and cursor-based navigation

### 8.3. Security
- OIDC-based authentication
- Role-based access control
- Secure asset management

### 8.4. Extensibility
- Pluggable architecture for future enhancements
- API-first design for potential mobile clients
- Support for metadata extensions

## 9. Future Enhancements

### 9.1. Short-term Roadmap
- Advanced filtering capabilities
- Import/export functionality
- Tagging system improvements
- Reading statistics dashboards
- E-book format conversion tools
- Enhanced device integration

### 9.2. Long-term Vision
- Mobile companion apps
- Comprehensive e-reader integration
- AI-assisted metadata completion
- Social sharing features
- Reading recommendations
- Cloud library synchronization
- Book format conversion with customizable options
- E-book editor with WYSIWYG interface
- News download and conversion to e-book format
- Integrated content store for purchasing new books

## 10. Success Metrics

- User adoption and retention
- Library size and growth rate
- Search performance and accuracy
- System responsiveness under load

## 11. Technical Considerations

### 11.1. Development Approach
- Hexagonal architecture for maintainable code
- Modern Java features (records, pattern matching)
- Angular best practices
- Comprehensive test coverage

### 11.2. Dependencies
- Quarkus 3.25.2 extensions for core functionality:
  - quarkus-rest and quarkus-rest-jackson for REST API
  - quarkus-arc for dependency injection
  - quarkus-flyway for database migrations
  - quarkus-quartz for scheduling
  - quarkus-smallrye-openapi for API documentation
  - quarkus-smallrye-health for health checks
  - quarkus-micrometer and quarkus-opentelemetry for observability
  - quarkus-oidc for security
  - quarkus-jdbc-postgresql for database connectivity
- PostgreSQL for persistence
- Angular Material for UI components
- OIDC for authentication/authorization

### 11.3. Deployment
- Docker container support
- Environment-specific configurations
- CI/CD integration

---

This PRD represents the current state and vision for Librarie based on the codebase analysis. The application provides a comprehensive solution for digital library management with modern technologies and thoughtful design.
