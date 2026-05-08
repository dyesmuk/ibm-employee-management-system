# Jenkins — Hands-On Guide (CI/CD Pipelines)

> **Prerequisites:** Docker, Kubernetes, Ansible modules completed. Your `hello-express` image is on Docker Hub.
>
> **Environment:** Windows 11, Docker Desktop. All `docker` commands in **PowerShell**. Pipeline `sh` steps run inside the Jenkins Linux container — Linux commands work regardless of Windows host.

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

### Plugins to install

**Manage Jenkins → Plugins → Available:**

| Plugin | Required for |
|---|---|
| Pipeline | Declarative pipelines |
| Git | Cloning repos |
| Docker Pipeline | `docker.build`, `docker.push` in pipelines |
| Maven Integration | `mvn` builds |
| Deploy to container | WAR → Tomcat |
| Kubernetes | `kubectl` in pipelines |
| Blue Ocean | Visual pipeline view |

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

> `sh` in pipeline steps runs inside the Jenkins Linux container — not on Windows. `mvn`, `docker`, `kubectl`, bash all work normally.

---

## Step 2 — Declarative Pipeline Fundamentals

```groovy
pipeline {
    agent any

    tools {
        maven 'maven-3.9'
        jdk   'jdk-17'
    }

    environment {
        APP_NAME = 'hello-jenkins'
    }

    stages {
        stage('Hello') {
            steps {
                echo "Building ${APP_NAME}"
                sh 'date && whoami'
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

| Block | Purpose |
|---|---|
| `pipeline` | Root wrapper |
| `agent` | Where to run — `any`, label, or Docker image |
| `tools` | Pre-configured JDK/Maven/Node from Global Tool Config |
| `environment` | Key-value env vars for all stages |
| `stages` | Ordered list of stages |
| `stage` | Named phase — shown as a box in Blue Ocean |
| `steps` | Commands inside a stage |
| `post` | After all stages — always / success / failure |

---

## Step 3 — CI Pipeline: Git + Jenkins + Maven

Core syllabus pipeline: a **Spring Boot Java app** built with Maven, triggered by GitHub push.

### Configure JDK and Maven in Jenkins

**Manage Jenkins → Tools:**
1. **JDK** → Add → Name: `jdk-17` → Install automatically → OpenJDK 17
2. **Maven** → Add → Name: `maven-3.9` → Install automatically → 3.9.x
3. Save

### Minimal Spring Boot project

`pom.xml`:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example</groupId>
  <artifactId>hello-jenkins</artifactId>
  <version>1.0.${BUILD_NUMBER}</version>
  <packaging>jar</packaging>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
  </parent>

  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>
</project>
```

`src/main/java/com/example/HelloController.java`:

```java
@RestController
public class HelloController {
    @GetMapping("/")
    public String hello() {
        return "Hello from Jenkins CI/CD!";
    }
}
```

### `Jenkinsfile` — Git + Jenkins + Maven

```groovy
pipeline {
    agent any

    tools {
        maven 'maven-3.9'
        jdk   'jdk-17'
    }

    environment {
        APP_NAME = 'hello-jenkins'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch: ${GIT_BRANCH} | Commit: ${GIT_COMMIT[0..7]}"
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Archive Artifact') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

    }

    post {
        success { echo "Build ${BUILD_NUMBER} succeeded." }
        failure { echo "Build ${BUILD_NUMBER} failed."   }
    }
}
```

### Connect to GitHub — Pipeline from SCM

**Job → Configure → Pipeline → Pipeline script from SCM:**
- SCM: Git
- Repository URL: `https://github.com/yourname/hello-jenkins`
- Branch: `*/main`
- Script Path: `Jenkinsfile`

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

### Maven commands reference

| Command | What it does |
|---|---|
| `mvn clean` | Delete `target/` |
| `mvn compile` | Compile Java source |
| `mvn test` | Run unit tests |
| `mvn package` | Compile + test + JAR/WAR |
| `mvn clean package` | Clean then package |
| `mvn clean package -DskipTests` | Package without tests |
| `mvn install` | Package + install to local repo |
| `mvn verify` | Run integration tests |

---

## Step 4 — Integrating Tomcat in the CI/CD Pipeline

### What is Tomcat?

Apache Tomcat is a Java application server. It hosts WAR (Web Application Archive) files. The classic enterprise Java deployment model:

```
Maven builds → hello-jenkins.war
                    ↓
          Deploy to Tomcat
                    ↓
    http://server:8090/hello-jenkins/
```

### Change packaging to WAR

In `pom.xml`, change `<packaging>jar</packaging>` to `<packaging>war</packaging>` and add the Tomcat dependency:

```xml
<packaging>war</packaging>

<dependencies>
  ...
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

Make `HelloApplication` extend `SpringBootServletInitializer`:

```java
@SpringBootApplication
public class HelloApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(HelloApplication.class, args);
    }
}
```

### Run Tomcat in Docker

```powershell
docker run -d `
  --name tomcat `
  -p 8090:8080 `
  tomcat:10-jdk17
```

