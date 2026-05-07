# Docker — Course Materials

Practical, hands-on Docker courseware from containers to orchestration.

---

## Modules

| # | Module | Topics Covered |
|---|--------|---------------|
| [00](./00-introduction.md) | Introduction | Why containers, VMs vs containers, core concepts, architecture |
| [01](./01-docker-commands.md) | Docker Commands | Full CLI reference: run, ps, exec, logs, inspect, stats, prune |
| [02](./02-docker-run.md) | Docker Run | Ports, volumes, env vars, resource limits, restart policies, networking |
| [03](./03-docker-images.md) | Docker Images | Dockerfile, layering, multi-stage builds, optimisation, security |
| [04](./04-docker-compose.md) | Docker Compose | Multi-container apps, override files, health checks, CI/CD |
| [05](./05-docker-registry.md) | Docker Registry | Docker Hub, private registries, ECR/GCR/GHCR, image scanning |
| [06](./06-engine-storage-networking.md) | Engine, Storage, Networking | Daemon config, overlay2, volumes, CNI, bridge/overlay networking |
| [07](./07-docker-on-mac-windows.md) | Docker on Mac & Windows | Docker Desktop, WSL 2, Apple Silicon, multi-platform builds |
| [08](./08-orchestration-swarm-kubernetes.md) | Orchestration | Docker Swarm, stacks, services, rolling updates, Kubernetes intro |
| [09](./09-conclusion.md) | Conclusion | Production checklist, workflow recap, next steps |

---

## Prerequisites

- Basic Linux command line comfort
- A computer running macOS, Windows, or Linux
- Docker Desktop installed ([docker.com/products/docker-desktop](https://docker.com/products/docker-desktop))

---

## Quick Reference

```bash
# Build an image
docker build -t myapp:v1 .

# Run a container
docker run -d -p 8080:80 --name web nginx

# Multi-container stack
docker compose up -d

# See what's running
docker ps
docker compose ps

# Debug
docker logs -f web
docker exec -it web bash
docker stats

# Clean up
docker compose down
docker system prune -a
```
