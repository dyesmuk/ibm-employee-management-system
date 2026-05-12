# 06 — Jenkins CI/CD Pipeline

> **Module 6 of 6** | Prerequisites: Modules 01–05 done, Docker Hub account ready, GitHub repo ready.

---

## What You Are Building

A pipeline that runs automatically every time you push code to GitHub:

```
git push
   ↓
GitHub → Jenkins (via ngrok tunnel)
   ↓
npm install + run tests
   ↓
docker build → docker push → Docker Hub
   ↓
kubectl deploy → Kubernetes
```

---

## Windows Setup — Two Terminals

Open these two terminals and keep them open throughout:

| Terminal | Used for |
|---|---|
| **WSL** (`wsl` in PowerShell) | All `docker` and `kubectl` commands |
| **CMD** (regular Command Prompt) | `git add`, `git commit`, `git push` |

> All commands below marked **[WSL]** go in WSL. Commands marked **[CMD]** go in CMD.

---

## Step 1 — Start Jenkins

**[WSL]**
```bash
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts
```

Wait 30 seconds, then get the admin password:

**[WSL]**
```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Open `http://localhost:8080` in your browser → paste the password → **Install suggested plugins** → create your admin user.

---

## Step 2 — Configure Jenkins (browser, do once)

### Install plugins

**Manage Jenkins → Plugins → Available plugins** — search and install each:

- NodeJS
- Docker Pipeline
- Blue Ocean

Restart Jenkins when prompted.

### Add Node.js

**Manage Jenkins → Tools → NodeJS → Add NodeJS**
- Name: `nodejs-18`
- Version: NodeJS 18.x
- Tick: Install automatically

Click **Save**.

### Fix missing system libraries

Jenkins needs two extra packages to run `docker` commands and Node.js. Run this once:

**[WSL]**
```bash
docker exec -it --user root jenkins bash -c \
  "apt-get update && apt-get install -y docker.io libatomic1"
```

> You will see some `debconf` warnings — ignore them, the install succeeds regardless.

---

## Step 3 — Create the App

**[CMD]** — in your project folder:
```cmd
mkdir hello-jenkins
cd hello-jenkins
git init
git remote add origin https://github.com/YOUR_USERNAME/hello-jenkins.git
```

> Create the repo on GitHub first (github.com/new → name: `hello-jenkins` → public → no README).

Create these four files:

**`app.js`**
```js
const express = require('express');
const app = express();

app.get('/', (req, res) => res.send('Hello from Jenkins CI/CD!'));

module.exports = app;

if (require.main === module) {
  app.listen(3000, () => console.log('Running on port 3000'));
}
```

**`app.test.js`**
```js
const request = require('supertest');
const app = require('./app');

test('GET / returns greeting', async () => {
  const res = await request(app).get('/');
  expect(res.statusCode).toBe(200);
  expect(res.text).toContain('Hello from Jenkins CI/CD!');
});
```

**`package.json`**
```json
{
  "name": "hello-jenkins",
  "version": "1.0.0",
  "main": "app.js",
  "scripts": {
    "start": "node app.js",
    "test": "jest --ci --reporters=default --reporters=jest-junit"
  },
  "dependencies": { "express": "4.18.2" },
  "devDependencies": {
    "jest": "29.7.0",
    "jest-junit": "16.0.0",
    "supertest": "6.3.4"
  }
}
```

**`.gitignore`**
```
node_modules/
*.log
test-results/
```

Now install dependencies and push:

**[CMD]**
```cmd
npm install
git add .
git commit -m "initial commit"
git push -u origin main
```

---

## Step 4 — Add a Dockerfile

Create **`Dockerfile`** in the same folder:

```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --production
COPY app.js .
EXPOSE 3000
CMD ["node", "app.js"]
```

**[CMD]**
```cmd
git add Dockerfile
git commit -m "Add Dockerfile"
git push
```

---

## Step 5 — Add Jenkinsfile

Create **`Jenkinsfile`** in the same folder:

