# Module 00 — Introduction to Docker

Welcome to the **Docker** course. This course takes you from understanding why containers exist to running multi-container applications in production. Each module builds practical skills with real commands and real scenarios.

---

## Course Outline

| Module | Topic |
|--------|-------|
| 00 | Introduction (this module) |
| 01 | Docker Commands |
| 02 | Docker Run |
| 03 | Docker Images |
| 04 | Docker Compose |
| 05 | Docker Registry |
| 06 | Docker Engine, Storage and Networking |
| 07 | Docker on Mac & Windows |
| 08 | Container Orchestration — Docker Swarm & Kubernetes |
| 09 | Conclusion |

---

## 1. The Problem Docker Solves

### "It works on my machine"

Every developer has said it. The application runs perfectly locally but crashes in staging. The cause is almost always **environment differences** — different OS versions, different library versions, different environment variables, different file paths.

### The traditional solution: Virtual Machines

Virtual Machines (VMs) solved the consistency problem by packaging an entire OS alongside the application. But VMs are:
- **Heavy** — each VM includes a full OS (gigabytes)
- **Slow to start** — boot times measured in minutes
- **Resource-hungry** — each VM consumes a full OS's worth of RAM and CPU

```
VM Architecture:
┌──────────────────────────────────────────────────┐
│  App A   │  App B   │  App C                     │
├──────────┼──────────┼──────────                  │
│  Guest   │  Guest   │  Guest   (full OS each)     │
│  OS      │  OS      │  OS                         │
├──────────────────────────────────────────────────┤
│              Hypervisor                           │
├──────────────────────────────────────────────────┤
│              Host OS                             │
├──────────────────────────────────────────────────┤
│              Hardware                            │
└──────────────────────────────────────────────────┘
```

### The Docker solution: Containers

Containers share the host OS kernel but isolate the application's filesystem, processes, and network. The result:
- **Lightweight** — megabytes, not gigabytes
- **Fast** — start in milliseconds
- **Efficient** — dozens of containers on the same hardware as a few VMs

```
Container Architecture:
┌──────────────────────────────────────────────────┐
│  App A   │  App B   │  App C                     │
│  + Libs  │  + Libs  │  + Libs  (just app + deps) │
├──────────────────────────────────────────────────┤
│              Docker Engine                       │
├──────────────────────────────────────────────────┤
│              Host OS (shared kernel)             │
├──────────────────────────────────────────────────┤
│              Hardware                            │
└──────────────────────────────────────────────────┘
```

### VM vs Container comparison

| Feature | Virtual Machine | Container |
|---------|----------------|-----------|
| Startup time | Minutes | Milliseconds |
| Size | Gigabytes | Megabytes |
| OS | Full guest OS per VM | Shared host OS kernel |
| Isolation | Strong (hardware-level) | Process-level |
| Portability | Limited | Excellent |
| Density | ~10s per host | ~100s per host |

---

## 2. What is Docker?

Docker is an **open-source platform** for developing, shipping, and running applications in containers. It packages your application and all its dependencies into a standardised unit — the container — that runs identically everywhere.

Docker was released in 2013 and rapidly became the industry standard for containerisation. It consists of:

| Component | Role |
|-----------|------|
| **Docker Engine** | The daemon that builds and runs containers |
| **Docker CLI** | The `docker` command you type |
| **Docker Hub** | The public registry for sharing images |
| **Docker Compose** | Tool for defining multi-container apps |
| **Docker Desktop** | GUI application for Mac/Windows |

---

## 3. Core Concepts

### Image

A **Docker image** is a read-only template for creating containers. Think of it as a recipe or a blueprint. It contains:
- The application code
- Runtime (Node.js, Python, Java, etc.)
- Libraries and dependencies
- Configuration files
- Environment variables

Images are built in **layers**. Each instruction in a Dockerfile adds a layer. Layers are cached and reused — this makes builds fast.

### Container

A **container** is a running instance of an image. The relationship:

```
Image  ──►  Container(s)
(recipe)    (meals made from the recipe)
```

You can run many containers from the same image simultaneously.

### Dockerfile

A **Dockerfile** is a text file containing instructions to build an image. Every line is an instruction:

