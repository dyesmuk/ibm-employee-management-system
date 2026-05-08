# Docker — Hands-On Guide (Node.js + Express)

> **Prerequisites:** You have completed the Git and GitHub module. Your `hello-git` project is version-controlled and pushed to GitHub.
>
> **Environment:** Windows 11, Docker Desktop installed. All commands run in **Windows Terminal (PowerShell)**.

---

## The Bridge from Git to Docker

In Git you learned to track and share your source code. But sharing code is only half the problem — the other half is making it *run* the same way everywhere.

| Git solves | Docker solves |
|---|---|
| "Which version of the code is this?" | "Which version of Node, OS, libraries?" |
| Code travels via GitHub | App + runtime travels via Docker Hub |
| `git clone` → you have the source | `docker pull` → you have the running environment |
| `.gitignore` excludes `node_modules` | Docker image *includes* everything needed to run |

The two tools are complementary:

```
Git/GitHub                          Docker/Docker Hub
──────────────────────────          ──────────────────────────
Tracks source code changes          Packages the runtime environment
Developer pushes code               CI/CD builds and pushes image
Team member clones to contribute    Server pulls image to deploy
"Here's the recipe"                 "Here's the sealed, ready meal"
```

The complete workflow you will build across all modules:

```
git push → GitHub → Jenkins → docker build → docker push → Docker Hub
                                                                  ↓
                                                      Server: docker pull & run
                                                      (or Kubernetes pulls it)
```

---

## Step 1 — Hello World (Plain Node.js)

### Start the project with Git

```powershell
mkdir hello-docker
cd hello-docker
git init
```

Create `.gitignore` **before** writing any code — always:

```
node_modules/
.env
*.log
dist/
```

Create `app.js`:

```js
const http = require('http');

const server = http.createServer((req, res) => {
  res.end('Hello from inside Docker!');
});

server.listen(3000, () => console.log('Server running on port 3000'));
```

### The Dockerfile

```dockerfile
FROM node:18-alpine

WORKDIR /app

COPY app.js .

CMD ["node", "app.js"]
```

| Line | What it means |
|---|---|
| `FROM node:18-alpine` | Start from an existing image with Node 18. `alpine` = tiny Linux (~5MB base) |
| `WORKDIR /app` | All subsequent commands run inside `/app` in the container |
| `COPY app.js .` | Copy your file from your Windows machine into the container image |
| `CMD [...]` | The command to run when the container starts |

### Build and run

```powershell
docker build -t hello-docker .
docker run -p 3000:3000 hello-docker
```

Browser → `http://localhost:3000` → **Hello from inside Docker!**

```powershell
docker ps
docker stop <container-id>
```

> **Docker Desktop tip:** You can see running containers, stop them, and view logs in Docker Desktop's **Containers** panel — a useful visual companion while learning commands.

### Commit the Dockerfile to Git

```powershell
git add .
git commit -m "Add Dockerfile for hello-docker"
```

The Dockerfile belongs in the repository — just like `package.json`. Anyone who clones the repo can build the identical image.

### The Mental Model

```
Your files          Image                    Container
(app.js +      →   (Node runtime +      →   (Running process,
Dockerfile)         your app, frozen)         isolated, alive)

   Recipe        Cake mould (reusable)       Actual cake
```

- `docker build` turns your files into an **image** — like a Git commit, immutable once created
- `docker run` turns an image into a **container**

### Three Questions to Ask Trainees

**1. "What happens if you run `docker run -p 3000:3000 hello-docker` again in another terminal?"**
→ A second container starts. Same image, two independent running processes. This is how scaling works.

**2. "Can you change `app.js` and refresh the browser?"**
→ No. You must `docker build` again. An image is a fixed snapshot — like a Git commit, not a live folder.

**3. "Where is Node.js installed on your laptop?"**
→ It isn't (or it doesn't matter). Node lives *inside* the container. The whole runtime is bundled.

---

