# Appendix — Setup Multi-Node Cluster Using Kubeadm

Kubeadm is the official tool for bootstrapping production-grade Kubernetes clusters. Unlike Minikube or kind, kubeadm builds a real, TLS-secured cluster on actual (or virtual) machines. This is what managed services like EKS and GKE do under the hood.

---

## Architecture We'll Build

```
┌──────────────────────┐     ┌──────────────────────┐
│   Control Plane      │     │   Worker Node 1       │
│   (master)           │     │                       │
│   192.168.1.10       │     │   192.168.1.11        │
│                      │     └──────────────────────┘
│   - kube-apiserver   │     ┌──────────────────────┐
│   - etcd             │────►│   Worker Node 2       │
│   - kube-scheduler   │     │                       │
│   - controller-mgr   │     │   192.168.1.12        │
└──────────────────────┘     └──────────────────────┘
```

**Requirements per node:**
- Ubuntu 20.04 or 22.04 (or CentOS/RHEL 8+)
- 2+ CPUs, 2GB+ RAM (4GB recommended for control plane)
- Network connectivity between nodes
- Unique hostname, MAC, and product_uuid per node

---

## Phase 1: Prepare ALL Nodes

Run the following on **every node** (control plane and workers).

### Step 1 — Update and set hostnames

```bash
# On control plane
sudo hostnamectl set-hostname master-node
sudo bash -c 'echo "192.168.1.10 master-node" >> /etc/hosts'
sudo bash -c 'echo "192.168.1.11 worker-node-1" >> /etc/hosts'
sudo bash -c 'echo "192.168.1.12 worker-node-2" >> /etc/hosts'

# On worker-1
sudo hostnamectl set-hostname worker-node-1

# On worker-2
sudo hostnamectl set-hostname worker-node-2
```

### Step 2 — Disable swap (required by Kubernetes)

```bash
sudo swapoff -a

# Permanently disable (survive reboots)
sudo sed -i '/ swap / s/^/#/' /etc/fstab

# Verify
free -h
# Swap: 0B
```

### Step 3 — Enable kernel modules

```bash
cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF

sudo modprobe overlay
sudo modprobe br_netfilter

# Verify
lsmod | grep br_netfilter
lsmod | grep overlay
```

### Step 4 — Configure sysctl (network settings)

```bash
cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

sudo sysctl --system
```

### Step 5 — Install containerd (container runtime)

```bash
# Install dependencies
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg

# Add Docker's GPG key (containerd is distributed with Docker)
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# Add Docker repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install containerd
sudo apt-get update
sudo apt-get install -y containerd.io

# Configure containerd to use systemd cgroup driver
sudo mkdir -p /etc/containerd
containerd config default | sudo tee /etc/containerd/config.toml > /dev/null

# Edit the config to use SystemdCgroup
sudo sed -i 's/SystemdCgroup = false/SystemdCgroup = true/' \
  /etc/containerd/config.toml

# Start and enable containerd
sudo systemctl restart containerd
sudo systemctl enable containerd
sudo systemctl status containerd
```

### Step 6 — Install kubeadm, kubelet, kubectl

```bash
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates curl gpg

# Add Kubernetes APT repository
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.29/deb/Release.key \
  | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] \
  https://pkgs.k8s.io/core:/stable:/v1.29/deb/ /' \
  | sudo tee /etc/apt/sources.list.d/kubernetes.list

sudo apt-get update
sudo apt-get install -y kubelet kubeadm kubectl

# Pin versions (prevent accidental upgrades)
sudo apt-mark hold kubelet kubeadm kubectl

# Verify
kubeadm version
kubelet --version
kubectl version --client
```

---

## Phase 2: Initialise the Control Plane

Run the following **only on the control plane node**.

### Step 7 — Initialise the cluster

```bash
sudo kubeadm init \
  --pod-network-cidr=10.244.0.0/16 \   # for Flannel CNI
  --apiserver-advertise-address=192.168.1.10 \
  --node-name=master-node

# For Calico, use: --pod-network-cidr=192.168.0.0/16
```

**Successful output (save this!):**

```
Your Kubernetes control-plane has initialized successfully!

To start using your cluster, run as regular user:

  mkdir -p $HOME/.kube
  sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
  sudo chown $(id -u):$(id -g) $HOME/.kube/config

You should now deploy a pod network to the cluster.
See: https://kubernetes.io/docs/concepts/cluster-administration/networking/

Then you can join any number of worker nodes by running on each:

  kubeadm join 192.168.1.10:6443 --token abc123.xyz789 \
      --discovery-token-ca-cert-hash sha256:abcdef1234567890...
```

