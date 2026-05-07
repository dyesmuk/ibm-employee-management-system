# Kubernetes — Hands-On Guide (Node.js + Express)

> **Prerequisites:** You have completed the Docker module. You have the image `yourname/hello-express:1.0` pushed to Docker Hub. Docker Desktop is installed and running on your Windows 11 laptop.

---

## The Bridge from Docker to Kubernetes

In Docker you learned to run containers. In Kubernetes you learn to *manage* them — at scale, with self-healing, rolling updates, and service discovery built in.

| Docker | Kubernetes |
|---|---|
| Runs containers on **one machine** | Runs containers across **many machines** |
| You manage containers manually | Kubernetes manages them for you |
| A container dies → it stays dead | A container dies → Kubernetes restarts it |
| Scaling = you run `docker run` many times | Scaling = you change one number in a file |
| Networking = port mapping | Networking = automatic service discovery |

The mental model shift:

```
Docker:   You tell it WHAT to run and HOW.
          docker run -p 3000:3000 yourname/hello-express

Kubernetes: You tell it WHAT you WANT.
            "I want 3 copies of this app always running."
            Kubernetes figures out HOW and keeps it that way.
```

This is called **declarative** infrastructure. You describe the desired state. Kubernetes continuously works to match reality to that description.

---

## Step 1 — Setting Up Kubernetes Locally

### Enable Kubernetes in Docker Desktop

Docker Desktop ships with a single-node Kubernetes cluster built in. No separate install needed.

1. Open Docker Desktop → **Settings** → **Kubernetes**
2. Check **Enable Kubernetes**
3. Click **Apply & Restart**
4. Wait for the green Kubernetes indicator (bottom-left of Docker Desktop)

### Verify the install

```bash
# Check kubectl is available
kubectl version --client

# Check the cluster is running
kubectl cluster-info

# See the single node
kubectl get nodes
```

Expected output:
```
NAME             STATUS   ROLES           AGE   VERSION
docker-desktop   Ready    control-plane   1m    v1.29.x
```

`STATUS: Ready` means Kubernetes is running and your node is healthy.

### What is `kubectl`?

`kubectl` is the command-line tool for Kubernetes — exactly like `docker` is for Docker. Every interaction with the cluster goes through `kubectl`.

```
docker build / run / ps / stop
kubectl apply / get / describe / delete
```

---

## YAML — The Language of Kubernetes

Every Kubernetes object is defined in a YAML file. Before writing your first Pod, understand these four fields that appear in **every** Kubernetes YAML:

```yaml
apiVersion: apps/v1        # Which Kubernetes API to use
kind: Deployment           # What type of object this is
metadata:                  # Information about the object
  name: hello-app
spec:                      # The actual desired state — what you want
  ...
```

| Field | Purpose |
|---|---|
| `apiVersion` | Which version of the Kubernetes API handles this resource |
| `kind` | The type of object: Pod, Deployment, Service, etc. |
| `metadata` | Name, labels, namespace — how you identify this object |
| `spec` | The desired state — the heart of every YAML file |

YAML rules to remember:
- Indentation = structure. Use 2 spaces. **Never tabs.**
- `key: value` for single values
- `-` for list items
- Colons and spacing are significant

---

## Step 2 — Your First Pod

A **Pod** is the smallest deployable unit in Kubernetes. It wraps one or more containers. Think of it as a thin envelope around your Docker container.

### Create `pod.yaml`

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: hello-pod
  labels:
    app: hello-app
spec:
  containers:
    - name: hello-container
      image: yourname/hello-express:1.0
      ports:
        - containerPort: 3000
```

Replace `yourname` with your Docker Hub username.

### Apply it

```bash
# Create the Pod from the YAML file
kubectl apply -f pod.yaml

# See it running
kubectl get pods

# See full details
kubectl describe pod hello-pod

# See logs from the container inside the Pod
kubectl logs hello-pod
```

Expected output of `kubectl get pods`:
```
NAME        READY   STATUS    RESTARTS   AGE
hello-pod   1/1     Running   0          30s
```

`1/1` means 1 container is running out of 1 total. `STATUS: Running` means the container is alive.

### Access the app (temporary)

Pods are not directly accessible from outside the cluster. Use `port-forward` for testing:

```bash
kubectl port-forward pod/hello-pod 3000:3000
```

Browser → `http://localhost:3000` → **Hello from Express inside Docker!**

