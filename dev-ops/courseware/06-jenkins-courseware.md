# 06 — Jenkins (CI/CD Pipelines)

> **Series:** DevOps Hands-On | **Module:** 6 of 6 | **Project:** Full CI/CD pipeline

> **Prerequisites:** Modules 01–05 completed. Docker Hub account with `yourname/hello-express:1.0` pushed. GitHub repository set up.
>
> **Environment:** Windows 11, Docker Desktop. All `docker` commands in **PowerShell**. Pipeline `sh` steps run inside the Jenkins Linux container — Linux commands work regardless of Windows host OS.

---

## About This Guide

This is the sixth and final module in the DevOps series. Every previous module built one piece of the delivery system. This module connects all of them into a single automated pipeline — a push to GitHub triggers everything else without any manual intervention.

**What you will learn:**
- Install and configure Jenkins in Docker on Windows 11
- Write declarative Jenkinsfile pipelines stored in Git
- Connect Jenkins to GitHub using webhooks (with ngrok for the local lab)
- Build a Node.js Express app with npm inside Jenkins
- Run the app as a local Docker container as a first deploy step
- Integrate Docker: build images and push to Docker Hub from the pipeline
- Integrate Kubernetes: deploy to the cluster with rolling updates and auto-rollback
- Use parallel stages, conditional logic, and parameterised builds

**How this fits into the series:**
```
01 Git & GitHub  — source of truth; webhook triggers the pipeline
02 Docker        — `docker build` and `docker push` happen in the pipeline
03 YAML          — Kubernetes YAMLs are applied by the pipeline
04 Kubernetes    — the deployment target; rolling updates happen here
05 Ansible       — provisions the servers; called from the deploy stage
06 Jenkins       ← YOU ARE HERE — automates everything above, end to end
```

**Project thread:** The pipeline built in this module takes the Node.js Express app from a `git push` all the way to a running Kubernetes Deployment — with npm installing dependencies and running tests, Docker packaging it, Docker Hub storing it, and Kubernetes deploying it. A failed deployment triggers an automatic `kubectl rollout undo`.

**Tools needed for this module:** Docker Desktop (with Kubernetes enabled), Windows Terminal (PowerShell), a GitHub account, a Docker Hub account, ngrok (for local webhook testing).

---

## The Bridge from Ansible to Jenkins

In Ansible you wrote a deployment playbook. But someone still had to *trigger* it manually — and before that, manually run tests, build the Docker image, and push it.

Jenkins automates the trigger. The moment you push code, the entire chain fires automatically.

| What you did manually | What Jenkins automates |
|---|---|
| `git push` → remember to test | Push → tests run automatically |
| `docker build` → `docker push` | Tests pass → image built and pushed |
| `ansible-playbook deploy.yaml` | Image pushed → deployment triggered |
| Hope nobody forgot a step | Every step runs in order, every time |

```
Manual:   Developer pushes → remembers to test → builds → deploys
          (Steps get skipped. Environments drift. Fridays are scary.)

Jenkins:  Developer pushes.
          Everything else happens automatically.
          (Pipeline is the process. Process is in Git.)
```

---

## Step 1 — Installing Jenkins on Windows 11

### Run Jenkins in Docker

```powershell
docker run -d `
  --name jenkins `
  -p 8080:8080 `
  -p 50000:50000 `
  -v jenkins_home:/var/jenkins_home `
  -v /var/run/docker.sock:/var/run/docker.sock `
  jenkins/jenkins:lts
```

Or 


```powershell
docker run -d --name jenkins -p 8080:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock jenkins/jenkins:lts
```

> **PowerShell:** Backtick `` ` `` is PowerShell's line-continuation (equivalent to `\` in bash).

| Flag | Purpose |
|---|---|
| `-p 8080:8080` | Jenkins web UI |
| `-p 50000:50000` | Agent communication port |
| `-v jenkins_home:/var/jenkins_home` | Persist all Jenkins data |
| `-v /var/run/docker.sock:/var/run/docker.sock` | Let Jenkins run `docker` commands |

```powershell
# Get initial admin password
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Browser → `http://localhost:8080` → paste password → Install suggested plugins → create admin user.

### Install Docker CLI inside Jenkins

The Jenkins image includes the Docker *socket* mount but not the Docker *CLI*. Without this, every `docker` command in the pipeline will fail with `docker: command not found`.

