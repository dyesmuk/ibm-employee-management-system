# Module 03 — Kubernetes Concepts

This module introduces the fundamental concepts and objects that form the vocabulary of Kubernetes. Understanding these building blocks is essential before working with any real workload.

---

## 1. The Object Model

Every resource in Kubernetes is an **object** with a consistent structure:

```yaml
apiVersion: apps/v1          # API group + version
kind: Deployment             # object type
metadata:                    # identity and metadata
  name: web-server
  namespace: production
  labels:
    app: web
    version: v2
  annotations:
    description: "Frontend web server"
spec:                        # desired state — you write this
  replicas: 3
  ...
status:                      # actual state — Kubernetes fills this
  readyReplicas: 3
  conditions: [...]
```

> **Rule of thumb:** You write `spec`. Kubernetes writes `status`.

---

## 2. API Groups and Versions

Not all Kubernetes resources live in the same API group:

| API Version | Resources |
|-------------|----------|
| `v1` (core) | Pod, Service, ConfigMap, Secret, Namespace, PersistentVolume |
| `apps/v1` | Deployment, ReplicaSet, DaemonSet, StatefulSet |
| `batch/v1` | Job, CronJob |
| `autoscaling/v2` | HorizontalPodAutoscaler |
| `networking.k8s.io/v1` | Ingress, NetworkPolicy |
| `rbac.authorization.k8s.io/v1` | Role, ClusterRole, RoleBinding |
| `storage.k8s.io/v1` | StorageClass |

```bash
# See all available API resources
kubectl api-resources

# See available API versions
kubectl api-versions
```

---

## 3. Pods

A **Pod** is the smallest deployable unit in Kubernetes. It wraps one or more containers that share:
- The same network namespace (same IP address, same ports)
- The same storage volumes
- The same lifecycle

```
Pod: 172.17.0.4
┌──────────────────────────────────────────┐
│  Container: web (nginx)    port 80       │
│  Container: log-shipper    (sidecar)     │
│  Volume: /app/logs  ────────────────────►│── shared storage
└──────────────────────────────────────────┘
```

### Why pods, not just containers?

Pods exist to support multi-container patterns:

| Pattern | Description | Example |
|---------|-------------|---------|
| **Sidecar** | Helper container alongside main | Log shipper, service mesh proxy |
| **Init container** | Run before main containers start | Database migration, secrets fetching |
| **Ambassador** | Proxy traffic to/from main container | Local proxy to remote service |
| **Adapter** | Normalise output of main container | Transform logs to standard format |

### Pod lifecycle

```
Pending → Running → Succeeded
                 ↘ Failed
                 ↘ Unknown
```

| Phase | Meaning |
|-------|---------|
| `Pending` | Pod accepted; containers not yet started |
| `Running` | At least one container is running |
| `Succeeded` | All containers exited successfully (code 0) |
| `Failed` | At least one container exited with failure |
| `Unknown` | Node communication lost |

### Pod conditions

```bash
kubectl describe pod <name>
# Conditions:
#   Initialized    True
#   Ready          True
#   PodScheduled   True
#   ContainersReady True
```

---

## 4. Container Probes (Health Checks)

Kubernetes can check whether a container is alive and ready to receive traffic.

### Liveness Probe

*Is the container alive? Should it be restarted?*

```yaml
livenessProbe:
  httpGet:
    path: /healthz
    port: 3000
  initialDelaySeconds: 15   # wait before first check
  periodSeconds: 20          # check every 20 seconds
  failureThreshold: 3        # restart after 3 failures
```

### Readiness Probe

*Is the container ready to receive traffic?*

```yaml
readinessProbe:
  httpGet:
    path: /ready
    port: 3000
  initialDelaySeconds: 5
  periodSeconds: 10
  failureThreshold: 3
```

If the readiness probe fails, the pod is **removed from Service endpoints** (no traffic sent) but NOT restarted.

### Startup Probe

*Has the container finished starting up? (for slow-starting apps)*

```yaml
startupProbe:
  httpGet:
    path: /healthz
    port: 3000
  failureThreshold: 30      # 30 * 10s = 5 minutes to start
  periodSeconds: 10
```

### Probe types

