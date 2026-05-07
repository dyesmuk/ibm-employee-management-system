# Module 02 — Setup Kubernetes

There are multiple ways to run Kubernetes — from a local single-node cluster on your laptop to a managed multi-node cluster in the cloud. This module covers the most common options.

---

## 1. Local Development Options

### Option A: Docker Desktop (Easiest)

Docker Desktop includes a built-in single-node Kubernetes cluster.

**Enable:**
1. Docker Desktop → Settings → Kubernetes
2. Check **"Enable Kubernetes"**
3. Click **"Apply & Restart"**
4. Wait ~2 minutes for the cluster to start

**Verify:**
```bash
kubectl config current-context
# docker-desktop

kubectl get nodes
# NAME             STATUS   ROLES           AGE   VERSION
# docker-desktop   Ready    control-plane   2m    v1.29.1

kubectl get pods -n kube-system
```

**Pros:** Zero configuration, integrated with Docker.
**Cons:** Single node, outdated K8s version, resets when Docker Desktop updates.

---

### Option B: Minikube (Recommended for Learning)

Minikube creates a local Kubernetes cluster in a VM or container.

**Install:**
```bash
# macOS
brew install minikube

# Windows (with Chocolatey)
choco install minikube

# Linux
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube
```

**Start a cluster:**
```bash
# Start with default settings (2 CPUs, 2GB RAM)
minikube start

# Start with more resources
minikube start --cpus 4 --memory 8192

# Use a specific Kubernetes version
minikube start --kubernetes-version=v1.29.0

# Start with multiple nodes
minikube start --nodes 3
```

**Minikube commands:**
```bash
minikube status          # cluster status
minikube stop            # stop cluster (preserves state)
minikube delete          # delete cluster entirely
minikube dashboard       # open K8s dashboard in browser
minikube addons list     # available add-ons
minikube addons enable metrics-server
minikube addons enable ingress

# Tunnel to expose LoadBalancer services locally
minikube tunnel

# SSH into the node
minikube ssh

# Get node IP
minikube ip

# Use Docker inside minikube (for building images)
eval $(minikube docker-env)
docker build -t myapp:latest .  # builds inside minikube
```

---

### Option C: kind (Kubernetes IN Docker)

`kind` runs Kubernetes nodes as Docker containers — fast, lightweight, great for CI.

**Install:**
```bash
# macOS
brew install kind

# Linux
curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.22.0/kind-linux-amd64
chmod +x ./kind
sudo mv ./kind /usr/local/bin/kind
```

**Basic usage:**
```bash
# Create a single-node cluster
kind create cluster

# Create a named cluster
kind create cluster --name dev-cluster

# List clusters
kind get clusters

# Delete a cluster
kind delete cluster --name dev-cluster
```

**Multi-node cluster with kind:**
```yaml
# kind-config.yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
  - role: control-plane
  - role: worker
  - role: worker
  - role: worker
```

```bash
kind create cluster --config kind-config.yaml --name multi-node

kubectl get nodes
# NAME                       STATUS   ROLES           AGE
# multi-node-control-plane   Ready    control-plane   2m
# multi-node-worker          Ready    <none>          90s
# multi-node-worker2         Ready    <none>          90s
# multi-node-worker3         Ready    <none>          90s
```

**Load images into kind (no registry needed):**
```bash
docker build -t myapp:latest .
kind load docker-image myapp:latest --name dev-cluster
```

---

### Option D: k3s / k3d (Lightweight Production-Grade)

k3s is a certified Kubernetes distribution with a small footprint, ideal for edge, IoT, and local development.

```bash
# Install k3s on Linux
curl -sfL https://get.k3s.io | sh -

# Check
sudo k3s kubectl get nodes

# k3d: k3s in Docker (like kind but uses k3s)
brew install k3d
k3d cluster create mycluster
```

---

## 2. Installing kubectl

`kubectl` is required regardless of which cluster option you choose.

```bash
# macOS
brew install kubectl

# Windows (Chocolatey)
choco install kubernetes-cli

# Linux
curl -LO "https://dl.k8s.io/release/$(curl -Ls https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/

# Verify
kubectl version --client
# Client Version: v1.29.0
```

### Shell autocompletion (saves enormous amounts of typing)

```bash
# bash (Linux)
echo 'source <(kubectl completion bash)' >> ~/.bashrc

# zsh (macOS default)
echo 'source <(kubectl completion zsh)' >> ~/.zshrc

# Also add alias
echo 'alias k=kubectl' >> ~/.zshrc
echo 'complete -F __start_kubectl k' >> ~/.zshrc
```

### Useful kubectl plugins (krew)

