# Module 03 — Docker Images

Images are the foundation of Docker. This module covers how images work, how to write production-quality Dockerfiles, how to understand layers, and how to optimise image size and security.

---

## 1. How Images Work

### The layered filesystem

Every Docker image is built from a stack of **read-only layers**. Each Dockerfile instruction creates one layer. Layers are identified by a SHA256 hash and cached locally.

```
┌────────────────────────────────┐
│  Layer 5: COPY . .             │  ← your app code
├────────────────────────────────┤
│  Layer 4: RUN npm install      │  ← node_modules
├────────────────────────────────┤
│  Layer 3: COPY package*.json . │  ← package.json
├────────────────────────────────┤
│  Layer 2: WORKDIR /app         │  ← directory created
├────────────────────────────────┤
│  Layer 1: FROM node:20-alpine  │  ← base image layers
└────────────────────────────────┘
```

When you run a container, Docker adds a **thin writable layer** on top. All writes go to this layer. The image layers beneath remain untouched.

### Layer caching

Docker caches each layer. When you rebuild:
- If a layer hasn't changed → **use the cache** (fast)
- If a layer changes → **rebuild that layer and all subsequent layers**

This is why layer order matters enormously in a Dockerfile.

### Inspecting layers

```bash
# Show image layers (history)
docker history node:20-alpine

# Show detailed layer info
docker inspect node:20-alpine | grep -A20 '"Layers"'

# Analyse image with dive (third-party tool)
brew install dive
dive node:20-alpine
```

---

## 2. The Dockerfile

A Dockerfile is a text file with instructions that Docker executes top-to-bottom to build an image.

### Complete Dockerfile reference

```dockerfile
# ── FROM ─────────────────────────────────────────
# Every Dockerfile starts with FROM.
# Specifies the base image.
FROM node:20-alpine

# ── LABEL ────────────────────────────────────────
# Metadata about the image
LABEL maintainer="you@example.com"
LABEL version="1.0"
LABEL description="My Node.js application"

# ── ARG ──────────────────────────────────────────
# Build-time variables (not available at runtime)
ARG NODE_ENV=production
ARG APP_PORT=3000

# ── ENV ──────────────────────────────────────────
# Runtime environment variables
ENV NODE_ENV=${NODE_ENV}
ENV PORT=${APP_PORT}
ENV PATH="/app/node_modules/.bin:${PATH}"

# ── WORKDIR ──────────────────────────────────────
# Sets the working directory for subsequent instructions
# Created if it doesn't exist
WORKDIR /app

# ── COPY ─────────────────────────────────────────
# Copies files from build context to image
# COPY <src> <dest>
COPY package*.json ./
COPY src/ ./src/

# ── ADD ──────────────────────────────────────────
# Like COPY, but also handles URLs and auto-extracts tar
# Prefer COPY unless you need ADD's extra features
ADD https://example.com/file.tar.gz /tmp/

# ── RUN ──────────────────────────────────────────
# Executes commands during build
# Each RUN creates a new layer
RUN npm ci --only=production

# Combine commands to reduce layers:
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# ── EXPOSE ───────────────────────────────────────
# Documents which port the app listens on
# Does NOT actually publish the port — use -p at runtime
EXPOSE 3000

# ── VOLUME ───────────────────────────────────────
# Declares a mount point for external volumes
VOLUME ["/app/data"]

# ── USER ─────────────────────────────────────────
# Switch to a non-root user (security best practice)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# ── HEALTHCHECK ──────────────────────────────────
# Docker will periodically run this to check if container is healthy
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:3000/health || exit 1

# ── ENTRYPOINT ───────────────────────────────────
# The command that always runs when the container starts
# Cannot be overridden by docker run arguments (only --entrypoint)
ENTRYPOINT ["node"]

# ── CMD ──────────────────────────────────────────
# Default arguments to ENTRYPOINT
# Can be overridden by docker run arguments
CMD ["server.js"]

# Together: container runs "node server.js"
# Override: docker run myapp index.js  →  runs "node index.js"
```

---

## 3. ENTRYPOINT vs CMD

