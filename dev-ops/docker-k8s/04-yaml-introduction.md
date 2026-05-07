# Module 04 — YAML Introduction

All Kubernetes resources are defined in YAML. This module teaches you everything you need to read, write, and troubleshoot Kubernetes YAML manifests confidently.

---

## 1. What is YAML?

YAML (YAML Ain't Markup Language) is a human-readable data serialisation format. It uses **indentation** to represent structure — there are no braces `{}` or brackets `[]` wrapping blocks (unlike JSON).

```yaml
# This is a YAML comment
name: Alice
age: 30
active: true
```

Equivalent JSON:
```json
{
  "name": "Alice",
  "age": 30,
  "active": true
}
```

---

## 2. YAML Data Types

### Scalars (single values)

```yaml
# Strings
first_name: Alice
last_name: "O'Brien"         # quotes when value contains special chars
description: 'Single quotes also work'
multiline: "line one\nline two"   # \n interpreted in double quotes

# Numbers
port: 8080
price: 9.99
hex_value: 0xFF

# Booleans (watch out — these are all valid!)
enabled: true
visible: True
active: TRUE
disabled: false

# Null
image: null
value: ~              # also null

# Explicit types
version: "1.10"       # force string (1.10 would become float otherwise)
zip: "01234"          # force string (leading zero would be lost as int)
```

> ⚠️ **Common YAML gotcha:** `yes`, `no`, `on`, `off` are boolean in YAML 1.1 (used by many parsers). Always quote them if you mean the string "yes" or "no".

### Multiline Strings

```yaml
# Literal block scalar (|) — preserves newlines
description: |
  This is line one.
  This is line two.
  Trailing newline is kept.

# Folded block scalar (>) — newlines become spaces
summary: >
  This is a long description
  that will be joined into
  a single line with spaces.

# Equivalent to: "This is a long description that will be joined into a single line with spaces."
```

The literal block scalar `|` is used extensively in Kubernetes for shell scripts and config file content:

```yaml
data:
  entrypoint.sh: |
    #!/bin/bash
    set -e
    echo "Starting application..."
    exec node server.js
```

---

## 3. Collections

### Mappings (key-value pairs / dictionaries)

```yaml
server:
  host: localhost
  port: 8080
  debug: false
  database:
    host: postgres
    port: 5432
    name: myapp
```

Equivalent JSON:
```json
{
  "server": {
    "host": "localhost",
    "port": 8080,
    "debug": false,
    "database": {
      "host": "postgres",
      "port": 5432,
      "name": "myapp"
    }
  }
}
```

### Sequences (lists / arrays)

```yaml
# Block style (common in Kubernetes manifests)
fruits:
  - apple
  - banana
  - cherry

# Inline style
fruits: [apple, banana, cherry]

# List of objects
servers:
  - name: web-1
    ip: 192.168.1.10
    port: 80
  - name: web-2
    ip: 192.168.1.11
    port: 80
```

---

## 4. YAML Indentation Rules

YAML is **indentation-sensitive**. These are the rules:

```yaml
# Rule 1: Use spaces ONLY — never tabs
# Rule 2: Consistent indentation within a block (usually 2 spaces)
# Rule 3: Child elements are indented one level from parent

parent:
  child:          # 2 spaces
    grandchild:   # 4 spaces

# Rule 4: List items use a dash (-) followed by a space
items:
  - item1         # dash is at the parent's indent level
  - item2

# Rule 5: Inline mappings in lists — two styles:

# Style A: Value on the same line as dash
containers:
  - name: web
    image: nginx

# Style B: Mapping starts after dash with indentation
containers:
  - name: web
    image: nginx
    ports:
      - containerPort: 80
```

---

## 5. YAML in Kubernetes — The Four Required Fields

Every Kubernetes YAML manifest must have these four top-level fields:

```yaml
apiVersion: apps/v1    # 1. API group + version
kind: Deployment       # 2. Resource type
metadata:              # 3. Identity
  name: web-server
spec:                  # 4. Desired state
  ...
```

### How to find the right `apiVersion`

```bash
# List all resource types with their API versions
kubectl api-resources

# SHORTNAMES   APIVERSION   NAMESPACED   KIND
# pods         v1           true         Pod
# deployments  apps/v1      true         Deployment
# services     v1           true         Service
# configmaps   v1           true         ConfigMap
# secrets      v1           true         Secret
# ingresses    networking.k8s.io/v1 true Ingress

# Explain any resource and see its fields
kubectl explain pod
kubectl explain pod.spec
kubectl explain pod.spec.containers
kubectl explain deployment.spec.template.spec.containers.resources
```

`kubectl explain` is your best friend — it shows every available field with documentation, right in the terminal.

---

## 6. Complete Pod YAML Example

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: web-pod
  namespace: default
  labels:
    app: web
    tier: frontend
  annotations:
    description: "Example web pod"
spec:
  # Init containers run before app containers
  initContainers:
    - name: init-db-check
      image: busybox:1.36
      command: ['sh', '-c',
        'until nc -z db 5432; do echo waiting for db; sleep 2; done']

  containers:
    - name: web
      image: nginx:1.25-alpine
      imagePullPolicy: IfNotPresent   # Always | Never | IfNotPresent

      ports:
        - name: http
          containerPort: 80
          protocol: TCP

      env:
        - name: APP_ENV
          value: production
        - name: DB_HOST
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: DB_HOST
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password

      resources:
        requests:
          cpu: "100m"
          memory: "64Mi"
        limits:
          cpu: "500m"
          memory: "256Mi"

      volumeMounts:
        - name: config-vol
          mountPath: /etc/nginx/conf.d
          readOnly: true
        - name: data-vol
          mountPath: /var/www/html

      livenessProbe:
        httpGet:
          path: /healthz
          port: 80
        initialDelaySeconds: 10
        periodSeconds: 15

      readinessProbe:
        httpGet:
          path: /ready
          port: 80
        initialDelaySeconds: 5
        periodSeconds: 10

  volumes:
    - name: config-vol
      configMap:
        name: nginx-config
    - name: data-vol
      persistentVolumeClaim:
        claimName: web-data-pvc

  # Scheduling
  nodeSelector:
    kubernetes.io/arch: amd64

  restartPolicy: Always    # Always | OnFailure | Never
  terminationGracePeriodSeconds: 30
```

---

## 7. Working with YAML Files

### Apply manifests

```bash
# Create or update resources from a file
kubectl apply -f pod.yaml

# Apply all YAML files in a directory
kubectl apply -f ./manifests/

# Apply from a URL
kubectl apply -f https://raw.githubusercontent.com/org/repo/main/deploy.yaml

# Dry run (validate without applying)
kubectl apply -f deployment.yaml --dry-run=client
kubectl apply -f deployment.yaml --dry-run=server   # validates against API server

# Diff current vs new
kubectl diff -f deployment.yaml
```

### Generate YAML from kubectl

```bash
# Generate YAML without creating the resource (--dry-run + -o yaml)
kubectl create deployment web --image=nginx --replicas=3 \
  --dry-run=client -o yaml > deployment.yaml

kubectl create service clusterip web --tcp=80:80 \
  --dry-run=client -o yaml > service.yaml

kubectl create configmap app-config \
  --from-literal=KEY=value \
  --dry-run=client -o yaml > configmap.yaml

# Get YAML of an existing resource
kubectl get deployment web -o yaml > current-deployment.yaml
```

This is how professionals create starter YAML — generate it from `kubectl`, then edit.

### Validate YAML

```bash
# Syntax check only (no cluster)
kubectl apply -f deployment.yaml --dry-run=client

# Validate against running cluster (full schema validation)
kubectl apply -f deployment.yaml --dry-run=server

# External validator
brew install kubeval
kubeval deployment.yaml
```

---

## 8. Multi-Document YAML

One file can contain multiple Kubernetes resources separated by `---`:

```yaml
# all-in-one.yaml

apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  DB_HOST: postgres

---

apiVersion: apps/v1
kind: Deployment
metadata:
  name: web
spec:
  replicas: 3
  ...

---

apiVersion: v1
kind: Service
metadata:
  name: web-svc
spec:
  selector:
    app: web
  ports:
    - port: 80
```

```bash
kubectl apply -f all-in-one.yaml
# configmap/app-config created
# deployment.apps/web created
# service/web-svc created
```

---

## 9. YAML Anchors and Aliases (Advanced)

YAML supports reuse with anchors (`&`) and aliases (`*`):

```yaml
# Define anchor
defaults: &defaults
  restartPolicy: Always
  terminationGracePeriodSeconds: 30

# Reuse with alias
spec:
  <<: *defaults        # merge all keys from defaults
  containers:
    - name: app
      image: myapp:v1
```

> Kubernetes itself doesn't natively support YAML anchors in all cases. For proper templating, use **Kustomize** or **Helm**.

---

## 10. Common YAML Mistakes in Kubernetes

```yaml
# ❌ Wrong: Using tabs for indentation
spec:
	containers:       # TAB — will fail

# ✅ Correct: Use spaces
spec:
  containers:        # 2 spaces

# ❌ Wrong: Misaligned list item
spec:
  containers:
  - name: web         # dash at same level as 'containers' key
    image: nginx

# ✅ Correct: Dash indented under containers
spec:
  containers:
    - name: web       # dash indented 2 more spaces
      image: nginx

# ❌ Wrong: Port as string instead of int
containerPort: "80"

# ✅ Correct: Port as integer
containerPort: 80

# ❌ Wrong: Missing required fields
kind: Deployment
metadata:
  name: web
# Missing apiVersion and spec!

# ❌ Wrong: Label value as integer
labels:
  version: 2.0       # numbers aren't valid label values!

# ✅ Correct: Label values must be strings
labels:
  version: "2.0"
```

---

## 11. The `kubectl explain` Workflow

When unsure what fields a resource needs:

```bash
# Start broad
kubectl explain pod

# Drill into spec
kubectl explain pod.spec

# Drill into containers
kubectl explain pod.spec.containers

# Drill into resources
kubectl explain pod.spec.containers.resources

# Drill into probes
kubectl explain pod.spec.containers.livenessProbe

# Show all fields recursively
kubectl explain pod --recursive | head -100
```

This is equivalent to having the entire Kubernetes API reference offline in your terminal.

---

## Summary

| YAML Feature | Kubernetes use |
|-------------|---------------|
| Mappings (key: value) | All resource fields |
| Lists (- item) | containers, ports, env, volumes |
| Multiline `\|` | Scripts, config file content |
| `---` separator | Multiple resources in one file |
| `kubectl explain` | Find valid fields and types |
| `--dry-run -o yaml` | Generate starter YAML |

**Next:** [05 — PODs, ReplicaSets, Deployments →](./05-pods-replicasets-deployments.md)