Press `Ctrl+C` to stop port-forwarding. The Pod keeps running.

### Clean up

```bash
kubectl delete pod hello-pod

# Or delete using the same file you used to create it
kubectl delete -f pod.yaml
```

### The Mental Model

```
Docker container         →      Kubernetes Pod
"Just run this container"        "Run this container, give it
                                  a name, a label, and integrate
                                  it into the cluster"

One-off, manual              Managed, part of a system
```

A Pod by itself is not resilient — if it crashes, it stays dead. That's what Deployments are for.

### Three Questions to Ask Trainees

**1. "What happens if you delete the Pod?"**
→ It's gone and stays gone. A Pod on its own has no self-healing. This motivates the next step — Deployments.

**2. "The image is `yourname/hello-express:1.0`. Where does Kubernetes pull it from?"**
→ Docker Hub. Kubernetes uses the same registry you pushed to in the Docker module. The image name is identical.

**3. "What does `labels: app: hello-app` do right now?"**
→ Nothing visible yet. Labels are just metadata — but they become critical in the next steps when Services use them to find Pods.

---

## Step 3 — Deployments

A **Deployment** is how you actually run apps in Kubernetes. It does two things:

1. **ReplicaSet** — maintains a specified number of identical Pods running at all times
2. **Self-healing** — if a Pod crashes, the Deployment replaces it automatically

### Create `deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hello-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: hello-app
  template:
    metadata:
      labels:
        app: hello-app
    spec:
      containers:
        - name: hello-container
          image: yourname/hello-express:1.0
          ports:
            - containerPort: 3000
```

### Apply it

```bash
kubectl apply -f deployment.yaml

# See the Deployment
kubectl get deployments

# See the Pods it created
kubectl get pods

# See the ReplicaSet behind it
kubectl get replicasets
```

Expected output of `kubectl get pods`:
```
NAME                                READY   STATUS    RESTARTS   AGE
hello-deployment-7d9f5c8b6-4xk2p   1/1     Running   0          30s
hello-deployment-7d9f5c8b6-9qhvn   1/1     Running   0          30s
hello-deployment-7d9f5c8b6-kmt7s   1/1     Running   0          30s
```

Three Pods — one per replica — each with an auto-generated name.

### How the YAML fields connect

```
Deployment
  └── spec.selector.matchLabels: app=hello-app   ← "Manage Pods with this label"
  └── spec.template                               ← "Here's the Pod blueprint"
        └── metadata.labels: app=hello-app        ← "Give every Pod this label"
        └── spec.containers                        ← "Run this container in each Pod"
```

The `selector` tells the Deployment which Pods to manage. The `template` tells it how to create them. The labels must match.

### Demo: Self-healing

```bash
# Get the Pod names
kubectl get pods

# Delete one Pod by name
kubectl delete pod hello-deployment-7d9f5c8b6-4xk2p

# Immediately watch what happens
kubectl get pods
```

Within seconds, a new Pod appears. The Deployment noticed one Pod was missing and created a replacement automatically. This is Kubernetes' core promise.

### Scaling

Change one number:

```bash
# Scale up to 5 replicas
kubectl scale deployment hello-deployment --replicas=5

kubectl get pods
```

```bash
# Scale back down
kubectl scale deployment hello-deployment --replicas=2

kubectl get pods
```

Or edit the YAML, change `replicas: 3` to `replicas: 5`, and apply:

```bash
kubectl apply -f deployment.yaml
```

### Three Questions to Ask Trainees

**1. "If you delete a Pod that belongs to a Deployment, what happens?"**
→ Kubernetes immediately creates a replacement. The Deployment maintains the desired replica count.

**2. "In `docker-compose.yml` how did you run 3 copies of a container?"**
→ You couldn't easily — Compose is for single-host. `replicas: 3` in Kubernetes spans across nodes in a real cluster.

**3. "What's the difference between `kubectl apply` and `kubectl create`?"**
→ `apply` is declarative — if the object exists, it updates it. `create` fails if the object already exists. Always use `apply`.

---

## Step 4 — Services

Three Pods are running. But they each have their own private IP address that changes every time a Pod is replaced. How does anything connect to them reliably?

**Services** solve this. A Service is a stable network endpoint that sits in front of a group of Pods and load-balances traffic across them.

```
Client Request
      ↓
  Service (stable IP + DNS name)
      ↓ (load balances)
  Pod 1   Pod 2   Pod 3