```yaml
# HTTP GET
httpGet:
  path: /health
  port: 8080
  httpHeaders:
    - name: Authorization
      value: Bearer mytoken

# TCP socket (just checks port is open)
tcpSocket:
  port: 5432

# Execute command in container
exec:
  command:
    - pg_isready
    - -U
    - postgres
```

---

## 5. Resource Requests and Limits

```yaml
resources:
  requests:               # minimum guaranteed resources
    memory: "128Mi"       # 128 mebibytes
    cpu: "250m"           # 250 millicores = 0.25 CPU core
  limits:                 # maximum allowed resources
    memory: "512Mi"
    cpu: "1"              # 1 full CPU core
```

### Requests vs Limits

| | Requests | Limits |
|--|---------|--------|
| Purpose | Scheduling (node must have this available) | Enforcement (container cannot exceed this) |
| Memory enforcement | — | Container OOM-killed if exceeded |
| CPU enforcement | — | Container throttled if exceeded |
| Effect on scheduling | Node must have enough available | No effect on scheduling |

### CPU units

```
1 CPU = 1000m (millicores)

250m = 0.25 of a CPU core
500m = 0.50 of a CPU core
1    = 1 full CPU core
2    = 2 full CPU cores
```

### Memory units

```
Ki = kibibyte (1024 bytes)
Mi = mebibyte (1024 Ki)
Gi = gibibyte (1024 Mi)

128Mi = 134,217,728 bytes
1Gi   = 1,073,741,824 bytes
```

---

## 6. ConfigMaps

**ConfigMaps** store non-sensitive configuration as key-value pairs, decoupled from application code.

### Creating ConfigMaps

```bash
# From literal values
kubectl create configmap app-config \
  --from-literal=DB_HOST=postgres \
  --from-literal=LOG_LEVEL=info

# From a file
kubectl create configmap nginx-config \
  --from-file=nginx.conf

# From a directory of files
kubectl create configmap app-configs \
  --from-file=./configs/
```

### ConfigMap YAML

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  DB_HOST: "postgres"
  DB_PORT: "5432"
  LOG_LEVEL: "info"
  app.properties: |          # multi-line file content
    server.port=8080
    server.debug=false
```

### Using ConfigMaps in Pods

```yaml
spec:
  containers:
    - name: app
      image: myapp:v1
      # Inject all keys as environment variables
      envFrom:
        - configMapRef:
            name: app-config

      # Inject specific keys
      env:
        - name: DATABASE_HOST
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: DB_HOST

      # Mount as a volume (each key becomes a file)
      volumeMounts:
        - name: config-vol
          mountPath: /etc/config
  volumes:
    - name: config-vol
      configMap:
        name: app-config
```

---

## 7. Secrets

**Secrets** store sensitive data — passwords, tokens, TLS certificates. They are base64-encoded (not encrypted by default — enable encryption at rest in production).

### Creating Secrets

```bash
# From literal values
kubectl create secret generic db-credentials \
  --from-literal=username=admin \
  --from-literal=password=s3cr3t

# From files (e.g., TLS certificates)
kubectl create secret tls tls-cert \
  --cert=server.crt \
  --key=server.key

# Docker registry credentials
kubectl create secret docker-registry regcred \
  --docker-server=registry.example.com \
  --docker-username=user \
  --docker-password=pass \
  --docker-email=user@example.com
```

### Secret YAML (values are base64)

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: db-credentials
type: Opaque
data:
  username: YWRtaW4=       # echo -n 'admin' | base64
  password: czNjcjN0       # echo -n 's3cr3t' | base64
# OR use stringData (plain text — K8s encodes it)
stringData:
  username: admin
  password: s3cr3t
```

### Using Secrets in Pods

```yaml
spec:
  containers:
    - name: app
      image: myapp:v1
      env:
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password

      # Mount as files in /etc/secrets/
      volumeMounts:
        - name: secret-vol
          mountPath: /etc/secrets
          readOnly: true

  volumes:
    - name: secret-vol
      secret:
        secretName: db-credentials
```

### Secret types

