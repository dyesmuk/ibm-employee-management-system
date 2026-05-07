# Module 06 — Networking in Kubernetes

Kubernetes networking is one of the most important and complex topics in the ecosystem. This module covers how pods communicate, how DNS works, network policies, and the CNI plugin system.

---

## 1. The Kubernetes Networking Model

Kubernetes imposes a flat networking model with four fundamental rules:

1. **Every Pod gets its own unique IP address**
2. **Pods on the same node can communicate without NAT**
3. **Pods on different nodes can communicate without NAT**
4. **The IP a pod sees for itself is the same IP others use to reach it**

```
Node 1                          Node 2
┌─────────────────────┐         ┌─────────────────────┐
│ Pod A: 10.244.1.4   │──────── │ Pod C: 10.244.2.3   │
│ Pod B: 10.244.1.5   │         │ Pod D: 10.244.2.4   │
└─────────────────────┘         └─────────────────────┘
         │                               │
         └─────── cluster network ───────┘
                 (10.244.0.0/16)
```

Pod A can directly reach Pod C at `10.244.2.3`. No NAT, no port mapping.

---

## 2. CNI — Container Network Interface

Kubernetes delegates pod networking to **CNI plugins**. The cluster installs one CNI plugin that implements the networking model.

| Plugin | Key features | Common use |
|--------|-------------|-----------|
| **Flannel** | Simple, overlay (VXLAN) | Learning, small clusters |
| **Calico** | BGP routing, Network Policies | Production, policy-heavy |
| **Cilium** | eBPF-based, observability | Modern production |
| **Weave Net** | Simple mesh, encrypted | Multi-cloud |
| **AWS VPC CNI** | Native VPC IPs | EKS |
| **GKE CNI** | GVE/VPC native | GKE |

```bash
# See which CNI is installed
kubectl get pods -n kube-system | grep -E "flannel|calico|cilium|weave"

# Calico installation example
kubectl apply -f https://docs.projectcalico.org/manifests/calico.yaml
```

---

## 3. Pod Networking Internals

When a pod is created on a node:

```
Linux network namespace created for the pod
                │
        veth pair created:
        ┌──────────────────────────────────────┐
        │   Pod namespace      Host namespace  │
        │   eth0 ◄────────────► vethXXXX       │
        │   10.244.1.4          (host bridge)  │
        └──────────────────────────────────────┘
                                    │
                               cni0 bridge
                                    │
                            other pod veth pairs
```

Multi-container pods share the same network namespace:

```bash
# Inside a pod with two containers — both see the same IP
kubectl exec -it mypod -c app -- ip addr
kubectl exec -it mypod -c sidecar -- ip addr
# Both show the same 10.244.x.x address
```

---

## 4. DNS in Kubernetes

Kubernetes runs a DNS server (**CoreDNS**) that provides DNS resolution for services and pods.

```bash
# See CoreDNS
kubectl get pods -n kube-system | grep coredns
kubectl get configmap coredns -n kube-system -o yaml
```

### Service DNS format

```
<service-name>.<namespace>.svc.cluster.local
```

```
web-service.default.svc.cluster.local
postgres.production.svc.cluster.local
```

### Resolution rules

From within a pod:
- `web-service` → resolves if in same namespace
- `web-service.production` → resolves across namespaces
- `web-service.production.svc.cluster.local` → fully qualified name (FQDN)

```bash
# Test DNS resolution from inside a pod
kubectl run dns-test --image=busybox --rm -it --restart=Never -- sh
# Inside the pod:
nslookup kubernetes
nslookup web-service
nslookup postgres.production.svc.cluster.local
cat /etc/resolv.conf
# nameserver 10.96.0.10       ← CoreDNS cluster IP
# search default.svc.cluster.local svc.cluster.local cluster.local
```

### Pod DNS format

```
<pod-ip-dashes>.<namespace>.pod.cluster.local

# Example: pod with IP 10.244.1.4 in namespace default:
10-244-1-4.default.pod.cluster.local
```

Pod DNS is mostly used for StatefulSets, where each pod gets a stable DNS name:
```
postgres-0.postgres.default.svc.cluster.local
postgres-1.postgres.default.svc.cluster.local
```

### Custom DNS configuration in pods

```yaml
spec:
  dnsPolicy: ClusterFirst   # default — use cluster DNS, fall back to host
  dnsConfig:
    nameservers:
      - 1.1.1.1
    searches:
      - mycompany.local
    options:
      - name: ndots
        value: "5"
```

| dnsPolicy | Meaning |
|-----------|---------|
| `ClusterFirst` | Use cluster DNS; fall back to host DNS |
| `ClusterFirstWithHostNet` | Same, but for pods with hostNetwork |
| `Default` | Inherit node's DNS configuration |
| `None` | No DNS; configure manually via `dnsConfig` |

---

## 5. Network Policies

By default, **all pods can communicate with all other pods**. Network Policies add firewall rules.

### Deny all ingress (default-deny)

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny-ingress
  namespace: production
