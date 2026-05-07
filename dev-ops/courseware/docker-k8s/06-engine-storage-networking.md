# Module 06 — Docker Engine, Storage and Networking

This module goes under the hood — how Docker's engine works, how data is stored and persisted, and how containers communicate over networks.

---

## Part A: Docker Engine

### 1. Docker Engine Architecture

Docker Engine consists of three components:

```
┌────────────────────────────────────────────────────────┐
│  Docker CLI  (docker run, docker build, etc.)          │
└───────────────────────┬────────────────────────────────┘
                        │ HTTP REST API
                        │ (Unix socket: /var/run/docker.sock)
┌───────────────────────▼────────────────────────────────┐
│  Docker Daemon  (dockerd)                              │
│  - API server                                          │
│  - Image management                                    │
│  - Container lifecycle                                 │
│  - Volume & network management                         │
└───────────────────────┬────────────────────────────────┘
                        │ gRPC
┌───────────────────────▼────────────────────────────────┐
│  containerd                                            │
│  - Container runtime supervisor                        │
│  - Manages container lifecycle                         │
│  - Pulls images from registries                        │
└───────────────────────┬────────────────────────────────┘
                        │
┌───────────────────────▼────────────────────────────────┐
│  runc (OCI runtime)                                    │
│  - Creates and runs containers                         │
│  - Uses Linux namespaces and cgroups                   │
└────────────────────────────────────────────────────────┘
```

### 2. Linux Primitives

Docker containers are not magic — they use existing Linux kernel features:

#### Namespaces (isolation)

| Namespace | Isolates |
|-----------|---------|
| `pid` | Process IDs — container has its own PID 1 |
| `net` | Network interfaces, IPs, routing tables |
| `mnt` | Filesystem mount points |
| `uts` | Hostname and domain name |
| `ipc` | Inter-process communication (shared memory) |
| `user` | User and group IDs |
| `cgroup` | Control group membership |

```bash
# Inspect a container's network namespace
docker run -d --name mycontainer nginx
docker inspect mycontainer | grep '"Pid"'
# "Pid": 12345

# Look at the container's network from the host
sudo nsenter -t 12345 -n ip addr
```

#### Control Groups / cgroups (resource limits)

```bash
# See cgroup for a container
cat /sys/fs/cgroup/memory/docker/<container-id>/memory.limit_in_bytes

# Docker sets cgroups when you use resource flags
docker run --memory 512m --cpus 1.0 nginx
```

### 3. Configuring the Docker Daemon

The daemon is configured via `/etc/docker/daemon.json`:

```json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  },
  "storage-driver": "overlay2",
  "insecure-registries": ["registry.local:5000"],
  "registry-mirrors": ["https://mirror.example.com"],
  "dns": ["8.8.8.8", "8.8.4.4"],
  "default-address-pools": [
    {"base": "172.17.0.0/16", "size": 24}
  ],
  "experimental": false,
  "metrics-addr": "127.0.0.1:9323"
}
```

```bash
# Reload config
sudo systemctl reload docker

# Restart daemon (required for some changes)
sudo systemctl restart docker
```

### 4. Logging Drivers

```bash
# Default: json-file (logs stored on host)
docker run --log-driver json-file --log-opt max-size=10m nginx

# Syslog
docker run --log-driver syslog --log-opt syslog-address=udp://1.2.3.4:514 nginx

# Journald (systemd)
docker run --log-driver journald nginx

# Fluentd
docker run --log-driver fluentd --log-opt fluentd-address=localhost:24224 nginx

# AWS CloudWatch
docker run --log-driver awslogs \
  --log-opt awslogs-region=us-east-1 \
  --log-opt awslogs-group=my-log-group nginx

# No logging (performance-sensitive tasks)
docker run --log-driver none nginx
```

---

## Part B: Storage

### 5. The Union Filesystem (overlay2)

Docker images use a layered filesystem. The default storage driver is `overlay2`:

```
Container Layer (writable — thin, ephemeral)
    ▲
Layer N: RUN npm install      (read-only)
    ▲
Layer N-1: COPY . .           (read-only)
    ▲
Layer N-2: WORKDIR /app       (read-only)
    ▲
Base image layers             (read-only)
```