### Enable Tomcat Manager (required for remote deploy)

```powershell
docker exec -it tomcat bash
```

Inside the container:

```bash
# Create manager user
cat > /usr/local/tomcat/conf/tomcat-users.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<tomcat-users>
  <role rolename="manager-script"/>
  <user username="deployer" password="deployer123" roles="manager-script"/>
</tomcat-users>
EOF

# Allow remote access (Tomcat 10 restricts manager by default)
sed -i 's/allow="127/allow=".*/' \
  /usr/local/tomcat/webapps/manager/META-INF/context.xml

exit
```

```powershell
docker restart tomcat
```

Verify: `http://localhost:8090/manager/html` → login: `deployer` / `deployer123`

### Add Tomcat credentials to Jenkins

**Manage Jenkins → Credentials → Add:**
- Kind: Username with password
- Username: `deployer`
- Password: `deployer123`
- ID: `tomcat-creds`

### `Jenkinsfile` — with Tomcat deploy

```groovy
pipeline {
    agent any

    tools {
        maven 'maven-3.9'
        jdk   'jdk-17'
    }

    environment {
        APP_NAME     = 'hello-jenkins'
        TOMCAT_URL   = 'http://host.docker.internal:8090'
        TOMCAT_CREDS = credentials('tomcat-creds')
    }

    stages {

        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Build') {
            steps { sh 'mvn clean package -DskipTests' }
        }

        stage('Test') {
            steps { sh 'mvn test' }
            post {
                always { junit 'target/surefire-reports/*.xml' }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                deploy adapters: [
                    tomcat9(
                        credentialsId: 'tomcat-creds',
                        path: "/${APP_NAME}",
                        url: "${TOMCAT_URL}"
                    )
                ],
                contextPath: "/${APP_NAME}",
                war: "target/*.war"
            }
        }

    }

    post {
        success { echo "Deployed to ${TOMCAT_URL}/${APP_NAME}" }
        failure { echo "Build ${BUILD_NUMBER} failed." }
    }
}
```

> **`host.docker.internal`** resolves to your Windows machine's IP inside any Docker container. This is how the Jenkins container reaches the Tomcat container on Docker Desktop.

### Verify

Browser → `http://localhost:8090/hello-jenkins/` → **Hello from Jenkins CI/CD!**

### Three Questions to Ask Trainees

**1. "Why deploy a WAR to Tomcat instead of just `java -jar`?"**
→ Traditional enterprise Java — banking, government, telecom — uses WAR + application servers. Trainees will encounter Tomcat, JBoss, and WebLogic in real organisations. Spring Boot supports both models.

**2. "What does `host.docker.internal` resolve to?"**
→ On Docker Desktop (Windows/Mac) it resolves to the host machine's IP. It's how a container (Jenkins) reaches another service running on the same host (Tomcat container). On Linux Docker, use `172.17.0.1`.

**3. "What happens to live traffic when you deploy a new WAR to Tomcat?"**
→ Tomcat undeploys the old version and deploys the new one — there's a brief outage. This is why Docker rolling updates and Kubernetes are preferred for zero-downtime deployments.

---

## Step 5 — Integrating Docker in the CI/CD Pipeline

Package the Spring Boot app as a Docker image instead of deploying WAR to Tomcat.

### Dockerfile for Spring Boot