## Container Lifecycle

A stopped container still exists — it is paused, not deleted.

```powershell
docker ps -a                    # all containers including stopped
docker start <container-id>     # restart a stopped container
docker start -a <container-id>  # restart and attach to logs
```

| Command | What it does |
|---|---|
| `docker run` | Creates a **new** container from the image and starts it |
| `docker start` | Starts an **existing** stopped container |
| `docker stop` | Stops a running container (still exists) |
| `docker rm` | Permanently deletes a container |

```
run → stop → start → stop → start ... → rm
```

> `docker run` is used only **once** per container. After that it's `start` and `stop`.

---

## Step 2 — Adding Express and Understanding Image Layers

### Updated `app.js`

```js
const express = require('express');
const app = express();

app.get('/', (req, res) => {
  res.send('Hello from Express inside Docker!');
});

app.listen(3000, () => console.log('Server running on port 3000'));
```

### `package.json`

```json
{
  "name": "hello-docker",
  "version": "1.0.0",
  "main": "app.js",
  "dependencies": {
    "express": "4.18.2"
  }
}
```

### `.dockerignore` — Docker's equivalent of `.gitignore`

Create `.dockerignore` in the project root:

```
node_modules
.git
.gitignore
*.log
.env
```

**Why this matters:** Without `.dockerignore`, `COPY . .` copies your local `node_modules` (built for Windows) into the Linux container — breaking the app and bloating the image. Always create `.dockerignore` alongside `.gitignore`.

| File | Tells | What to exclude |
|---|---|---|
| `.gitignore` | Git | Don't track these files in version control |
| `.dockerignore` | Docker | Don't include these in the build context |
| Both should exclude | `node_modules`, `.env`, logs | For the same reason — generated, OS-specific, or secret |

### Updated Dockerfile

```dockerfile
FROM node:18-alpine

WORKDIR /app

COPY package.json .

RUN npm install

COPY . .

CMD ["node", "app.js"]
```

```powershell
docker build -t hello-express .
docker run -p 3000:3000 hello-express
```

### Image Layers and Caching

Every instruction in the Dockerfile is a **layer**. Run the build a second time without changes:

```
 => CACHED [3/5] COPY package.json .
 => CACHED [4/5] RUN npm install        ← from cache, not re-run
 => CACHED [5/5] COPY . .
```

Now change one word in `app.js` and build again:

```
 => CACHED [4/5] RUN npm install        ← still cached, package.json unchanged
 => [5/5] COPY . .                      ← only this re-runs
```

**The rule:** copy what changes least, first. `package.json` rarely changes. `app.js` changes constantly. Dependencies before source code — always.

### The Wrong Order

```dockerfile
# BAD — every app.js change re-runs npm install
COPY . .
RUN npm install
```

### Commit to Git

```powershell
git add .
git commit -m "Add Express, package.json, .dockerignore"
git push
```

---

## Step 3 — Docker Engine, Storage and Networking

### Docker Engine Architecture

```
Docker CLI (docker commands you type)
        ↓  REST API calls
Docker Daemon (dockerd — background service)
        ↓  delegates to
containerd → runc (actually starts containers)

On Windows 11: Docker Desktop runs a WSL2 Linux VM.
The daemon runs inside that VM.
Your PowerShell terminal talks to it via a named pipe.
```

| Component | Role |
|---|---|
| **Docker CLI** | The `docker` command — sends requests to the daemon |
| **Docker Daemon** (`dockerd`) | Background service that manages everything |
| **containerd** | Lower-level runtime that manages container lifecycle |
| **runc** | The actual process that starts containers using Linux namespaces and cgroups |
| **REST API** | Interface between CLI and daemon — also used by Docker Desktop UI |

### Storage — Volumes vs Bind Mounts

**Volumes** (Docker-managed — recommended for production):

