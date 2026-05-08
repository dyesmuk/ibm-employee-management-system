# Jenkins — Hands-On Guide (CI/CD Pipelines)

> **Prerequisites:** You have completed the Docker, Kubernetes, and Ansible modules. Your `hello-express` image is on Docker Hub. Git and GitHub basics are assumed.

---

## The Bridge from Ansible to Jenkins

In Ansible you wrote a deployment playbook. But someone still had to *trigger* it manually — and before that, someone had to manually run tests, manually build the Docker image, and manually push it to Docker Hub.

Jenkins automates the trigger. It watches your Git repository, and the moment you push code, it kicks off the entire chain automatically.

| What you did manually | What Jenkins automates |
|---|---|
| `git push` → remember to run tests | Push → tests run automatically |
| `docker build` → `docker push` | Tests pass → image built and pushed |
| `ansible-playbook deploy.yaml` | Image pushed → deployment triggered |
| Hope nobody forgot a step | Every step runs in order, every time |

The mental model shift:

```
Manual:   Developer pushes code.
          Developer remembers to test.
          Developer builds the image.
          Developer deploys.
          (Steps get skipped. Environments drift. Fridays are scary.)

Jenkins:  Developer pushes code.
          Everything else happens automatically.
          (Pipeline is the process. Process is in Git.)
```

This is **Continuous Integration / Continuous Delivery**. The pipeline is version-controlled, repeatable, and visible to the whole team.

---

## Step 1 — Installing Jenkins

### Run Jenkins in Docker (recommended for the lab)

Jenkins itself runs as a Docker container. No system install needed.

```bash
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts
```

| Flag | Purpose |
|---|---|
| `-p 8080:8080` | Jenkins web UI |
| `-p 50000:50000` | Jenkins agent communication port |
| `-v jenkins_home:/var/jenkins_home` | Persist Jenkins data (jobs, plugins, config) across restarts |
| `-v /var/run/docker.sock:/var/run/docker.sock` | Let Jenkins run Docker commands on the host |

### Initial Setup

```bash
# Get the initial admin password
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

1. Browser → `http://localhost:8080`
2. Paste the admin password
3. Click **Install suggested plugins** — wait for the install
4. Create your admin user
5. Jenkins is ready

### Install additional plugins

Go to **Manage Jenkins → Plugins → Available plugins** and install:

- **Docker Pipeline** — build and push Docker images in pipelines
- **Git** — clone repositories (usually pre-installed)
- **Pipeline** — declarative pipeline support (usually pre-installed)

### The Architecture

```
Developer
    ↓  git push
  GitHub
    ↓  webhook / poll
  Jenkins Controller (port 8080)
    ↓  dispatches jobs to
  Jenkins Agent(s)
    ↓  runs
  Pipeline Stages (test → build → push → deploy)
```

The **Controller** manages the UI, config, and job scheduling. **Agents** (also called nodes) do the actual work. In our lab, the controller also acts as the agent.

---

## Step 2 — Your First Pipeline (Hello World)

Jenkins pipelines are defined in a file called `Jenkinsfile` — stored in your repository alongside your code. The pipeline lives in Git, not in Jenkins' UI.

### Create a `Jenkinsfile` in your project root

```groovy
pipeline {
    agent any

    stages {
        stage('Hello') {
            steps {
                echo 'Hello from Jenkins Pipeline!'
                sh 'date'
                sh 'whoami'
            }
        }
    }
}
```

### Create a Pipeline Job in Jenkins

1. **New Item** → name it `hello-pipeline` → select **Pipeline** → OK
2. Scroll to **Pipeline** section
3. Set **Definition** to `Pipeline script`
4. Paste the Jenkinsfile content
5. **Save** → **Build Now**

Watch the **Console Output**:
```
Started by user admin
[Pipeline] Start of Pipeline
[Pipeline] agent
[Pipeline] stage
[Pipeline] { (Hello)
[Pipeline] echo
Hello from Jenkins Pipeline!
[Pipeline] sh
+ date
Thu May  7 10:22:31 UTC 2026
[Pipeline] sh
+ whoami
jenkins
[Pipeline] End of Pipeline
Finished: SUCCESS
```

