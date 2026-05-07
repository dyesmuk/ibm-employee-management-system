# Module 04 — Docker Compose

Docker Compose lets you define and manage multi-container applications using a single YAML file. Instead of running five `docker run` commands and remembering every flag, you write it once and use `docker compose up`.

---

## 1. Why Docker Compose?

A typical web application has multiple components:

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  Nginx   │───►│  Node.js │───►│ Postgres │    │  Redis   │
│ (proxy)  │    │   (api)  │    │  (db)    │    │  (cache) │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
```

Running each manually:
```bash
docker network create app-net
docker volume create pgdata
docker run -d --name redis --network app-net redis:7-alpine
docker run -d --name db --network app-net -e POSTGRES_DB=myapp ... postgres:15
docker run -d --name api --network app-net -e DB_HOST=db ... myapp:latest
docker run -d --name proxy --network app-net -p 80:80 ... nginx
```

With Docker Compose:
```bash
docker compose up -d
```

---

## 2. Installation

Docker Compose V2 is included with Docker Desktop (Mac/Windows). On Linux:

```bash
# Included with Docker Engine 23+
docker compose version
# Docker Compose version v2.24.6

# Legacy V1 was a separate binary: docker-compose (with hyphen)
# V2 is: docker compose (space, built into docker CLI)
```

---

## 3. The `docker-compose.yml` File

The compose file defines your entire application stack.

### Complete annotated example

```yaml
# docker-compose.yml

# Compose file format version (optional in V2+)
version: "3.9"

