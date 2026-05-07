# Module 01 — Docker Commands

This module is your Docker command reference. You'll learn the essential commands for working with containers and images, with real examples and expected output for each.

---

## 1. Container Lifecycle Commands

### `docker run` — Create and start a container

```bash
docker run nginx
```

Pulls `nginx` from Docker Hub (if not local) and starts a container. The terminal attaches to the container's output. Press `Ctrl+C` to stop.

### `docker ps` — List running containers

```bash
docker ps
```
```
CONTAINER ID   IMAGE     COMMAND                  CREATED         STATUS         PORTS     NAMES
a1b2c3d4e5f6   nginx     "/docker-entrypoint.…"   2 minutes ago   Up 2 minutes   80/tcp    elegant_hopper
```

```bash
# List all containers (including stopped ones)
docker ps -a
docker ps --all

# Show only container IDs (useful for scripting)
docker ps -q
```

### `docker stop` — Gracefully stop a container

```bash
docker stop a1b2c3d4e5f6
# or use the name:
docker stop elegant_hopper
```

Sends `SIGTERM` and waits 10 seconds, then sends `SIGKILL`.

```bash
# Stop with custom timeout (seconds)
docker stop --time 30 a1b2c3d4e5f6
```

### `docker start` — Start a stopped container

```bash
docker start a1b2c3d4e5f6
```

### `docker restart` — Stop then start

```bash
docker restart a1b2c3d4e5f6
```

### `docker kill` — Force stop immediately

```bash
docker kill a1b2c3d4e5f6
# Sends SIGKILL immediately — no graceful shutdown
```

### `docker rm` — Remove a stopped container

```bash
docker rm a1b2c3d4e5f6

# Remove multiple containers
docker rm container1 container2 container3

# Remove a running container (force)
docker rm -f a1b2c3d4e5f6

# Remove all stopped containers
docker container prune

# One-liner: stop and remove
docker rm -f $(docker ps -aq)
```

---

## 2. Image Commands

### `docker images` — List local images

```bash
docker images
```
```
REPOSITORY   TAG       IMAGE ID       CREATED        SIZE
nginx        latest    a6bd71f48f68   2 weeks ago    187MB
node         20        3f5e8a123b45   3 weeks ago    1.09GB
ubuntu       22.04     1f6ddc1b2547   4 weeks ago    77.8MB
```

```bash
# List only image IDs
docker images -q
```

### `docker pull` — Download an image

```bash
docker pull ubuntu
docker pull ubuntu:22.04       # specific tag
docker pull ubuntu:22.04@sha256:abc123  # pin to digest (immutable)
```

### `docker push` — Upload an image to a registry

```bash
docker push yourname/myapp:v1.0
```

### `docker rmi` — Remove an image

```bash
docker rmi nginx
docker rmi a6bd71f48f68

# Remove all unused images
docker image prune

# Remove all images (including used ones — destructive)
docker image prune -a
```

### `docker build` — Build an image from a Dockerfile

```bash
docker build -t myapp:v1.0 .

# Build from a specific Dockerfile
docker build -f Dockerfile.prod -t myapp:prod .

# Build without using cache
docker build --no-cache -t myapp:v1.0 .
```

### `docker tag` — Create a tag alias

```bash
docker tag myapp:v1.0 yourname/myapp:v1.0
docker tag myapp:v1.0 myapp:latest
```

---

## 3. Inspect and Debug Commands

### `docker logs` — View container output

```bash
docker logs a1b2c3d4e5f6

# Follow log output in real time
docker logs -f a1b2c3d4e5f6

# Show last N lines
docker logs --tail 50 a1b2c3d4e5f6

# Show timestamps
docker logs -t a1b2c3d4e5f6

# Combine: follow with timestamps, last 20 lines
docker logs -ft --tail 20 a1b2c3d4e5f6
```

### `docker exec` — Run a command inside a running container

```bash
# Open an interactive bash shell
docker exec -it a1b2c3d4e5f6 bash

# Run a single command
docker exec a1b2c3d4e5f6 ls /app

# Run as a specific user
docker exec -u root -it a1b2c3d4e5f6 bash
```

`-i` = interactive (keep stdin open)
`-t` = allocate a pseudo-TTY (terminal)

> Use `sh` instead of `bash` for Alpine-based images — Alpine doesn't include bash by default.

### `docker inspect` — Detailed container/image info

```bash
docker inspect a1b2c3d4e5f6
```

Returns a large JSON object with every detail about the container. Useful with `--format`:

```bash
# Get the container's IP address
docker inspect --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' a1b2c3d4e5f6

# Get the image's environment variables
docker inspect --format='{{range .Config.Env}}{{println .}}{{end}}' myapp:v1.0
```

### `docker stats` — Live resource usage

```bash
# Watch all running containers
docker stats

# Watch a specific container
docker stats a1b2c3d4e5f6
```
```
CONTAINER ID   NAME        CPU %   MEM USAGE / LIMIT     MEM %   NET I/O
a1b2c3d4e5f6   web-app     0.07%   42.5MiB / 7.636GiB   0.54%   1.2kB / 0B
```

### `docker top` — Show processes inside a container