```powershell
docker exec -it --user root jenkins bash -c "
  apt-get update &&
  apt-get install -y docker.io
"
```


Or 


```powershell
docker exec -it --user root jenkins bash -c " apt-get update && apt-get install -y docker.io " 
```


Verify:

```powershell
docker exec jenkins docker --version
```

> You only need to do this once. The `jenkins_home` volume persists everything — this survives container restarts.

### Configure Node.js in Jenkins

Do this now — before writing any pipeline — so the `tools { nodejs 'nodejs-18' }` block works when you reach Step 2.

**Manage Jenkins → Tools:**
1. **NodeJS** → Add → Name: `nodejs-18` → Install automatically → NodeJS 18.x
2. Save

> The NodeJS plugin downloads and installs Node 18 inside the Jenkins container on first pipeline run. The name `nodejs-18` is what you reference in `tools {}` — it must match exactly.

### Plugins to install

**Manage Jenkins → Plugins → Available:**

| Plugin | Required for |
|---|---|
| Pipeline | Declarative pipelines |
| Git | Cloning repos |
| Docker Pipeline | `docker.build`, `docker.push` in pipelines |
| NodeJS | `node` and `npm` builds |
| Kubernetes | `kubectl` in pipelines |
| Blue Ocean | Visual pipeline view |

> **NodeJS Plugin replaces Maven Integration** — instead of configuring JDK + Maven, you configure a Node.js installation. The plugin automatically adds `node` and `npm` to the pipeline's `PATH`.

### Architecture

```
Developer (Windows 11)
    ↓  git push
  GitHub
    ↓  webhook / poll
  Jenkins Controller (Docker container :8080)
    ↓
  Pipeline Stages (sh runs inside Jenkins Linux container)
```

> `sh` in pipeline steps runs inside the Jenkins Linux container — not on Windows. `node`, `npm`, `docker`, `kubectl`, bash all work normally.

---

## Step 2 — Declarative Pipeline Fundamentals

**No project folder needed yet.** For this step only, you paste the pipeline directly into the Jenkins UI. The goal is to learn the syntax and see a pipeline run — without worrying about Git or a real app.

From Step 3 onwards, the pipeline moves into a `Jenkinsfile` that lives inside your project folder.

### Create a Pipeline job in Jenkins

1. Browser → `http://localhost:8080`
2. **New Item** → enter name `hello-pipeline` → select **Pipeline** → OK
3. Scroll down to the **Pipeline** section
4. **Definition** → select **Pipeline script** *(not "Pipeline script from SCM")*
5. Paste the pipeline below into the text box
6. **Save** → **Build Now**

```groovy
pipeline {
    agent any

    tools {
        nodejs 'nodejs-18'
    }

    environment {
        APP_NAME = 'hello-jenkins'
    }

    stages {
        stage('Hello') {
            steps {
                echo "Building ${APP_NAME}"
                sh 'node --version && npm --version'
            }
        }
    }

    post {
        success { echo 'SUCCESS' }
        failure { echo 'FAILED'  }
        always  { echo 'Always runs — good for cleanup' }
    }
}
```

Jenkins runs it immediately. Click the build number → **Console Output** to see each line execute.

> **"Pipeline script" vs "Pipeline script from SCM"**
> - **Pipeline script** — you paste the pipeline directly in the Jenkins UI. Good for learning and quick tests.
> - **Pipeline script from SCM** — Jenkins reads the `Jenkinsfile` from your Git repository. This is what you use from Step 3 onwards — it's the real-world approach where the pipeline lives with the code.

```
Step 2  →  Pipeline script      (paste in UI, no project folder needed)
Step 3+ →  Pipeline script from SCM  (Jenkinsfile in your Git repo)
```

| Block | Purpose |
|---|---|
| `pipeline` | Root wrapper |
| `agent` | Where to run — `any`, label, or Docker image |
| `tools` | Pre-configured Node.js from Global Tool Config |
| `environment` | Key-value env vars for all stages |
| `stages` | Ordered list of stages |
| `stage` | Named phase — shown as a box in Blue Ocean |
| `steps` | Commands inside a stage |
| `post` | After all stages — always / success / failure |

---

## Step 3 — CI Pipeline: Git + Jenkins + Node.js