### Anatomy of a Declarative Pipeline

```groovy
pipeline {                    // Root block — always present
    agent any                 // Run on any available agent

    environment {             // Environment variables available to all stages
        APP_NAME = 'hello-express'
    }

    stages {                  // The ordered list of stages
        stage('Test') {       // A stage — shown as a box in the UI
            steps {           // What to do in this stage
                sh 'npm test'
            }
        }

        stage('Build') {
            steps {
                sh 'docker build -t myapp .'
            }
        }
    }

    post {                    // Runs after all stages, regardless of outcome
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed!'
        }
        always {
            echo 'This always runs — good for cleanup'
        }
    }
}
```

| Block | Purpose |
|---|---|
| `pipeline` | Root wrapper — every declarative pipeline starts here |
| `agent` | Where to run — `any`, a specific node, or a Docker container |
| `environment` | Key-value pairs available as env vars throughout the pipeline |
| `stages` | Container for all stages |
| `stage('Name')` | A logical phase of the pipeline — visible in the UI |
| `steps` | The actual commands inside a stage |
| `post` | Actions after the pipeline finishes — cleanup, notifications |

### Three Questions to Ask Trainees

**1. "Why is the Jenkinsfile stored in the Git repository and not in Jenkins itself?"**
→ Because the pipeline *is* part of the codebase. It gets code-reviewed, versioned, and rolled back alongside the application. If Jenkins dies, you haven't lost your pipeline.

**2. "What does `agent any` mean? When would you change it?"**
→ Run on any available agent. You'd change it to target a specific agent with tools installed (e.g., a Java agent for Maven builds, or a Docker-capable agent).

**3. "What's the difference between `stage` and `step`?"**
→ A `stage` is a named phase visible in the Jenkins UI (e.g., "Test", "Build"). A `step` is an individual command inside a stage. Multiple steps can exist in one stage.

---

## Step 3 — CI Pipeline: Test, Build, Push

Now build a real CI pipeline for the Express + MongoDB app. This pipeline runs on every push to GitHub.

### Project structure

```
hello-express/
  ├── app.js
  ├── package.json
  ├── Dockerfile
  └── Jenkinsfile
```

### `package.json` — add a test script

```json
{
  "name": "hello-express",
  "version": "1.0.0",
  "scripts": {
    "test": "node -e \"console.log('Tests passed'); process.exit(0)\""
  },
  "dependencies": {
    "express": "4.18.2",
    "mongoose": "7.6.3"
  }
}
```

### `Jenkinsfile` — full CI pipeline

```groovy
pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        IMAGE_NAME = "yourname/hello-express"
        IMAGE_TAG  = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                echo "Building commit: ${GIT_COMMIT}"
            }
        }

        stage('Install Dependencies') {
            steps {
                sh 'npm install'
            }
        }

        stage('Run Tests') {
            steps {
                sh 'npm test'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                sh "docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest"
            }
        }

        stage('Push to Docker Hub') {
            steps {
                sh "echo ${DOCKERHUB_CREDENTIALS_PSW} | docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin"
                sh "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
                sh "docker push ${IMAGE_NAME}:latest"
            }
        }

        stage('Cleanup') {
            steps {
                sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true"
                sh "docker rmi ${IMAGE_NAME}:latest || true"
            }
        }

    }

    post {
        success {
            echo "Image ${IMAGE_NAME}:${IMAGE_TAG} built and pushed successfully."
        }
        failure {
            echo "Pipeline failed. Image was NOT pushed."
        }
    }
}
```

### Store Docker Hub credentials in Jenkins

1. **Manage Jenkins → Credentials → System → Global credentials → Add Credentials**
2. Kind: **Username with password**
3. Username: your Docker Hub username
4. Password: your Docker Hub password or access token
5. ID: `dockerhub-creds` ← this must match `credentials('dockerhub-creds')` in the Jenkinsfile