| Type | Use for |
|------|---------|
| `Opaque` | Generic key-value (default) |
| `kubernetes.io/tls` | TLS certificates |
| `kubernetes.io/dockerconfigjson` | Registry credentials |
| `kubernetes.io/service-account-token` | Service account tokens |

> **Production note:** Enable encryption at rest (`EncryptionConfiguration`) and consider external secret managers (HashiCorp Vault, AWS Secrets Manager, External Secrets Operator).

---

## 8. Namespaces

Namespaces divide a cluster into virtual isolation zones:

```bash
# List namespaces
kubectl get ns

# Create a namespace
kubectl create namespace staging

# Deploy into a namespace
kubectl apply -f deployment.yaml -n staging

# List resources across all namespaces
kubectl get pods -A
kubectl get pods --all-namespaces
```

### Resource Quotas (per namespace)

```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: staging-quota
  namespace: staging
spec:
  hard:
    pods: "20"
    requests.cpu: "4"
    requests.memory: "8Gi"
    limits.cpu: "8"
    limits.memory: "16Gi"
    persistentvolumeclaims: "10"
```

### LimitRange (default resource constraints)

```yaml
apiVersion: v1
kind: LimitRange
metadata:
  name: default-limits
  namespace: staging
spec:
  limits:
    - type: Container
      default:
        cpu: "500m"
        memory: "256Mi"
      defaultRequest:
        cpu: "100m"
        memory: "64Mi"
      max:
        cpu: "2"
        memory: "2Gi"
```

---

## 9. Node Selectors and Affinity

### nodeSelector (simple)

```yaml
spec:
  nodeSelector:
    disktype: ssd           # only run on nodes with this label
    kubernetes.io/arch: amd64
```

```bash
# Label a node
kubectl label node worker-1 disktype=ssd
```

### Node Affinity (expressive)

```yaml
spec:
  affinity:
    nodeAffinity:
      # Hard requirement
      requiredDuringSchedulingIgnoredDuringExecution:
        nodeSelectorTerms:
          - matchExpressions:
              - key: kubernetes.io/arch
                operator: In
                values: [amd64, arm64]
      # Soft preference
      preferredDuringSchedulingIgnoredDuringExecution:
        - weight: 80
          preference:
            matchExpressions:
              - key: disktype
                operator: In
                values: [ssd]
```

### Pod Affinity / Anti-Affinity

```yaml
affinity:
  # Spread pods across different availability zones
  podAntiAffinity:
    preferredDuringSchedulingIgnoredDuringExecution:
      - weight: 100
        podAffinityTerm:
          labelSelector:
            matchLabels:
              app: web
          topologyKey: topology.kubernetes.io/zone
```

---

## 10. Taints and Tolerations

Taints prevent pods from being scheduled on certain nodes. Tolerations allow specific pods to "tolerate" the taint.

```bash
# Taint a node (e.g., reserve for GPU workloads)
kubectl taint nodes gpu-node-1 gpu=true:NoSchedule

# Remove taint
kubectl taint nodes gpu-node-1 gpu=true:NoSchedule-
```

```yaml
# Only GPU pods tolerate this taint
spec:
  tolerations:
    - key: "gpu"
      operator: "Equal"
      value: "true"
      effect: "NoSchedule"
```

### Taint effects

| Effect | Behaviour |
|--------|-----------|
| `NoSchedule` | Don't schedule new pods without toleration |
| `PreferNoSchedule` | Try not to schedule; allowed if no choice |
| `NoExecute` | Don't schedule new; evict existing pods without toleration |

---

## Summary

| Concept | Key Points |
|---------|-----------|
| **Pod** | Smallest unit; wraps 1+ containers sharing network/storage |
| **Probes** | Liveness (restart?), Readiness (traffic?), Startup (slow apps) |
| **Resources** | Requests = guaranteed scheduling; Limits = enforced ceiling |
| **ConfigMap** | Non-sensitive config; inject as env vars or mounted files |
| **Secret** | Sensitive config; base64-encoded; enable encryption at rest |
| **Namespace** | Virtual cluster isolation; ResourceQuota limits usage |
| **Affinity** | Control which nodes pods land on |
| **Taints/Tolerations** | Reserve nodes for specific workloads |

**Next:** [04 — YAML Introduction →](./04-yaml-introduction.md)