```groovy
pipeline {
    agent any

    tools {
        nodejs 'nodejs-18'
    }

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        IMAGE_NAME = "YOUR_DOCKERHUB_USERNAME/hello-jenkins"
        IMAGE_TAG  = "${BUILD_NUMBER}"
        JEST_JUNIT_OUTPUT_DIR  = 'test-results'
        JEST_JUNIT_OUTPUT_NAME = 'junit.xml'
    }

    stages {

        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Install & Test') {
            steps { sh 'npm ci && npm test' }
            post {
                always { junit 'test-results/junit.xml' }
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                sh """
                    echo ${DOCKERHUB_CREDENTIALS_PSW} | \
                    docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin
                    docker push ${IMAGE_NAME}:${IMAGE_TAG}
                """
            }
        }

        stage('Deploy to Kubernetes') {
            when { branch 'main' }
            steps {
                sh """
                    kubectl set image deployment/hello-jenkins \
                        hello-jenkins=${IMAGE_NAME}:${IMAGE_TAG}
                    kubectl rollout status deployment/hello-jenkins --timeout=120s
                """
            }
        }

    }

    post {
        success { echo "Build ${BUILD_NUMBER} deployed successfully." }
        failure {
            sh "kubectl rollout undo deployment/hello-jenkins || true"
            echo "Build ${BUILD_NUMBER} failed — rolled back."
        }
    }
}
```

> Replace `YOUR_DOCKERHUB_USERNAME` with your actual Docker Hub username.

**[CMD]**
```cmd
git add Jenkinsfile
git commit -m "Add Jenkinsfile"
git push
```

---

## Step 6 — Add Docker Hub Credentials to Jenkins

Jenkins needs your Docker Hub password to push images.

**Manage Jenkins → Credentials → (global) → Add Credentials**
- Kind: `Username with password`
- Username: your Docker Hub username
- Password: your Docker Hub password (or access token)
- ID: `dockerhub-creds`
- Click **Create**

---

## Step 7 — Set Up Kubernetes (run once)

Mount your kubeconfig into Jenkins so it can run `kubectl`:

**[WSL]**
```bash
docker stop jenkins && docker rm jenkins

docker run -d \
  --name jenkins \
  -p 8080:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v ~/.kube:/root/.kube:ro \
  jenkins/jenkins:lts
```

Re-install the packages (new container):

**[WSL]**
```bash
docker exec -it --user root jenkins bash -c \
  "apt-get update && apt-get install -y docker.io libatomic1"
```

Install `kubectl` inside Jenkins:

**[WSL]**
```bash
docker exec -it --user root jenkins bash -c \
  "curl -LO https://dl.k8s.io/release/v1.29.0/bin/linux/amd64/kubectl && \
   chmod +x kubectl && mv kubectl /usr/local/bin/"
```

Create the Kubernetes deployment (run once):

**[WSL]**
```bash
kubectl create deployment hello-jenkins \
  --image=YOUR_DOCKERHUB_USERNAME/hello-jenkins:latest \
  --replicas=2

kubectl expose deployment hello-jenkins \
  --type=NodePort --port=80 --target-port=3000 \
  --name=hello-jenkins-svc
```

Verify Jenkins can reach the cluster:

**[WSL]**
```bash
docker exec jenkins kubectl get nodes
```

---

## Step 8 — Create the Jenkins Job

1. Browser → `http://localhost:8080` → **New Item**
2. Name: `hello-jenkins` → select **Pipeline** → OK
3. Scroll to **Pipeline** section:
   - Definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: `https://github.com/YOUR_USERNAME/hello-jenkins`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
4. **Save** → **Build Now**

Watch the build in **Console Output**. It should go green.

---

## Step 9 — Set Up Auto-Trigger (ngrok + GitHub Webhook)

Right now Jenkins only builds when you click **Build Now**. This step makes it build automatically on every `git push`.

**Why ngrok:** Jenkins is running on your laptop — GitHub cannot reach it directly. ngrok creates a public URL that tunnels through to your local Jenkins.

### Start ngrok

