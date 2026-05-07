# Module 07 — Docker on Mac & Windows

Docker was built for Linux. On macOS and Windows, Docker Desktop creates a transparent Linux virtual machine to run containers. This module explains how that works, the differences you'll encounter, and how to get the best performance.

---

## 1. Why a VM is Needed

Docker containers use Linux kernel features (namespaces, cgroups). macOS and Windows don't have these features. Docker Desktop solves this by:

1. Running a lightweight Linux VM silently in the background
2. Forwarding Docker CLI commands from your Mac/Windows terminal to the Linux VM
3. Managing networking and filesystem sharing between your machine and the VM

```
macOS / Windows
┌─────────────────────────────────────────────────────┐
│  Your terminal: docker run nginx                    │
│  Docker Desktop GUI                                 │
└──────────────────────────┬──────────────────────────┘
                           │ Docker socket
┌──────────────────────────▼──────────────────────────┐
│  Linux VM (HyperKit/Hyper-V/Apple Virtualization)   │
│  ┌────────────────────────────────────────────────┐ │
│  │  Docker Engine (daemon)                        │ │
│  │  Containers run here                           │ │
│  └────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
```

---

## 2. Docker Desktop

### Installation

**macOS:**
1. Download from [docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)
2. Open the `.dmg` and drag to Applications
3. Launch Docker Desktop from Applications — the whale icon appears in the menu bar
4. Accept the licence agreement
5. Docker starts the Linux VM automatically

**Windows:**
1. Download the installer from the same URL
2. Ensure **WSL 2** or **Hyper-V** is enabled (installer checks this)
3. Run the installer — may require a restart
4. Docker Desktop appears in the system tray after reboot

### Verifying

```bash
docker --version
docker compose version
docker run hello-world
```

### Docker Desktop features

| Feature | Description |
|---------|-------------|
| **Dashboard** | Visual list of containers, images, volumes |
| **Dev Environments** | Clone + run repos in one click |
| **Kubernetes** | Single-click local Kubernetes cluster |
| **Docker Scout** | Vulnerability scanning in the GUI |
| **Extensions** | Third-party tools (Portainer, Lens, etc.) |
| **Settings** | VM resources, file sharing, proxies |

---

## 3. macOS Specifics

### Virtualization backends (macOS)

Docker Desktop for Mac supports two VM backends:

| Backend | Available on | Notes |
|---------|-------------|-------|
| **Apple Virtualization framework** | M1/M2/M3 Mac + Intel | Default on Apple Silicon |
| **HyperKit** | Intel Mac only | Legacy; being phased out |
| **VirtioFS** | macOS 13+ | Fastest file sharing |

Switch under: **Settings → General → Virtual Machine Options**

### Apple Silicon (M1/M2/M3)

Apple Silicon is ARM64 architecture. Most Docker images are built for AMD64 (Intel/x86_64). Docker Desktop handles this via emulation, but there are caveats:

```bash
# Check your Mac's architecture
uname -m
# arm64   ← Apple Silicon
# x86_64  ← Intel

# See an image's platform
docker inspect nginx | grep Architecture
# "Architecture": "amd64"

# Run an amd64 image on Apple Silicon (emulated via Rosetta)
docker run --platform linux/amd64 nginx

# Run native arm64 image (faster)
docker run --platform linux/arm64 nginx

# Build multi-platform images
docker buildx build --platform linux/amd64,linux/arm64 -t myapp:latest --push .
```

### File sharing and performance on macOS

macOS uses a virtual filesystem bridge to share files between your Mac and the Linux VM. This causes performance differences:

```bash
# Bind mounts can be slower on macOS than on Linux
docker run -v $(pwd):/app node:20 npm install
# ... slower than on Linux
```

**Performance options:**

```yaml
# In docker-compose.yml — use delegated/cached mounts
services:
  app:
    volumes:
      - .:/app:delegated     # host is authoritative for writes
      - /app/node_modules    # anonymous volume (don't sync node_modules!)
```

> **Tip:** Never bind-mount `node_modules` from Mac to container. Use an anonymous volume for `node_modules` and let npm install inside the container.

### Configuring resources (macOS)

Docker Desktop → Settings → Resources:
- **CPU:** Limit how many CPU cores the VM can use
- **Memory:** How much RAM the VM gets (default: 8GB)
- **Swap:** Virtual memory
- **Disk:** Maximum size of the Docker VM disk image

```bash
# See current VM resource usage
docker system df
docker stats
```

---

## 4. Windows Specifics

### WSL 2 vs Hyper-V backends

Docker Desktop on Windows can use two backends:

| Backend | Requirements | Recommendation |
|---------|-------------|---------------|
| **WSL 2** | Windows 10 2004+, WSL 2 enabled | Recommended — faster, more features |
| **Hyper-V** | Windows Pro/Enterprise, Hyper-V enabled | Fallback if WSL 2 not available |

### Setting up WSL 2

