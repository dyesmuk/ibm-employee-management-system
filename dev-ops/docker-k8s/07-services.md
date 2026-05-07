# Module 07 — Services

Pods are ephemeral — they get new IP addresses when rescheduled. A **Service** provides a stable endpoint (IP address and DNS name) that routes traffic to the right pods, regardless of which node they're on or how often they restart.

---

## 1. Why Services Exist

```
Without Service:
  Pod A → Pod B's IP (10.244.1.5) → Pod B crashes, gets new IP 10.244.1.7
                                     ← Pod A doesn't know the new IP

With Service:
  Pod A → web-service (10.96.100.50) → Service routes to healthy pods
                                        always the same address
```

Services provide:
- **Stable IP address** (ClusterIP) that doesn't change
- **DNS name** (`web-service.default.svc.cluster.local`)
- **Load balancing** across all matching pods
- **Discovery** — services are registered in cluster DNS

---

## 2. How Services Find Pods — Selectors

Services use **label selectors** to find their pods. The service sends traffic to any pod with matching labels.

```yaml
# Service selector:
selector:
  app: web
  tier: frontend

# Pods with these labels are automatically registered as endpoints:
# Pod 1 (10.244.1.4) — labels: app=web, tier=frontend  ✅
# Pod 2 (10.244.1.5) — labels: app=web, tier=frontend  ✅
# Pod 3 (10.244.2.3) — labels: app=api, tier=backend   ❌ not selected
```

```bash
# See which pods a service is routing to
kubectl get endpoints web-service
# NAME          ENDPOINTS                         AGE
# web-service   10.244.1.4:80,10.244.1.5:80,...   5m
```

---

## 3. ClusterIP — Internal Service

**ClusterIP** is the default service type. It creates a virtual IP accessible only within the cluster.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: web-service
  namespace: default
spec:
  type: ClusterIP   # default; optional to specify
  selector:
    app: web
  ports:
    - name: http
      protocol: TCP
      port: 80          # port on the Service (what pods call)
      targetPort: 8080  # port on the container (where app listens)
```

```
Client Pod → web-service:80 → Service → Pod container:8080
```

### Named ports

```yaml
# In the Deployment:
ports:
  - name: http
    containerPort: 8080

# In the Service, reference by name (more maintainable):
ports:
  - port: 80
    targetPort: http    # references the named port
```

### Headless Services (no ClusterIP)

```yaml
spec:
  clusterIP: None    # headless
  selector:
    app: postgres
```

With a headless service, DNS returns the **individual pod IPs** instead of a single virtual IP. Used for StatefulSets where clients need to connect to a specific pod (e.g., PostgreSQL primary vs replicas).

```bash
# Regular service DNS → single ClusterIP
nslookup web-service
# Address: 10.96.100.50

# Headless service DNS → individual pod IPs
nslookup postgres
# Address: 10.244.1.4    ← postgres-0
# Address: 10.244.1.5    ← postgres-1
# Address: 10.244.2.3    ← postgres-2
```

---

## 4. NodePort — External Access via Node

**NodePort** opens a port on **every node** in the cluster and forwards traffic to the Service.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: web-nodeport
spec:
  type: NodePort
  selector:
    app: web
  ports:
    - port: 80           # ClusterIP port (internal)
      targetPort: 8080   # container port
      nodePort: 30080    # node port (30000-32767); optional — auto-assigned if omitted
```

```
External client → Node IP:30080
                      ↓
              kube-proxy routes to
                      ↓
              web pod:8080
```

```bash
# Access the service
curl http://<ANY_NODE_IP>:30080

# Minikube
minikube service web-nodeport --url

# Get node IPs
kubectl get nodes -o wide
```

### NodePort limitations
- Port range limited to 30000-32767
- Every node exposes every NodePort — security concern
- Direct node IP exposure — not suitable for production load balancing
- Use LoadBalancer or Ingress for production

---

## 5. LoadBalancer — Cloud Load Balancer

**LoadBalancer** provisions an external load balancer in the cloud provider (AWS ELB, GCP LB, Azure LB) and assigns it a public IP.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: web-lb
  annotations:
    # AWS-specific: internal LB (not public-facing)
    service.beta.kubernetes.io/aws-load-balancer-internal: "true"
spec:
  type: LoadBalancer
  selector:
    app: web
  ports:
    - port: 80
      targetPort: 8080
```

```bash
kubectl get service web-lb
# NAME     TYPE           CLUSTER-IP     EXTERNAL-IP        PORT(S)
# web-lb   LoadBalancer   10.96.100.50   a1b2c3.elb.amazonaws.com   80:30045/TCP
#                                        ↑ cloud load balancer DNS
```

### Traffic flow

```
Internet
   │
   ▼
AWS ELB / GCP LB
   │
   ▼
NodePort (auto-created)
   │
   ▼
ClusterIP
   │
   ▼
Pod(s)
```

LoadBalancer builds on top of NodePort which builds on top of ClusterIP. All three layers exist simultaneously.

### LoadBalancer on local clusters (MetalLB)

```bash
# Install MetalLB for bare-metal / local clusters
kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.14.0/config/manifests/metallb-native.yaml

# Configure IP address pool
kubectl apply -f - <<EOF
apiVersion: metallb.io/v1beta1
kind: IPAddressPool
metadata:
  name: local-pool
  namespace: metallb-system
spec:
  addresses:
    - 192.168.1.200-192.168.1.250

---
apiVersion: metallb.io/v1beta1
kind: L2Advertisement
metadata:
  name: local
  namespace: metallb-system
