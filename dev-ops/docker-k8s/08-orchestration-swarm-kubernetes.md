# Module 08 — Container Orchestration: Docker Swarm & Kubernetes

Running a single container is easy. Running hundreds across multiple servers, handling failures, scaling under load, and doing zero-downtime deployments — that's what container orchestration solves.

---

## 1. Why Orchestration?

### The single-container problem

```bash
docker run -d myapp
```

This works. But in production you need to answer:
- What happens if the container crashes?
- How do you scale to handle 10× traffic?
- How do you update to a new version without downtime?
- How do you spread containers across multiple servers?
- How do you route traffic to healthy containers only?

No single `docker run` command answers these questions. **Orchestrators** do.

### What an orchestrator provides

| Challenge | Orchestrator solution |
|-----------|----------------------|
| Container crashes | Automatic restart / rescheduling |
| Traffic spike | Auto-scaling (more container replicas) |
| Server failure | Reschedule containers to healthy nodes |
| New version deploy | Rolling updates with zero downtime |
| Traffic routing | Built-in load balancing and service discovery |
| Configuration | Centralised config and secret management |
| Placement | Schedule containers based on resource requirements |

---

## 2. Docker Swarm

Docker Swarm is Docker's built-in clustering solution. It turns a group of Docker hosts into a single virtual Docker host.

### Architecture

```
┌──────────────────────────────────────────────────────┐
│                    Swarm Cluster                     │
│                                                      │
│  ┌──────────────┐  ┌──────────────┐                 │
│  │ Manager Node │  │ Manager Node │ ← Raft consensus│
│  │  (Leader)    │  │  (Follower)  │                 │
│  └──────┬───────┘  └──────────────┘                 │
│         │ Orchestrate                                │
│  ┌──────▼──────────────────────────────────────┐    │
│  │          Worker Nodes                        │    │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  │    │
│  │  │ Worker 1 │  │ Worker 2 │  │ Worker 3 │  │    │
│  │  │ [c][c]   │  │ [c][c]   │  │ [c][c]   │  │    │
│  │  └──────────┘  └──────────┘  └──────────┘  │    │
│  └─────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────┘
```

- **Manager nodes** orchestrate the cluster, maintain state
- **Worker nodes** run containers
- Recommended: 3 or 5 managers (odd number for Raft quorum)

### Setting up a Swarm

```bash
# On the first manager node
docker swarm init --advertise-addr 192.168.1.10
# Swarm initialized: current node is now a manager.
# To add a worker to this swarm, run the following command:
#     docker swarm join --token SWMTKN-1-... 192.168.1.10:2377

# On each worker node (use the token from above)
docker swarm join \
  --token SWMTKN-1-abc123... \
  192.168.1.10:2377

# On the manager — verify
docker node ls
# ID          HOSTNAME   STATUS  AVAILABILITY  MANAGER STATUS
# abc123 *   manager1   Ready   Active        Leader
# def456     worker1    Ready   Active
# ghi789     worker2    Ready   Active
```

### Services — the Swarm deployment unit

In Swarm, you don't `docker run` — you create **services**:

```bash
# Create a service with 3 replicas
docker service create \
  --name web \
  --replicas 3 \
  --publish published=80,target=80 \
  nginx

# List services
docker service ls

# See where replicas are running
docker service ps web

# Scale up/down
docker service scale web=5

# Update (rolling update — zero downtime)
docker service update --image nginx:1.25 web

# Remove service
docker service rm web
```

### Rolling updates

```bash
docker service update \
  --image myapp:v2.0 \
  --update-parallelism 2 \      # update 2 replicas at a time
  --update-delay 30s \          # wait 30s between batches
  --update-failure-action rollback \  # auto-rollback on failure
  web

# Manually rollback
docker service rollback web
```

### Stacks — Swarm's equivalent of Compose

Deploy an entire `docker-compose.yml` to a Swarm:

```yaml
# docker-compose.yml (Swarm stack version)
version: "3.9"

services:
  web:
    image: myapp:v1.0
    deploy:
      replicas: 3
      update_config:
        parallelism: 1
        delay: 30s
        failure_action: rollback
      restart_policy:
        condition: on-failure
        delay: 5s
        max_attempts: 3
      resources:
        limits:
          cpus: "0.5"
          memory: 256M
      placement:
        constraints:
          - node.role == worker
    ports:
      - "80:80"
    networks:
      - app-net

  db:
    image: postgres:15
    deploy:
      replicas: 1
      placement:
        constraints:
          - node.labels.db == true   # only on labelled nodes
    volumes:
      - db-data:/var/lib/postgresql/data
    secrets:
      - db_password
    networks:
      - app-net

secrets:
  db_password:
    external: true

volumes:
  db-data:

networks:
  app-net:
    driver: overlay
```

```bash
# Deploy the stack
docker stack deploy -c docker-compose.yml myapp

# List stacks
docker stack ls

# List services in a stack
docker stack services myapp

# List tasks in a stack
docker stack ps myapp

# Remove stack
docker stack rm myapp
```

### Swarm secrets

```bash
# Create a secret
echo "supersecretpassword" | docker secret create db_password -

# Secrets are mounted at /run/secrets/<name> inside the container
docker service create \
  --name db \
  --secret db_password \
  -e POSTGRES_PASSWORD_FILE=/run/secrets/db_password \
  postgres:15
```

