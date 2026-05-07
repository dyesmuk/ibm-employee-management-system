# Module 01 — Kubernetes Overview

This module covers Kubernetes' architecture in depth — the control plane, worker nodes, and the components that make orchestration work.

---

## 1. Cluster Architecture

A Kubernetes cluster consists of a **control plane** (the brain) and **worker nodes** (the muscles).

```
┌───────────────────────────────────────────────────────────────────┐
│                        Kubernetes Cluster                         │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │                    Control Plane                            │  │
│  │                                                             │  │
│  │  ┌──────────────┐  ┌───────────┐  ┌─────────────────────┐  │  │
│  │  │  API Server  │  │ Scheduler │  │ Controller Manager  │  │  │
│  │  │  (kube-      │  │ (kube-    │  │ (kube-controller-   │  │  │
│  │  │   apiserver) │  │  scheduler│  │  manager)           │  │  │
│  │  └──────────────┘  └───────────┘  └─────────────────────┘  │  │
│  │                                                             │  │
│  │  ┌──────────────────────────────────────────────────────┐  │  │
│  │  │  etcd  (distributed key-value store — cluster state)  │  │  │
│  │  └──────────────────────────────────────────────────────┘  │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                               │                                   │
│              ┌────────────────┼─────────────────┐                 │
│              │                │                 │                 │
│  ┌───────────▼──┐  ┌──────────▼───┐  ┌──────────▼───┐           │
│  │ Worker Node  │  │ Worker Node  │  │ Worker Node  │           │
│  │              │  │              │  │              │           │
│  │  ┌─────────┐ │  │  ┌─────────┐ │  │  ┌─────────┐ │           │
│  │  │ kubelet │ │  │  │ kubelet │ │  │  │ kubelet │ │           │
│  │  │kube-    │ │  │  │kube-    │ │  │  │kube-    │ │           │
│  │  │ proxy   │ │  │  │ proxy   │ │  │  │ proxy   │ │           │
│  │  │Container│ │  │  │Container│ │  │  │Container│ │           │
│  │  │ Runtime │ │  │  │ Runtime │ │  │  │ Runtime │ │           │
│  │  └─────────┘ │  │  └─────────┘ │  │  └─────────┘ │           │
│  │  [Pod][Pod]  │  │  [Pod][Pod]  │  │  [Pod][Pod]  │           │
│  └──────────────┘  └──────────────┘  └──────────────┘           │
└───────────────────────────────────────────────────────────────────┘
```

---

## 2. Control Plane Components

### API Server (`kube-apiserver`)

The **front door** of Kubernetes. Every interaction — from `kubectl` commands to controller reconciliation — goes through the API server.

- Exposes the Kubernetes REST API (HTTPS, port 6443)
- Validates and processes all API requests
- Persists state to etcd
- Stateless — can be replicated for high availability

```bash
# All kubectl commands talk to the API server
kubectl get pods
# → GET https://k8s-api:6443/api/v1/namespaces/default/pods
```

### etcd

A **distributed, consistent key-value store** that holds the entire cluster state. Think of it as Kubernetes' database.

- Stores all object definitions (pods, deployments, services, etc.)
- Uses the Raft consensus algorithm for strong consistency
- Must be backed up regularly — losing etcd means losing cluster state

```bash
# Back up etcd (critical for production)
etcdctl snapshot save backup.db \
  --endpoints=https://127.0.0.1:2379 \
  --cacert=/etc/etcd/ca.crt \
  --cert=/etc/etcd/etcd-server.crt \
  --key=/etc/etcd/etcd-server.key
```

### Scheduler (`kube-scheduler`)

Watches for **newly created Pods with no assigned node** and selects a node for them to run on.

The scheduling decision considers:
- Resource requirements (`requests` and `limits`)
- Node affinity/anti-affinity rules
- Taints and tolerations
- Pod topology constraints
- Available resources on each node