spec:
  podSelector: {}      # applies to ALL pods in namespace
  policyTypes:
    - Ingress
  # No ingress rules = deny all ingress
```

### Allow specific ingress

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-api-to-db
  namespace: production
spec:
  podSelector:
    matchLabels:
      app: postgres       # this policy applies to postgres pods

  policyTypes:
    - Ingress

  ingress:
    - from:
        - podSelector:
            matchLabels:
              app: api     # only allow from api pods
          namespaceSelector:
            matchLabels:
              kubernetes.io/metadata.name: production
      ports:
        - protocol: TCP
          port: 5432
```

### Allow egress to external services

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-external-api
spec:
  podSelector:
    matchLabels:
      app: api
  policyTypes:
    - Egress
  egress:
    - to:
        - ipBlock:
            cidr: 0.0.0.0/0          # any external IP
            except:
              - 10.0.0.0/8           # except internal range
      ports:
        - protocol: TCP
          port: 443
    - to:
        - namespaceSelector: {}      # allow DNS (CoreDNS)
      ports:
        - protocol: UDP
          port: 53
```

### Common Network Policy patterns

```yaml
# 1. Default deny all (start here for zero-trust)
spec:
  podSelector: {}
  policyTypes: [Ingress, Egress]

# 2. Allow same-namespace communication
ingress:
  - from:
      - podSelector: {}

# 3. Allow from specific namespace
ingress:
  - from:
      - namespaceSelector:
          matchLabels:
            kubernetes.io/metadata.name: monitoring

# 4. Allow from external CIDR
ingress:
  - from:
      - ipBlock:
          cidr: 10.0.0.0/24
```

> ⚠️ Network Policies require a CNI that supports them (Calico, Cilium, Weave). Flannel does NOT enforce network policies.

---

## 6. Ingress (HTTP/HTTPS Routing)

An **Ingress** routes external HTTP/HTTPS traffic to Services inside the cluster. It requires an **Ingress Controller** to be installed.

```
Internet
    │
    ▼
Ingress Controller (nginx / traefik / AWS ALB)
    │
    ├──► /api/*        ──► api-service:3000
    ├──► /             ──► web-service:80
    └──► blog.example.com ──► blog-service:8080
```

### Install Nginx Ingress Controller (minikube)

```bash
minikube addons enable ingress
kubectl get pods -n ingress-nginx
```

### Install Nginx Ingress Controller (general)

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml
```

### Ingress YAML

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: app-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  ingressClassName: nginx

  tls:
    - hosts:
        - myapp.example.com
      secretName: tls-secret      # TLS cert stored in Secret

  rules:
    - host: myapp.example.com
      http:
        paths:
          - path: /api
            pathType: Prefix
            backend:
              service:
                name: api-service
                port:
                  number: 3000
          - path: /
            pathType: Prefix
            backend:
              service:
                name: web-service
                port:
                  number: 80

    - host: admin.example.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: admin-service
                port:
                  number: 8080
```

```bash
kubectl apply -f ingress.yaml
kubectl get ingress
kubectl describe ingress app-ingress
```

### Path types

| Type | Behaviour |
|------|-----------|
| `Exact` | Must match exactly |
| `Prefix` | Matches prefix; `/api` matches `/api/users`, `/api/orders` |
| `ImplementationSpecific` | Up to the controller |

---

## 7. Pod-to-Pod Communication Summary

```
Same Pod:
  Container A → localhost:port → Container B
  (share network namespace)

Same Node, different Pod:
  Pod A (10.244.1.4) → 10.244.1.5 → Pod B
  (via virtual bridge)

Different Nodes:
  Pod A (10.244.1.4) → 10.244.2.3 → Pod C
  (via CNI overlay/routing)

Pod → Service:
  Pod → ClusterIP (10.96.x.x) → kube-proxy → Pod endpoint
  (also via DNS: web-service.default.svc.cluster.local)

External → Cluster:
  Browser → LoadBalancer IP → NodePort → Service → Pod
  Or: Browser → Ingress Controller → Service → Pod
```

---

## 8. Debugging Networking

```bash
# Test connectivity between pods
kubectl run test-pod --image=busybox --rm -it --restart=Never -- sh
# Inside:
wget -qO- http://web-service
nslookup web-service
ping 10.244.1.4

# Check endpoints (are pods registered with the service?)
kubectl get endpoints web-service
kubectl describe endpoints web-service

# Check network policies affecting a pod
kubectl describe networkpolicy

# Check if CoreDNS is running
kubectl get pods -n kube-system -l k8s-app=kube-dns

# See all pod IPs
kubectl get pods -o wide
```

---

## Summary

| Concept | Key points |
|---------|-----------|
| Networking model | Every pod gets a unique routable IP; no NAT |
| CNI | Plugin implements the network model; Calico/Cilium for production |
| CoreDNS | Cluster-internal DNS; `svc-name.namespace.svc.cluster.local` |
| Network Policies | Pod-level firewall rules; requires CNI support |
| Ingress | HTTP/HTTPS routing into the cluster; requires Ingress Controller |

**Next:** [07 — Services →](./07-services.md)
