# Docker — Hands-On Guide (Node.js)

---

## Step 1 — Hello World (Plain Node.js)

### The App

Create a folder called `hello-docker` and put this inside as `app.js`:

```js
const http = require('http');

const server = http.createServer((req, res) => {
  res.end('Hello from inside Docker!');
});

server.listen(3000, () => console.log('Server running on port 3000'));
```

No npm install, no dependencies, no framework. Just Node's built-in `http` module.

### The Dockerfile

```dockerfile
FROM node:18-alpine

WORKDIR /app

COPY app.js .

CMD ["node", "app.js"]
```

| Line | What it means |
|---|---|
| `FROM node:18-alpine` | Start from an existing image that has Node installed (alpine = tiny Linux) |
| `WORKDIR /app` | All commands from here run inside `/app` in the container |
| `COPY app.js .` | Copy your file from your laptop into the container |
| `CMD [...]` | The command to run when the container starts |

### Commands

```bash
# Build the image
docker build -t hello-docker .

# Run the container — map laptop port 3000 to container port 3000
docker run -p 3000:3000 hello-docker
```

Browser → `http://localhost:3000` → **Hello from inside Docker!**

```bash
# See running containers
docker ps

# Stop it
docker stop <container-id>
```

### The Mental Model

```
Your files          Image                    Container
(app.js +      →   (Node runtime +      →   (Running process,
Dockerfile)         your app, frozen)         isolated, alive)

   Recipe        Cake mould (reusable)       Actual cake
```

- `docker build` turns your files into an **image**
- `docker run` turns an image into a **container**

### Three Questions to Ask Trainees

**1. "What happens if you run `docker run -p 3000:3000 hello-docker` again in another terminal?"**
→ A second container starts. Same image, two independent running containers. This is how scaling works.

**2. "Can you change `app.js` and refresh the browser?"**
→ No. The change doesn't reflect. You have to `docker build` again. This teaches that an image is **immutable** — a snapshot, not a live folder.

**3. "Where is Node.js installed on your laptop?"**
→ It isn't (or it doesn't matter). Node lives *inside* the container. This is the isolation point — the whole runtime is bundled.

---

## Container Lifecycle

A stopped container still exists — it's just paused, not deleted.

```bash
# See all containers — including stopped ones
docker ps -a

# Start an existing stopped container
docker start <container-id>

# Start and see logs in the terminal
docker start -a <container-id>
```

| Command | What it does |
|---|---|
| `docker run` | Creates a **brand new** container from the image and starts it |
| `docker start` | Starts an **existing** (stopped) container |
| `docker stop` | Stops a running container (container still exists) |
| `docker rm` | **Deletes** the container permanently |

```
run → stop → start → stop → start ... → rm
```

> `docker run` is only used **once** per container. After that it's `start` and `stop`.

---

## Step 2 — Adding Express and Seeing Image Layers

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

### Updated Dockerfile

```dockerfile
FROM node:18-alpine

WORKDIR /app

COPY package.json .

RUN npm install

COPY app.js .

CMD ["node", "app.js"]
```

```bash
docker build -t hello-express .
docker run -p 3000:3000 hello-express
```

### Image Layers and Caching

Every line in the Dockerfile is a **layer**. Run the build a second time without changes:

```
 => CACHED [3/5] COPY package.json .
 => CACHED [4/5] RUN npm install        ← pulled from cache, not re-run!
 => CACHED [5/5] COPY app.js .
```

Now change one word in `app.js` and build again:

```
 => CACHED [4/5] RUN npm install        ← still cached!
 => [5/5] COPY app.js .                 ← only this re-runs
```

`npm install` is skipped because `package.json` didn't change.

### Why Order Matters — The Wrong Way

```dockerfile
# BAD ORDER — don't do this
COPY app.js .
COPY package.json .
RUN npm install
```

Every change to `app.js` now invalidates the `npm install` cache — it re-runs every build.

**The rule:** copy what changes least, first. Dependencies before source code — always.

---

## Step 3 — Docker Compose (Express + MongoDB)

### Folder Structure

```
hello-docker/
  ├── app.js
  ├── package.json
  ├── Dockerfile
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

> The connection string uses `mongo` as the hostname — not `localhost`. That is the **service name** defined in docker-compose.yml. Docker resolves it automatically.

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

  mongo:
    image: mongo:6
    volumes:
      - mongo-data:/data/db

volumes:
  mongo-data:
```

### Commands

