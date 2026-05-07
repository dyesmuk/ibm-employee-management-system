# Module 09 — Conclusion

Congratulations on completing the Docker course. This module recaps what you've learned, gives you a practical mental model for applying Docker, and points you toward the next steps.

---

## 1. What You've Learned

| Module | Core Skills |
|--------|-------------|
| **00 Introduction** | Why containers exist, VMs vs containers, Docker architecture |
| **01 Commands** | Full docker CLI: run, ps, exec, logs, inspect, stats, prune |
| **02 Docker Run** | Ports, volumes, env vars, resource limits, restart policies |
| **03 Images** | Dockerfiles, layering, multi-stage builds, optimisation, security |
| **04 Docker Compose** | Multi-container apps, override files, health checks, dependencies |
| **05 Registry** | Docker Hub, private registries, ECR/GCR/GHCR, image scanning |
| **06 Engine, Storage, Networking** | Internals, overlay2, volume types, bridge/overlay/host networking |
| **07 Mac & Windows** | Docker Desktop, WSL 2, Apple Silicon, multi-platform builds |
| **08 Orchestration** | Docker Swarm, stacks, services, rolling updates, Kubernetes intro |

---

## 2. The Docker Mental Model

Think of Docker in three layers:

```
┌───────────────────────────────────────────────────────┐
│  ORCHESTRATION LAYER                                  │
│  Docker Swarm / Kubernetes                            │
│  → Runs containers across multiple machines           │
│  → Auto-healing, scaling, rolling updates             │
├───────────────────────────────────────────────────────┤
│  APPLICATION LAYER                                    │
│  Docker Compose                                       │
│  → Defines multi-service applications                 │
│  → One file, one command to run the whole stack       │
├───────────────────────────────────────────────────────┤
│  CONTAINER LAYER                                      │
│  Images + Containers                                  │
│  → Dockerfile builds the image                        │
│  → docker run starts the container                    │
│  → Registry distributes the image                     │
└───────────────────────────────────────────────────────┘
```

---

## 3. The Developer Workflow in Practice

### Day-to-day development

```bash
# Morning: start your dev stack
docker compose up -d

# During development: watch logs
docker compose logs -f api

# When you change something: rebuild
docker compose up -d --build api

# Run a one-off command (migrations, seeds)
docker compose exec api npm run migrate

# End of day: tear down
docker compose down
```

### Before committing / CI pipeline

```bash
# Build the production image
docker build -t myapp:$(git rev-parse --short HEAD) .

# Scan for vulnerabilities
trivy image myapp:$(git rev-parse --short HEAD)

# Run tests inside the container
docker run --rm myapp npm test

# Push to registry
docker push myapp:$(git rev-parse --short HEAD)
```

### Deployment

```bash
# Swarm
docker service update --image myapp:v2.0 web

# Kubernetes (covered in the Kubernetes course)
kubectl set image deployment/web web=myapp:v2.0
```

---

## 4. Production Checklist

Before running containers in production, verify:

**Security**
- [ ] Base image pinned to a specific version (not `latest`)
- [ ] Container runs as non-root user
- [ ] No secrets embedded in the image or environment variables in plain text
- [ ] Image scanned for vulnerabilities (Trivy, Docker Scout)
- [ ] Read-only filesystem where possible
- [ ] Resource limits set (CPU and memory)

**Reliability**
- [ ] Restart policy set (`unless-stopped` or orchestrator-managed)
- [ ] Health checks configured
- [ ] Logging driver configured (not just `json-file` for distributed apps)
- [ ] Persistent data in named volumes or external storage
- [ ] Graceful shutdown handled (SIGTERM caught, connections drained)

**Networking**
- [ ] Only necessary ports exposed
- [ ] Services on isolated networks where appropriate
- [ ] No containers using `--privileged` unless required

**Operations**
- [ ] Image tag strategy documented (SemVer + commit SHA)
- [ ] CI/CD pipeline builds and pushes images automatically
- [ ] Image retention policy set on the registry

---

