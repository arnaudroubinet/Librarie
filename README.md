# 📚 Librarie

> A modern, full-stack digital library management system for organizing and reading eBooks.

**Quick Links**: [📐 Architecture](./ARCHITECTURE.md) · [📖 Full Documentation](./docs/PROJECT_OVERVIEW.md) · [📝 Glossary](./docs/GLOSSARY.md) · [🤝 Contributing](./CONTRIBUTING.md)

---

## ⚡ Quick Start

**Clone and run in 2 minutes:**

```bash
# 1. Start backend (auto-provisions PostgreSQL via TestContainers)
cd backend
./mvnw quarkus:dev

# 2. Start frontend (in another terminal)
cd frontend
npm install
npm start
```

🎯 **Access**: Frontend at `http://localhost:4200` · Backend API at `http://localhost:8080` · Swagger UI at `http://localhost:8080/q/swagger-ui`

**Requirements**: Java 21+ · Node.js 18+ · Docker (for Dev Services)

---

## 🎯 What is Librarie?

A **library management system** for digital books (EPUB, PDF) with:

- 📚 Book catalog with authors, series, and metadata
- 📖 Built-in EPUB reader (Readium-powered)
- 📊 Reading progress tracking across devices
- � Fast search across books, authors, and series
- 🔐 OIDC authentication (Keycloak-ready)
- 🌍 Multi-language support (BCP 47)

**Use Cases**: Personal library · Book club · Educational institution · Small publishing house

---

## 🏗️ Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Frontend** | Angular (Standalone Components) | 20.3 |
| **Backend** | Quarkus (Hexagonal Architecture) | 3.25.2 |
| **Database** | PostgreSQL + Flyway | 17 |
| **Auth** | Keycloak (OIDC) | 26.3 |
| **Reader** | Readium Navigator | Latest |

**Architecture**: Backend uses hexagonal (ports & adapters) pattern for clean separation. Frontend uses signals for reactive state. [→ Details in ARCHITECTURE.md](./ARCHITECTURE.md)

---

## 📚 Documentation

| Document | Description | When to Read |
|----------|-------------|--------------|
| **[PROJECT_OVERVIEW.md](./docs/PROJECT_OVERVIEW.md)** | Complete project documentation | Understanding the full system |
| **[GLOSSARY.md](./docs/GLOSSARY.md)** | Terminology and domain concepts | Learning the vocabulary |
| **[ARCHITECTURE.md](./ARCHITECTURE.md)** | Architecture decisions, C4 diagrams, patterns | Technical deep dive |
| **[CONTRIBUTING.md](./CONTRIBUTING.md)** | Contribution guidelines and CI/CD | Before contributing |
| **[Backend HTTP_CACHING.md](./backend/docs/HTTP_CACHING.md)** | HTTP caching strategy | Performance optimization |
| **[Backend DEMO_DATA.md](./backend/DEMO_DATA_IDEMPOTENCY.md)** | Demo data loading | Development setup |

**For LLMs**: Start with [`llms.txt`](./llms.txt) for structured documentation index.

---

## 🚀 Development

### Run Tests

```bash
# Backend tests (includes ArchUnit architecture validation)
cd backend && ./mvnw test

# Frontend tests
cd frontend && npm test

# Architecture validation only
cd backend && ./mvnw test -Dtest=HexagonalArchitectureTest
```

### Build for Production

```bash
# Backend (creates quarkus-app/ runnable)
cd backend && ./mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar

# Frontend (outputs to dist/)
cd frontend && npm run build
```

### Configuration

Backend settings in `backend/src/main/resources/application.properties`:

```properties
# Demo data (auto-loads sample books)
librarie.demo.enabled=true

# Storage
librarie.storage.base-dir=assets
librarie.storage.max-file-size=104857600

# Database
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/librarie
```

**Profiles**: `%dev` (development with TestContainers), `%prod` (production with external DB/OIDC)

---

## 🤝 Contributing

1. Read [CONTRIBUTING.md](./CONTRIBUTING.md) for process and CI/CD details
2. Follow hexagonal architecture patterns (backend) and standalone component patterns (frontend)
3. Check [GLOSSARY.md](./docs/GLOSSARY.md) for terminology
4. Run tests before committing
5. Architecture tests will fail if you violate layer boundaries ✅

---

## 📁 Project Structure

```
Librarie/
├── backend/                    # Quarkus backend
│   ├── src/main/java/org/rlh/
│   │   ├── domain/            # Core business logic (entities, value objects)
│   │   ├── application/       # Use cases and orchestration
│   │   └── infrastructure/    # Adapters (REST, DB, file I/O)
│   ├── src/main/resources/
│   │   ├── db/migration/      # Flyway SQL migrations
│   │   └── application.properties
│   └── assets/                # Book covers, EPUB files, author photos
├── frontend/                   # Angular frontend
│   ├── src/app/
│   │   ├── components/        # Feature components (book-list, reader, etc.)
│   │   ├── services/          # HTTP services (BookService, AuthService)
│   │   └── models/            # TypeScript interfaces (Book, Author, Series)
│   └── proxy.conf.json        # Dev proxy to backend
├── docs/                       # Documentation
│   ├── PROJECT_OVERVIEW.md    # Complete project documentation
│   └── GLOSSARY.md            # Terminology reference
├── llms.txt                    # LLM documentation index
└── ARCHITECTURE.md             # Architecture deep dive

```

---

## 🛠️ Troubleshooting

**Backend won't start?**
- Ensure Docker is running (for Dev Services)
- Check Java version: `java --version` (must be 21+)

**Frontend build errors?**
- Clear node_modules: `rm -rf node_modules && npm install`
- Check Node version: `node --version` (must be 18+)

**Database issues?**
- Dev mode uses TestContainers (auto-provisioned)
- Production requires external PostgreSQL

**More help**: See issue tracker or documentation.

---

## 📄 License

[Add your license here]

---

**Built with**: Quarkus · Angular · PostgreSQL · Readium · Keycloak
