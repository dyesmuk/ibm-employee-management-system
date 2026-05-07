# Module 05 — PODs, ReplicaSets, Deployments

This module covers the three most important workload objects in Kubernetes — and how they layer on top of each other to provide a complete, self-healing deployment system.

---

## 1. The Hierarchy

```
Deployment
└── ReplicaSet
    ├── Pod 1 (nginx:v2)
    ├── Pod 2 (nginx:v2)
    └── Pod 3 (nginx:v2)
```

| Object | Responsibility |
|--------|---------------|
| **Pod** | Run the containers |
| **ReplicaSet** | Ensure the right number of pod replicas exist |
| **Deployment** | Manage ReplicaSets; enable rolling updates and rollback |

In practice, **you always work with Deployments** — never raw Pods or ReplicaSets directly (except to inspect them).

---

## 2. Pods in Depth

### Pod YAML — minimal

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx-pod
  labels:
    app: web
spec:
  containers:
    - name: nginx
      image: nginx:1.25
      ports:
        - containerPort: 80
```

```bash
kubectl apply -f pod.yaml
kubectl get pods
kubectl describe pod nginx-pod
kubectl logs nginx-pod
kubectl exec -it nginx-pod -- bash
kubectl delete pod nginx-pod
```

### Why you rarely create bare Pods

- If a node dies, bare pods are **not rescheduled**
- You can't scale a bare pod
- No rolling update support

Always use a higher-level controller (Deployment, StatefulSet, DaemonSet).

### Multiple containers in a Pod

```yaml
spec:
  containers:
    - name: app
      image: myapp:v1
      ports:
        - containerPort: 3000

    - name: log-shipper          # sidecar
      image: fluentd:v1.16
      volumeMounts:
        - name: logs
          mountPath: /var/log/app

  volumes:
    - name: logs
      emptyDir: {}               # shared between containers in this pod
```

```bash
# Exec into a specific container in a multi-container pod
kubectl exec -it mypod -c log-shipper -- sh
# Logs of a specific container
kubectl logs mypod -c log-shipper
# Follow all containers
kubectl logs mypod --all-containers -f
```

### Init Containers

```yaml
spec:
  initContainers:
    - name: wait-for-db
      image: busybox:1.36
      command:
        - sh
        - -c
        - |
          until nc -z db-service 5432; do
            echo "Waiting for database..."
            sleep 3
          done
          echo "Database is ready!"

  containers:
    - name: app
      image: myapp:v1
      # Only starts after ALL init containers complete successfully
```

Init containers:
- Run one at a time, in order
- Must exit with code 0 before app containers start
- Restart if they fail (until success or Pod fails)
- Have different images from app containers

---

## 3. ReplicaSets

A **ReplicaSet** ensures a specified number of identical pod replicas are always running.

### ReplicaSet YAML

```yaml
apiVersion: apps/v1
kind: ReplicaSet
metadata:
  name: web-rs
spec:
  replicas: 3
  selector:
    matchLabels:
      app: web         # manages all pods with this label
  template:            # pod template — same as a Pod spec under here
    metadata:
      labels:
        app: web       # must match selector above
    spec:
      containers:
        - name: nginx
          image: nginx:1.25
          ports:
            - containerPort: 80
```

### How ReplicaSets maintain replicas

```
Desired: 3 pods
─────────────────────────────────────────
Actual: 3 pods    → do nothing
Actual: 2 pods    → create 1 new pod
Actual: 4 pods    → delete 1 pod (even if not created by this RS!)
Actual: 0 pods    → create 3 new pods
```

The selector is critical: the ReplicaSet **adopts** any pod matching its selector, even if it didn't create it.

### ReplicaSet commands

```bash
kubectl get replicasets
kubectl get rs             # shorthand
kubectl describe rs web-rs
kubectl scale rs web-rs --replicas=5
kubectl delete rs web-rs   # deletes RS and all its pods
kubectl delete rs web-rs --cascade=orphan  # delete RS, keep pods
```

### Why not use ReplicaSets directly?

ReplicaSets don't support **rolling updates**. If you change the image from `nginx:1.24` to `nginx:1.25` in a ReplicaSet, existing pods are not updated — only new pods created after the change get the new image.

**Deployments** solve this. They manage ReplicaSets for you.

---

## 4. Deployments

A **Deployment** is the standard way to run stateless applications on Kubernetes. It manages ReplicaSets to provide:
- Declarative updates
- Rolling updates with configurable strategy
- Rollback to previous versions

### Deployment YAML

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: web-deployment
  labels:
    app: web
spec:
  replicas: 3

  selector:
    matchLabels:
      app: web

  # Rolling update strategy (default)
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1     # max pods that can be down during update
      maxSurge: 1           # max extra pods above desired count

  template:
    metadata:
      labels:
        app: web            # must match selector
    spec:
      containers:
        - name: nginx
          image: nginx:1.25-alpine
          ports:
            - containerPort: 80
          resources:
            requests:
              cpu: "100m"
              memory: "64Mi"
            limits:
              cpu: "500m"
              memory: "256Mi"
          readinessProbe:
            httpGet:
              path: /
              port: 80
            initialDelaySeconds: 5
            periodSeconds: 5
```