## 5. Common Mistakes and How to Avoid Them

| Mistake | Fix |
|---------|-----|
| Using `latest` in production | Pin to specific tag or digest |
| Running as root | `USER` instruction + non-root user |
| `COPY . .` before `npm install` | Copy `package*.json` first for layer caching |
| No `.dockerignore` | Create one; exclude `node_modules`, `.git`, `.env` |
| Secrets in Dockerfiles or ENV | Use runtime secrets (`--secret`, Docker Secrets, K8s Secrets) |
| One giant `docker run` command | Use Docker Compose |
| Not setting resource limits | `--memory`, `--cpus`, or Compose `deploy.resources` |
| No health checks | `HEALTHCHECK` in Dockerfile |
| Ignoring image size | Multi-stage builds, Alpine base, `RUN` chain with cleanup |
| Bind-mounting `node_modules` on Mac | Use anonymous volume for `node_modules` |

---

## 6. Key Commands — Final Reference

```bash
# === IMAGES ===
docker build -t name:tag .              # build
docker pull image:tag                   # download
docker push name:tag                    # upload
docker images                           # list
docker rmi image                        # remove
docker image prune -a                   # clean unused

# === CONTAINERS ===
docker run -d -p 8080:80 --name c nginx # run detached
docker run -it ubuntu bash              # interactive
docker run --rm ubuntu echo hi          # ephemeral
docker ps / docker ps -a               # list
docker stop / docker start / docker rm  # lifecycle
docker exec -it c bash                  # shell into
docker logs -f c                        # follow logs
docker stats                            # resource usage
docker inspect c                        # full details

# === COMPOSE ===
docker compose up -d                    # start stack
docker compose down                     # stop stack
docker compose down -v                  # stop + remove volumes
docker compose logs -f                  # follow logs
docker compose exec svc bash           # shell into service
docker compose build                    # build images

# === SWARM ===
docker swarm init                       # create swarm
docker service create --replicas 3 ... # deploy service
docker service scale web=5             # scale
docker service update --image v2 web  # update
docker stack deploy -c compose.yml app # deploy stack

# === MAINTENANCE ===
docker system df                        # disk usage
docker system prune -a                  # full cleanup
docker volume prune                     # remove unused volumes
docker network prune                    # remove unused networks
```

---

## 7. Next Steps

### Kubernetes (the natural next step)

The **Kubernetes course** (included in this courseware bundle) takes you from local clusters to production deployments. Kubernetes is where Docker containers go to run at scale.

### Deepen Docker knowledge

- [Docker official documentation](https://docs.docker.com) — comprehensive, well-maintained
- [Play with Docker](https://labs.play-with-docker.com) — free browser-based Docker playground
- [Docker Samples](https://github.com/dockersamples) — official example apps
- [Awesome Docker](https://github.com/veggiemonk/awesome-docker) — curated tools and resources

### Ecosystem tools to explore

| Tool | What it does |
|------|-------------|
| **Portainer** | Web GUI for Docker/Swarm management |
| **Traefik** | Reverse proxy + automatic SSL for Docker |
| **Watchtower** | Auto-update containers when images change |
| **Trivy** | Vulnerability scanner |
| **Dive** | Inspect image layers |
| **ctop** | Top-like interface for containers |
| **lazydocker** | Terminal UI for Docker |

```bash
# Install useful tools
brew install trivy dive lazydocker

# ctop
docker run --rm -ti --name ctop \
  -v /var/run/docker.sock:/var/run/docker.sock:ro \
  quay.io/vektorlab/ctop
```

---

## Final Thoughts

Docker solves a real problem — making software run consistently from a developer's laptop to a production server. Once you internalise the image/container/registry model, and have Dockerfiles and Compose files as natural parts of your workflow, you'll wonder how you shipped software without it.

The path forward: take what you've learned here and apply it to a real project. Dockerise your current app, set up a Compose file for your local dev environment, build a CI pipeline that pushes images to a registry. The concepts become second nature through practice.

Then: Kubernetes awaits.
