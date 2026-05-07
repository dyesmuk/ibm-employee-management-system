# Module 02 — Docker Run

`docker run` is the most used Docker command. This module covers every important option — ports, volumes, environment variables, resource limits, restart policies, and more.

---

## 1. The `docker run` Anatomy

```bash
docker run [OPTIONS] IMAGE [COMMAND] [ARGS...]
```

```bash
docker run -d -p 8080:80 --name web-server --restart unless-stopped nginx
#          │  │           │                 │                            │
#          │  │           │                 │                            └── image
#          │  │           │                 └── restart policy
#          │  │           └── container name
#          │  └── port mapping (host:container)
#          └── detached mode (run in background)
```

---

## 2. Foreground vs Background

### Attached (foreground) — default

```bash
docker run nginx
```

Your terminal is attached to the container's stdout/stderr. The container's output appears directly. `Ctrl+C` stops the container.

### Detached (background) — `-d`

```bash
docker run -d nginx
# a1b2c3d4e5f6789abcdef1234567890abcdef1234567890abcdef1234567890ab
```

Returns the container ID immediately. Container runs in the background.

### Interactive with TTY — `-it`

```bash
docker run -it ubuntu bash
root@a1b2c3d4:/# 
```

`-i` = keep stdin open
`-t` = allocate a pseudo-terminal

Used when you want a shell inside the container.

### Remove on exit — `--rm`

```bash
docker run --rm ubuntu echo "Hello"
# Container is automatically deleted when it exits
```

Perfect for one-off commands and testing.

---

## 3. Naming Containers

```bash
# Without --name, Docker assigns a random name (e.g., "quirky_hopper")
docker run -d nginx

# With --name, you control it
docker run -d --name web-server nginx

# Reference by name in other commands
docker stop web-server
docker logs web-server
docker exec -it web-server bash
```

Container names must be unique. Use descriptive names in production.

---

## 4. Port Mapping — `-p`

Containers have their own network namespace. To access a container's service from the host, map ports:

```bash
docker run -p HOST_PORT:CONTAINER_PORT image
```

```bash
# Map host port 8080 to container port 80
docker run -d -p 8080:80 nginx
curl localhost:8080   # works!

# Map to a specific host interface (secure — localhost only)
docker run -d -p 127.0.0.1:8080:80 nginx

# Map to a random available host port
docker run -d -p 80 nginx
docker port <container-id>   # find which port was assigned

# Map multiple ports
docker run -d -p 80:80 -p 443:443 nginx

# Map all exposed ports to random host ports
docker run -d -P nginx
```

### Checking port mappings

```bash
docker port web-server
# 80/tcp -> 0.0.0.0:8080
```

---

## 5. Environment Variables — `-e`

```bash
docker run -e MY_VAR=hello ubuntu env

# Multiple variables
docker run -e DB_HOST=localhost -e DB_PORT=5432 myapp

# From a file
docker run --env-file .env myapp
```

### The `.env` file format

```env
DB_HOST=postgres
DB_PORT=5432
DB_NAME=myapp
DB_USER=admin
DB_PASSWORD=secret123
```

```bash
docker run --env-file .env myapp
```

> ⚠️ Never pass secrets (passwords, API keys) via `-e` on shared machines — they appear in `docker inspect` and `docker history`. Use Docker Secrets for production.

### Example: Running a database with environment variables

```bash
docker run -d \
  --name postgres-db \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=secret \
  -e POSTGRES_DB=myapp \
  -p 5432:5432 \
  postgres:15
```

---

## 6. Volumes — `-v` and `--mount`

By default, everything inside a container is ephemeral — it disappears when the container is removed. Volumes persist data.

### Bind mounts — link a host directory to the container

```bash
docker run -v HOST_PATH:CONTAINER_PATH image
```

```bash
# Mount current directory into /app in the container
docker run -v $(pwd):/app node:20 node /app/server.js

# Mount a specific folder (read-only)
docker run -v $(pwd)/config:/app/config:ro nginx
```

**Use case:** Development — your code changes on the host are immediately visible inside the container.

### Named volumes — Docker manages the storage

```bash
# Create a named volume
docker volume create mydata

# Use it
docker run -v mydata:/var/lib/postgresql/data postgres

# Docker chooses where to store it on the host
docker volume inspect mydata
# "Mountpoint": "/var/lib/docker/volumes/mydata/_data"
```

**Use case:** Databases, persistent application data.

### `--mount` syntax (explicit, recommended for scripts)

```bash
docker run --mount type=bind,source=$(pwd),target=/app node:20 node server.js
docker run --mount type=volume,source=mydata,target=/data postgres
```

### Volume comparison

| Type | Syntax | Managed by | Use for |
|------|--------|-----------|---------|
| Bind mount | `-v /host/path:/container/path` | You | Dev: live code reload |
| Named volume | `-v mydata:/container/path` | Docker | Prod: persistent data |
| tmpfs | `--tmpfs /tmp` | Memory | Sensitive temp data |

---

## 7. Resource Limits

Prevent a container from consuming all system resources:

