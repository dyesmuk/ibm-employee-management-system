# Module 05 — Docker Registry

A registry is the distribution hub for Docker images — where you push images to share and pull images to run. This module covers Docker Hub, private registries, authentication, and best practices.

---

## 1. What is a Registry?

```
Developer        Registry              Production
─────────        ────────              ──────────
Build image ──►  Push image  ◄──────►  Pull image
                 Store image            Run container
```

A **registry** stores and serves Docker images. It's organized into **repositories**, which hold multiple **tags** of the same image.

```
registry.example.com / myteam / myapp : v1.2.3
│                      │        │        │
└── Registry host       │        │        └── Tag
                        │        └── Repository
                        └── Namespace (user/org)
```

---

## 2. Docker Hub

[hub.docker.com](https://hub.docker.com) is the default public registry. Every `docker pull` or `docker run` without a registry prefix uses Docker Hub.

### Image naming on Docker Hub

```bash
# Official images (maintained by Docker or publishers)
docker pull nginx                # short for: docker.io/library/nginx:latest
docker pull postgres:15
docker pull node:20-alpine

# User/organisation images
docker pull yourname/myapp:v1.0
```

### Creating an account and pushing

```bash
# Create an account at hub.docker.com, then:
docker login
# Username: yourname
# Password: ••••••••
# Login Succeeded

# Tag your image for Docker Hub
docker build -t yourname/myapp:v1.0 .
docker tag yourname/myapp:v1.0 yourname/myapp:latest

# Push
docker push yourname/myapp:v1.0
docker push yourname/myapp:latest
```

### Docker Hub rate limits (free tier)

| Account | Pull limit |
|---------|-----------|
| Anonymous | 100 pulls/6 hours per IP |
| Authenticated | 200 pulls/6 hours |
| Pro/Team | Unlimited |

In CI/CD, always authenticate to avoid rate limits.

### Automated builds

Docker Hub can watch a GitHub repository and automatically rebuild/push the image whenever you push a commit. Set up under **Repository → Builds**.

---

## 3. Running a Private Registry

Host your own registry for proprietary images:

```bash
# Run the official registry image
docker run -d \
  --name registry \
  -p 5000:5000 \
  --restart unless-stopped \
  -v registry-data:/var/lib/registry \
  registry:2
```

### Push to your private registry

```bash
# Tag for local registry
docker tag myapp:v1.0 localhost:5000/myapp:v1.0

# Push
docker push localhost:5000/myapp:v1.0

# Pull from another machine on the same network
docker pull 192.168.1.100:5000/myapp:v1.0
```

### Enable HTTPS for a production private registry

```bash
docker run -d \
  --name registry \
  -p 443:5000 \
  --restart unless-stopped \
  -v registry-data:/var/lib/registry \
  -v /path/to/certs:/certs \
  -e REGISTRY_HTTP_TLS_CERTIFICATE=/certs/domain.crt \
  -e REGISTRY_HTTP_TLS_KEY=/certs/domain.key \
  registry:2
```

### Add authentication (htpasswd)

```bash
# Generate credentials
mkdir auth
docker run --rm \
  --entrypoint htpasswd \
  httpd:2 -Bbn admin secretpassword > auth/htpasswd

# Run registry with auth
docker run -d \
  --name registry \
  -p 5000:5000 \
  -v registry-data:/var/lib/registry \
  -v $(pwd)/auth:/auth \
  -e REGISTRY_AUTH=htpasswd \
  -e REGISTRY_AUTH_HTPASSWD_REALM="Registry Realm" \
  -e REGISTRY_AUTH_HTPASSWD_PATH=/auth/htpasswd \
  registry:2
```

---

## 4. Cloud Registries

### AWS Elastic Container Registry (ECR)

```bash
# Authenticate (AWS CLI required)
aws ecr get-login-password --region us-east-1 \
  | docker login --username AWS --password-stdin \
    123456789.dkr.ecr.us-east-1.amazonaws.com

# Create a repository (one-time)
aws ecr create-repository --repository-name myapp

# Tag and push
docker tag myapp:v1.0 \
  123456789.dkr.ecr.us-east-1.amazonaws.com/myapp:v1.0
docker push 123456789.dkr.ecr.us-east-1.amazonaws.com/myapp:v1.0
```

### Google Container Registry / Artifact Registry

```bash
# Authenticate
gcloud auth configure-docker us-central1-docker.pkg.dev

# Tag and push
docker tag myapp:v1.0 \
  us-central1-docker.pkg.dev/my-project/my-repo/myapp:v1.0
docker push us-central1-docker.pkg.dev/my-project/my-repo/myapp:v1.0
```

### GitHub Container Registry (GHCR)

```bash
# Create a Personal Access Token with 'write:packages' scope
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Tag and push
docker tag myapp:v1.0 ghcr.io/yourname/myapp:v1.0
docker push ghcr.io/yourname/myapp:v1.0
```

GHCR images can be linked to GitHub repositories, making them appear in the repository's **Packages** section.

### Registry comparison

| Registry | Best for | Cost |
|----------|---------|------|
| Docker Hub | Open source, small teams | Free tier; paid for private |
| AWS ECR | AWS deployments | Pay per storage + transfer |
| GCR / Artifact Registry | GCP deployments | Pay per storage + transfer |
| GHCR | GitHub-based projects | Free for public; included in GitHub plans |
| GitLab Registry | GitLab users | Included with GitLab |
| JFrog Artifactory | Enterprise, multi-format | Enterprise pricing |

---

## 5. Image Tags and Digests

### Tags are mutable

```bash
docker pull nginx:latest     # today: pulls image A
# ... two weeks later ...
docker pull nginx:latest     # now pulls image B (updated!)
```

Tags can be overwritten. `latest` is just a convention.

### Digests are immutable

```bash
# Pull by digest — always gets exactly this image
docker pull nginx@sha256:abc123def456...

# See the digest of a local image
docker inspect nginx:latest | grep -i digest

# Pull and show digest
docker pull nginx:latest
# latest: Pulling from library/nginx
# Digest: sha256:abc123def456...
```

### Pin images in production

```bash
# In Dockerfile — don't do this:
FROM node:20-alpine         # could change tomorrow

# Do this (pinned to exact image):
FROM node:20.12.1-alpine3.19

# Or pin by digest (most secure):
FROM node:20-alpine@sha256:abc123def456...
```

---

## 6. Registry in CI/CD

### GitHub Actions example

```yaml
# .github/workflows/build-push.yml
name: Build and Push

on:
  push:
    branches: [main]
    tags: ['v*']

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}    # use token, not password

      - name: Extract metadata (tags, labels)
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: yourname/myapp
          tags: |
            type=semver,pattern={{version}}
            type=sha

      - name: Build and push
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
          cache-from: type=gha
          cache-to: type=gha,mode=max
```

---

## 7. Searching and Inspecting Images

```bash
# Search Docker Hub from CLI
docker search nginx
docker search --filter=is-official=true node
docker search --filter=stars=100 postgres

# List tags (no built-in docker CLI command — use API)
curl -s "https://hub.docker.com/v2/repositories/library/node/tags?page_size=10" \
  | python3 -m json.tool | grep '"name"'

# Inspect a remote image without pulling (with skopeo)
skopeo inspect docker://nginx:latest
```

---

## 8. Image Scanning

Scan images for known vulnerabilities before pushing to production:

```bash
# Docker Scout (built into Docker Desktop and CLI)
docker scout quickview myapp:v1.0
docker scout cves myapp:v1.0
docker scout recommendations myapp:v1.0

# Trivy (open source, widely used in CI)
brew install trivy
trivy image myapp:v1.0
trivy image --severity HIGH,CRITICAL myapp:v1.0

# Snyk
snyk container test myapp:v1.0
```

### Integrate scanning in CI

```yaml
# In GitHub Actions
- name: Run Trivy vulnerability scanner
  uses: aquasecurity/trivy-action@master
  with:
    image-ref: yourname/myapp:${{ github.sha }}
    format: sarif
    output: trivy-results.sarif
    severity: CRITICAL,HIGH

- name: Upload Trivy results to GitHub Security
  uses: github/codeql-action/upload-sarif@v2
  with:
    sarif_file: trivy-results.sarif
```

---

## 9. Cleanup and Maintenance

```bash
# Remove images not used by any container
docker image prune

# Remove ALL unused images (including tagged ones)
docker image prune -a

# See what's taking space
docker system df

# Full cleanup (containers, images, networks, build cache)
docker system prune -a

# List registry contents (private registry)
curl http://localhost:5000/v2/_catalog
curl http://localhost:5000/v2/myapp/tags/list
```

---

## 10. Best Practices

```
✅ Use specific tags in production (never 'latest')
✅ Pin base images by digest for maximum reproducibility
✅ Scan images for vulnerabilities in CI before pushing
✅ Use read-only tokens/credentials for pulling in production
✅ Set up image retention policies — delete old tags automatically
✅ Use multi-stage builds to keep images small
✅ Keep credentials in CI secrets, never in Dockerfiles
✅ Enable Docker Content Trust (DCT) for signed images

❌ Don't use 'latest' in production Dockerfiles
❌ Don't store secrets in images
❌ Don't push images without scanning
❌ Don't give developers write access to production registries
```

---

## Summary

| Registry | Auth command |
|----------|-------------|
| Docker Hub | `docker login` |
| AWS ECR | `aws ecr get-login-password \| docker login` |
| GCR | `gcloud auth configure-docker` |
| GHCR | `echo $TOKEN \| docker login ghcr.io` |
| Private | `docker login registry.example.com` |

**Next:** [06 — Docker Engine, Storage and Networking →](./06-engine-storage-networking.md)