Core syllabus pipeline: a **Node.js Express app** built with npm, triggered by GitHub push.

### Create the project folder

On your Windows machine, open PowerShell and create the project:

```powershell
mkdir hello-jenkins
cd hello-jenkins
git init
git remote add origin https://github.com/yourname/hello-jenkins.git

e.g. 
git remote add origin https://github.com/dyesmuk/hello-jenkins.git
```

> Create the GitHub repository first at `github.com/new` (name: `hello-jenkins`, public, no README). Then the remote add above will work.

Create all four files — `package.json`, `app.js`, `app.test.js`, and `.gitignore` — as shown below, then run:

```powershell
npm install
```

This generates `package-lock.json`. **Commit it** — `npm ci` in the pipeline reads from this file exactly. Without it, the pipeline fails.

```powershell
git add .
git commit -m "Initial Node.js Express app"
git push -u origin main
```

Your folder should look like this:

```
hello-jenkins/
  ├── app.js
  ├── app.test.js
  ├── package.json
  ├── package-lock.json   ← generated by npm install, must be committed
  ├── Jenkinsfile         ← added below
  └── .gitignore
```

`package.json`:

```json
{
  "name": "hello-jenkins",
  "version": "1.0.0",
  "description": "Jenkins CI/CD demo app",
  "main": "app.js",
  "scripts": {
    "start": "node app.js",
    "test": "jest --ci --reporters=default --reporters=jest-junit"
  },
  "dependencies": {
    "express": "4.18.2"
  },
  "devDependencies": {
    "jest": "29.7.0",
    "jest-junit": "16.0.0",
    "supertest": "6.3.4"
  }
}
```

`app.js`:

```js
const express = require('express');
const app = express();

app.get('/', (req, res) => {
  res.send('Hello from Jenkins CI/CD!');
});

module.exports = app;

if (require.main === module) {
  app.listen(3000, () => console.log('Server running on port 3000'));
}
```

`app.test.js`:

```js
const request = require('supertest');
const app = require('./app');

test('GET / returns greeting', async () => {
  const res = await request(app).get('/');
  expect(res.statusCode).toBe(200);
  expect(res.text).toContain('Hello from Jenkins CI/CD!');
});
```

`.gitignore` / `.dockerignore`:

```
node_modules/
.env
*.log
junit.xml
test-results/
```

> **Why `module.exports = app` and the `require.main` guard?** — Separating the Express app from the `listen()` call means the test file can import `app` without starting a real server. The guard `if (require.main === module)` runs `listen` only when `node app.js` is invoked directly.

### `Jenkinsfile` — Git + Jenkins + Node.js

```groovy
pipeline {
    agent any

    tools {
        nodejs 'nodejs-18'
    }

    environment {
        APP_NAME = 'hello-jenkins'
        JEST_JUNIT_OUTPUT_DIR = 'test-results'
        JEST_JUNIT_OUTPUT_NAME = 'junit.xml'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch: ${GIT_BRANCH} | Commit: ${GIT_COMMIT[0..7]}"
            }
        }

        stage('Install') {
            steps {
                sh 'npm ci'
            }
        }

        stage('Test') {
            steps {
                sh 'npm test'
            }
            post {
                always {
                    junit 'test-results/junit.xml'
                }
            }
        }

        stage('Archive Artifact') {
            steps {
                archiveArtifacts artifacts: 'app.js,package.json', fingerprint: true
            }
        }

    }

    post {
        success { echo "Build ${BUILD_NUMBER} succeeded." }
        failure { echo "Build ${BUILD_NUMBER} failed."   }
    }
}
```

> **`npm ci` vs `npm install`** — `npm ci` always deletes `node_modules` and installs from `package-lock.json` exactly. Faster, reproducible, no surprises from floating versions. Always use `npm ci` in CI pipelines.

### Create a Pipeline job and connect to GitHub

Create a new job for the real app — separate from the `hello-pipeline` you used in Step 2.

1. **New Item** → name: `hello-jenkins` → select **Pipeline** → OK
2. Scroll to **Pipeline** section
3. **Definition** → select **Pipeline script from SCM**
4. Fill in:
   - SCM: Git
   - Repository URL: `https://github.com/yourname/hello-jenkins`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
5. **Save** → **Build Now**

### Triggers

**Option A — GitHub Webhook (instant):**