```bash
# Start everything (builds image automatically if needed)
docker compose up

# Start in background
docker compose up -d

# Rebuild image and start (use after changing code)
docker compose up --build

# See running services
docker compose ps

# See logs
docker compose logs
docker compose logs app
docker compose logs mongo

# Stop (keeps data volumes)
docker compose down

# Stop and delete all data
docker compose down -v
```

Browser → `http://localhost:3000` → **Hello from Express! MongoDB: Connected ✅**

### Three Concepts Docker Compose Teaches

**1. Service name = hostname**
The app connects to `mongo:27017`. Inside Docker Compose, every service is reachable by its name. Docker's built-in DNS handles the resolution.

**2. Automatic private network**
Docker Compose creates a private network for all services automatically. Containers can talk to each other; your laptop cannot reach MongoDB directly unless you explicitly add a `ports` mapping for it.

**3. Volumes = persistence**
Stop and restart with `docker compose down` then `docker compose up` — MongoDB data is still there because it lives in the `mongo-data` volume, not inside the container. Use `down -v` to wipe it clean.

### The Teaching Question to Ask

> "What happens if you rename `mongo` to `database` in the yml but forget to update `app.js`?"

Connection fails. The hostname in the connection string must match the service name exactly.

---

## Step 4 — Docker Registry (Push & Pull)

### Step 4a — Create a Docker Hub account