# ── SERVICES ──────────────────────────────────────────────────────
services:

  # ── Web / Proxy ────────────────────────────────────────────────
  nginx:
    image: nginx:1.25-alpine
    container_name: app-proxy
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/certs:/etc/nginx/certs:ro
    depends_on:
      api:
        condition: service_healthy   # wait for api healthcheck
    restart: unless-stopped
    networks:
      - frontend

  # ── API / Application ──────────────────────────────────────────
  api:
    build:
      context: .                    # Dockerfile location
      dockerfile: Dockerfile        # Dockerfile name
      args:
        NODE_ENV: production        # build arguments
    container_name: app-api
    ports:
      - "3000:3000"                 # expose for local dev
    environment:
      NODE_ENV: production
      PORT: 3000
      DB_HOST: db                   # use service name as hostname
      DB_PORT: 5432
      DB_NAME: myapp
      DB_USER: admin
      REDIS_HOST: cache
    env_file:
      - .env.prod                   # additional env from file
    depends_on:
      db:
        condition: service_healthy
      cache:
        condition: service_started
    healthcheck:
      test: ["CMD", "wget", "-qO-", "http://localhost:3000/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    restart: unless-stopped
    networks:
      - frontend
      - backend
    deploy:
      resources:
        limits:
          cpus: "1.0"
          memory: 512M

  # ── Database ───────────────────────────────────────────────────
  db:
    image: postgres:15-alpine
    container_name: app-db
    environment:
      POSTGRES_USER: admin
      POSTGRES_PASSWORD_FILE: /run/secrets/db_password
      POSTGRES_DB: myapp
    volumes:
      - postgres-data:/var/lib/postgresql/data  # named volume
      - ./db/init.sql:/docker-entrypoint-initdb.d/init.sql:ro
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U admin -d myapp"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped
    networks:
      - backend

  # ── Cache ──────────────────────────────────────────────────────
  cache:
    image: redis:7-alpine
    container_name: app-cache
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis-data:/data
    restart: unless-stopped
    networks:
      - backend

# ── VOLUMES ───────────────────────────────────────────────────────
volumes:
  postgres-data:
    driver: local
  redis-data:
    driver: local

# ── NETWORKS ──────────────────────────────────────────────────────
networks:
  frontend:
    driver: bridge
  backend:
    driver: bridge
    internal: true    # no internet access from backend network
```

---

## 4. Core Compose Commands

```bash
# Start all services (detached)
docker compose up -d

# Start and rebuild images
docker compose up -d --build

# Start specific services only
docker compose up -d api db

# Stop all services (containers remain)
docker compose stop

# Stop and remove containers, networks
docker compose down

# Stop, remove containers, networks, AND volumes (⚠️ data loss)
docker compose down -v

# Stop, remove, AND remove images
docker compose down --rmi all

# View running services
docker compose ps

# View logs
docker compose logs
docker compose logs -f          # follow
docker compose logs -f api      # specific service
docker compose logs --tail 50

# Execute a command in a running service
docker compose exec api bash
docker compose exec db psql -U admin myapp

# Run a one-off command (new container)
docker compose run --rm api npm run migrate

# Scale a service
docker compose up -d --scale api=3

# Restart a service
docker compose restart api

# Pull latest images
docker compose pull

# Build images without starting
docker compose build
docker compose build --no-cache api
```

---

## 5. Environment Variables in Compose

### Substitution from the shell

Compose automatically reads variables from the shell environment:

```yaml
services:
  db:
    image: postgres:${POSTGRES_VERSION:-15}  # default: 15
    environment:
      POSTGRES_PASSWORD: ${DB_PASSWORD}       # from shell
```

### The `.env` file

Compose automatically loads `.env` from the same directory:

```env
# .env
POSTGRES_VERSION=15
DB_PASSWORD=secret
REDIS_PASSWORD=redissecret
```

```bash
# Override .env file location
docker compose --env-file .env.prod up -d
```

### Multiple environment files per service

```yaml
services:
  api:
    env_file:
      - .env.base
      - .env.prod     # later file wins on conflicts
```

---

## 6. Multiple Compose Files (Overrides)

Split configuration for dev vs production:

### `docker-compose.yml` (base)

```yaml
services:
  api:
    build: .
    environment:
      NODE_ENV: production

  db:
    image: postgres:15
```

### `docker-compose.override.yml` (auto-loaded in dev)

```yaml
# docker-compose.override.yml — loaded automatically
services:
  api:
    environment:
      NODE_ENV: development
    volumes:
      - .:/app           # live code reload
    command: npm run dev

  db:
    ports:
      - "5432:5432"      # expose db port locally in dev
```

### `docker-compose.prod.yml`

```yaml
services:
  api:
    image: registry.example.com/myapp:${VERSION}
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 1G
```

```bash
# Development (auto-loads override)
docker compose up -d

# Production (explicit files)
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

---

## 7. Networking in Compose

By default, Compose creates a single network for the project. All services can reach each other using their service name as a hostname.

```yaml
services:
  api:
    image: myapp
    # Can reach 'db' at hostname 'db', port 5432
    environment:
      DB_HOST: db

  db:
    image: postgres:15
```

### Custom networks

```yaml
services:
  nginx:
    networks: [frontend]
  api:
    networks: [frontend, backend]
  db:
    networks: [backend]

networks:
  frontend:
  backend:
    internal: true  # no outbound internet access
```

### Using existing networks

```yaml
networks:
  existing-net:
    external: true
    name: my-pre-created-network
```

---

## 8. Volumes in Compose

```yaml
services:
  db:
    volumes:
      # Named volume (persistent)
      - db-data:/var/lib/postgresql/data

      # Bind mount (dev: live reload)
      - ./src:/app/src

      # Anonymous volume (ephemeral)
      - /app/node_modules

      # Read-only bind mount
      - ./config.json:/app/config.json:ro

volumes:
  db-data:             # managed by Docker
    driver: local

  db-data-nfs:         # NFS example
    driver: local
    driver_opts:
      type: nfs
      o: addr=192.168.1.100,rw
      device: ":/exports/data"
```

---

## 9. Health Checks and Dependencies

```yaml
services:
  db:
    image: postgres:15
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s

  api:
    depends_on:
      db:
        condition: service_healthy   # wait for db to be healthy

      cache:
        condition: service_started   # just wait for it to start

      migration:
        condition: service_completed_successfully  # wait for it to exit 0
```

### Health check conditions

| Condition | Meaning |
|-----------|---------|
| `service_started` | Container is running |
| `service_healthy` | Healthcheck is passing |
| `service_completed_successfully` | Container exited with code 0 |

---

## 10. A Full Development Stack Example

### Project structure

```
my-app/
├── docker-compose.yml
├── docker-compose.override.yml  ← dev overrides
├── .env
├── api/
│   ├── Dockerfile
│   └── src/
├── nginx/
│   └── nginx.conf
└── db/
    └── init.sql
```

### `docker-compose.yml`

```yaml
services:
  nginx:
    image: nginx:alpine
    ports: ["80:80"]
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
    depends_on:
      - api
    networks: [app]

  api:
    build: ./api
    environment:
      DB_HOST: db
      REDIS_HOST: cache
    depends_on:
      db:
        condition: service_healthy
    networks: [app]

  db:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: myapp
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASS}
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./db/init.sql:/docker-entrypoint-initdb.d/init.sql:ro
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER}"]
      interval: 10s
      retries: 5
    networks: [app]

  cache:
    image: redis:7-alpine
    networks: [app]

volumes:
  pgdata:

networks:
  app:
```

### `docker-compose.override.yml` (dev)

```yaml
services:
  api:
    volumes:
      - ./api/src:/app/src     # live reload
    command: npm run dev
    ports: ["3000:3000"]        # expose API directly

  db:
    ports: ["5432:5432"]        # access DB from host

  cache:
    ports: ["6379:6379"]        # access Redis from host
```

```bash
# Start dev environment
docker compose up -d

# Check everything is running
docker compose ps

# Run database migrations
docker compose exec api npm run migrate

# Watch logs
docker compose logs -f api
```

---

## 11. Compose for CI/CD

```yaml
# docker-compose.test.yml
services:
  test:
    build:
      context: .
      target: builder
    command: npm test
    environment:
      NODE_ENV: test
      DB_HOST: test-db
    depends_on:
      test-db:
        condition: service_healthy

  test-db:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: testdb
      POSTGRES_USER: test
      POSTGRES_PASSWORD: test
    healthcheck:
      test: ["CMD-SHELL", "pg_isready"]
      interval: 5s
      retries: 10
```

```bash
# In CI pipeline
docker compose -f docker-compose.test.yml run --rm test
echo "Exit code: $?"
docker compose -f docker-compose.test.yml down -v
```

---

## Summary

| Command | What it does |
|---------|-------------|
| `docker compose up -d` | Start all services |
| `docker compose down` | Stop and remove containers |
| `docker compose down -v` | Also remove volumes |
| `docker compose ps` | List service status |
| `docker compose logs -f` | Follow all logs |
| `docker compose exec <svc> bash` | Shell into service |
| `docker compose build` | Build images |
| `docker compose pull` | Pull latest images |
| `docker compose run --rm <svc> cmd` | One-off command |

**Next:** [05 — Docker Registry →](./05-docker-registry.md)