Jenkins: **Build Triggers → GitHub hook trigger for GITScm polling**

GitHub: **Settings → Webhooks → Add webhook**
- Payload URL: `http://YOUR_IP:8080/github-webhook/`
- Content type: `application/json`

> **Windows 11 lab:** Use ngrok to expose localhost:
> ```powershell
> winget install ngrok
> ngrok http 8080
> ```
> Use the `https://xxxx.ngrok.io` URL as the webhook payload URL.

**Option B — SCM polling:**
```groovy
triggers { pollSCM('H/5 * * * *') }
```

### npm commands reference

| Command | What it does |
|---|---|
| `npm install` | Install dependencies (updates `package-lock.json`) |
| `npm ci` | Clean install from `package-lock.json` exactly — use in CI |
| `npm test` | Run the `test` script from `package.json` |
| `npm start` | Run the `start` script (`node app.js`) |
| `npm run <script>` | Run any custom script defined in `package.json` |
| `npm run lint` | Run linter (if configured) |
| `npm audit` | Check for known vulnerabilities in dependencies |
| `npm audit --audit-level=high` | Fail only on high/critical vulnerabilities |
| `npm prune --production` | Remove devDependencies (before packaging) |

---

## Step 4 — First Deployment: Run the App as a Docker Container

Before pushing to Docker Hub or Kubernetes, the simplest possible deployment is: build the image, run it locally as a container, and verify it responds. One pipeline, one `docker run`.

```
npm test passes
      ↓
docker build → local image
      ↓
docker run   → container on port 3001
      ↓
curl localhost:3001 → Hello from Jenkins CI/CD!
```

### Add a Dockerfile to the project

The pipeline uses `docker build` — which requires a `Dockerfile` in the project root. Create this file now and commit it:

```dockerfile
FROM node:18-alpine

WORKDIR /app

COPY package*.json ./

RUN npm ci --production

COPY app.js .

EXPOSE 3000

CMD ["node", "app.js"]
```

```powershell
git add Dockerfile
git commit -m "Add Dockerfile"
git push
```

> `npm ci --production` installs only `dependencies`, not `devDependencies` (jest, supertest). Tests run in the pipeline *before* the Docker build — the image only needs the runtime code.

### `Jenkinsfile` — build, run, verify

```groovy
pipeline {
    agent any

    tools {
        nodejs 'nodejs-18'
    }

    environment {
        APP_NAME = 'hello-jenkins'
        JEST_JUNIT_OUTPUT_DIR  = 'test-results'
        JEST_JUNIT_OUTPUT_NAME = 'junit.xml'
    }

    stages {

        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Install') {
            steps { sh 'npm ci' }
        }

        stage('Test') {
            steps { sh 'npm test' }
            post {
                always { junit 'test-results/junit.xml' }
            }
        }

        stage('Build Image') {
            steps {
                sh "docker build -t ${APP_NAME}:${BUILD_NUMBER} ."
            }
        }

        stage('Run Container') {
            steps {
                sh "docker rm -f ${APP_NAME} || true"
                sh "docker run -d --name ${APP_NAME} -p 3001:3000 ${APP_NAME}:${BUILD_NUMBER}"
                sh "sleep 3 && curl -f http://localhost:3001/ || (docker logs ${APP_NAME} && exit 1)"
            }
        }

    }

    post {
        success { echo "App running at http://localhost:3001/" }
        failure { echo "Build ${BUILD_NUMBER} failed." }
    }
}
```

### Verify

Browser → `http://localhost:3001/` → **Hello from Jenkins CI/CD!**

### Three Questions to Ask Trainees

**1. "Why `docker rm -f hello-jenkins || true` before `docker run`?"**
→ `docker run` fails if a container with the same name already exists. The `|| true` means: try to remove it, and if there's nothing to remove, that's fine — don't fail the pipeline.

**2. "Why `sleep 3` before `curl`?"**
→ The container starts instantly but Node.js takes a moment to bind to the port. Without the sleep, `curl` hits the port before Express is ready and the pipeline fails a perfectly good build.

**3. "This works — so why do we need Docker Hub and Kubernetes at all?"**
→ This container only runs on the Jenkins machine. No one else can reach it, it doesn't survive a restart, and there's only one copy. Docker Hub lets any server pull the image. Kubernetes runs multiple copies, restarts failed ones, and updates them with zero downtime. The next two steps build toward that.