### Deployment commands

```bash
# Create deployment imperatively
kubectl create deployment web --image=nginx:1.25 --replicas=3

# From YAML
kubectl apply -f deployment.yaml

# List deployments
kubectl get deployments
kubectl get deploy                # shorthand

# See rollout status
kubectl rollout status deployment/web-deployment

# See ReplicaSets the deployment manages
kubectl get rs

# See pods
kubectl get pods -l app=web

# Describe
kubectl describe deployment web-deployment
```

---

## 5. Updating a Deployment

### Update the image (triggers a rolling update)

```bash
# Imperative
kubectl set image deployment/web-deployment nginx=nginx:1.26

# Declarative — edit the YAML and apply
kubectl apply -f deployment.yaml

# Edit in-place
kubectl edit deployment web-deployment
```

### Annotate for change tracking

```bash
kubectl annotate deployment web-deployment \
  kubernetes.io/change-cause="Update to nginx 1.26 for CVE fix"
```

---

## 6. Rolling Update Strategy

```
Before update: [v1][v1][v1]  desired=3

With maxUnavailable=1, maxSurge=1:

Step 1: Start 1 new pod    [v1][v1][v1][v2]   (4 pods — surge by 1)
Step 2: Remove 1 old pod   [v1][v1][v2]       (back to 3 — 1 unavailable)
Step 3: Start 1 new pod    [v1][v1][v2][v2]
Step 4: Remove 1 old pod   [v1][v2][v2]
Step 5: Start 1 new pod    [v1][v2][v2][v2]
Step 6: Remove 1 old pod   [v2][v2][v2]  ✅ done

Old RS:  replicas reduced from 3 → 0
New RS:  replicas increased from 0 → 3
```

### Recreate strategy (causes downtime)

```yaml
strategy:
  type: Recreate    # Kill ALL old pods, then create new ones
                    # Causes downtime — use when you can't run v1 and v2 simultaneously
```

---

## 7. Rollback

```bash
# View rollout history
kubectl rollout history deployment/web-deployment
# REVISION   CHANGE-CAUSE
# 1          Initial deployment
# 2          Update to nginx 1.26 for CVE fix
# 3          Update to nginx 1.27

# See details of a specific revision
kubectl rollout history deployment/web-deployment --revision=2

# Rollback to the previous revision
kubectl rollout undo deployment/web-deployment

# Rollback to a specific revision
kubectl rollout undo deployment/web-deployment --to-revision=1

# Check rollback status
kubectl rollout status deployment/web-deployment
```

### How rollbacks work

Deployments keep a history of ReplicaSets. Rollback simply scales up the old RS and scales down the new one — same mechanism as a forward update, in reverse.

```bash
# See all ReplicaSets (including old ones kept for rollback)
kubectl get rs
# NAME                  DESIRED  CURRENT  READY  AGE
# web-deployment-abc    3        3        3      5m    ← active (v2)
# web-deployment-xyz    0        0        0      1h    ← previous (v1)
```

Control history depth:

```yaml
spec:
  revisionHistoryLimit: 10   # default is 10; set to 0 to disable rollback
```

---

## 8. Scaling

```bash
# Imperative scaling
kubectl scale deployment web-deployment --replicas=10

# Edit the YAML and apply (declarative)
kubectl apply -f deployment.yaml   # with replicas: 10 in the file

# Autoscaling (Horizontal Pod Autoscaler)
kubectl autoscale deployment web-deployment \
  --min=3 --max=20 --cpu-percent=70

# See HPA status
kubectl get hpa
```

### HPA YAML

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: web-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: web-deployment
  minReplicas: 3
  maxReplicas: 20
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: AverageValue
          averageValue: 200Mi
```

---

## 9. Other Workload Controllers

### DaemonSet — one pod per node

```yaml
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: log-collector
spec:
  selector:
    matchLabels:
      app: log-collector
  template:
    metadata:
      labels:
        app: log-collector
    spec:
      containers:
        - name: fluentd
          image: fluentd:v1.16
          volumeMounts:
            - name: varlog
              mountPath: /var/log
      volumes:
        - name: varlog
          hostPath:
            path: /var/log