```dockerfile
FROM node:20-alpine          # start from an existing image
WORKDIR /app                 # set working directory
COPY package*.json ./        # copy dependency files
RUN npm install              # install dependencies
COPY . .                     # copy app source code
EXPOSE 3000                  # document the port
CMD ["node", "server.js"]    # command to run the container
```

### Registry

A **registry** is a storage and distribution system for Docker images. Docker Hub is the default public registry. Private registries (AWS ECR, GCR, etc.) store proprietary images.

### Docker Compose

**Docker Compose** is a tool for defining and running multi-container applications. A single `docker-compose.yml` file describes your entire application stack — web server, database, cache, queue — and brings it all up with one command.

---

## 4. The Docker Workflow

```
  Write       Build          Push            Pull & Run
Dockerfile ──► Image ──► Registry ──► Container (anywhere)

Developer    docker build   docker push    docker run
             docker tag     docker pull
```

1. **Develop** your app and write a Dockerfile
2. **Build** the image from the Dockerfile
3. **Push** the image to a registry (Docker Hub, etc.)
4. **Pull** the image on any machine — dev, CI, staging, production
5. **Run** containers from the image

Every machine with Docker installed can run any image. The container behaves identically everywhere.

---

## 5. Installing Docker

### Docker Desktop (Mac & Windows)

Download from [https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)

Docker Desktop includes:
- Docker Engine
- Docker CLI
- Docker Compose
- Kubernetes (optional)
- A GUI dashboard

### Linux

```bash
# Install using the convenience script (Ubuntu/Debian)
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add your user to the docker group (avoid sudo)
sudo usermod -aG docker $USER
newgrp docker

# Enable Docker to start on boot
sudo systemctl enable docker
sudo systemctl start docker
```

### Verify the installation

```bash
docker --version
# Docker version 25.0.3

docker run hello-world
```

Output:
```
Hello from Docker!
This message shows that your installation appears to be working correctly.
```

What happened:
1. Docker pulled the `hello-world` image from Docker Hub
2. Created a container from the image
3. The container ran, printed the message, and exited

---

## 6. Docker Architecture (Deep Dive)

Docker uses a **client-server architecture**:

```
┌─────────────────────────────────────────────────────┐
│  Docker Client (CLI)                                │
│  docker build / run / pull / push                   │
└───────────────────┬─────────────────────────────────┘
                    │ REST API (Unix socket or TCP)
┌───────────────────▼─────────────────────────────────┐
│  Docker Daemon (dockerd)                            │
│  - Manages images, containers, networks, volumes    │
│  - Listens for API requests                         │
└───────────────────┬─────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────┐
│  Container Runtime (containerd + runc)              │
│  - Actually creates and runs containers             │
│  - Uses Linux namespaces + cgroups                  │
└─────────────────────────────────────────────────────┘
```

### Linux primitives Docker uses

| Primitive | What it provides |
|-----------|-----------------|
| **Namespaces** | Process isolation (PID, network, mount, UTS, IPC) |
| **cgroups** | Resource limits (CPU, memory, I/O) |
| **Union filesystem** | Layered image filesystem (overlay2) |

> Docker didn't invent containers — Linux had the building blocks for years. Docker made them easy to use.

---

## 7. Use Cases

| Use Case | How Docker helps |
|----------|-----------------|
| **Microservices** | Each service in its own container, independently deployable |
| **CI/CD pipelines** | Consistent build environments across all pipeline stages |
| **Local development** | Run databases, queues, caches without installing them |
| **Testing** | Spin up isolated test environments instantly |
| **Legacy app migration** | Containerise and run without changing the host OS |
| **Multi-tenant SaaS** | Isolate customer workloads |

---

## Summary

- Containers are lightweight, fast, and portable — they solve the "works on my machine" problem
- An **image** is the blueprint; a **container** is the running instance
- A **Dockerfile** defines how to build an image
- Docker uses Linux **namespaces** and **cgroups** under the hood
- The Docker workflow: Write Dockerfile → Build image → Push to registry → Run anywhere

**Next:** [01 — Docker Commands →](./01-docker-commands.md)