### Step 8 — Configure kubectl for your user

```bash
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

# Verify
kubectl get nodes
# NAME           STATUS     ROLES           AGE   VERSION
# master-node    NotReady   control-plane   1m    v1.29.0
# Status is NotReady until CNI is installed
```

### Step 9 — Install a CNI plugin (Flannel)

```bash
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
```

Or for Calico (recommended for production):

```bash
kubectl create -f https://raw.githubusercontent.com/projectcalico/calico/v3.27.0/manifests/tigera-operator.yaml

kubectl create -f - <<EOF
apiVersion: operator.tigera.io/v1
kind: Installation
metadata:
  name: default
spec:
  calicoNetwork:
    ipPools:
      - blockSize: 26
        cidr: 192.168.0.0/16
        encapsulation: VXLANCrossSubnet
        natOutgoing: Enabled
EOF
```

```bash
# Wait for CNI pods to be Running
kubectl get pods -n kube-flannel
# or
kubectl get pods -n calico-system

# Control plane should now be Ready
kubectl get nodes
# NAME           STATUS   ROLES           AGE   VERSION
# master-node    Ready    control-plane   3m    v1.29.0
```

---

## Phase 3: Join Worker Nodes

Run on **each worker node**.

### Step 10 — Join workers to the cluster

Use the `kubeadm join` command from the init output:

```bash
# On worker-node-1 and worker-node-2:
sudo kubeadm join 192.168.1.10:6443 \
  --token abc123.xyz789 \
  --discovery-token-ca-cert-hash sha256:abcdef1234567890...
```

### If the join token expired (24-hour TTL)

```bash
# On the control plane — generate a new token
kubeadm token create --print-join-command
# Prints the full kubeadm join command
```

---

## Phase 4: Verify the Cluster

Back on the **control plane**:

```bash
# All nodes should be Ready
kubectl get nodes
# NAME             STATUS   ROLES           AGE   VERSION
# master-node      Ready    control-plane   10m   v1.29.0
# worker-node-1    Ready    <none>          3m    v1.29.0
# worker-node-2    Ready    <none>          3m    v1.29.0

# All system pods should be Running
kubectl get pods -n kube-system
kubectl get pods -A

# Deploy a test application
kubectl create deployment test --image=nginx --replicas=3
kubectl get pods -o wide
# Pods should spread across worker nodes

# Create a service and test
kubectl expose deployment test --port=80 --type=NodePort
kubectl get svc
curl http://192.168.1.11:<nodePort>   # access via any worker node

# Clean up
kubectl delete deployment test
kubectl delete service test
```

---

## Phase 5: Post-Install Configuration

### Install the Kubernetes Dashboard (optional)

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml

# Create admin user
kubectl apply -f - <<EOF
apiVersion: v1
kind: ServiceAccount
metadata:
  name: admin-user
  namespace: kubernetes-dashboard

---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: admin-user
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
  - kind: ServiceAccount
    name: admin-user
    namespace: kubernetes-dashboard
EOF

# Get login token
kubectl -n kubernetes-dashboard create token admin-user

# Access (in another terminal)
kubectl proxy
# Open: http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/
```

### Install metrics-server

Required for `kubectl top nodes/pods` and HPA:

```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# On bare-metal/VMs, add --kubelet-insecure-tls flag:
kubectl patch deployment metrics-server -n kube-system --type='json' \
  -p='[{"op":"add","path":"/spec/template/spec/containers/0/args/-","value":"--kubelet-insecure-tls"}]'

kubectl top nodes
kubectl top pods -A
```

---

## Phase 6: Cluster Maintenance

### Upgrade the cluster (from v1.28 → v1.29)

```bash
# 1. Upgrade kubeadm on control plane
sudo apt-mark unhold kubeadm
sudo apt-get install -y kubeadm=1.29.0-1.1
sudo apt-mark hold kubeadm

# 2. Verify upgrade plan
kubeadm upgrade plan

# 3. Apply upgrade
sudo kubeadm upgrade apply v1.29.0