```

Use DaemonSets for: log collectors, monitoring agents, network plugins — anything that must run on every node.

### StatefulSet — ordered, stable identity pods

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgres
spec:
  serviceName: postgres    # headless service name
  replicas: 3
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
        - name: postgres
          image: postgres:15
          volumeMounts:
            - name: data
              mountPath: /var/lib/postgresql/data
  volumeClaimTemplates:    # each pod gets its own PVC
    - metadata:
        name: data
      spec:
        accessModes: [ReadWriteOnce]
        resources:
          requests:
            storage: 10Gi
```

StatefulSets provide:
- Stable, predictable pod names (`postgres-0`, `postgres-1`, `postgres-2`)
- Ordered, sequential rolling updates
- Stable network identity (DNS: `postgres-0.postgres.default.svc.cluster.local`)
- Per-pod persistent storage

Use for: databases, Kafka, Zookeeper, Elasticsearch.

### Job — run to completion

```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: db-migrate
spec:
  completions: 1
  parallelism: 1
  backoffLimit: 3      # retry on failure up to 3 times
  template:
    spec:
      containers:
        - name: migrate
          image: myapp:v2
          command: ["npm", "run", "migrate"]
      restartPolicy: OnFailure   # required for Jobs
```

### CronJob — scheduled jobs

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: daily-backup
spec:
  schedule: "0 2 * * *"   # cron format: 2 AM every day
  jobTemplate:
    spec:
      template:
        spec:
          containers:
            - name: backup
              image: backup-tool:v1
              command: ["/backup.sh"]
          restartPolicy: OnFailure
```

---

## 10. Deployment Patterns and Strategies

### Blue-Green Deployment

Run two identical environments; switch traffic instantly:

```bash
# Blue (current): deployment/web-blue  (v1)
# Green (new):    deployment/web-green (v2)

# Switch service from blue to green
kubectl patch service web-svc \
  -p '{"spec":{"selector":{"version":"v2"}}}'

# Rollback is instant — patch service back
kubectl patch service web-svc \
  -p '{"spec":{"selector":{"version":"v1"}}}'
```

### Canary Deployment

Gradually shift traffic to the new version:

```bash
# v1: 9 replicas    (90% traffic via shared Service selector)
# v2: 1 replica     (10% traffic)

kubectl scale deployment web-v1 --replicas=9
kubectl scale deployment web-v2 --replicas=1

# Gradually increase v2, decrease v1
kubectl scale deployment web-v1 --replicas=5
kubectl scale deployment web-v2 --replicas=5
```

---

## 11. Practical Full Deployment Walkthrough

```bash
# 1. Create namespace
kubectl create namespace demo

# 2. Apply deployment
kubectl apply -f - <<EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: web
  namespace: demo
spec:
  replicas: 3
  selector:
    matchLabels:
      app: web
  template:
    metadata:
      labels:
        app: web
    spec:
      containers:
        - name: nginx
          image: nginx:1.24
          ports:
            - containerPort: 80
          readinessProbe:
            httpGet:
              path: /
              port: 80
            initialDelaySeconds: 5
EOF

# 3. Watch rollout
kubectl rollout status deployment/web -n demo

# 4. Verify
kubectl get pods -n demo

# 5. Update image
kubectl set image deployment/web nginx=nginx:1.25 -n demo
kubectl annotate deployment/web kubernetes.io/change-cause="Update to 1.25" -n demo

# 6. Watch rolling update
kubectl rollout status deployment/web -n demo

# 7. Check history
kubectl rollout history deployment/web -n demo

# 8. Rollback
kubectl rollout undo deployment/web -n demo

# 9. Scale
kubectl scale deployment web --replicas=5 -n demo

# 10. Clean up
kubectl delete namespace demo
```

---

## Summary

| Object | Use for | Key difference |
|--------|---------|----------------|
| **Pod** | Smallest unit; runs containers | Not self-healing, no scaling |
| **ReplicaSet** | Maintain N pod replicas | No rolling update |
| **Deployment** | Stateless apps | Rolling updates + rollback |
| **DaemonSet** | One pod per node | Node-level daemons |
| **StatefulSet** | Stateful apps (DBs) | Stable identity + ordered updates |
| **Job** | One-off tasks | Run to completion |
| **CronJob** | Scheduled tasks | Run on a schedule |

**Next:** [06 — Networking in Kubernetes →](./06-networking.md)