This is the most commonly misunderstood part of Dockerfiles.

| | `CMD` | `ENTRYPOINT` |
|--|-------|-------------|
| Purpose | Default command/args | The executable to run |
| Override | Easy — just add args to `docker run` | Requires `--entrypoint` flag |
| Combined | CMD provides args to ENTRYPOINT | ENTRYPOINT is the command |

### Shell form vs exec form

```dockerfile
# Shell form — runs in /bin/sh -c (has shell features, but no signal handling)
CMD node server.js
ENTRYPOINT node server.js

# Exec form — runs directly (recommended — proper signal handling)
CMD ["node", "server.js"]
ENTRYPOINT ["node", "server.js"]
```

**Always use exec form** (`["executable", "arg"]`) for `ENTRYPOINT` and `CMD`. The shell form wraps the process in `/bin/sh -c`, which means `SIGTERM` signals (from `docker stop`) won't reach your application.

### Common patterns

```dockerfile
# Pattern 1: CMD only (typical for single-purpose images)
FROM node:20-alpine
CMD ["node", "server.js"]
# docker run myapp              → node server.js
# docker run myapp node other.js → node other.js

# Pattern 2: ENTRYPOINT + CMD (flexible images)
FROM node:20-alpine
ENTRYPOINT ["node"]
CMD ["server.js"]
# docker run myapp          → node server.js
# docker run myapp other.js → node other.js

# Pattern 3: ENTRYPOINT as wrapper script (most flexible)
COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["node", "server.js"]
```

---

## 4. A Real-World Node.js Dockerfile

### Naive Dockerfile (don't do this)

```dockerfile
FROM node:20
WORKDIR /app
COPY . .
RUN npm install
CMD ["node", "server.js"]
```

Problems:
- Based on full Debian image (~1GB)
- `node_modules` invalidates cache whenever any source file changes
- Runs as root
- Development dependencies included in production image

### Production-quality Dockerfile

```dockerfile
# Stage 1: Build
FROM node:20-alpine AS builder

WORKDIR /app

# Copy dependency files first (cache this layer)
COPY package*.json ./

# Install ALL deps (including dev) for building
RUN npm ci

# Copy source and build
COPY . .
RUN npm run build

# Stage 2: Production image
FROM node:20-alpine AS production

# Install dumb-init for proper signal handling
RUN apk add --no-cache dumb-init

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy only production deps
COPY package*.json ./
RUN npm ci --only=production && npm cache clean --force

# Copy built app from builder stage
COPY --from=builder /app/dist ./dist

# Own the files
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -qO- http://localhost:3000/health || exit 1

ENTRYPOINT ["dumb-init", "--"]
CMD ["node", "dist/server.js"]
```

---

## 5. Multi-Stage Builds

Multi-stage builds let you use multiple `FROM` instructions in one Dockerfile. Each stage starts fresh. You copy only what you need into the final image.

### Benefits
- **Smaller images** — build tools (compilers, test runners) don't end up in production
- **No separate build scripts** — everything in one Dockerfile
- **Cache optimisation** — builder stage cached independently

### Go application example

```dockerfile
# Build stage
FROM golang:1.22 AS builder

WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -o /app/myapp .

# Final stage — tiny scratch image
FROM scratch

COPY --from=builder /app/myapp /myapp
COPY --from=builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/

EXPOSE 8080
ENTRYPOINT ["/myapp"]
```

Result: A single-binary image of just ~10MB instead of ~1GB.

### Targeting a specific stage

```bash
# Build only the builder stage (useful for CI testing)
docker build --target builder -t myapp:test .

# Build the final production image
docker build --target production -t myapp:prod .
```

---

## 6. The `.dockerignore` File

Like `.gitignore`, `.dockerignore` tells Docker what to exclude from the build context.

```dockerignore
# Version control
.git
.gitignore

# Node.js
node_modules
npm-debug.log

# Testing
coverage/
.nyc_output

# Build output
dist/
build/

# Environment files
.env
.env.*

# Editor files
.vscode/
.idea/
*.swp

# OS files
.DS_Store
Thumbs.db

# Docker files (no need to include these in the image)
Dockerfile*
docker-compose*
```