`credentials()` injects the username as `_USR` and password as `_PSW` automatically. The password is never printed in logs.

### Built-in Environment Variables

Jenkins provides these automatically in every build:

| Variable | Value |
|---|---|
| `BUILD_NUMBER` | Auto-incrementing build number (1, 2, 3...) |
| `BUILD_URL` | URL to this build's console output |
| `GIT_COMMIT` | Full SHA of the commit being built |
| `GIT_BRANCH` | Branch name |
| `WORKSPACE` | Path to the build workspace on disk |
| `JOB_NAME` | Name of the Jenkins job |

### Three Questions to Ask Trainees

**1. "Why use `BUILD_NUMBER` as the image tag instead of `latest` only?"**
→ `latest` is overwritten every build — you can't roll back to a specific version. `BUILD_NUMBER` gives every image a unique, traceable tag. `docker pull yourname/hello-express:42` always gives you exactly what build 42 produced.

**2. "What happens if `Run Tests` fails?"**
→ Jenkins marks the stage as failed, stops the pipeline immediately, and never reaches the Build or Push stages. A broken test prevents a broken image from reaching Docker Hub.

**3. "Where does `${DOCKERHUB_CREDENTIALS_PSW}` come from?"**
→ Jenkins injects it from the credential store. It never appears in the Jenkinsfile or in build logs — Jenkins masks it. This is how you keep secrets out of source code.

---

## Step 4 — Connecting GitHub: Webhooks and Automatic Triggers

Right now you click **Build Now** manually. The goal is: push to GitHub → pipeline runs automatically.

### Option A — GitHub Webhook (recommended)

A webhook tells GitHub: "When someone pushes, call this URL."

**In Jenkins:**

1. Open your pipeline job → **Configure**
2. Under **Build Triggers**, check **GitHub hook trigger for GITScm polling**
3. Save

**In GitHub:**

1. Your repository → **Settings → Webhooks → Add webhook**
2. Payload URL: `http://YOUR_JENKINS_IP:8080/github-webhook/`
3. Content type: `application/json`
4. Trigger: **Just the push event**
5. Add webhook

Now every `git push` to the repository triggers the Jenkins pipeline automatically.