```bash
# Memory limit
docker run -m 512m nginx               # 512 megabytes
docker run --memory 1g nginx           # 1 gigabyte
docker run --memory 512m --memory-swap 1g nginx  # memory + swap

# CPU limits
docker run --cpus="1.5" nginx          # use at most 1.5 CPU cores
docker run --cpu-shares=512 nginx      # relative weight (default 1024)
docker run --cpuset-cpus="0,1" nginx   # pin to specific CPU cores

# Combine limits
docker run -d \
  --name web \
  --memory 512m \
  --cpus="0.5" \
  nginx
```

### Check limits

```bash
docker inspect web | grep -i memory
# "Memory": 536870912   ← 512 * 1024 * 1024
```

---

## 8. Restart Policies

Control what happens when a container exits:

| Policy | Behaviour |
|--------|-----------|
| `no` (default) | Never restart |
| `on-failure` | Restart only on non-zero exit code |
| `on-failure:5` | Restart up to 5 times on failure |
| `always` | Always restart (even after `docker stop`) |
| `unless-stopped` | Always restart unless explicitly stopped |

```bash
docker run -d --restart unless-stopped nginx

# Check policy
docker inspect nginx | grep RestartPolicy
```

> Use `unless-stopped` for production services. Use `on-failure` for tasks that should retry on crash but stop cleanly when asked.

---

## 9. Networking Modes

```bash
# Default bridge network (isolated, can talk to other containers)
docker run nginx

# Host network (shares host's network stack — no isolation)
docker run --network host nginx

# No networking
docker run --network none nginx

# User-defined network
docker network create mynet
docker run --network mynet nginx
```

### Container-to-container communication

```bash
# Create a network
docker network create app-net

# Run two containers on the same network
docker run -d --name db --network app-net postgres
docker run -d --name web --network app-net -e DB_HOST=db myapp

# 'web' can reach 'db' using the name 'db' as hostname
```

---

## 10. Other Useful Flags

```bash
# Set working directory inside container
docker run -w /app node:20 node server.js

# Override the entrypoint
docker run --entrypoint /bin/bash nginx

# Set hostname
docker run --hostname my-container ubuntu

# Run as specific user
docker run -u 1000:1000 myapp
docker run -u node myapp

# Add extra hosts to /etc/hosts
docker run --add-host=myhost:192.168.1.100 ubuntu

# Privileged mode (⚠️ gives full host access — avoid in production)
docker run --privileged myapp

# Read-only filesystem
docker run --read-only nginx

# Set ulimits
docker run --ulimit nofile=1024:1024 nginx

# Labels (metadata)
docker run --label env=prod --label team=backend myapp
```

---

## 11. Common Patterns

### One-off command execution

```bash
# Run a command in Ubuntu and delete the container
docker run --rm ubuntu cat /etc/os-release

# Run a Python script
docker run --rm -v $(pwd):/code python:3.12 python /code/script.py

# Use Node.js without installing it
docker run --rm -v $(pwd):/app -w /app node:20 npm install
```

### Database containers for local development

```bash
# PostgreSQL
docker run -d \
  --name dev-postgres \
  -e POSTGRES_PASSWORD=devpass \
  -e POSTGRES_DB=myapp \
  -p 5432:5432 \
  -v postgres-data:/var/lib/postgresql/data \
  postgres:15

# MySQL
docker run -d \
  --name dev-mysql \
  -e MYSQL_ROOT_PASSWORD=devpass \
  -e MYSQL_DATABASE=myapp \
  -p 3306:3306 \
  -v mysql-data:/var/lib/mysql \
  mysql:8

# Redis
docker run -d \
  --name dev-redis \
  -p 6379:6379 \
  redis:7-alpine
```

### Web server with custom config

```bash
docker run -d \
  --name nginx-prod \
  -p 80:80 \
  -p 443:443 \
  -v $(pwd)/nginx.conf:/etc/nginx/nginx.conf:ro \
  -v $(pwd)/certs:/etc/nginx/certs:ro \
  -v $(pwd)/html:/usr/share/nginx/html:ro \
  --restart unless-stopped \
  nginx
```

---

## 12. Inspecting Defaults

Every image has built-in defaults you can override:

```bash
# See what command the image runs by default
docker inspect nginx | grep -A5 '"Cmd"'
docker inspect nginx | grep -A5 '"Entrypoint"'

# See exposed ports
docker inspect nginx | grep -A5 '"ExposedPorts"'

# See default environment variables
docker inspect nginx | grep -A10 '"Env"'
```

---

## Summary

| Flag | Purpose | Example |
|------|---------|---------|
| `-d` | Detached mode | `docker run -d nginx` |
| `-it` | Interactive terminal | `docker run -it ubuntu bash` |
| `--rm` | Remove on exit | `docker run --rm ubuntu echo hi` |
| `--name` | Container name | `--name web` |
| `-p` | Port mapping | `-p 8080:80` |
| `-e` | Environment variable | `-e KEY=value` |
| `--env-file` | Env vars from file | `--env-file .env` |
| `-v` | Volume / bind mount | `-v data:/var/lib/mysql` |
| `-m` | Memory limit | `-m 512m` |
| `--cpus` | CPU limit | `--cpus="1.5"` |
| `--restart` | Restart policy | `--restart unless-stopped` |
| `--network` | Network mode | `--network mynet` |

**Next:** [03 — Docker Images →](./03-docker-images.md)