```bash
# Install krew (kubectl plugin manager)
(
  set -x; cd "$(mktemp -d)" &&
  OS="$(uname | tr '[:upper:]' '[:lower:]')" &&
  ARCH="$(uname -m | sed -e 's/x86_64/amd64/' -e 's/arm.*$/arm/')" &&
  curl -fsSLO "https://github.com/kubernetes-sigs/krew/releases/latest/download/krew-${OS}_${ARCH}.tar.gz" &&
  tar zxvf "krew-${OS}_${ARCH}.tar.gz" &&
  ./krew-"${OS}_${ARCH}" install krew
)

# Install plugins
kubectl krew install ctx       # switch contexts easily
kubectl krew install ns        # switch namespaces easily
kubectl krew install neat      # clean output
kubectl krew install tree      # show object hierarchy
```

---

## 3. Managed Kubernetes (Cloud)

For production, use a managed service — the cloud provider runs the control plane.

### AWS EKS (Elastic Kubernetes Service)

```bash
# Install eksctl
brew tap weaveworks/tap
brew install weaveworks/tap/eksctl

# Create a cluster (takes ~15 minutes)
eksctl create cluster \
  --name my-cluster \
  --region us-east-1 \
  --nodegroup-name standard-workers \
  --node-type t3.medium \
  --nodes 3 \
  --nodes-min 1 \
  --nodes-max 5 \
  --managed

# Update kubeconfig
aws eks update-kubeconfig --region us-east-1 --name my-cluster

# Verify
kubectl get nodes
```

### Google GKE (Google Kubernetes Engine)

```bash
# Create a cluster
gcloud container clusters create my-cluster \
  --zone us-central1-a \
  --num-nodes 3 \
  --machine-type e2-medium \
  --enable-autoscaling \
  --min-nodes 1 \
  --max-nodes 10

# Get credentials
gcloud container clusters get-credentials my-cluster --zone us-central1-a

# Verify
kubectl get nodes
```

### Azure AKS (Azure Kubernetes Service)

```bash
# Create resource group
az group create --name myResourceGroup --location eastus

# Create AKS cluster
az aks create \
  --resource-group myResourceGroup \
  --name myAKSCluster \
  --node-count 3 \
  --enable-addons monitoring \
  --generate-ssh-keys

# Get credentials
az aks get-credentials --resource-group myResourceGroup --name myAKSCluster

# Verify
kubectl get nodes
```

---

## 4. The kubeconfig File

`kubectl` knows which cluster to talk to via the **kubeconfig** file (`~/.kube/config`).

```yaml
# ~/.kube/config
apiVersion: v1
kind: Config

# Cluster definitions (API server addresses + certs)
clusters:
  - name: docker-desktop
    cluster:
      server: https://127.0.0.1:6443
      certificate-authority-data: <base64-cert>
  - name: production-eks
    cluster:
      server: https://abc123.gr7.us-east-1.eks.amazonaws.com
      certificate-authority-data: <base64-cert>

# User credentials
users:
  - name: docker-desktop
    user:
      client-certificate-data: <base64-cert>
      client-key-data: <base64-key>
  - name: eks-admin
    user:
      exec:
        command: aws
        args: ["eks", "get-token", "--cluster-name", "production-eks"]

# Contexts — combine a cluster + user + namespace
contexts:
  - name: docker-desktop
    context:
      cluster: docker-desktop
      user: docker-desktop
      namespace: default
  - name: production
    context:
      cluster: production-eks
      user: eks-admin
      namespace: production

# Which context is active
current-context: docker-desktop
```

```bash
# Switch between contexts
kubectl config use-context production

# With kubectx plugin (easier)
kubectx production
kubectx -   # switch back to previous
```

---

## 5. First Steps After Setup

```bash
# Check the cluster is healthy
kubectl get nodes
kubectl get pods -n kube-system

# Deploy a test application
kubectl create deployment hello --image=nginx --replicas=2
kubectl expose deployment hello --port=80 --type=NodePort

# Access it
minikube service hello   # opens browser (minikube only)
kubectl port-forward service/hello 8080:80
# Now open localhost:8080

# Clean up
kubectl delete deployment hello
kubectl delete service hello
```

---

## 6. Setup Comparison

| Option | Setup time | Resources | Multi-node | Use for |
|--------|-----------|----------|-----------|---------|
| Docker Desktop | 2 min | Shared | ❌ | Quick local testing |
| Minikube | 5 min | Dedicated VM | ✅ (add nodes) | Local dev & learning |
| kind | 1 min | Docker containers | ✅ | CI, local dev |
| k3s | 2 min | Minimal | ✅ | Edge, lightweight prod |
| EKS/GKE/AKS | 15-30 min | Cloud | ✅ | Production |

---

## Summary

- **Minikube** is the best local learning environment — full Kubernetes in a VM
- **kind** is best for CI and fast local clusters — Kubernetes nodes in Docker
- **Docker Desktop** is easiest if you already have it installed
- **Managed services** (EKS, GKE, AKS) handle the control plane for production
- `kubectl` is your primary tool; set up shell completion immediately
- The **kubeconfig** file tells kubectl which cluster to target

**Next:** [03 — Kubernetes Concepts →](./03-kubernetes-concepts.md)
