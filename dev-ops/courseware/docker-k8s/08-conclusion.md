# Module 08 — Conclusion

Congratulations on completing the Kubernetes course. This module consolidates everything you've learned and maps out where to go next.

---

## 1. What You've Learned

| Module | Core Skills |
|--------|-------------|
| **00 Introduction** | Why K8s exists, desired-state model, ecosystem overview |
| **01 Overview** | Control plane, worker nodes, all component roles |
| **02 Setup** | Minikube, kind, Docker Desktop, EKS/GKE/AKS |
| **03 Concepts** | Pods, probes, resources, ConfigMaps, Secrets, Namespaces, Affinity |
| **04 YAML** | Syntax, Kubernetes structure, kubectl explain, dry-run workflow |
| **05 Workloads** | Pod, ReplicaSet, Deployment, DaemonSet, StatefulSet, Job, CronJob |
| **06 Networking** | CNI, DNS, Network Policies, Ingress |
| **07 Services** | ClusterIP, NodePort, LoadBalancer, ExternalName, Headless |

---

## 2. The Kubernetes Mental Model

```
┌──────────────────────────────────────────────────────────┐
│  YOUR APPLICATION                                        │
│  What runs:  Pod (container)                            │
│  How many:   ReplicaSet                                 │
│  Lifecycle:  Deployment (update, rollback, scale)       │
├──────────────────────────────────────────────────────────┤
│  NETWORKING                                              │
│  Pod → Pod:      CNI, flat network, pod IPs             │
│  Stable endpoint: Service (ClusterIP / DNS)             │
│  External HTTP:   Ingress controller                    │
│  External TCP:    LoadBalancer service                  │
├──────────────────────────────────────────────────────────┤
│  CONFIGURATION                                           │
│  Non-sensitive:  ConfigMap                              │
│  Sensitive:      Secret                                 │
│  Isolation:      Namespace + ResourceQuota              │
├──────────────────────────────────────────────────────────┤
│  CLUSTER CONTROL                                         │
│  Scheduling:     Requests, nodeSelector, Affinity       │
│  Reserved nodes: Taints + Tolerations                   │
│  Self-healing:   Controllers watch and reconcile        │
└──────────────────────────────────────────────────────────┘
```

---

## 3. The Day-to-Day Kubernetes Workflow

### Deploying a new application

```bash
# 1. Write manifests
mkdir -p k8s/{base,overlays/staging,overlays/production}

# 2. Apply to staging
kubectl apply -f k8s/base/ -n staging
kubectl rollout status deployment/web -n staging

# 3. Check everything is healthy
kubectl get pods -n staging
kubectl logs -l app=web -n staging --tail=50

# 4. Run a smoke test
kubectl port-forward service/web-service 8080:80 -n staging
curl localhost:8080/health

# 5. Promote to production
kubectl apply -f k8s/base/ -n production
```

### Debugging a problem

```bash
# Pod not starting?
kubectl get pods                     # see status
kubectl describe pod <name>          # see Events section
kubectl logs <pod-name>              # see container logs
kubectl logs <pod-name> --previous   # logs before last restart

# CrashLoopBackOff?
kubectl logs <pod-name>              # what's the error?
kubectl describe pod <name>          # check exit code, OOM?

# Service not routing?
kubectl get endpoints <service>      # are pods registered?
kubectl describe service <service>   # check selector
kubectl run test --image=busybox --rm -it -- wget <service>:<port>

# Scheduling failed?
kubectl describe pod <name>          # look for: Insufficient cpu/memory
kubectl get nodes -o wide            # check node status
kubectl describe node <node>         # see conditions and resource pressure
```

---

## 4. Production Checklist

Before going to production, verify:

**Workloads**
- [ ] Deployments (not bare Pods) for all stateless services
- [ ] Resource `requests` and `limits` set on all containers
- [ ] Liveness and readiness probes configured
- [ ] `minReadySeconds` set to avoid premature traffic routing
- [ ] `revisionHistoryLimit` set on Deployments

**Configuration**
- [ ] Secrets used for all sensitive values (not ConfigMaps)
- [ ] Secrets encrypted at rest (`EncryptionConfiguration`)
- [ ] No secrets in environment variables — use mounted files or external secrets
- [ ] Namespaces per team/environment with `ResourceQuota` applied

**Networking**
- [ ] Network Policies — default deny all, explicit allow rules
- [ ] Ingress with TLS termination
- [ ] Services use `ClusterIP` unless external access is required

**Reliability**
- [ ] PodDisruptionBudgets (PDBs) configured for critical workloads
- [ ] Pod anti-affinity to spread replicas across nodes/zones
- [ ] HPA configured for services with variable load