When a container writes to a read-only file, the file is **copied up** to the writable layer (Copy-on-Write). This is transparent to the process inside the container.

```bash
# Check storage driver
docker info | grep "Storage Driver"

# Inspect overlay2 layers on disk
sudo ls /var/lib/docker/overlay2/
```

### 6. Storage Drivers

| Driver | Use case | Notes |
|--------|---------|-------|
| `overlay2` | Default on modern Linux | Recommended, requires kernel 4.0+ |
| `devicemapper` | RHEL/CentOS legacy | Deprecated |
| `btrfs` | btrfs filesystem | Use if already on btrfs |
| `zfs` | ZFS filesystem | Enterprise NAS setups |
| `vfs` | Testing only | No copy-on-write, very slow |

### 7. Volumes Deep Dive

#### Volume types recap

```bash
# Named volumes (Docker-managed)
docker run -v myvolume:/data postgres

# Bind mounts (host-path mapped)
docker run -v /host/path:/container/path nginx

# tmpfs (memory — not persisted, not on disk)
docker run --tmpfs /tmp:rw,size=100m myapp

# Named pipe (Windows only)
```

#### Volume drivers for distributed storage

```bash
# Install a volume driver plugin
docker plugin install vieux/sshfs

# Use the plugin
docker volume create \
  --driver vieux/sshfs \
  --opt sshcmd=user@host:/path \
  --opt password=secret \
  ssh-volume

docker run -v ssh-volume:/data myapp
```

#### Backing up and restoring volumes

```bash
# Backup a named volume to a tar file
docker run --rm \
  -v mydata:/source:ro \
  -v $(pwd):/backup \
  ubuntu \
  tar czf /backup/mydata-backup.tar.gz -C /source .

# Restore from tar
docker run --rm \
  -v mydata:/target \
  -v $(pwd):/backup \
  ubuntu \
  tar xzf /backup/mydata-backup.tar.gz -C /target
```

#### Sharing volumes between containers

```bash
# Container A writes data
docker run -d --name writer -v shared-data:/data writer-image

# Container B reads data
docker run -d --name reader -v shared-data:/data:ro reader-image
```

---

## Part C: Networking

### 8. Network Drivers

Docker provides several network drivers:

| Driver | Use case | Description |
|--------|---------|-------------|
| `bridge` | Default for standalone containers | Software bridge, NAT to host |
| `host` | High-performance, no isolation | Container uses host network directly |
| `none` | Complete isolation | No networking |
| `overlay` | Swarm/multi-host | Distributed networking across hosts |
| `macvlan` | Legacy apps needing MAC | Container gets its own MAC address |
| `ipvlan` | Similar to macvlan | L2/L3 IP-level assignments |

### 9. The Default Bridge Network

When you run a container without `--network`, it attaches to the default bridge (`docker0`):

```bash
# See the bridge
ip addr show docker0
# 3: docker0: inet 172.17.0.1/16

# See connected containers
docker network inspect bridge
```

**Limitations of the default bridge:**
- Containers cannot resolve each other by name (no DNS)
- Must use `--link` (deprecated) or IP addresses

### 10. User-Defined Bridge Networks

User-defined bridges are superior — containers get automatic DNS resolution by name:

```bash
# Create
docker network create my-net
docker network create --driver bridge \
  --subnet 192.168.100.0/24 \
  --gateway 192.168.100.1 \
  my-custom-net

# Run containers on it
docker run -d --name db --network my-net postgres
docker run -d --name web --network my-net myapp

# 'web' can reach 'db' at hostname 'db'
docker exec web ping db
```

### 11. Container Networking Internals

```bash
# Inspect a container's network settings
docker inspect mycontainer | python3 -c "
import sys, json
d = json.load(sys.stdin)[0]
nets = d['NetworkSettings']['Networks']
for name, cfg in nets.items():
    print(f'Network: {name}')
    print(f'  IP: {cfg[\"IPAddress\"]}')
    print(f'  Gateway: {cfg[\"Gateway\"]}')
    print(f'  MAC: {cfg[\"MacAddress\"]}')
"

# See all networks a container belongs to
docker inspect --format='{{range $k,$v := .NetworkSettings.Networks}}{{$k}} {{end}}' mycontainer
```