```

The Service finds its Pods using — labels. This is why you set `app: hello-app` on every Pod.

### Types of Services

| Type | What it does | When to use |
|---|---|---|
| `ClusterIP` | Accessible only inside the cluster | Service-to-service communication |
| `NodePort` | Exposes a port on every node's IP | Development and testing |
| `LoadBalancer` | Provisions a cloud load balancer | Production on AWS / GCP / Azure |

### Create `service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: hello-service
spec:
  type: NodePort
  selector:
    app: hello-app
  ports:
    - port: 80          # Port the Service listens on
      targetPort: 3000  # Port the container is running on
      nodePort: 30080   # Port exposed on the node (30000–32767)
```

### Apply it

```bash
kubectl apply -f service.yaml

kubectl get services
```

Expected output:
```
NAME            TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)        AGE
hello-service   NodePort   10.100.42.150   <none>        80:30080/TCP   10s
kubernetes      ClusterIP  10.96.0.1       <none>        443/TCP        2d
```

Browser → `http://localhost:30080` → **Hello from Express inside Docker!**

No more port-forward. This is now a persistent, load-balanced entry point to all three Pods.

### How Service Discovery Works Inside the Cluster

Every Service automatically gets a DNS name:

```
hello-service                          (same namespace)
hello-service.default                  (service.namespace)
hello-service.default.svc.cluster.local  (fully qualified)
```

Any Pod in the cluster can reach your app at `http://hello-service` — without knowing any IP address. This replaces Docker Compose's service-name DNS with something that works across an entire cluster.

### Three Questions to Ask Trainees

**1. "What would happen if you deleted one Pod and a request came in right after?"**
→ The Service routes to the remaining healthy Pods. It constantly knows which Pods match the selector and are ready.

**2. "How does the Service know which Pods to send traffic to?"**
→ The `selector: app: hello-app` matches the labels on the Pods. If you change the label on a Pod (or a Pod crashes and a new one starts), the Service updates automatically.

**3. "In Docker Compose you used `depends_on: mongo`. How does that translate in Kubernetes?"**
→ In Kubernetes, services connect by DNS name, not `depends_on`. You connect to `mongo-service` and Kubernetes resolves it to whatever Pods are currently behind that service.

---

## Step 5 — Full App: Express + MongoDB on Kubernetes

Now deploy a two-service application — the Express app and MongoDB — just like you did with Docker Compose, but on Kubernetes.

### Folder Structure

```
k8s-app/
  ├── mongo-deployment.yaml
  ├── mongo-service.yaml
  ├── app-deployment.yaml
  └── app-service.yaml
```

### `mongo-deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mongo-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mongo
  template:
    metadata:
      labels:
        app: mongo
    spec:
      containers:
        - name: mongo
          image: mongo:6
          ports:
            - containerPort: 27017
          volumeMounts:
            - name: mongo-storage
              mountPath: /data/db
      volumes:
        - name: mongo-storage
          emptyDir: {}
```

> `emptyDir` is temporary storage tied to the Pod's lifetime. For persistence across Pod restarts, use a `PersistentVolumeClaim` — covered in the concepts section below.

### `mongo-service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: mongo-service
spec:
  type: ClusterIP
  selector:
    app: mongo
  ports:
    - port: 27017
      targetPort: 27017
```

`ClusterIP` — MongoDB should only be reachable from inside the cluster, not from the internet.

### Update your `app.js` connection string

```js
mongoose.connect('mongodb://mongo-service:27017/hellodb')
```

The hostname is `mongo-service` — the Kubernetes Service name. Rebuild and push:

```bash
docker build -t yourname/hello-express:2.0 .
docker push yourname/hello-express:2.0
```

### `app-deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: hello-app
  template:
    metadata:
      labels:
        app: hello-app
    spec:
      containers:
        - name: hello-container
          image: yourname/hello-express:2.0
          ports:
            - containerPort: 3000