```powershell
# In PowerShell (Administrator)

# Install WSL 2
wsl --install

# Or if WSL is already installed, set version 2 as default
wsl --set-default-version 2

# Install Ubuntu
wsl --install -d Ubuntu

# Verify
wsl -l -v
# NAME      STATE    VERSION
# Ubuntu    Running  2       ← must be VERSION 2
```

### Docker inside WSL 2

With Docker Desktop installed and WSL 2 integration enabled, your WSL 2 distro automatically gets Docker:

```bash
# Inside WSL 2 (Ubuntu terminal)
docker ps
docker run hello-world
```

Enable under: **Settings → Resources → WSL Integration → Enable for Ubuntu**

### Running Linux containers (default)

Windows containers and Linux containers are different. By default, Docker Desktop runs Linux containers (what you'll use for all standard Docker work):

```
System tray → Right-click Docker → Switch to Linux containers
```

### Running Windows containers

For Windows-specific workloads (ASP.NET, PowerShell, Windows Server):

```bash
# Switch to Windows container mode first
# System tray → Right-click Docker → Switch to Windows containers

docker run mcr.microsoft.com/windows/servercore:ltsc2022 cmd /c echo Hello
docker run mcr.microsoft.com/dotnet/framework/aspnet:4.8 cmd
```

Windows containers only run on Windows. They cannot run on Mac or Linux.

### Windows path considerations

```bash
# In Git Bash / WSL, use Linux-style paths
docker run -v $(pwd):/app node:20 bash

# In PowerShell / CMD, use Windows paths with forward slashes
docker run -v C:/Users/you/project:/app node:20 bash

# Or use the COMPOSE_CONVERT_WINDOWS_PATHS env var in PowerShell
$env:COMPOSE_CONVERT_WINDOWS_PATHS=1
docker compose up
```

---

## 5. Multi-Platform Builds (Buildx)

**Docker Buildx** extends `docker build` with multi-platform support — build images for Linux/amd64, Linux/arm64, and more from a single machine.

```bash
# See available builders
docker buildx ls

# Create a multi-platform builder
docker buildx create --name multibuilder --use
docker buildx inspect --bootstrap

# Build for multiple platforms and push
docker buildx build \
  --platform linux/amd64,linux/arm64,linux/arm/v7 \
  -t yourname/myapp:latest \
  --push \
  .

# Verify the manifest
docker buildx imagetools inspect yourname/myapp:latest
```

When a user pulls the image, Docker automatically selects the right architecture.

---

## 6. Performance Tips (Mac & Windows)

```bash
# 1. Avoid bind-mounting node_modules
# Bad:
volumes:
  - .:/app
# Good:
volumes:
  - .:/app
  - /app/node_modules  # anonymous volume — stays in VM

# 2. Use VirtioFS (macOS 13+) for fastest file sync
# Docker Desktop → Settings → General → VirtioFS

# 3. Allocate enough memory to the VM
# Most apps need at least 4GB; build pipelines need more

# 4. Use Docker Desktop's built-in Kubernetes
# instead of running Minikube separately

# 5. Prune regularly — the VM disk image grows
docker system prune -a --volumes
```

---

## 7. Alternatives to Docker Desktop

Docker Desktop is free for small businesses and personal use, but requires a paid subscription for larger companies.

| Alternative | Platform | Notes |
|-------------|---------|-------|
| **OrbStack** | macOS | Fast, lightweight, Docker-compatible |
| **Rancher Desktop** | Mac, Windows, Linux | Open source, includes Kubernetes |
| **Podman Desktop** | All | Rootless, daemonless, Docker-compatible |
| **Lima** | macOS | Linux VM with Docker inside |
| **Colima** | macOS | Docker + Kubernetes on Lima |

```bash
# OrbStack (macOS) — drop-in Docker Desktop replacement
brew install orbstack

# Rancher Desktop — includes nerdctl (containerd) or dockerd
# Download from rancherdesktop.io

# Podman Desktop — rootless containers
brew install podman-desktop
podman machine init
podman machine start
```

---

## 8. Docker Context (Connecting to Remote Docker)

Docker Desktop manages a local context. But you can connect your CLI to a remote Docker host:

```bash
# List contexts
docker context ls

# Create a remote context
docker context create remote-server \
  --docker "host=ssh://user@remote-host"

# Switch context
docker context use remote-server

# Now all docker commands go to the remote host
docker ps
docker run -d nginx

# Switch back to local
docker context use default
```

---

## Summary

| Platform | VM Tech | Performance | Recommendation |
|----------|--------|------------|----------------|
| macOS (Apple Silicon) | Apple Virtualization + Rosetta | Excellent with native images | Use arm64 images where possible |
| macOS (Intel) | HyperKit | Good | VirtioFS for best file sync |
| Windows (WSL 2) | WSL 2 | Excellent | Recommended default |
| Windows (Hyper-V) | Hyper-V | Good | Use if WSL 2 unavailable |

**Next:** [08 — Container Orchestration →](./08-orchestration-swarm-kubernetes.md)