Go to [hub.docker.com](https://hub.docker.com) and create a free account. Your username becomes part of every image name you publish.

### Step 4b — Login from terminal

```bash
docker login
```

Enter your Docker Hub username and password. You'll see `Login Succeeded`.

### Step 4c — Tag your image

Docker Hub requires images to be named as `username/imagename:tag`.

```bash
# Format: docker tag <local-image> <dockerhub-username>/<image-name>:<tag>
docker tag hello-express yourname/hello-express:1.0
```

Check it appeared:

```bash
docker images
```

You'll see both `hello-express` and `yourname/hello-express` — same image, two names (like an alias).

### Step 4d — Push to Docker Hub

```bash
docker push yourname/hello-express:1.0
```

You'll see each layer uploading. Layers that already exist on Docker Hub (like `node:18-alpine`) are skipped — only your app layers upload. Visit `hub.docker.com` in a browser — your image is now publicly visible.

### Step 4e — Pull and run from anywhere

This is the payoff. Delete your local image completely:

```bash
docker rmi yourname/hello-express:1.0
docker rmi hello-express
```

Now pull it back down — as if you're on a brand new machine:

```bash
docker pull yourname/hello-express:1.0
docker run -p 3000:3000 yourname/hello-express:1.0
```

Same result. No code, no Dockerfile, no Node installed — just Docker.

### Step 4f — Update docker-compose.yml to use the registry image

Once pushed, the `docker-compose.yml` can reference the registry image directly instead of building locally:

```yaml
version: '3'

services:

  app:
    image: yourname/hello-express:1.0    # ← pull from registry, no build needed
    ports:
      - "3000:3000"
    depends_on:
      - mongo

  mongo:
    image: mongo:6
    volumes:
      - mongo-data:/data/db

volumes:
  mongo-data:
```

Any machine with Docker can now run the full stack with just this `docker-compose.yml` — no source code required.

### The key insight

> "This is how deployment works in the real world. Your CI/CD pipeline builds the image, pushes it to a registry, and the production server just does `docker pull` and `docker run`. The server never sees your source code."

---

## Docker — Key Concepts Summary

### The Big Picture

Docker solves one problem: **"it works on my machine."** It packages your app and everything it needs to run into a single unit that behaves identically everywhere — your laptop, a teammate's machine, or a production server.

---

### Core Definitions

**Image**
A read-only snapshot of your app + its runtime + dependencies. Built once from a Dockerfile. Think of it as a frozen, portable template. An image never changes — if you need changes, you build a new one.

**Container**
A running instance of an image. Isolated from the host and from other containers. You can run many containers from the same image simultaneously. Lightweight because it shares the host OS kernel — unlike a VM which needs its own OS.

**Dockerfile**
A plain text script of instructions that tells Docker how to build an image. Each instruction creates a layer.

**Layer**
One step in the image build process. Docker caches each layer. If nothing changed in a layer, it reuses the cache on the next build — making rebuilds fast. Order matters: put things that change least at the top.

**Docker Hub (Registry)**
A cloud store for images. `docker push` uploads your image. `docker pull` downloads it. This is how images travel from your laptop to a server. Docker Hub is the default public registry — companies run private ones (AWS ECR, Google Artifact Registry, etc.).

**Docker Compose**
A tool for defining and running multi-container applications. You describe all your services, networks and volumes in one `docker-compose.yml` file and start everything with `docker compose up`.

**Volume**
A persistent storage location that lives outside the container. Data in a volume survives `docker stop` and `docker restart`. Without a volume, all data inside a container is lost when it's removed.

**Network**
Docker Compose creates a private network for your services automatically. Containers on the same network reach each other by service name (e.g. `mongo:27017`). Containers are invisible to the outside world unless you explicitly open a port with `ports:`.

**Port Mapping**
`-p 3000:3000` means: forward traffic from port 3000 on your laptop → port 3000 inside the container. Format is always `host:container`.

---

### Commands at a Glance

| What you want to do | Command |
|---|---|
| Build an image | `docker build -t name .` |
| Run a new container | `docker run -p 3000:3000 name` |
| See running containers | `docker ps` |
| See all containers | `docker ps -a` |
| Stop a container | `docker stop <id>` |
| Start it again | `docker start <id>` |
| Delete a container | `docker rm <id>` |
| See images | `docker images` |
| Delete an image | `docker rmi name` |
| Push to Docker Hub | `docker tag` then `docker push` |
| Pull from Docker Hub | `docker pull name:tag` |
| Start all services | `docker compose up` |
| Stop all services | `docker compose down` |
| Open shell in container | `docker exec -it <id> sh` |

---

### One-Line Distinctions (commonly confused)

| These seem similar... | But... |
|---|---|
| `docker run` vs `docker start` | `run` creates a new container. `start` restarts an existing one. |
| Image vs Container | Image is the blueprint. Container is the running instance. |
| `docker stop` vs `docker rm` | `stop` pauses it. `rm` deletes it. |
| Volume vs Bind mount | Volume is managed by Docker. Bind mount points to a folder on your laptop. |
| `CMD` vs `RUN` in Dockerfile | `RUN` executes at build time (e.g. `npm install`). `CMD` executes at container start. |
| Docker Compose vs Dockerfile | Dockerfile builds one image. Compose orchestrates many containers. |

---

### How Everything Connects

```
Dockerfile  →  docker build  →  Image  →  docker push  →  Docker Hub
                                  ↓
                            docker run
                                  ↓
                            Container(s)
                          ↙           ↘
                      Volume        Network
                  (persistent     (talks to other
                     data)          containers)
                          ↑
                   docker-compose.yml
                (defines all of the above)
```

---

## Docker Commands Reference

```bash
# Images
docker images                           # list all local images
docker pull mongo:6                     # download an image from Docker Hub
docker tag hello-express name/img:1.0   # tag an image for pushing
docker push name/hello-express:1.0      # push to Docker Hub
docker rmi hello-express                # delete a local image

# Containers
docker ps                               # running containers
docker ps -a                            # all containers including stopped
docker run -p 3000:3000 <image>         # create and start a container
docker run -d -p 3000:3000 <image>      # run in background (detached)
docker start <id>                       # start a stopped container
docker start -a <id>                    # start and attach to logs
docker stop <id>                        # stop a running container
docker rm <id>                          # delete a container
docker logs <id>                        # see container output
docker exec -it <id> sh                 # open a shell inside a container

# Cleanup
docker system prune                     # remove all stopped containers + unused images
docker volume ls                        # list volumes
docker volume rm <name>                 # delete a volume
```

---

## ToC Coverage Map

| Docker Topic | Covered in |
|---|---|
| Introduction | Step 1 — Hello World, mental model, the recipe analogy |
| Docker Commands | Commands Reference + every step throughout |
| Docker Run | Step 1 — port mapping, detached mode, run vs start |
| Docker Images | Step 2 — build, layers, caching, tag, `docker images` |
| Docker Compose | Step 3 — multi-container with MongoDB, networks, volumes |
| Docker Registry | Step 4 — tag, push to Docker Hub, pull, deploy via registry |
| Docker Engine, Storage and Networking | Volumes (Step 3), auto-networking (Step 3), Engine = runtime used throughout |
| Docker on Mac & Windows | Docker Desktop used throughout (Windows 11 laptops) |
| Container Orchestration — Docker Swarm & Kubernetes | Next phase — Kubernetes section |
| Conclusion | Key Concepts Summary — definitions, distinctions, how everything connects |

> **Next:** Kubernetes — take the `yourname/hello-express:1.0` image from Docker Hub and deploy it to a Kubernetes cluster using Pods, Deployments, and Services.