---

## Step 5 — Integrating Docker in the CI/CD Pipeline

In Step 4 the image stayed local on the Jenkins machine. This step pushes it to Docker Hub so any server — or Kubernetes — can pull it.

### Add Docker Hub credentials first

The pipeline references `credentials('dockerhub-creds')`. Jenkins must have this stored *before* the pipeline runs, or it will fail immediately.

**Manage Jenkins → Credentials → (global) → Add Credentials:**
- Kind: Username with password
- Username: your Docker Hub username
- Password: your Docker Hub password or access token
- ID: `dockerhub-creds`
- Save

> The credential ID `dockerhub-creds` is what the pipeline references. The actual username and password are never shown in logs — Jenkins masks them.

### `Jenkinsfile` — Node.js + Docker

```groovy
pipeline {
    agent any

    tools {
        nodejs 'nodejs-18'
    }

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        IMAGE_NAME = "yourname/hello-jenkins"
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

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                sh "docker tag  ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest"
            }
        }

        stage('Push to Docker Hub') {
            steps {
                sh """
                    echo ${DOCKERHUB_CREDENTIALS_PSW} | \
                    docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin
                    docker push ${IMAGE_NAME}:${IMAGE_TAG}
                    docker push ${IMAGE_NAME}:latest
                """
            }
        }

        stage('Smoke Test') {
            steps {
                sh "docker rm -f smoke-test || true"
                sh "docker run -d --name smoke-test -p 3002:3000 ${IMAGE_NAME}:${IMAGE_TAG}"
                sh "sleep 5 && curl -f http://localhost:3002/ || (docker logs smoke-test && exit 1)"
                sh "docker rm -f smoke-test"
            }
        }

        stage('Cleanup') {
            steps {
                sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true"
            }
        }

    }

    post {
        success { echo "Image ${IMAGE_NAME}:${IMAGE_TAG} pushed." }
        failure { echo "Pipeline failed at build ${BUILD_NUMBER}." }
    }
}
```

### The Docker CI/CD flow

```
git push → Jenkins
    ↓
npm ci && npm test   (install dependencies + run Jest tests)
    ↓
docker build :42     (package app + node_modules into image)
    ↓
docker push :42      → Docker Hub
    ↓
Smoke test           (run container, curl endpoint, verify response)
    ↓
docker rmi           (clean local image from Jenkins agent)
```

---

## Step 6 — Integrating Kubernetes in the CI/CD Pipeline

After the Docker image is on Docker Hub, deploy it to Kubernetes with zero downtime.

### Configure kubectl inside Jenkins

Jenkins needs access to your kubeconfig file to run `kubectl` commands. This requires recreating the container with an extra volume mount.

> **Your Jenkins config is safe** — all jobs, credentials, and plugins live in the `jenkins_home` named volume. Stopping and removing the *container* does not touch the volume. When you re-run with the new flags, everything is exactly as you left it.

```powershell
# Recreate Jenkins with kubeconfig mounted
docker stop jenkins && docker rm jenkins

docker run -d `
  --name jenkins `
  -p 8080:8080 -p 50000:50000 `
  -v jenkins_home:/var/jenkins_home `
  -v /var/run/docker.sock:/var/run/docker.sock `
  -v ${env:USERPROFILE}\.kube:/root/.kube:ro `
  jenkins/jenkins:lts

# Install kubectl inside Jenkins container
docker exec -it --user root jenkins bash -c `
  "curl -LO https://dl.k8s.io/release/v1.29.0/bin/linux/amd64/kubectl && `
   chmod +x kubectl && mv kubectl /usr/local/bin/"

# Verify
docker exec jenkins kubectl get nodes
```

### Create the initial K8s Deployment (run once)

```powershell
kubectl create deployment hello-jenkins `
  --image=yourname/hello-jenkins:latest `
  --replicas=3

kubectl expose deployment hello-jenkins `
  --type=NodePort `
  --port=80 --target-port=3000 `
  --name=hello-jenkins-svc
```

> **`--target-port=3000`** — Node.js Express listens on port 3000 inside the container (set in `app.js`). The Service maps external port 80 to the container's port 3000.

### `Jenkinsfile` — Full pipeline: Node.js → Docker → Kubernetes