**Why this matters:** The build context is the directory sent to the Docker daemon. Without `.dockerignore`, sending `node_modules/` (hundreds of MB) slows every build.

---

## 7. Image Tagging Strategy

```bash
# Semantic versioning
docker build -t myapp:1.2.3 .
docker tag myapp:1.2.3 myapp:1.2
docker tag myapp:1.2.3 myapp:1
docker tag myapp:1.2.3 myapp:latest

# Date-based
docker build -t myapp:2025-05-06 .

# Git commit SHA (immutable — great for CI)
docker build -t myapp:$(git rev-parse --short HEAD) .

# Environment-specific
docker build -t myapp:prod -f Dockerfile.prod .
docker build -t myapp:staging -f Dockerfile.staging .
```

> **Production tip:** Tag images with both `latest` AND the commit SHA. Use the SHA tag in Kubernetes/production deployments — it's immutable. `latest` can change unexpectedly.

---

## 8. Image Size Optimisation

### Choose the right base image

| Base | Size | Use when |
|------|------|----------|
| `ubuntu:22.04` | ~77MB | Need full system tools |
| `debian:slim` | ~75MB | Debian without extras |
| `alpine:3.19` | ~7MB | Minimal, musl libc |
| `distroless` | ~2MB | Production, no shell |
| `scratch` | 0 | Statically compiled apps (Go, Rust) |

```bash
# Compare sizes
docker images | grep node
# node:20          → 1.09GB
# node:20-slim     → 246MB
# node:20-alpine   → 128MB
```

### Reduce layer count

```dockerfile
# Bad — 3 layers, intermediate state pollutes image
RUN apt-get update
RUN apt-get install -y curl
RUN rm -rf /var/lib/apt/lists/*

# Good — 1 layer, cleanup in same command
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
```

### Don't reinstall deps unnecessarily

```dockerfile
# Bad — any file change rebuilds node_modules
COPY . .
RUN npm install

# Good — package.json changes rarely; cache node_modules layer
COPY package*.json ./
RUN npm install
COPY . .
```

---

## 9. Security Best Practices

```dockerfile
# 1. Use specific tags, never 'latest' in production
FROM node:20.12.1-alpine3.19   # pinned

# 2. Run as non-root
RUN addgroup -S app && adduser -S app -G app
USER app

# 3. Read-only filesystem where possible
# (set at runtime: docker run --read-only)

# 4. Scan for vulnerabilities
# docker scout quickview myapp:latest
# or: trivy image myapp:latest

# 5. Don't embed secrets in images
# Bad:
ENV API_KEY=secret123   # visible in docker history!
# Good: pass at runtime with -e or Docker Secrets

# 6. COPY only what you need
COPY src/ ./src/      # not: COPY . .
```

---

## 10. Useful Image Commands Summary

```bash
# Build
docker build -t myapp:v1 .
docker build --no-cache -t myapp:v1 .
docker build --target prod -t myapp:prod .

# Inspect
docker history myapp:v1
docker inspect myapp:v1
docker image ls

# Tag and push
docker tag myapp:v1 registry.example.com/myapp:v1
docker push registry.example.com/myapp:v1

# Pull with digest (immutable)
docker pull nginx@sha256:abc123...

# Remove
docker rmi myapp:v1
docker image prune -a
```

---

## Summary

| Concept | Key Points |
|---------|-----------|
| **Layers** | Read-only, cached, shared between images |
| **Build cache** | Changes invalidate the current layer and all below |
| **Layer order** | Copy deps before source code for better caching |
| **Multi-stage** | Build in one stage, produce minimal production image |
| **Base images** | Alpine for small; distroless/scratch for minimal attack surface |
| **ENTRYPOINT** | The fixed executable; always use exec form `["cmd"]` |
| **CMD** | Default arguments to ENTRYPOINT; overridable at runtime |
| **Security** | Non-root user, no secrets in ENV, pinned tags |

**Next:** [04 — Docker Compose →](./04-docker-compose.md)