```

### `app-service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: app-service
spec:
  type: NodePort
  selector:
    app: hello-app
  ports:
    - port: 80
      targetPort: 3000
      nodePort: 30080
```

### Deploy everything

```bash
kubectl apply -f mongo-deployment.yaml
kubectl apply -f mongo-service.yaml
kubectl apply -f app-deployment.yaml
kubectl apply -f app-service.yaml
```

Or apply the whole folder at once:

```bash
kubectl apply -f k8s-app/
```

### Verify

```bash
kubectl get all
```

Expected output:
```
NAME                                    READY   STATUS    RESTARTS   AGE
pod/app-deployment-xxx-aaa              1/1     Running   0          30s
pod/app-deployment-xxx-bbb              1/1     Running   0          30s
pod/app-deployment-xxx-ccc              1/1     Running   0          30s
pod/mongo-deployment-yyy-zzz            1/1     Running   0          30s

NAME                  TYPE        CLUSTER-IP      PORT(S)
service/app-service   NodePort    10.100.1.10     80:30080/TCP
service/mongo-service ClusterIP   10.100.1.20     27017/TCP
service/kubernetes    ClusterIP   10.96.0.1       443/TCP

NAME                               READY   UP-TO-DATE   AVAILABLE
deployment.apps/app-deployment     3/3     3            3
deployment.apps/mongo-deployment   1/1     1            1
```

Browser → `http://localhost:30080` → **Hello from Express! MongoDB: Connected ✅**

### Docker Compose vs Kubernetes — Side by Side

| Docker Compose | Kubernetes |
|---|---|
| `docker-compose.yml` | Multiple YAML files (or one with `---` separators) |
| `services:` | Deployment + Service |
| `image:` | `spec.containers[].image` |
| `ports:` | Service `nodePort` |
| `depends_on:` | DNS-based discovery via Service name |
| `volumes:` | `PersistentVolumeClaim` |
| `docker compose up` | `kubectl apply -f .` |
| `docker compose down` | `kubectl delete -f .` |

---

## Step 6 — Rolling Updates and Rollbacks

One of Kubernetes' most valuable features: update a running app **with zero downtime**.

### Push a new version

Update `app.js` to return a different message:

```js
app.get('/', (req, res) => {
  res.send('Hello from Express v3 — updated with zero downtime!');
});
```

```bash
docker build -t yourname/hello-express:3.0 .
docker push yourname/hello-express:3.0
```

### Update the Deployment

```bash
kubectl set image deployment/app-deployment \
  hello-container=yourname/hello-express:3.0
```

Or edit `app-deployment.yaml`, change `image: yourname/hello-express:2.0` to `3.0`, and run:

```bash
kubectl apply -f app-deployment.yaml
```

### Watch it happen

```bash
kubectl rollout status deployment/app-deployment
```

```
Waiting for deployment "app-deployment" rollout to finish: 1 out of 3 new replicas have been updated...
Waiting for deployment "app-deployment" rollout to finish: 2 out of 3 new replicas have been updated...
deployment "app-deployment" successfully rolled out
```

Kubernetes replaces Pods one at a time, ensuring a minimum number stay available throughout. During the update, some Pods run v2 and some run v3 — the Service routes to whichever Pods are `Ready`.

### Rollback

Something went wrong with v3? Roll back instantly:

```bash
kubectl rollout undo deployment/app-deployment
```

```bash
# See rollout history
kubectl rollout history deployment/app-deployment

# Roll back to a specific revision
kubectl rollout undo deployment/app-deployment --to-revision=1
```

### The Teaching Moment

> "In Docker, updating an app means stopping the old container and starting a new one — there is a gap. In Kubernetes, the Deployment's rolling update strategy ensures the app is always serving traffic. This is how Netflix, Amazon, and every modern platform deploys without maintenance windows."

---

## Kubernetes — Key Concepts Summary

### The Big Picture

Kubernetes solves one problem: **running containers reliably at scale.** It is a *container orchestration* platform — it decides where containers run, restarts them when they fail, scales them on demand, and routes traffic to healthy instances.

---

### Core Objects