EOF
```

---

## 6. ExternalName — DNS Alias

**ExternalName** maps a service name to an external DNS name. No proxying occurs — DNS CNAME record only.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: external-db
spec:
  type: ExternalName
  externalName: mydb.rds.amazonaws.com
```

Pods calling `external-db` get a CNAME to `mydb.rds.amazonaws.com`. Useful for:
- Migrating external databases into the cluster later (swap ExternalName → ClusterIP)
- Providing cluster-internal names for external services

---

## 7. Service Discovery Patterns

### Environment variables (legacy)

When a pod starts, Kubernetes injects environment variables for all services that existed at creation time:

```bash
# Inside a pod, see service env vars:
kubectl exec mypod -- env | grep WEB_SERVICE
# WEB_SERVICE_SERVICE_HOST=10.96.100.50
# WEB_SERVICE_SERVICE_PORT=80
```

Limitation: Only services that existed before the pod started.

### DNS (preferred)

```bash
# From any pod in the same namespace:
curl http://web-service/api/users

# From a different namespace:
curl http://web-service.production.svc.cluster.local/api/users

# Short form works from same namespace thanks to search domains:
curl http://web-service        → resolves via search: default.svc.cluster.local
```

---

## 8. Session Affinity

By default, each request is load balanced to a random pod. For session-based apps:

```yaml
spec:
  sessionAffinity: ClientIP   # route same client IP to same pod
  sessionAffinityConfig:
    clientIP:
      timeoutSeconds: 10800   # 3 hours
```

---

## 9. Service YAML — All Types Together

```yaml
# ClusterIP (internal only)
apiVersion: v1
kind: Service
metadata:
  name: api-service
spec:
  type: ClusterIP
  selector:
    app: api
  ports:
    - port: 3000
      targetPort: 3000

---

# NodePort (dev/testing external access)
apiVersion: v1
kind: Service
metadata:
  name: web-nodeport
spec:
  type: NodePort
  selector:
    app: web
  ports:
    - port: 80
      targetPort: 8080
      nodePort: 30080

---

# LoadBalancer (production external access)
apiVersion: v1
kind: Service
metadata:
  name: web-lb
spec:
  type: LoadBalancer
  selector:
    app: web
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080

---

# Headless (StatefulSet access)
apiVersion: v1
kind: Service
metadata:
  name: postgres
spec:
  clusterIP: None
  selector:
    app: postgres
  ports:
    - port: 5432
      targetPort: 5432
```

---

## 10. Service Commands Reference

```bash
# Create service imperatively
kubectl expose deployment web --port=80 --target-port=8080 --type=ClusterIP

# List services
kubectl get services
kubectl get svc               # shorthand
kubectl get svc -o wide

# Describe (see selector, endpoints, events)
kubectl describe svc web-service

# Get endpoints (which pod IPs are registered)
kubectl get endpoints web-service

# Port-forward for local testing (no LoadBalancer needed)
kubectl port-forward service/web-service 8080:80
kubectl port-forward pod/web-pod 8080:3000

# Delete
kubectl delete service web-service
```

---

## 11. Complete Application Stack Example

Here is a full microservice stack with Services wiring everything together:

```yaml
# Frontend Deployment + Service
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: frontend
  template:
    metadata:
      labels:
        app: frontend
    spec:
      containers:
        - name: nginx
          image: myapp-frontend:v1
          env:
            - name: API_URL
              value: http://api-service:3000

---
apiVersion: v1
kind: Service
metadata:
  name: frontend-service
spec:
  type: LoadBalancer
  selector:
    app: frontend
  ports:
    - port: 80
      targetPort: 80

---
# API Deployment + Service
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: api
  template:
    metadata:
      labels:
        app: api
    spec:
      containers:
        - name: api
          image: myapp-api:v1
          env:
            - name: DB_HOST
              value: postgres-service
            - name: REDIS_HOST
              value: redis-service

---
apiVersion: v1
kind: Service
metadata:
  name: api-service
spec:
  type: ClusterIP        # internal only — frontend calls it by name
  selector:
    app: api
  ports:
    - port: 3000
      targetPort: 3000

---
# Postgres StatefulSet + Headless Service
apiVersion: v1
kind: Service
metadata:
  name: postgres-service
spec:
  clusterIP: None         # headless
  selector:
    app: postgres
  ports:
    - port: 5432

---
# Redis + Service
apiVersion: v1
kind: Service
metadata:
  name: redis-service
spec:
  selector:
    app: redis
  ports:
    - port: 6379
```

---

## 12. Service Type Decision Tree

```
Need to expose the service?
├── NO (internal only)
│   └── ClusterIP  ✅
└── YES
    ├── For development/testing on local cluster
    │   └── NodePort
    ├── For production on cloud
    │   └── LoadBalancer (if just one service)
    │       or Ingress (if multiple services / path routing)
    └── For external DNS alias (no proxying)
        └── ExternalName
```

---

## Summary

| Type | Accessible from | Use case |
|------|----------------|---------|
| **ClusterIP** | Inside cluster only | Service-to-service communication |
| **NodePort** | Node IP + static port | Dev/test, bare-metal |
| **LoadBalancer** | Public IP via cloud LB | Production single service |
| **ExternalName** | Inside cluster (CNAME) | External service aliasing |
| **Headless** | Individual pod IPs via DNS | StatefulSets, custom discovery |
| **Ingress** | HTTP/HTTPS routing | Multiple services behind one IP |

**Next:** [08 — Conclusion →](./08-conclusion.md)