```powershell
# Create a named volume
docker volume create mydata

# Use it when running a container
docker run -v mydata:/data hello-express

# List volumes
docker volume ls

# Inspect a volume
docker volume inspect mydata

# Delete a volume
docker volume rm mydata
```

Volumes live inside Docker's storage area (in the WSL2 VM on Windows). Docker manages them fully.

**Bind Mounts** (maps a Windows folder — useful for development):

```powershell
# Map current folder into container (live reload during development)
docker run -p 3000:3000 -v ${PWD}:/app hello-express
```

The container reads your actual Windows files in real time. Edit `app.js` → the container immediately sees the change.

| | Volume | Bind Mount |
|---|---|---|
| Storage location | Docker-managed (inside WSL2) | Your Windows folder |
| Portability | ✅ Works everywhere | ❌ Tied to Windows paths |
| Performance on Windows | Faster | Slower (cross-VM filesystem) |
| Use case | Databases, production data | Development — live code editing |
| Survives `docker rm` | ✅ Yes | ✅ Yes (it's your files) |

### Networking

```powershell
# List all networks
docker network ls

# Create a custom bridge network
docker network create my-network

# Run containers on the same network (they can talk by name)
docker run -d --network my-network --name mongo mongo:6
docker run -d --network my-network --name app -p 3000:3000 hello-express

# Inspect — see which containers are connected
docker network inspect my-network
```

**Default Docker networks:**

| Network | Type | Description |
|---|---|---|
| `bridge` | Bridge | Default. Containers get private IPs. Communicate by IP (not name). |
| `host` | Host | Container shares host network stack. Not available on Windows. |
| `none` | None | Complete network isolation. |
| Custom bridge | Bridge | Like `bridge` but containers can talk **by name** — Docker provides DNS. |

Docker Compose always creates a **custom bridge network** automatically — which is why service names work as hostnames (`mongo:27017`).

---

## Step 4 — Docker Compose (Express + MongoDB)

### Folder Structure

```
hello-docker\
  ├── app.js
  ├── package.json
  ├── Dockerfile
  ├── .dockerignore
  ├── .gitignore
  └── docker-compose.yml
```

### Updated `package.json`

```json
{
  "name": "hello-docker",
  "version": "1.0.0",
  "main": "app.js",
  "dependencies": {
    "express": "4.18.2",
    "mongoose": "7.6.3"
  }
}
```

### Updated `app.js`

```js
const express = require('express');
const mongoose = require('mongoose');

const app = express();

mongoose.connect('mongodb://mongo:27017/hellodb')
  .then(() => console.log('Connected to MongoDB'))
  .catch(err => console.error('MongoDB error:', err));

app.get('/', (req, res) => {
  const status = mongoose.connection.readyState === 1 ? 'Connected ✅' : 'Disconnected ❌';
  res.send(`Hello from Express! MongoDB: ${status}`);
});

app.listen(3000, () => console.log('Server running on port 3000'));
```

> The hostname `mongo` in the connection string is the **service name** in `docker-compose.yml`. Docker's built-in DNS resolves it.

### `docker-compose.yml`

```yaml
version: '3'

services:

  app:
    build: .
    ports:
      - "3000:3000"
    depends_on:
      - mongo
    environment:
      - NODE_ENV=production

  mongo:
    image: mongo:6
    volumes:
      - mongo-data:/data/db

volumes:
  mongo-data:
```

### Commands

```powershell
docker compose up -d                  # start in background
docker compose up --build -d          # rebuild image then start
docker compose ps                     # see service status
docker compose logs                   # logs from all services
docker compose logs -f app            # follow logs from app service
docker compose down                   # stop, keep volumes
docker compose down -v                # stop, delete volumes (wipes MongoDB)
```

Browser → `http://localhost:3000` → **Hello from Express! MongoDB: Connected ✅**

### Three Concepts Docker Compose Teaches

**1. Service name = hostname** — `mongo:27017` works because Compose puts both containers on a custom bridge network and registers DNS entries by service name.

**2. Automatic private network** — No manual `docker network create`. Compose handles it. The outside world can only reach what you explicitly map with `ports:`.

**3. Volumes = persistence** — The `mongo-data` volume outlives containers. `docker compose down` then `up` — data is still there. `down -v` wipes it.

### Commit and push

```powershell
git add .
git commit -m "Add docker-compose with MongoDB"
git push
```

---

## Step 5 — Docker Registry (Push to Docker Hub)

### Why a registry?

```
Git workflow:     code → git push → GitHub     → team/CI clones
Docker workflow:  image → docker push → Docker Hub → server/CI pulls
```

Both GitHub and Docker Hub serve the same purpose for their domain — a central place to store, share, and distribute artefacts.

### Login

```powershell
docker login
```

> Or sign in from Docker Desktop (top-right avatar) — automatically authenticates `docker push`.

### Tag and push

```powershell
docker tag hello-express yourname/hello-express:1.0
docker push yourname/hello-express:1.0
```

### Pull and run from anywhere

```powershell
docker rmi yourname/hello-express:1.0
docker pull yourname/hello-express:1.0
docker run -p 3000:3000 yourname/hello-express:1.0
```

No source code. No Node install. Just Docker. This is the deployment model.

### Update Compose to use registry image

```yaml
services:
  app:
    image: yourname/hello-express:1.0    # pull from registry
    ports:
      - "3000:3000"
    depends_on:
      - mongo
```

> "The CI/CD pipeline builds the image, pushes it. The production server only needs `docker pull` and `docker run`. The server never sees your source code."

---

## Step 6 — Docker Images Deep Dive

```powershell
docker history hello-express           # see all layers
docker inspect hello-express           # full metadata as JSON
docker image prune                     # remove dangling (untagged) images
docker image prune -a                  # remove all unused images
```

### Multi-stage builds

```dockerfile
# Stage 1 — install dependencies
FROM node:18-alpine AS builder
WORKDIR /app
COPY package.json .
RUN npm install --production

# Stage 2 — lean runtime image (only copies what's needed)
FROM node:18-alpine
WORKDIR /app
COPY --from=builder /app/node_modules ./node_modules
COPY app.js .
CMD ["node", "app.js"]
```

The final image contains no build tools, no npm cache, no dev dependencies — only the runtime.

### Base image comparison

| Base image | Approx. size | Notes |
|---|---|---|
| `node:18` | ~950MB | Full Debian — maximum compatibility |
| `node:18-slim` | ~200MB | Debian, fewer packages |
| `node:18-alpine` | ~50MB | ✅ Recommended — Alpine Linux, production-ready |

---

## Step 7 — Container Orchestration: Docker Swarm and Kubernetes

### The scaling problem

Docker Compose runs containers on **one machine**. When load grows:
- One machine has limits
- If it crashes, everything crashes
- Updates require downtime

**Orchestration** distributes containers across a **cluster** of machines, restarts failed containers, and enables zero-downtime updates.

### Docker Swarm

Docker Swarm is Docker's built-in clustering system — built into Docker Engine, zero extra install.

```powershell
# Initialise a swarm on this machine (becomes manager node)
docker swarm init

# Deploy a stack (multi-host equivalent of docker compose up)
docker stack deploy -c docker-compose.yml hello-stack

# See services in the stack
docker stack services hello-stack

# Scale a service to 5 replicas
docker service scale hello-stack_app=5

# See running tasks (containers across all nodes)
docker service ps hello-stack_app

# Remove the stack
docker stack rm hello-stack

# Leave the swarm
docker swarm leave --force
```

**Core Swarm concepts:**

| Concept | Meaning |
|---|---|
| **Swarm** | A cluster of Docker hosts managed as one |
| **Manager node** | Schedules work, maintains desired state |
| **Worker node** | Runs containers assigned by the manager |
| **Service** | Declares "run N replicas of this image" — the Swarm deployment unit |
| **Stack** | A group of services from a Compose file, deployed to the Swarm |
| **Task** | One running container instance of a service |

### Docker Swarm vs Kubernetes

| Feature | Docker Swarm | Kubernetes |
|---|---|---|
| Setup | Simple — built into Docker | Complex — separate install |
| Learning curve | Low | Steep |
| Features | Basic orchestration | Full-featured — autoscaling, RBAC, CRDs |
| Ecosystem | Shrinking | Dominant — industry standard |
| Use case | Small teams, simple deployments | Production at any scale |
| CLI | `docker stack`, `docker service` | `kubectl` |

**For this training:** Swarm is covered to complete the Docker curriculum. **Kubernetes** is the industry standard and the subject of the next full module.

### The progression

```
Docker Compose        →   Docker Swarm        →   Kubernetes
(one machine,             (multiple machines,      (multiple machines,
 development)              simple orchestration)    full production platform)

Same image works in all three.
The orchestration layer changes. The container does not.
```

---

## Docker — Key Concepts Summary

### The Big Picture

Docker solves **"it works on my machine"** by packaging the app, its runtime, and its dependencies into a single portable image. That image runs identically on your Windows laptop, a Linux CI server, and a Kubernetes cluster in the cloud.

---

### Core Definitions

**Image** — Read-only snapshot: app + runtime + dependencies. Built from a Dockerfile. Immutable.

**Container** — Running instance of an image. Isolated, lightweight, ephemeral.

**Dockerfile** — Build script. Each instruction = one layer. Order matters for caching.

**Layer** — One build step, cached. Unchanged layers reuse cache — fast rebuilds.

**Docker Hub** — Default public registry. `push` uploads, `pull` downloads.

**Docker Compose** — Defines multi-container apps in `docker-compose.yml`. Creates private network automatically.

**Volume** — Persistent Docker-managed storage. Survives container deletion.

**Bind Mount** — Maps a host folder into a container. Used in development.

**Docker Engine** — The daemon managing all containers. CLI talks to it via REST API.

**Docker Swarm** — Built-in multi-host orchestration. Simpler but less powerful than Kubernetes.

**.dockerignore** — Excludes files from the build context. Always exclude `node_modules`, `.env`, `.git`.

---

### Commands at a Glance

| What you want to do | Command |
|---|---|
| Build an image | `docker build -t name .` |
| Run a new container | `docker run -p 3000:3000 name` |
| Run in background | `docker run -d -p 3000:3000 name` |
| See running containers | `docker ps` |
| See all containers | `docker ps -a` |
| Stop a container | `docker stop <id>` |
| Start a stopped container | `docker start <id>` |
| Delete a container | `docker rm <id>` |
| See images | `docker images` |
| Delete an image | `docker rmi name` |
| See image layers | `docker history name` |
| Shell inside container | `docker exec -it <id> sh` |
| See logs | `docker logs <id>` |
| Tag for push | `docker tag name yourname/name:1.0` |
| Push to registry | `docker push yourname/name:1.0` |
| Pull from registry | `docker pull name:tag` |
| Start Compose | `docker compose up -d` |
| Stop Compose | `docker compose down` |
| List volumes | `docker volume ls` |
| List networks | `docker network ls` |
| Init Swarm | `docker swarm init` |
| Deploy stack | `docker stack deploy -c file.yml stackname` |
| Scale service | `docker service scale stack_svc=5` |

---

### One-Line Distinctions (commonly confused)

| These seem similar... | But... |
|---|---|
| `docker run` vs `docker start` | `run` creates a new container. `start` restarts an existing stopped one. |
| Image vs Container | Image is the frozen blueprint. Container is the live running instance. |
| `docker stop` vs `docker rm` | `stop` pauses (still exists). `rm` deletes permanently. |
| Volume vs Bind mount | Volume = Docker-managed, portable. Bind mount = your Windows folder, dev-only. |
| `RUN` vs `CMD` in Dockerfile | `RUN` executes at build time. `CMD` executes at container start. |
| `COPY` vs `ADD` | `COPY` copies files. `ADD` can also extract archives. Use `COPY` by default. |
| `.gitignore` vs `.dockerignore` | `.gitignore` → Git excludes. `.dockerignore` → Docker build context excludes. Both exclude `node_modules`. |
| Docker Compose vs Swarm | Compose = single host. Swarm = multi-host cluster. Same file format, different commands. |
| Docker Swarm vs Kubernetes | Swarm = simple, built-in. Kubernetes = powerful, industry standard. |

---

### How Everything Connects

```
.gitignore + .dockerignore  ← exclude node_modules from both Git and Docker
        ↓
Source code pushed to GitHub   ←  git push
        ↓ (CI/CD pipeline pulls it)
docker build                   ←  Dockerfile + app source
        ↓
Image (frozen, versioned)
        ↓
docker push                    →  Docker Hub (registry)
        ↓                              ↓
docker-compose.yml             docker pull + run
(local dev, single host)       (server, CI, Kubernetes)
        ↓
Docker Swarm                   ←  multi-host, simple
        ↓
Kubernetes                     ←  multi-host, production (next module)
```

---

## Docker Commands Reference

```powershell
# Images
docker images
docker pull mongo:6
docker tag hello-express yourname/hello-express:1.0
docker push yourname/hello-express:1.0
docker rmi hello-express
docker history hello-express
docker inspect hello-express
docker image prune
docker image prune -a

# Containers
docker ps
docker ps -a
docker run -p 3000:3000 <image>
docker run -d -p 3000:3000 <image>
docker run -d -p 3000:3000 -v mydata:/data <image>
docker start <id>
docker start -a <id>
docker stop <id>
docker rm <id>
docker logs <id>
docker logs -f <id>
docker exec -it <id> sh
docker inspect <id>

# Volumes
docker volume ls
docker volume create mydata
docker volume inspect mydata
docker volume rm mydata
docker volume prune

# Networks
docker network ls
docker network create my-net
docker network inspect my-net
docker network rm my-net

# Compose
docker compose up -d
docker compose up --build -d
docker compose down
docker compose down -v
docker compose ps
docker compose logs
docker compose logs -f app

# Swarm
docker swarm init
docker swarm leave --force
docker stack deploy -c docker-compose.yml mystack
docker stack services mystack
docker stack ps mystack
docker stack rm mystack
docker service ls
docker service ps mystack_app
docker service scale mystack_app=5
docker node ls

# Cleanup
docker system prune
docker system prune -a
docker volume prune
```

---

## ToC Coverage Map

| Docker Topic | Covered in |
|---|---|
| Introduction | Bridge from Git + Step 1 — Hello World, mental model, Git-first workflow |
| Docker Commands | Commands Reference + all steps throughout |
| Docker Run | Step 1 — `docker run`, port mapping, detached mode, container lifecycle |
| Docker Images | Step 2 + Step 6 — layers, caching, layer order, multi-stage, base image sizes |
| Docker Compose | Step 4 — multi-container app, service DNS, volumes, private network |
| Docker Registry | Step 5 — Docker Hub, tag, push, pull, update Compose to use registry image |
| Docker Engine, Storage and Networking | Step 3 — Engine architecture, volumes vs bind mounts table, network types |
| Docker on Mac & Windows | Windows 11 + Docker Desktop throughout. `.dockerignore` for Windows paths. WSL2 explained in Engine section. |
| Container Orchestration — Docker Swarm & Kubernetes | Step 7 — Swarm commands, concepts, Swarm vs Kubernetes comparison, progression diagram |
| Conclusion | Key Concepts Summary — all definitions, distinctions table, full connection diagram |

> **Next:** Kubernetes — take `yourname/hello-express:1.0` from Docker Hub and deploy it to a Kubernetes cluster with self-healing Deployments, Services, and zero-downtime rolling updates.