```groovy
pipeline {
    agent any

    tools {
        nodejs 'nodejs-18'
    }

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        IMAGE_NAME      = "yourname/hello-jenkins"
        IMAGE_TAG       = "${BUILD_NUMBER}"
        DEPLOYMENT_NAME = "hello-jenkins"
        CONTAINER_NAME  = "hello-jenkins"
        K8S_NAMESPACE   = "default"
        JEST_JUNIT_OUTPUT_DIR  = 'test-results'
        JEST_JUNIT_OUTPUT_NAME = 'junit.xml'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                echo "Building ${GIT_BRANCH} @ ${GIT_COMMIT[0..7]}"
            }
        }

        stage('Install & Test') {
            steps { sh 'npm ci && npm test' }
            post {
                always { junit 'test-results/junit.xml' }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                sh "docker tag  ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest"
            }
        }

        stage('Push to Docker Hub') {
            steps {
                sh """
                    echo ${DOCKERHUB_CREDENTIALS_PSW} | \
                    docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin
                    docker push ${IMAGE_NAME}:${IMAGE_TAG}
                    docker push ${IMAGE_NAME}:latest
                """
            }
        }

        stage('Deploy to Kubernetes') {
            when { branch 'main' }
            steps {
                sh """
                    kubectl set image deployment/${DEPLOYMENT_NAME} \
                        ${CONTAINER_NAME}=${IMAGE_NAME}:${IMAGE_TAG} \
                        -n ${K8S_NAMESPACE}

                    kubectl rollout status deployment/${DEPLOYMENT_NAME} \
                        -n ${K8S_NAMESPACE} \
                        --timeout=120s
                """
            }
        }

        stage('Verify') {
            when { branch 'main' }
            steps {
                sh "kubectl get pods -n ${K8S_NAMESPACE} -l app=${DEPLOYMENT_NAME}"
                sh "kubectl get svc hello-jenkins-svc -n ${K8S_NAMESPACE}"
            }
        }

        stage('Cleanup') {
            steps { sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true" }
        }

    }

    post {
        success {
            echo "Build ${BUILD_NUMBER}: image :${IMAGE_TAG} deployed to Kubernetes."
        }
        failure {
            sh """
                echo 'Pipeline failed — rolling back Kubernetes deployment'
                kubectl rollout undo deployment/${DEPLOYMENT_NAME} \
                    -n ${K8S_NAMESPACE} || true
            """
        }
    }
}
```

### The complete end-to-end flow

```
Developer: git push origin main
                ↓
        GitHub Webhook → Jenkins
                ↓
┌──────────────────────────────────────────────────────────────┐
│  Checkout      → git clone                                   │
│  Install&Test  → npm ci + jest + junit results               │
│  Docker Build  → docker build :42                            │
│  Docker Push   → docker push :42 → Docker Hub               │
│  K8s Deploy    → kubectl set image :42                       │
│                  kubectl rollout status (waits for healthy)  │
│  Verify        → kubectl get pods + svc                      │
│  Cleanup       → docker rmi local image                      │
└──────────────────────────────────────────────────────────────┘
                ↓
  Kubernetes pulls :42 from Docker Hub
  Rolling update — Pods replaced one at a time
  Zero downtime
                ↓
  Pipeline failed? → post { failure } → kubectl rollout undo
```

### Three Questions to Ask Trainees

**1. "Why does the K8s deploy stage have `when { branch 'main' }`?"**
→ Feature branches build and test but never deploy to production. Only `main` — code-reviewed and approved — reaches the cluster.

**2. "What happens to live traffic during `kubectl set image`?"**
→ Kubernetes replaces Pods one at a time (rolling update). Some run old, some new, all behind the Service. Zero downtime — unlike a simple `docker run` replacement.

**3. "Why is `kubectl rollout status --timeout=120s` important?"**
→ Without it, the pipeline finishes immediately after issuing the update, before knowing if it worked. `rollout status` blocks until all Pods are healthy — or fails the pipeline if they aren't within 120 seconds, triggering the auto-rollback.

---

## Step 7 — Advanced Pipeline Patterns

### Parallel stages

```groovy
stage('Validate') {
    parallel {
        stage('Unit Tests')   { steps { sh 'npm test'                    } }
        stage('Lint')         { steps { sh 'npm run lint'                } }
        stage('Audit')        { steps { sh 'npm audit --audit-level=high'} }
    }
}
```