### 12. Port Publishing Deep Dive

```bash
# Port mapping: host 8080 → container 80
docker run -p 8080:80 nginx

# What actually happens:
# 1. Docker adds an iptables rule
sudo iptables -t nat -L DOCKER

# 2. The 'docker-proxy' process forwards traffic
ps aux | grep docker-proxy
```

### 13. Host Networking

```bash
docker run --network host nginx
# Container uses 172.x.x.x from the host's network stack
# Nginx listens on port 80 of the host directly
```

**When to use host networking:**
- Maximum network performance (no NAT overhead)
- Legacy apps that bind to specific host IPs
- Network monitoring tools that need raw host access

**Drawback:** No network isolation — container can bind any host port.

### 14. Overlay Networks (Swarm / Kubernetes)

Overlay networks span multiple Docker hosts, enabling containers on different machines to communicate:

```bash
# Requires Docker Swarm mode
docker swarm init

# Create overlay network
docker network create --driver overlay --attachable my-overlay

# Services on different nodes can communicate
docker service create --network my-overlay --name db postgres
docker service create --network my-overlay --name web myapp
# 'web' replicas on any node can reach 'db' by hostname 'db'
```

### 15. macvlan — Containers on the Physical Network

```bash
# Create macvlan network on eth0
docker network create \
  --driver macvlan \
  --subnet 192.168.1.0/24 \
  --gateway 192.168.1.1 \
  --opt parent=eth0 \
  macvlan-net

# Container gets an IP on your physical network
docker run --network macvlan-net --ip 192.168.1.50 myapp
# Now 192.168.1.50 is reachable from anywhere on the LAN
```

### 16. DNS in Docker

```bash
# Containers use Docker's built-in DNS (127.0.0.11)
docker exec mycontainer cat /etc/resolv.conf
# nameserver 127.0.0.11

# Custom DNS servers
docker run --dns 1.1.1.1 --dns 8.8.8.8 myapp

# Extra hosts (/etc/hosts entries)
docker run --add-host myhost:192.168.1.100 myapp

# Set in daemon.json for all containers
# { "dns": ["1.1.1.1", "8.8.8.8"] }
```

### 17. Network Troubleshooting

```bash
# Inspect a network
docker network inspect my-net

# List all networks
docker network ls

# Test connectivity between containers
docker exec web ping db
docker exec web curl http://db:5432
docker exec web nslookup db

# Trace routing
docker exec mycontainer traceroute 8.8.8.8

# Port scan within network
docker exec mycontainer nc -zv db 5432
# Connection to db 5432 port [tcp/postgresql] succeeded!

# Show iptables rules Docker created
sudo iptables -t nat -L -n -v
sudo iptables -L DOCKER-USER
```

---

## Summary

### Engine

| Concept | Key points |
|---------|-----------|
| Docker architecture | CLI → daemon → containerd → runc |
| Namespaces | Provide isolation (PID, net, mnt, uts, ipc, user) |
| cgroups | Enforce resource limits (CPU, memory) |
| daemon.json | Configure logging, storage driver, DNS, registries |

### Storage

| Type | Command | Persists? | Best for |
|------|---------|----------|---------|
| Named volume | `-v vol:/path` | Yes | Databases, app data |
| Bind mount | `-v /host:/path` | Yes (on host) | Dev: live reload |
| tmpfs | `--tmpfs /path` | No (memory) | Secrets, temp files |

### Networking

| Driver | Isolation | Multi-host | Use for |
|--------|----------|-----------|---------|
| bridge | Yes | No | Default; single-host dev |
| host | No | No | Max performance |
| none | Full | No | Air-gapped containers |
| overlay | Yes | Yes | Swarm, Kubernetes |
| macvlan | Yes | Yes | Direct LAN access |

**Next:** [07 — Docker on Mac & Windows →](./07-docker-on-mac-windows.md)