**Security**
- [ ] Pods run as non-root (`securityContext.runAsNonRoot: true`)
- [ ] Read-only root filesystem where possible
- [ ] Least-privilege service accounts (not default)
- [ ] Image scanning in CI/CD pipeline

---

## 5. `kubectl` Quick Reference

```bash
# Resource operations
kubectl apply -f <file>             # create or update
kubectl delete -f <file>            # delete
kubectl get <resource>              # list
kubectl describe <resource> <name>  # details + events
kubectl edit <resource> <name>      # edit in-place
kubectl patch <resource> <name> -p '<json>'

# Common resources shorthand
kubectl get po    # pods
kubectl get svc   # services
kubectl get deploy # deployments
kubectl get rs    # replicasets
kubectl get cm    # configmaps
kubectl get ns    # namespaces
kubectl get no    # nodes

# Namespaces
kubectl get pods -n <namespace>
kubectl get pods -A               # all namespaces

# Debugging
kubectl logs <pod> [-c <container>] [-f] [--previous]
kubectl exec -it <pod> -- bash
kubectl port-forward <pod|svc> <local>:<remote>
kubectl top pods
kubectl top nodes

# Rollouts
kubectl rollout status deployment/<name>
kubectl rollout history deployment/<name>
kubectl rollout undo deployment/<name>
kubectl rollout pause deployment/<name>
kubectl rollout resume deployment/<name>

# Output formats
kubectl get pods -o wide
kubectl get pods -o yaml
kubectl get pods -o jsonpath='{.items[*].metadata.name}'

# Labels
kubectl get pods -l app=web
kubectl label pod <name> env=prod
kubectl annotate deployment <name> reason="fix CVE"
```

---

## 6. What's Next — The Learning Path

This course covered the core foundation. The Kubernetes ecosystem extends far beyond:

### Essential next topics

| Topic | Why it matters |
|-------|---------------|
| **Helm** | Package manager for Kubernetes; deploy complex apps with one command |
| **Kustomize** | Overlay-based config management built into kubectl |
| **RBAC** | Role-Based Access Control; who can do what in the cluster |
| **PersistentVolumes** | Storage provisioning for stateful workloads |
| **Horizontal Pod Autoscaler** | Auto-scale based on CPU, memory, custom metrics |
| **Vertical Pod Autoscaler** | Auto-tune resource requests |
| **PodDisruptionBudgets** | Control how many pods can be down during maintenance |
| **Service Mesh (Istio/Linkerd)** | mTLS, traffic management, observability |
| **GitOps (ArgoCD / Flux)** | Declarative, Git-driven deployments |

### Certifications

| Cert | Level | Focus |
|------|-------|-------|
| **CKA** (Certified Kubernetes Administrator) | Intermediate | Cluster administration, troubleshooting |
| **CKAD** (Certified Kubernetes Application Developer) | Intermediate | Deploying and managing applications |
| **CKS** (Certified Kubernetes Security Specialist) | Advanced | Security hardening |

All three are hands-on, practical exams — no multiple choice. The killer study tool is [killer.sh](https://killer.sh) (included with exam purchase).

### Recommended resources

- **Kubernetes Documentation** — [kubernetes.io/docs](https://kubernetes.io/docs) — the authoritative reference
- **Kubernetes the Hard Way** (Kelsey Hightower) — build a cluster from scratch to understand internals
- **CKAD/CKA prep** — [KodeKloud](https://kodekloud.com) has excellent labs
- **Production examples** — [kubernetes/examples](https://github.com/kubernetes/examples)

---

## 7. Key Commands Summary — Docker + Kubernetes

```bash
# Docker
docker build -t myapp:v1 .
docker push registry/myapp:v1
docker compose up -d

# Kubernetes — deploy
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

# Kubernetes — status
kubectl get pods -o wide
kubectl get services
kubectl rollout status deployment/web

# Kubernetes — debug
kubectl describe pod <name>
kubectl logs <pod> -f
kubectl exec -it <pod> -- bash

# Kubernetes — update
kubectl set image deployment/web app=myapp:v2
kubectl rollout undo deployment/web

# Kubernetes — scale
kubectl scale deployment web --replicas=10
```

---

## Final Thoughts

Kubernetes is a large, complex system — but its complexity exists to solve genuinely hard problems. Every piece of machinery you've learned (pods, deployments, services, controllers) is there to answer a specific production challenge.

The best way to solidify this knowledge is to:
1. **Run a real app** on your local cluster (minikube or kind)
2. **Break things intentionally** — delete pods, kill a node, watch the self-healing
3. **Read the events** — `kubectl describe` is endlessly educational
4. **Deploy to a cloud cluster** — EKS, GKE, or AKS free tiers exist

The **Appendix** covers building a production-ready multi-node cluster with Kubeadm — the foundation for understanding what managed services do under the hood.