**Pod**
The smallest unit in Kubernetes. Wraps one or more containers that share network and storage. You almost never create Pods directly — Deployments create them for you.

**Deployment**
The standard way to run a stateless application. It maintains a ReplicaSet (a specified number of Pod copies) and handles rolling updates and rollbacks. If a Pod dies, the Deployment replaces it.

**ReplicaSet**
Ensures a specified number of identical Pods are running at any time. Managed automatically by a Deployment — you rarely interact with it directly.

**Service**
A stable network endpoint in front of a group of Pods. Finds its Pods via label selectors. Provides DNS-based service discovery and load balancing. Types: `ClusterIP` (internal), `NodePort` (external via node port), `LoadBalancer` (external via cloud LB).

**Namespace**
A virtual cluster inside a cluster — used to separate environments (dev, staging, prod) on the same physical cluster. The default namespace is called `default`.

**Node**
A physical or virtual machine in the cluster. Pods run on nodes. In Docker Desktop, there is one node.

**Control Plane**
The brain of Kubernetes. Includes the API server (kubectl talks to this), scheduler (decides which node a Pod runs on), and controller manager (enforces desired state).

**ConfigMap**
Stores non-sensitive configuration (environment variables, config files) outside the container image. Inject it into Pods at runtime so the same image works in dev, staging, and prod.

**Secret**
Like ConfigMap but for sensitive data (passwords, API keys). Values are base64-encoded and can be mounted into Pods as environment variables or files. Use with a secrets manager in production.

**PersistentVolume (PV) / PersistentVolumeClaim (PVC)**
Persistent storage that survives Pod restarts and replacements. A PV is the actual storage. A PVC is a Pod's request for storage. This is how you replace Docker volumes in Kubernetes.

---

### Architecture

```
                       kubectl
                          ↓
              ┌─────────────────────┐
              │    Control Plane    │
              │  API Server         │
              │  Scheduler          │
              │  Controller Manager │
              │  etcd (state store) │
              └────────┬────────────┘
                       │ manages
          ┌────────────┴────────────┐
          ↓                         ↓
    ┌──────────┐             ┌──────────┐
    │  Node 1  │             │  Node 2  │
    │  ┌────┐  │             │  ┌────┐  │
    │  │Pod │  │             │  │Pod │  │
    │  │Pod │  │             │  │Pod │  │
    │  └────┘  │             │  └────┘  │
    └──────────┘             └──────────┘
          ↑ traffic in
    ┌──────────┐
    │ Service  │ (load balances across nodes)
    └──────────┘
```

---

### Commands at a Glance

| What you want to do | Command |
|---|---|
| Apply a YAML file | `kubectl apply -f file.yaml` |
| Apply all YAMLs in a folder | `kubectl apply -f ./folder/` |
| See Pods | `kubectl get pods` |
| See Deployments | `kubectl get deployments` |
| See Services | `kubectl get services` |
| See everything | `kubectl get all` |
| Describe a resource | `kubectl describe pod <name>` |
| See container logs | `kubectl logs <pod-name>` |
| Open a shell in a Pod | `kubectl exec -it <pod-name> -- sh` |
| Delete a resource | `kubectl delete -f file.yaml` |
| Scale a Deployment | `kubectl scale deployment <name> --replicas=5` |
| Update an image | `kubectl set image deployment/<name> <container>=<image>:<tag>` |
| Check rollout status | `kubectl rollout status deployment/<name>` |
| Rollback | `kubectl rollout undo deployment/<name>` |
| Temporary port-forward | `kubectl port-forward pod/<name> 3000:3000` |

---

### One-Line Distinctions (commonly confused)

| These seem similar... | But... |
|---|---|
| Pod vs Deployment | Pod is one instance. Deployment manages many Pods with self-healing and updates. |
| `kubectl apply` vs `kubectl create` | `apply` is declarative and idempotent. `create` fails if the object exists. Always use `apply`. |
| ClusterIP vs NodePort vs LoadBalancer | ClusterIP = internal only. NodePort = external via a port. LoadBalancer = cloud-provisioned external IP. |
| Deployment vs StatefulSet | Deployment = stateless apps. StatefulSet = stateful apps needing stable identity (databases). |
| ConfigMap vs Secret | ConfigMap = plain config. Secret = sensitive data (base64-encoded, access-controlled). |
| `emptyDir` vs PVC | `emptyDir` is deleted when the Pod is replaced. PVC persists across Pod replacements. |
| Docker Compose `depends_on` vs Kubernetes Services | `depends_on` controls start order. Kubernetes uses DNS — services connect by name, and apps must handle retry/reconnect logic themselves. |