> Add a `lint` script to `package.json` to enable the Lint stage:
> ```json
> "scripts": {
>   "start": "node app.js",
>   "test": "jest --ci --reporters=default --reporters=jest-junit",
>   "lint": "eslint ."
> }
> ```

### Conditional logic

```groovy
when { branch 'main' }
when { not { branch 'main' } }
when { changeset '**/Dockerfile' }
when { expression { return params.DEPLOY == 'true' } }
```

### Parameterised builds

```groovy
parameters {
    string(name: 'IMAGE_TAG',   defaultValue: 'latest', description: 'Tag to deploy')
    booleanParam(name: 'SKIP_TESTS', defaultValue: false)
    choice(name: 'ENVIRONMENT', choices: ['dev','staging','prod'])
}
```

### Blue Ocean

**Manage Jenkins → Plugins → Available → Blue Ocean → Install**

`http://localhost:8080/blue` — horizontal flow diagram, pass/fail per stage, PR integration.

---

## Jenkins — Key Concepts Summary

### The Big Picture

Jenkins automates software delivery: watches Git, triggers pipelines on push, ensures code is tested, built, and deployed — consistently, without human intervention.

---

### Core Concepts

**Pipeline** — Full workflow from commit to deployment. Lives in a `Jenkinsfile` in the repo.

**Jenkinsfile** — Groovy DSL. Declarative syntax recommended.

**Stage** — Named phase. Failed stage stops the pipeline.

**Step** — Single command inside a stage. `sh` = shell. `junit` = publish test results. `archiveArtifacts` = save files.

**tools** — Pre-installed runtimes (Node.js) from Global Tool Config. Available in pipelines by name.

**Credential Store** — Secure vault. Referenced by ID. Never shown in logs.

**Build Number** — Auto-incrementing per run. Used as Docker image tag for traceability.

**Webhook** — GitHub calls Jenkins on push. Instant trigger.

**`host.docker.internal`** — Docker Desktop hostname → Windows host IP. How Jenkins container reaches other containers.

**Rolling update** — Kubernetes replaces Pods one at a time. Zero downtime.

**Auto-rollback** — `post { failure { kubectl rollout undo } }` — reverts K8s deployment if pipeline fails.

**`npm ci`** — Reproducible install from `package-lock.json`. Always use in CI. Never `npm install` in pipelines.

**`jest-junit`** — Jest reporter that outputs JUnit-compatible XML. Required for Jenkins to publish and track test results over time.

**`|| true`** — Shell pattern used in pipeline steps to make a command non-fatal. `docker rm -f app || true` means: remove the container if it exists, and if it doesn't, don't fail the build.

---

### One-Line Distinctions (commonly confused)

| These seem similar... | But... |
|---|---|
| CI vs CD | CI = test + build on push. CD = deliver to environment automatically. CI is the prerequisite for CD. |
| Declarative vs Scripted pipeline | Declarative = structured, validated. Scripted = full Groovy. Always use Declarative. |
| `stage` vs `step` | Stage = named UI phase. Step = single command inside a stage. |
| `npm install` vs `npm ci` | `npm install` can update `package-lock.json`. `npm ci` installs exactly what's locked — use in CI. |
| `docker run` local vs Docker Hub | Local image only exists on the Jenkins machine. Docker Hub lets any server pull and run it. |
| `kubectl set image` vs `kubectl apply` | `set image` updates one field. `apply` replaces the full spec from a YAML file. |
| Webhook vs SCM polling | Webhook = GitHub calls Jenkins (instant, needs ngrok in lab). Polling = Jenkins checks GitHub periodically. |
| `host.docker.internal` vs `localhost` | Inside a container, `localhost` is the container itself. `host.docker.internal` is your Windows machine. |


---

### How Everything Connects

```
Developer (Windows 11)
    ↓  git push
  GitHub → webhook (ngrok in lab) → Jenkins
    ↓
  Jenkinsfile
    ↓
  ┌──────────────────────────────────────────────────────────┐
  │  Checkout      → git clone                               │
  │  Install&Test  → npm ci + jest + junit                   │
  │  Docker Build  → docker build :42                        │
  │  Docker Push   → Docker Hub (:42)                        │
  │  K8s Deploy    → kubectl set image :42                   │
  │                  rollout status (wait for healthy)       │
  │  Verify        → kubectl get pods                        │
  │  Cleanup       → docker rmi                              │
  └──────────────────────────────────────────────────────────┘
    ↓
  Kubernetes: rolling update, zero downtime
    ↓
  App live — traceable to git commit SHA
    ↓
  Failed? → kubectl rollout undo (auto-rollback)
```