> **Lab note:** For local Jenkins, use [ngrok](https://ngrok.com) to expose `localhost:8080` to a public URL that GitHub can reach: `ngrok http 8080`

### Option B — SCM Polling (no public IP needed)

Jenkins polls GitHub every N minutes and triggers a build if new commits are detected.

```groovy
triggers {
    pollSCM('H/5 * * * *')    // Check every 5 minutes
}
```

Add this inside the `pipeline` block. Less instant than a webhook but works without a public IP.

### Pipeline from SCM (the proper way)

Instead of pasting the Jenkinsfile into the UI, point Jenkins at your repository:

1. Job → **Configure → Pipeline**
2. Definition: **Pipeline script from SCM**
3. SCM: **Git**
4. Repository URL: `https://github.com/yourname/hello-express`
5. Branch: `*/main`
6. Script Path: `Jenkinsfile`
7. Save

Now Jenkins reads the Jenkinsfile from GitHub on every build. Change the pipeline by editing the file and pushing — no Jenkins UI change needed.

---

## Step 5 — Full CI/CD Pipeline: Test → Build → Push → Deploy

Add the deployment stage. After the image is pushed to Docker Hub, Ansible deploys it to the production server.

### Updated `Jenkinsfile`

```groovy
pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        IMAGE_NAME = "yourname/hello-express"
        IMAGE_TAG  = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Install Dependencies') {
            steps {
                sh 'npm install'
            }
        }

        stage('Run Tests') {
            steps {
                sh 'npm test'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                sh "docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest"
            }
        }

        stage('Push to Docker Hub') {
            steps {
                sh "echo ${DOCKERHUB_CREDENTIALS_PSW} | docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin"
                sh "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
                sh "docker push ${IMAGE_NAME}:latest"
            }
        }

        stage('Deploy with Ansible') {
            steps {
                sh """
                    ansible-playbook -i ansible/inventory.ini \
                        ansible/deploy-update.yaml \
                        -e IMAGE_TAG=${IMAGE_TAG}
                """
            }
        }

        stage('Cleanup') {
            steps {
                sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true"
            }
        }

    }

    post {
        success {
            echo "Build ${BUILD_NUMBER} deployed successfully."
        }
        failure {
            echo "Build ${BUILD_NUMBER} failed. Check the console output."
        }
    }
}
```

### The complete flow, end to end

```
git push origin main
        ↓
  GitHub Webhook fires
        ↓
  Jenkins Pipeline starts
        ↓
  ┌─────────────────────────────────────────┐
  │  Stage 1: Checkout                      │  ← git clone
  │  Stage 2: Install Dependencies          │  ← npm install
  │  Stage 3: Run Tests                     │  ← npm test ✅ / ❌ stop
  │  Stage 4: Build Docker Image            │  ← docker build :42
  │  Stage 5: Push to Docker Hub            │  ← docker push :42
  │  Stage 6: Deploy with Ansible           │  ← ansible-playbook -e IMAGE_TAG=42
  │  Stage 7: Cleanup                       │  ← docker rmi local image
  └─────────────────────────────────────────┘
        ↓
  Production server pulls :42, restarts container
        ↓
  App is live — traceable to commit SHA
```

### Blue Ocean (visual pipeline view)

Jenkins has a modern UI plugin called **Blue Ocean** that visualises pipelines as a flowchart.

**Manage Jenkins → Plugins → Available → Blue Ocean → Install**

After install: `http://localhost:8080/blue` — pipelines display as a horizontal flow with pass/fail per stage, making it easy to see exactly where a build failed.

---

## Step 6 — Parallel Stages and Conditional Logic

Real pipelines need branching logic — run some stages in parallel, skip deployment on feature branches.

### Parallel stages

Run unit tests and linting simultaneously to save time:

```groovy
stage('Validate') {
    parallel {
        stage('Unit Tests') {
            steps {
                sh 'npm test'
            }
        }
        stage('Lint') {
            steps {
                sh 'npm run lint || true'
            }
        }
    }
}
```

Both stages run at the same time. The pipeline moves to the next stage only when both complete.

### Conditional deployment (only deploy from `main`)

```groovy
stage('Deploy with Ansible') {
    when {
        branch 'main'
    }
    steps {
        sh "ansible-playbook -i ansible/inventory.ini ansible/deploy-update.yaml -e IMAGE_TAG=${IMAGE_TAG}"
    }
}
```

Feature branch pushes run tests and build the image — but never deploy. Only `main` deploys to production. This is the standard **branch strategy** for CI/CD.

### Common `when` conditions

```groovy
when { branch 'main' }                    // Only on main branch
when { not { branch 'main' } }            // Any branch except main
when { changeset '**/Dockerfile' }        // Only if Dockerfile changed
when { environment name: 'ENV', value: 'prod' }   // Only in prod environment
when { expression { return params.DEPLOY == 'true' } }  // Based on a parameter
```

### Parameterised Builds

Add runtime parameters — useful for manual re-deploys of a specific version:

```groovy
pipeline {
    agent any

    parameters {
        string(name: 'IMAGE_TAG', defaultValue: 'latest', description: 'Image tag to deploy')
        booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Skip test stage')
        choice(name: 'ENVIRONMENT', choices: ['staging', 'production'], description: 'Target environment')
    }

    stages {
        stage('Run Tests') {
            when {
                expression { return !params.SKIP_TESTS }
            }
            steps {
                sh 'npm test'
            }
        }

        stage('Deploy') {
            steps {
                echo "Deploying tag ${params.IMAGE_TAG} to ${params.ENVIRONMENT}"
                sh "ansible-playbook -i ansible/inventory.ini ansible/deploy-update.yaml -e IMAGE_TAG=${params.IMAGE_TAG}"
            }
        }
    }
}
```

When you click **Build with Parameters**, Jenkins shows a form before starting — choose the environment, toggle test skipping, specify the tag.

---

## Jenkins — Key Concepts Summary

### The Big Picture

Jenkins solves one problem: **automating the software delivery process.** It watches repositories, runs pipelines on every change, and ensures that code is tested, built, and deployed in a consistent, repeatable way — removing humans from the manual steps that introduce errors.

---

### Core Concepts

**Pipeline**
The full automated workflow from code commit to deployment. Defined in a `Jenkinsfile` stored in the repository. The pipeline is code — versioned, reviewed, and auditable.

**Jenkinsfile**
A Groovy DSL file that defines the pipeline. Two syntaxes exist: Declarative (structured, recommended) and Scripted (more flexible, older). This guide uses Declarative throughout.

**Stage**
A named phase of the pipeline — Checkout, Test, Build, Push, Deploy. Stages appear as boxes in the Jenkins UI and Blue Ocean. A failed stage stops the pipeline.

**Step**
A single command inside a stage. `sh` runs shell commands. `echo` prints messages. `checkout scm` clones the repository.

**Agent**
Where the pipeline runs. `agent any` uses any available agent. You can target specific agents by label, or run each stage in a different Docker container.

**Controller**
The Jenkins server — manages jobs, UI, plugins, scheduling, and credential storage. Does not run build workloads directly in production setups.

**Agent / Node**
A machine that executes pipeline stages on behalf of the controller. Can be another VM, a container, or a cloud instance.

**Credential Store**
Jenkins' secure vault for secrets — Docker Hub passwords, SSH keys, API tokens. Credentials are referenced by ID and never exposed in logs or Jenkinsfiles.

**Build Number**
An auto-incrementing integer for each run of a job. Used as the Docker image tag so every build is uniquely traceable.

**Webhook**
An HTTP callback registered in GitHub (or GitLab, Bitbucket). When code is pushed, GitHub calls Jenkins' webhook URL — triggering the pipeline instantly.

**Blue Ocean**
A modern Jenkins UI plugin. Visualises pipelines as a horizontal flowchart with per-stage pass/fail indicators.

**Shared Library**
Reusable Groovy functions stored in a separate Git repository — called from any Jenkinsfile. The equivalent of Ansible Roles for pipelines.

---

### Commands at a Glance

| What you want to do | How |
|---|---|
| Start Jenkins in Docker | `docker run -d -p 8080:8080 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts` |
| Get initial admin password | `docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword` |
| Trigger a build manually | Jenkins UI → Job → Build Now |
| See console output | Jenkins UI → Job → Build #N → Console Output |
| Trigger via API | `curl -X POST http://localhost:8080/job/JOBNAME/build --user user:token` |
| Restart Jenkins | `http://localhost:8080/restart` |
| Reload config | `http://localhost:8080/reload` |

---

### One-Line Distinctions (commonly confused)

| These seem similar... | But... |
|---|---|
| CI vs CD | CI = Continuous Integration (test and build on every push). CD = Continuous Delivery/Deployment (automatically deliver to staging or production). CI always comes first. |
| Declarative vs Scripted pipeline | Declarative is structured, validated, easier to read — use this. Scripted is full Groovy — more powerful but harder to maintain. |
| `stage` vs `step` | Stage is a named phase visible in the UI. Step is an individual command inside a stage. |
| Webhook vs SCM polling | Webhook = GitHub pushes to Jenkins (instant, requires public IP). Polling = Jenkins checks GitHub every N minutes (slight delay, no public IP needed). |
| `agent any` vs `agent none` | `agent any` assigns a global agent for the whole pipeline. `agent none` means each stage declares its own agent — useful for multi-platform builds. |
| `environment {}` vs `parameters {}` | `environment` sets fixed variables baked into the pipeline. `parameters` creates runtime inputs — the user fills them in before a build starts. |
| Credentials in `environment` vs `withCredentials` | `environment { X = credentials('id') }` makes creds available as env vars. `withCredentials([...]) { }` scopes them to a single step block — slightly more restrictive. |

---

### How Everything Connects

```
Developer
    ↓ git push
  GitHub
    ↓ webhook
  Jenkins
    ↓
  Jenkinsfile (from repo)
    ↓
  ┌──────────────────────────────────────────────────────┐
  │ stage: Checkout   → git clone                        │
  │ stage: Test       → npm test                         │
  │ stage: Build      → docker build → image:42          │
  │ stage: Push       → docker push  → Docker Hub        │
  │ stage: Deploy     → ansible-playbook -e IMAGE_TAG=42 │
  └──────────────────────────────────────────────────────┘
                              ↓
                    Production server
                    pulls image:42
                    restarts container
                              ↓
                    App live — tested, versioned, traceable
```

---

## Jenkins Commands & URL Reference

```bash
# Start Jenkins
docker run -d --name jenkins \
  -p 8080:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts

# Get initial password
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# Restart Jenkins container
docker restart jenkins

# View Jenkins logs
docker logs -f jenkins

# Trigger a build via REST API
curl -X POST http://localhost:8080/job/JOBNAME/build \
  --user USERNAME:API_TOKEN

# Trigger a parameterised build via REST API
curl -X POST "http://localhost:8080/job/JOBNAME/buildWithParameters?IMAGE_TAG=42" \
  --user USERNAME:API_TOKEN
```

**Useful Jenkins URLs:**

| URL | Purpose |
|---|---|
| `http://localhost:8080` | Jenkins dashboard |
| `http://localhost:8080/blue` | Blue Ocean visual pipeline UI |
| `http://localhost:8080/manage` | Manage Jenkins (plugins, credentials, nodes) |
| `http://localhost:8080/credentials` | Credential store |
| `http://localhost:8080/restart` | Restart Jenkins |
| `http://localhost:8080/github-webhook/` | Webhook endpoint for GitHub |

---

## ToC Coverage Map

| Jenkins / CI-CD Topic | Covered in |
|---|---|
| CI/CD Concepts | Bridge section — CI vs CD, why automation, what Jenkins replaces |
| Jenkins Architecture | Step 1 — Controller, Agent, Docker socket, plugin install |
| Jenkinsfile & Declarative Pipeline | Step 2 — pipeline, agent, stages, steps, post blocks |
| Pipeline from SCM | Step 4 — Pipeline script from SCM, branch specifier |
| CI Pipeline (Test → Build → Push) | Step 3 — full Jenkinsfile with npm test, docker build, docker push |
| GitHub Webhooks & Triggers | Step 4 — webhook setup, SCM polling, `pollSCM` cron syntax |
| Full CI/CD with Ansible Deploy | Step 5 — Deploy stage calling ansible-playbook, end-to-end flow diagram |
| Parallel Stages | Step 6 — `parallel {}` block, simultaneous test and lint |
| Conditional Logic & Branch Strategy | Step 6 — `when { branch 'main' }`, feature branch vs main |
| Parameterised Builds | Step 6 — `parameters {}`, string / boolean / choice types |
| Credentials Management | Step 3 — credential store, `credentials()`, masked secrets |
| Blue Ocean | Step 5 — install, visual pipeline view |
| CI/CD Pipeline using Git, Jenkins and Maven | Covered structurally — swap `npm` for `mvn` in the same pipeline pattern |
| Integrating Docker in CI/CD | Step 3 & 5 — docker build/push stages, Docker Hub credentials |
| Integrating Ansible in CI/CD | Step 5 — Deploy stage, IMAGE_TAG variable injection |
| Integrating Kubernetes in CI/CD | Natural extension — replace Ansible deploy stage with `kubectl set image` |

> **Next:** Git & GitHub — the foundation everything else depends on: branching strategies, pull requests, and how source control integrates with every stage of the pipeline you just built.