---

### How Everything Connects

```
Dockerfile  →  docker build  →  Image  →  docker push  →  Docker Hub
                                                                ↓
                                                    kubectl apply -f deployment.yaml
                                                                ↓
                                                    Kubernetes pulls image
                                                                ↓
                                                    ReplicaSet creates Pods
                                                    (runs containers from image)
                                                                ↓
                                               ┌────────────────────────────┐
                                               │   Pod   Pod   Pod          │
                                               │    ↑     ↑     ↑           │
                                               │       Service              │
                                               │   (stable endpoint,        │
                                               │    load balancing,         │
                                               │    DNS discovery)          │
                                               └────────────────────────────┘
```

---

## Kubernetes Commands Reference

```bash
# Cluster information
kubectl cluster-info
kubectl get nodes
kubectl get namespaces

# Creating and updating resources
kubectl apply -f file.yaml           # create or update
kubectl apply -f ./directory/        # apply all YAMLs in folder
kubectl delete -f file.yaml          # delete resources defined in file

# Pods
kubectl get pods                     # list running pods
kubectl get pods -o wide             # include node and IP info
kubectl get pods --watch             # live updates
kubectl describe pod <name>          # full details and events
kubectl logs <pod-name>              # container stdout
kubectl logs <pod-name> -f           # stream logs live
kubectl exec -it <pod-name> -- sh    # open shell inside pod

# Deployments
kubectl get deployments
kubectl describe deployment <name>
kubectl scale deployment <name> --replicas=5
kubectl set image deployment/<name> <container>=<image>:<tag>
kubectl rollout status deployment/<name>
kubectl rollout history deployment/<name>
kubectl rollout undo deployment/<name>

# Services
kubectl get services
kubectl describe service <name>
kubectl port-forward pod/<name> 3000:3000   # temporary local access

# ConfigMaps and Secrets
kubectl get configmaps
kubectl get secrets
kubectl describe configmap <name>

# Cleanup
kubectl delete pod <name>
kubectl delete deployment <name>
kubectl delete service <name>
kubectl delete -f ./directory/      # delete everything applied from folder
```

---

## ToC Coverage Map

| Kubernetes Topic | Covered in |
|---|---|
| Introduction | Bridge from Docker section — declarative vs imperative, the mental model shift |
| Kubernetes Overview | Architecture diagram, control plane, nodes, how kubectl works |
| Setup Kubernetes | Step 1 — Docker Desktop Kubernetes, `kubectl get nodes`, cluster-info |
| YAML Introduction | YAML section — apiVersion, kind, metadata, spec; indentation rules |
| Kubernetes Concepts — PODs | Step 2 — Pod YAML, `kubectl apply`, `kubectl get pods`, `kubectl logs`, self-healing demo |
| Kubernetes Concepts — ReplicaSets | Step 3 — shown as the mechanism behind Deployments, `kubectl get replicasets` |
| Kubernetes Concepts — Deployments | Step 3 — Deployment YAML, replicas, rolling update, scale, self-healing demo |
| Networking in Kubernetes | Step 4 — Service types, ClusterIP vs NodePort, label selectors, DNS-based service discovery |
| Services | Step 4 and Step 5 — NodePort for app-service, ClusterIP for mongo-service |
| Rolling Updates & Rollbacks | Step 6 — `kubectl set image`, `kubectl rollout status`, `kubectl rollout undo` |
| Full Multi-Service App | Step 5 — Express + MongoDB on Kubernetes, Docker Compose vs Kubernetes side-by-side |
| Conclusion | Key Concepts Summary — definitions, distinctions, architecture, how everything connects |

> **Next:** Ansible — automate the provisioning and configuration of the machines your Kubernetes cluster runs on, and integrate everything into a CI/CD pipeline with Jenkins.