```
New Pod created (no node assigned)
         │
         ▼
    kube-scheduler
         │
    Filter phase: Which nodes CAN run this pod?
    (resource fit, taints, affinity, selectors)
         │
    Score phase: Which node is BEST?
    (least requested, balanced resource usage)
         │
         ▼
    Bind pod to best node
```

### Controller Manager (`kube-controller-manager`)

Runs a collection of **controllers** — control loops that watch the cluster state and make changes to drive toward the desired state.

| Controller | What it manages |
|-----------|----------------|
| Node Controller | Monitors node health; marks unavailable nodes |
| Replication Controller | Ensures correct number of pod replicas |
| Endpoints Controller | Populates Service → Pod endpoint mappings |
| Service Account Controller | Creates default service accounts |
| Job Controller | Manages batch Jobs |
| CronJob Controller | Manages scheduled CronJobs |

Each controller runs a loop:
```
while true:
    actual_state = observe_cluster()
    desired_state = read_spec()
    if actual_state != desired_state:
        take_action_to_reconcile()
    sleep(short_interval)
```

### Cloud Controller Manager (optional)

Integrates with cloud provider APIs (AWS, GCP, Azure) for:
- Provisioning LoadBalancer Services
- Managing cloud storage volumes
- Node lifecycle (deregister deleted VMs)

---

## 3. Worker Node Components

### kubelet

An **agent running on every worker node**. Responsible for making sure containers in pods are running and healthy.

- Receives PodSpecs from the API server
- Starts/stops containers via the container runtime
- Reports pod status back to the API server
- Runs container health checks (liveness, readiness probes)

```bash
# kubelet runs as a systemd service on worker nodes
sudo systemctl status kubelet
```

### kube-proxy

A **network proxy on every node** that implements the Kubernetes Service abstraction.

- Maintains network rules (iptables or IPVS) on each node
- Routes traffic to the correct pod based on Service rules
- Handles load balancing across pod replicas

```bash
# See iptables rules created by kube-proxy
sudo iptables -t nat -L -n | grep KUBE
```

### Container Runtime

The software that actually runs containers. Kubernetes uses the Container Runtime Interface (CRI):

| Runtime | Notes |
|---------|-------|
| **containerd** | Most common; used by Docker Desktop, EKS, GKE |
| **CRI-O** | Lightweight; used by OpenShift |
| **Docker** | No longer supported as CRI since K8s 1.24 |

```bash
# Check the runtime on a node
kubectl get node <node-name> -o jsonpath='{.status.nodeInfo.containerRuntimeVersion}'
# containerd://1.7.0
```

---

## 4. How It All Works Together — End-to-End

Let's trace what happens when you run:

```bash
kubectl create deployment web --image=nginx --replicas=3
```

```
1. kubectl → API Server
   "Please create a Deployment object with these specs"

2. API Server → etcd
   Persists the Deployment object

3. Deployment Controller (in controller-manager) notices new Deployment
   Creates a ReplicaSet: "I need 3 pods"

4. ReplicaSet Controller notices 0/3 pods exist
   Creates 3 Pod objects (with no node assigned)

5. Scheduler notices 3 unscheduled Pods
   Evaluates nodes, assigns each pod to a node
   Updates Pod objects in etcd with node assignment

6. kubelet on each assigned node notices the Pod
   Calls the container runtime: "Run nginx container"
   Container runtime pulls nginx image, starts container
   kubelet reports pod status: Running

7. kube-proxy on all nodes updates iptables
   Traffic to the Service IP can now route to pod IPs
```

---

## 5. Kubernetes Objects

Everything in Kubernetes is represented as an **object** — a persistent entity in the cluster state. Objects have:

- `apiVersion` — which API group and version
- `kind` — what type of object
- `metadata` — name, namespace, labels, annotations
- `spec` — the desired state (what you want)
- `status` — the actual current state (what K8s observes)

```yaml
apiVersion: apps/v1       # API group/version
kind: Deployment          # object type
metadata:
  name: web-server
  namespace: production
  labels:
    app: web
spec:                     # desired state — YOU write this
  replicas: 3
  selector:
    matchLabels:
      app: web
  template:
    ...
status:                   # actual state — Kubernetes fills this in
  readyReplicas: 3
  ...
```