Switch `pom.xml` back to `<packaging>jar</packaging>`. Then create `Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### `Jenkinsfile` — Maven + Docker

```groovy
pipeline {
    agent any

    tools {
        maven 'maven-3.9'
        jdk   'jdk-17'
    }

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        IMAGE_NAME = "yourname/hello-jenkins"
        IMAGE_TAG  = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Build & Test') {
            steps { sh 'mvn clean package' }
            post {
                always { junit 'target/surefire-reports/*.xml' }
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
                sh "docker run -d --name smoke-test -p 8081:8080 ${IMAGE_NAME}:${IMAGE_TAG}"
                sh "sleep 8 && curl -f http://localhost:8081/ || (docker logs smoke-test && exit 1)"
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

### Add Docker Hub credentials

**Manage Jenkins → Credentials → Add:**
- Kind: Username with password
- Username: Docker Hub username
- Password: Docker Hub password or access token
- ID: `dockerhub-creds`

### The Docker CI/CD flow

```
git push → Jenkins
    ↓
mvn clean package     (build JAR + run unit tests)
    ↓
docker build :42      (package JAR into image)
    ↓
docker push :42       → Docker Hub
    ↓
Smoke test            (run container, curl endpoint, verify response)
    ↓
docker rmi            (clean local image from Jenkins agent)
```

---

## Step 6 — Integrating Kubernetes in the CI/CD Pipeline

After the Docker image is on Docker Hub, deploy it to Kubernetes with zero downtime.

### Configure kubectl inside Jenkins

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
  --port=80 --target-port=8080 `
  --name=hello-jenkins-svc
```

### `Jenkinsfile` — Full pipeline: Maven → Docker → Kubernetes

```groovy
pipeline {
    agent any

    tools {
        maven 'maven-3.9'
        jdk   'jdk-17'
    }

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        IMAGE_NAME      = "yourname/hello-jenkins"
        IMAGE_TAG       = "${BUILD_NUMBER}"
        DEPLOYMENT_NAME = "hello-jenkins"
        CONTAINER_NAME  = "hello-jenkins"
        K8S_NAMESPACE   = "default"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                echo "Building ${GIT_BRANCH} @ ${GIT_COMMIT[0..7]}"
            }
        }

        stage('Build & Test') {
            steps { sh 'mvn clean package' }
            post {
                always { junit 'target/surefire-reports/*.xml' }
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
│  Build & Test  → mvn clean package + junit results          │
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
→ Kubernetes replaces Pods one at a time (rolling update). Some run old, some new, all behind the Service. Zero downtime — unlike a Tomcat redeploy.

**3. "Why is `kubectl rollout status --timeout=120s` important?"**
→ Without it, the pipeline finishes immediately after issuing the update, before knowing if it worked. `rollout status` blocks until all Pods are healthy — or fails the pipeline if they aren't within 120 seconds, triggering the auto-rollback.

---

## Step 7 — Advanced Pipeline Patterns

### Parallel stages

```groovy
stage('Validate') {
    parallel {
        stage('Unit Tests')    { steps { sh 'mvn test'              } }
        stage('Code Style')    { steps { sh 'mvn checkstyle:check'  } }
        stage('Dependency')    { steps { sh 'mvn dependency:resolve'} }
    }
}
```

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

**tools** — Pre-installed runtimes (JDK, Maven) from Global Tool Config. Available in pipelines by name.

**Credential Store** — Secure vault. Referenced by ID. Never shown in logs.

**Build Number** — Auto-incrementing per run. Used as Docker image tag for traceability.

**Webhook** — GitHub calls Jenkins on push. Instant trigger.

**`host.docker.internal`** — Docker Desktop hostname → Windows host IP. How Jenkins container reaches Tomcat container.

**Rolling update** — Kubernetes replaces Pods one at a time. Zero downtime.

**Auto-rollback** — `post { failure { kubectl rollout undo } }` — reverts K8s deployment if pipeline fails.

---

### One-Line Distinctions (commonly confused)

| These seem similar... | But... |
|---|---|
| CI vs CD | CI = test + build on push. CD = deliver to environment automatically. CI is the prerequisite for CD. |
| Declarative vs Scripted pipeline | Declarative = structured, validated. Scripted = full Groovy. Always use Declarative. |
| `stage` vs `step` | Stage = named UI phase. Step = single command inside a stage. |
| WAR vs JAR (Spring Boot) | WAR deploys to Tomcat (traditional). JAR = self-contained, runs with `java -jar` (Docker-friendly). |
| Tomcat deploy vs Docker | Tomcat = WAR file, brief downtime on redeploy. Docker = image, rolling updates, no downtime. |
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
  │  Build & Test  → mvn clean package + junit               │
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
| `http://localhost:8080/manage/configureTools/` | JDK + Maven tool config |
| `http://localhost:8080/restart` | Restart Jenkins |
| `http://localhost:8080/github-webhook/` | GitHub webhook endpoint |

---

## ToC Coverage Map

| CI/CD Syllabus Topic | Covered in |
|---|---|
| Introduction — CI vs CD | Bridge section + Step 1 architecture |
| Jenkins architecture, plugins | Step 1 — Controller, Docker socket, plugin table |
| Declarative pipeline fundamentals | Step 2 — pipeline, agent, tools, stages, steps, post |
| CI/CD pipeline using Git, Jenkins and Maven | Step 3 — full Jenkinsfile, JDK/Maven config, JUnit, `archiveArtifacts`, webhook/ngrok |
| Maven commands | Step 3 — `mvn` commands reference table |
| Integrating Tomcat in CI/CD pipeline | Step 4 — Tomcat in Docker, Manager setup, `host.docker.internal`, Deploy to container plugin |
| Integrating Docker in CI/CD pipeline | Step 5 — Dockerfile for Spring Boot, docker build/push, smoke test stage |
| Integrating Kubernetes in CI/CD pipeline | Step 6 — kubeconfig mount, `kubectl set image`, rollout status, auto-rollback |
| Parallel stages | Step 7 — `parallel {}` |
| Conditional logic / branch strategy | Steps 6–7 — `when { branch 'main' }` |
| Parameterised builds | Step 7 — `parameters {}` |
| Credentials management | Step 5 — dockerhub-creds, masked secrets |
| Blue Ocean | Step 7 — install + visual view |
| GitHub webhook + ngrok | Step 3 — Windows 11 webhook setup |
| Auto-rollback | Step 6 — `post { failure { kubectl rollout undo } }` |

> **Putting it all together:** Git branching strategy feeds GitHub webhooks → Jenkins triggers Maven build → Docker image pushed to Docker Hub → Kubernetes rolls out zero-downtime update → Ansible provisions the servers the cluster runs on.