```bash
docker top a1b2c3d4e5f6
```
```
UID   PID    PPID   C   STIME   TTY   TIME       CMD
root  15230  15210  0   10:30   ?     00:00:00   nginx: master process
101   15265  15230  0   10:30   ?     00:00:00   nginx: worker process
```

### `docker diff` — Show filesystem changes in a container

```bash
docker diff a1b2c3d4e5f6
```
```
C /var                  # C = Changed
A /var/log/nginx        # A = Added
A /var/log/nginx/access.log
D /tmp/setup.sh         # D = Deleted
```

---

## 4. Copy and Export Commands

### `docker cp` — Copy files between host and container

```bash
# Copy from container to host
docker cp a1b2c3d4e5f6:/app/logs/error.log ./error.log

# Copy from host to container
docker cp ./config.json a1b2c3d4e5f6:/app/config.json
```

### `docker export` / `docker import` — Container filesystem as tar

```bash
# Export container filesystem
docker export a1b2c3d4e5f6 > mycontainer.tar

# Import as a new image
docker import mycontainer.tar myimage:v1.0
```

### `docker save` / `docker load` — Image as tar (with layers)

```bash
# Save image(s) to a tar file
docker save -o myapp.tar myapp:v1.0

# Load on another machine (without a registry)
docker load -i myapp.tar
```

> `export`/`import` = container filesystem only (flattens layers)
> `save`/`load` = full image with all layers and metadata

---

## 5. System Commands

### `docker info` — System-wide information

```bash
docker info
```
```
Client: Docker Engine - Community
Server:
 Containers: 5
  Running: 3
  Paused: 0
  Stopped: 2
 Images: 12
 Storage Driver: overlay2
 Total Memory: 7.636GiB
 ...
```

### `docker version`

```bash
docker version
```
```
Client: Docker Engine - Community
 Version:           25.0.3
 API version:       1.44
Server: Docker Engine - Community
 Engine:
  Version:          25.0.3
```

### `docker system df` — Disk usage

```bash
docker system df
```
```
TYPE            TOTAL   ACTIVE   SIZE      RECLAIMABLE
Images          12      5        3.847GB   2.1GB (54%)
Containers      5       3        1.2MB     456kB (38%)
Local Volumes   3       2        245MB     0B (0%)
Build Cache     15      0        1.2GB     1.2GB
```

### `docker system prune` — Remove unused resources

```bash
# Remove stopped containers, unused networks, dangling images
docker system prune

# Also remove unused images (not just dangling)
docker system prune -a

# Remove volumes too (⚠️ data loss)
docker system prune -a --volumes
```

---

## 6. Network Commands

```bash
# List networks
docker network ls

# Create a network
docker network create my-network

# Inspect a network
docker network inspect my-network

# Connect a running container to a network
docker network connect my-network a1b2c3d4e5f6

# Disconnect
docker network disconnect my-network a1b2c3d4e5f6

# Remove a network
docker network rm my-network
```

---

## 7. Volume Commands

```bash
# List volumes
docker volume ls

# Create a volume
docker volume create mydata

# Inspect a volume
docker volume inspect mydata

# Remove a volume
docker volume rm mydata

# Remove all unused volumes
docker volume prune
```

---

## 8. Quick Reference — Command Cheat Sheet

### Container management

| Command | What it does |
|---------|-------------|
| `docker run <image>` | Create and start a container |
| `docker ps` | List running containers |
| `docker ps -a` | List all containers |
| `docker stop <id>` | Gracefully stop |
| `docker start <id>` | Start stopped container |
| `docker restart <id>` | Restart |
| `docker rm <id>` | Remove stopped container |
| `docker rm -f <id>` | Force remove running container |
| `docker container prune` | Remove all stopped containers |

### Image management

| Command | What it does |
|---------|-------------|
| `docker images` | List local images |
| `docker pull <image>` | Download image |
| `docker build -t <name> .` | Build from Dockerfile |
| `docker tag <src> <dest>` | Tag an image |
| `docker rmi <image>` | Remove image |
| `docker image prune` | Remove dangling images |

### Debugging

| Command | What it does |
|---------|-------------|
| `docker logs -f <id>` | Follow container logs |
| `docker exec -it <id> bash` | Open shell |
| `docker inspect <id>` | Full JSON details |
| `docker stats` | Live resource usage |
| `docker top <id>` | Show processes |

---

## 9. Putting It Together — A Typical Session

```bash
# Pull and run nginx, detached, on port 8080
docker run -d -p 8080:80 --name my-nginx nginx

# Check it's running
docker ps

# Test in browser or curl
curl localhost:8080

# Watch the logs
docker logs -f my-nginx

# Open a shell inside
docker exec -it my-nginx bash
# Inside: ls /etc/nginx/

# Check resource usage
docker stats my-nginx

# Stop and remove
docker stop my-nginx
docker rm my-nginx
```

---

## Summary

Docker commands follow consistent patterns:
- `docker <object> <verb>` — e.g., `docker container ls`, `docker image rm`
- Older shorthand forms also work: `docker ps`, `docker images`, `docker rmi`
- Most commands accept container IDs or names
- You can abbreviate IDs to the first few unique characters

**Next:** [02 — Docker Run →](./02-docker-run.md)