---

## Jenkins Commands & URL Reference

```powershell
# Start Jenkins
docker run -d --name jenkins `
  -p 8080:8080 -p 50000:50000 `
  -v jenkins_home:/var/jenkins_home `
  -v /var/run/docker.sock:/var/run/docker.sock `
  -v ${env:USERPROFILE}\.kube:/root/.kube:ro `
  jenkins/jenkins:lts

# Admin password
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# Restart / logs / stop
docker restart jenkins
docker logs -f jenkins
docker stop jenkins

# Install kubectl in Jenkins
docker exec -it --user root jenkins bash -c `
  "curl -LO https://dl.k8s.io/release/v1.29.0/bin/linux/amd64/kubectl && `
   chmod +x kubectl && mv kubectl /usr/local/bin/"

# Verify cluster access
docker exec jenkins kubectl get nodes

# Public webhook tunnel
ngrok http 8080
```

**Jenkins URLs:**

| URL | Purpose |
|---|---|
| `http://localhost:8080` | Dashboard |
| `http://localhost:8080/blue` | Blue Ocean visual UI |
| `http://localhost:8080/manage` | Manage Jenkins |
| `http://localhost:8080/credentials` | Credential store |
| `http://localhost:8080/manage/configureTools/` | Node.js tool config |
| `http://localhost:8080/restart` | Restart Jenkins |
| `http://localhost:8080/github-webhook/` | GitHub webhook endpoint |

---

## ToC Coverage Map

| CI/CD Syllabus Topic | Covered in |
|---|---|
| Introduction — CI vs CD | Bridge section + Step 1 architecture |
| Jenkins architecture, plugins | Step 1 — Controller, Docker socket, plugin table |
| Declarative pipeline fundamentals | Step 2 — pipeline, agent, tools, stages, steps, post |
| CI/CD pipeline using Git, Jenkins and Node.js | Step 3 — full Jenkinsfile, Node.js config, Jest/junit, `archiveArtifacts`, webhook/ngrok |
| npm commands | Step 3 — `npm` commands reference table |
| First deployment — local Docker container | Step 4 — `docker build`, `docker run`, `curl` smoke check, `\|\| true` pattern |
| Integrating Docker in CI/CD pipeline | Step 5 — Dockerfile for Node.js, docker build/push, smoke test stage |
| Integrating Kubernetes in CI/CD pipeline | Step 6 — kubeconfig mount, `kubectl set image`, rollout status, auto-rollback |
| Parallel stages | Step 7 — `parallel {}` |
| Conditional logic / branch strategy | Steps 6–7 — `when { branch 'main' }` |
| Parameterised builds | Step 7 — `parameters {}` |
| Credentials management | Step 5 — dockerhub-creds, masked secrets |
| Blue Ocean | Step 7 — install + visual view |
| GitHub webhook + ngrok | Step 3 — Windows 11 webhook setup |
| Auto-rollback | Step 6 — `post { failure { kubectl rollout undo } }` |

---

## Series Complete

You have now built the full DevOps pipeline from first principles:

```
Module 01 — Git & GitHub    Code is version-controlled and team-shareable
Module 02 — Docker          App is containerised and image is on Docker Hub
Module 03 — YAML            Configuration language for everything below
Module 04 — Kubernetes      Container is orchestrated with self-healing and scaling
Module 05 — Ansible         Servers are provisioned and configured automatically
Module 06 — Jenkins         The full pipeline runs on every git push, end to end
```

Every tool connects to the one before it. A single `git push` now triggers:

```
GitHub webhook
      ↓
Jenkins pipeline
      ↓
npm ci && npm test  (install + test)
      ↓
docker build :42   (package)
      ↓
docker push :42    → Docker Hub
      ↓
kubectl set image  → Kubernetes rolling update
      ↓
App live — zero downtime — traceable to the exact commit that triggered it
```

The infrastructure (the servers Kubernetes runs on) is provisioned by Ansible, version-controlled in Git, and can itself be re-run at any time to reproduce the environment exactly.