### Swarm vs Compose

| Feature | Compose | Swarm |
|---------|---------|-------|
| Multi-host | ❌ | ✅ |
| Auto-healing | ❌ | ✅ |
| Scaling | Manual | Built-in |
| Load balancing | ❌ | Built-in |
| Secrets | ❌ | Built-in |
| Use for | Dev/single host | Small-medium production |

---

## 3. Kubernetes — Overview

Kubernetes (K8s) is the industry-standard container orchestrator. Originally from Google, now maintained by the CNCF (Cloud Native Computing Foundation).

```
Kubernetes vs Docker Swarm:
┌────────────────┬──────────────────┬────────────────────┐
│ Feature        │ Docker Swarm     │ Kubernetes         │
├────────────────┼──────────────────┼────────────────────┤
│ Complexity     │ Simple           │ Complex            │
│ Features       │ Basic            │ Extensive          │
│ Ecosystem      │ Small            │ Vast               │
│ Adoption       │ Declining        │ Industry standard  │
│ Learning curve │ Low              │ High               │
│ Scaling        │ Manual           │ Auto + manual      │
│ Self-healing   │ Basic            │ Advanced           │
│ Storage        │ Basic            │ Rich (CSI)         │
│ Networking     │ Overlay          │ CNI plugins        │
│ Use for        │ Small teams      │ Enterprise/cloud   │
└────────────────┴──────────────────┴────────────────────┘
```

### Kubernetes architecture

```
Control Plane (Master):
┌────────────────────────────────────────────────────┐
│  API Server  │  Scheduler  │  Controller Manager   │
│  etcd        │             │                        │
└────────────────────────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         ▼                       ▼
┌──────────────────┐   ┌──────────────────┐
│   Worker Node    │   │   Worker Node    │
│  ┌────────────┐  │   │  ┌────────────┐  │
│  │  kubelet   │  │   │  │  kubelet   │  │
│  │  kube-proxy│  │   │  │  kube-proxy│  │
│  │  Container │  │   │  │  Container │  │
│  │  Runtime   │  │   │  │  Runtime   │  │
│  └────────────┘  │   │  └────────────┘  │
│  Pods run here   │   │  Pods run here   │
└──────────────────┘   └──────────────────┘
```

> The full Kubernetes course covers this architecture and all concepts in depth. This section gives you the context to understand where Kubernetes fits relative to Docker and Swarm.

### Kubernetes basic concepts (preview)

| Concept | Docker equivalent | Description |
|---------|------------------|-------------|
| Pod | Container | Smallest deployable unit |
| Deployment | docker service | Manages pods, rolling updates |
| Service | Port publish | Stable networking endpoint |
| ConfigMap | Environment vars | Non-secret configuration |
| Secret | docker secret | Sensitive configuration |
| Volume | Docker volume | Persistent storage |
| Namespace | — | Cluster isolation |
| Node | Swarm node | A server in the cluster |

### Running Kubernetes locally

```bash
# Option 1: Docker Desktop built-in Kubernetes
# Settings → Kubernetes → Enable Kubernetes

# Option 2: Minikube
brew install minikube
minikube start
kubectl get nodes

# Option 3: kind (Kubernetes in Docker)
brew install kind
kind create cluster

# Verify kubectl works
kubectl cluster-info
kubectl get nodes
# NAME       STATUS   ROLES           AGE
# minikube   Ready    control-plane   1m
```

### kubectl basics (preview)

```bash
# Deploy an app
kubectl create deployment web --image=nginx --replicas=3

# Expose it
kubectl expose deployment web --port=80 --type=LoadBalancer

# See what's running
kubectl get pods
kubectl get deployments
kubectl get services

# Scale
kubectl scale deployment web --replicas=5

# Update image
kubectl set image deployment/web nginx=nginx:1.25

# Delete
kubectl delete deployment web
```

---

## 4. When to Use What

```
Single machine development
    → docker compose up

Small production (1-5 servers), simple ops team
    → Docker Swarm

Medium-large production, cloud-native, complex requirements
    → Kubernetes

Managed (you don't want to run the control plane)
    → AWS EKS / Google GKE / Azure AKS
```

---

## 5. Choosing a Managed Kubernetes Service

| Service | Provider | Strengths |
|---------|---------|-----------|
| **EKS** | AWS | Deep AWS integration (IAM, ALB, EBS) |
| **GKE** | Google Cloud | Best autopilot, fastest releases |
| **AKS** | Azure | Azure AD integration, Windows nodes |
| **DigitalOcean K8s** | DigitalOcean | Simple, cheap for small teams |
| **Fly.io** | Fly | Edge deployments |

---

## Summary

| Platform | What it is | Use when |
|----------|-----------|---------|
| Docker Compose | Multi-container on one host | Local dev, simple single-host |
| Docker Swarm | Built-in Docker clustering | Small teams, simple ops needs |
| Kubernetes | Industry-standard orchestrator | Scale, resilience, enterprise |
| Managed K8s | Cloud-hosted K8s control plane | Don't want to manage K8s yourself |

**Next:** [09 — Conclusion →](./09-conclusion.md)