# 4. Upgrade kubelet + kubectl on control plane
sudo apt-mark unhold kubelet kubectl
sudo apt-get install -y kubelet=1.29.0-1.1 kubectl=1.29.0-1.1
sudo apt-mark hold kubelet kubectl
sudo systemctl daemon-reload
sudo systemctl restart kubelet

# 5. Upgrade worker nodes (one at a time)
# On control plane — drain the node
kubectl drain worker-node-1 --ignore-daemonsets --delete-emptydir-data

# On worker-node-1 — upgrade
sudo apt-mark unhold kubeadm
sudo apt-get install -y kubeadm=1.29.0-1.1
sudo apt-mark hold kubeadm
sudo kubeadm upgrade node

sudo apt-mark unhold kubelet kubectl
sudo apt-get install -y kubelet=1.29.0-1.1 kubectl=1.29.0-1.1
sudo apt-mark hold kubelet kubectl
sudo systemctl daemon-reload
sudo systemctl restart kubelet

# On control plane — uncordon the worker
kubectl uncordon worker-node-1

# Repeat for each worker node
```

### Back up etcd

```bash
# Install etcdctl
ETCD_VER=v3.5.10
curl -L https://github.com/etcd-io/etcd/releases/download/${ETCD_VER}/etcd-${ETCD_VER}-linux-amd64.tar.gz \
  | tar xzf - etcd-${ETCD_VER}-linux-amd64/etcdctl
sudo mv etcd-${ETCD_VER}-linux-amd64/etcdctl /usr/local/bin/

# Take a snapshot
sudo ETCDCTL_API=3 etcdctl snapshot save /backup/etcd-$(date +%Y%m%d).db \
  --endpoints=https://127.0.0.1:2379 \
  --cacert=/etc/kubernetes/pki/etcd/ca.crt \
  --cert=/etc/kubernetes/pki/etcd/server.crt \
  --key=/etc/kubernetes/pki/etcd/server.key

# Verify the backup
sudo ETCDCTL_API=3 etcdctl snapshot status /backup/etcd-$(date +%Y%m%d).db
```

### Node maintenance (drain + uncordon)

```bash
# Drain: evict all pods from a node before maintenance
kubectl drain worker-node-1 \
  --ignore-daemonsets \        # leave daemonset pods
  --delete-emptydir-data \     # allow emptyDir volumes to be deleted
  --grace-period=60            # seconds for pods to shutdown gracefully

# Cordon: prevent new pods from being scheduled
kubectl cordon worker-node-1

# After maintenance — uncordon to allow pods back
kubectl uncordon worker-node-1
```

---

## Troubleshooting Kubeadm

### Nodes in NotReady state

```bash
kubectl describe node worker-node-1
# Look for: Conditions, Capacity, Allocatable

# Check kubelet on the node
sudo systemctl status kubelet
sudo journalctl -u kubelet -f

# Common causes:
# - CNI not installed
# - containerd not running
# - swap not disabled
```

### Pod stuck in Pending

```bash
kubectl describe pod <name>
# Look for: Events at the bottom
# "0/3 nodes are available: 3 Insufficient cpu"  → increase resources or add nodes
# "0/3 nodes are available: 3 node(s) had taint" → add toleration
```

### API server unreachable

```bash
# Check on the control plane node
sudo crictl ps | grep kube-apiserver
sudo journalctl -u kubelet -n 50

# Check etcd health
sudo ETCDCTL_API=3 etcdctl endpoint health \
  --endpoints=https://127.0.0.1:2379 \
  --cacert=/etc/kubernetes/pki/etcd/ca.crt \
  --cert=/etc/kubernetes/pki/etcd/server.crt \
  --key=/etc/kubernetes/pki/etcd/server.key
```

---

## Summary: What Kubeadm Does

```
kubeadm init:
  1. Generates TLS certificates for all components
  2. Writes kubeconfig files (admin, controller, scheduler)
  3. Generates static Pod manifests (API server, controller, scheduler, etcd)
  4. Configures the bootstrap token
  5. Applies CoreDNS and kube-proxy addons

kubeadm join:
  1. Fetches cluster info using the bootstrap token
  2. Validates the CA certificate hash
  3. Generates kubelet credentials
  4. Configures and starts kubelet
  5. Node registers with the API server
```

Understanding kubeadm deeply means understanding how every component in the control plane is wired together — the same knowledge that helps you debug any Kubernetes cluster, whether self-managed or cloud-managed.