Open a **third terminal (WSL)** and keep it open:

**[WSL]**
```bash
curl -Lo ngrok.zip https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-linux-amd64.zip
unzip ngrok.zip && sudo mv ngrok /usr/local/bin/
ngrok config add-authtoken YOUR_NGROK_TOKEN
ngrok http 8080
```

> Get your free token at `https://dashboard.ngrok.com/get-started/your-authtoken`

ngrok will show a URL like:
```
Forwarding  https://a1b2c3d4.ngrok-free.app -> http://localhost:8080
```

Copy that URL.

### Enable the trigger in Jenkins

In your `hello-jenkins` job → **Configure** → **Build Triggers** → tick **GitHub hook trigger for GITScm polling** → **Save**.

### Add webhook in GitHub

Go to your `hello-jenkins` repo on GitHub → **Settings → Webhooks → Add webhook**:
- Payload URL: `https://a1b2c3d4.ngrok-free.app/github-webhook/`
  (your ngrok URL + `/github-webhook/`)
- Content type: `application/json`
- Click **Add webhook**

GitHub sends a test ping — a green tick confirms it works.

### Test it

Make any change and push:

**[CMD]**
```cmd
git commit --allow-empty -m "trigger test"
git push
```

Jenkins starts a build automatically within seconds.

> **Note:** The ngrok URL changes each time you restart ngrok. If you restart it, update the webhook URL in GitHub Settings.

---

## Quick Reference

### Daily startup sequence

```
1. [WSL]    docker start jenkins
2. [WSL]    ngrok http 8080          ← keep this terminal open
3. Browser  http://localhost:8080
```

### If a build fails — check logs

**[WSL]**
```bash
docker logs jenkins
```

Or in browser: click the build number → **Console Output**.

### Useful commands

```bash
# [WSL] Check Jenkins is running
docker ps

# [WSL] Check Kubernetes pods
kubectl get pods

# [WSL] See what image is deployed
kubectl get deployment hello-jenkins -o wide

# [WSL] Manual rollback
kubectl rollout undo deployment/hello-jenkins
```

### Jenkins URLs

| URL | Purpose |
|---|---|
| `http://localhost:8080` | Dashboard |
| `http://localhost:8080/blue` | Visual pipeline view |
| `http://localhost:8080/manage` | Settings |
| `http://localhost:8080/credentials` | Stored secrets |

---

## How It All Fits Together

```
Module 01 — Git & GitHub   → source of truth, triggers the pipeline
Module 02 — Docker         → packages the app, pushes to Docker Hub
Module 03 — YAML           → config language for Kubernetes
Module 04 — Kubernetes     → runs the app, does rolling updates
Module 05 — Ansible        → provisions the servers
Module 06 — Jenkins        → automates all of the above on every git push
```

## The complete flow  

```
Developer (Windows 11)
        │
        │  git push
        ▼
    ┌────────┐
    │ GitHub │  source code stored here
    └────────┘
        │
        │  webhook (ngrok tunnel)
        │  triggers automatically on every push
        ▼
  ┌─────────┐
  │ Jenkins │
  └─────────┘
        │
        ├─── Stage 1: Checkout ──────────────────── pulls latest code from GitHub
        │
        ├─── Stage 2: Install & Test ────────────── npm install + npm test (Jest)
        │                                           ✗ tests fail → pipeline stops here
        │                                           ✓ tests pass → continue
        │
        ├─── Stage 3: Build Docker Image ────────── docker build
        │                                           packages app + dependencies
        │                                           into a portable image
        │
        ├─── Stage 4: Push to Docker Hub ────────── docker push
        │                                           image stored in the cloud
        │              ┌──────────────┐             available to any server
        │              │  Docker Hub  │
        │              │  (registry)  │
        │              └──────────────┘
        │                     │
        │                     │  docker pull (automatic)
        │                     ▼
        └─── Stage 5: Deploy ────────────────────── docker run
                                                    old container stopped
                                                    new container started
                                                         │
                                                         ▼
                                               http://localhost:3000
                                               app is live ✓
```