---

## 6. Namespaces

**Namespaces** divide a cluster into virtual sub-clusters. Use them to isolate teams, projects, or environments.

```bash
# List namespaces
kubectl get namespaces
# NAME              STATUS   AGE
# default           Active   10d   ← objects go here without --namespace
# kube-system       Active   10d   ← K8s system components
# kube-public       Active   10d   ← readable by all, auto-provisioned
# kube-node-lease   Active   10d   ← node heartbeat objects

# Create a namespace
kubectl create namespace staging

# Work in a namespace
kubectl get pods --namespace staging
kubectl get pods -n staging              # short flag

# Set a default namespace for your session
kubectl config set-context --current --namespace=staging
```

---

## 7. Labels and Selectors

**Labels** are key-value pairs attached to objects. They're how Kubernetes groups and selects objects.

```yaml
metadata:
  labels:
    app: web-server
    version: v2.0
    environment: production
    tier: frontend
```

```bash
# Select by label
kubectl get pods -l app=web-server
kubectl get pods -l "environment=production,tier=frontend"
kubectl get pods -l "version in (v1.0, v2.0)"
kubectl get pods -l "environment!=development"
```

**Selectors** let objects refer to groups of other objects:

```yaml
# A Service uses a selector to find its pods
spec:
  selector:
    app: web-server     # routes traffic to pods with this label
```

### Label best practices

```yaml
labels:
  app.kubernetes.io/name: web-server
  app.kubernetes.io/version: "2.0"
  app.kubernetes.io/component: frontend
  app.kubernetes.io/part-of: my-platform
  app.kubernetes.io/managed-by: helm
```

---

## 8. Annotations

**Annotations** are also key-value pairs but are not used for selection — they store metadata for tools and humans.

```yaml
metadata:
  annotations:
    kubernetes.io/change-cause: "Update to v2.0 for dark mode feature"
    docs.example.com/runbook: "https://wiki.example.com/web-server"
    monitoring.example.com/scrape: "true"
    last-deployed: "2025-05-06T10:30:00Z"
```

---

## 9. kubectl — The Kubernetes CLI

`kubectl` is the primary way to interact with a cluster.

```bash
# Cluster info
kubectl cluster-info
kubectl get nodes
kubectl get nodes -o wide         # extra columns (IP, OS, kernel)
kubectl describe node <name>      # detailed info

# Resources — universal patterns
kubectl get <resource>
kubectl get <resource> <name>
kubectl describe <resource> <name>
kubectl delete <resource> <name>
kubectl apply -f <file.yaml>
kubectl edit <resource> <name>    # opens in $EDITOR

# Output formats
kubectl get pods -o wide          # more columns
kubectl get pods -o yaml          # full YAML
kubectl get pods -o json          # full JSON
kubectl get pods -o jsonpath='{.items[0].metadata.name}'

# Watch for changes
kubectl get pods -w

# Sorting
kubectl get pods --sort-by='.metadata.creationTimestamp'
```

### kubectl config (kubeconfig)

```bash
# See current context (which cluster you're talking to)
kubectl config current-context

# List all contexts
kubectl config get-contexts

# Switch context
kubectl config use-context production-cluster

# Merge multiple kubeconfig files
KUBECONFIG=~/.kube/config:~/.kube/staging-config kubectl config view --merge --flatten > ~/.kube/merged-config
```

---

## Summary

| Component | Location | Role |
|-----------|---------|------|
| API Server | Control plane | All requests go here; persists to etcd |
| etcd | Control plane | Cluster state database |
| Scheduler | Control plane | Assigns pods to nodes |
| Controller Manager | Control plane | Reconciles desired vs actual state |
| kubelet | Worker node | Runs pods on the node |
| kube-proxy | Worker node | Network rules for Services |
| Container Runtime | Worker node | Actually runs containers |

**Next:** [02 — Setup Kubernetes →](./02-setup-kubernetes.md)
