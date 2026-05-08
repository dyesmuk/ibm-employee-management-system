# Ansible — Hands-On Guide (DevOps Automation)

> **Prerequisites:** You have completed the Docker and Kubernetes modules. You are comfortable with YAML. A Linux environment is available — either WSL2 on Windows 11, or the lab VMs provided.

---

## The Bridge from Kubernetes to Ansible

In Kubernetes you declared *what* containers to run and it managed them. But someone still had to install Docker, install Kubernetes, configure the OS, set up users, open firewall ports, and repeat that across every server in the fleet.

Ansible automates that layer — everything *below* your application.

| What you've done so far | What Ansible handles |
|---|---|
| Wrote a `Dockerfile` — packaged the app | Installs Docker on every server |
| Wrote a `deployment.yaml` — ran the app in K8s | Installs Kubernetes on every node |
| Pushed to Docker Hub | Configures the server that hosts the registry |
| Set up MongoDB | Installs, configures, and starts the database service |
| Done all of this manually, one server at a time | Ansible does it on **100 servers simultaneously** |

The mental model shift:

```
Manual ops:    SSH into server 1 → run commands → SSH into server 2 → repeat
               (error-prone, slow, not reproducible, not trackable)

Ansible:       Write a Playbook once.
               Run it.
               Ansible SSHes into all servers in parallel and makes them
               match your specification — automatically.
```

Ansible is **agentless** — unlike other tools, nothing is installed on the target servers. Ansible uses plain SSH, which every Linux server already has. This is its biggest practical advantage.

---

## Step 1 — Installing Ansible and Understanding the Architecture

### Install Ansible (on your control machine — WSL2 or Linux VM)

```bash
sudo apt update
sudo apt install ansible -y

# Verify
ansible --version
```

Expected output:
```
ansible [core 2.16.x]
  python version = 3.12.x
  ...
```

### The Three-Part Architecture

```
Control Node                    Managed Nodes
(your machine —                 (target servers —
 Ansible installed here)         nothing installed)

   ansible / ansible-playbook
           |
           | SSH (port 22)
           |
     ┌─────┴─────┐
     ↓           ↓
  Server 1    Server 2    Server 3 ...
```

| Component | What it is |
|---|---|
| **Control Node** | The machine where Ansible is installed and commands are run from |
| **Managed Node** | Any server Ansible manages. Only needs Python and SSH. |
| **Inventory** | A list of managed nodes — the "who to run against" |
| **Playbook** | A YAML file describing automation tasks — the "what to do" |
| **Task** | A single unit of work (install a package, copy a file, start a service) |
| **Module** | The built-in tool that executes a task (`apt`, `copy`, `service`, `docker_container`, etc.) |
| **Role** | A reusable, structured bundle of tasks for a specific purpose (e.g. "install Java") |

### Set up a local lab with Docker (two managed nodes as containers)

For the hands-on lab on your Windows 11 laptop, use Docker containers as fake servers — they behave exactly like real Linux servers for Ansible's purposes.

**`docker-compose.yml` for the lab:**

```yaml
version: '3'

services:
  node1:
    image: ubuntu:22.04
    container_name: ansible-node1
    command: /bin/bash -c "apt-get update && apt-get install -y openssh-server python3 && mkdir /run/sshd && echo 'root:password' | chpasswd && sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config && /usr/sbin/sshd -D"
    ports:
      - "2221:22"

  node2:
    image: ubuntu:22.04
    container_name: ansible-node2
    command: /bin/bash -c "apt-get update && apt-get install -y openssh-server python3 && mkdir /run/sshd && echo 'root:password' | chpasswd && sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config && /usr/sbin/sshd -D"
    ports:
      - "2222:22"
```

```bash
docker compose up -d
```

You now have two "servers" to manage.

---

## Step 2 — Inventory: Telling Ansible What to Manage

The **inventory** is a file listing the servers Ansible will work on. It is the first thing Ansible reads before doing anything.

### Create `inventory.ini`

```ini
[webservers]
node1 ansible_host=127.0.0.1 ansible_port=2221 ansible_user=root ansible_password=password

[dbservers]
node2 ansible_host=127.0.0.1 ansible_port=2222 ansible_user=root ansible_password=password

[all:vars]
ansible_python_interpreter=/usr/bin/python3
```

| Field | Meaning |
|---|---|
| `[webservers]` | A group name. You can run playbooks against groups, not just individual hosts. |
| `ansible_host` | The actual IP or hostname to connect to |
| `ansible_port` | SSH port |
| `ansible_user` | SSH user |
| `ansible_password` | SSH password (use SSH keys in production) |
| `[all:vars]` | Variables that apply to every host |

### Test connectivity with an ad-hoc command

An **ad-hoc command** runs a single task instantly — no playbook needed. Good for quick checks.

```bash
# Ping all hosts (not ICMP ping — Ansible's connectivity check)
ansible all -i inventory.ini -m ping
```

Expected output:
```
node1 | SUCCESS => {
    "changed": false,
    "ping": "pong"
}
node2 | SUCCESS => {
    "changed": false,
    "ping": "pong"
}
```

`SUCCESS` means Ansible can reach both nodes over SSH and Python is available.

```bash
# Run a shell command on all servers at once
ansible all -i inventory.ini -m shell -a "uname -a"

# Run only on the webservers group
ansible webservers -i inventory.ini -m shell -a "whoami"

# Check free disk space on dbservers
ansible dbservers -i inventory.ini -m shell -a "df -h"
```

### Three Questions to Ask Trainees

**1. "If you want to manage 50 web servers and 10 database servers, how does grouping help?"**
→ Run one command against `[webservers]` or `[dbservers]` without listing every host individually. Groups let you target a class of servers.

**2. "The `-m ping` module — is this the same as the `ping` command in your terminal?"**
→ No. Ansible's `ping` module SSHes into the host, runs a small Python check, and returns `pong`. It confirms that Ansible can connect and that Python works. Network ICMP ping is a different thing.

**3. "Why is Ansible agentless? What's the practical benefit?"**
→ Nothing needs to be pre-installed on managed nodes. Standard SSH + Python (which every Linux server has) is enough. This means you can start managing an existing server immediately without any setup on it.

---

## Step 3 — Your First Playbook

An ad-hoc command runs one task. A **Playbook** runs many tasks in sequence — and it's repeatable, version-controlled, and readable.

### Create `install-node.yaml`

This playbook installs Node.js on the web servers.

```yaml
---
- name: Install Node.js on web servers
  hosts: webservers
  become: yes

  tasks:
    - name: Update apt cache
      apt:
        update_cache: yes

    - name: Install Node.js
      apt:
        name: nodejs
        state: present

    - name: Install npm
      apt:
        name: npm
        state: present

    - name: Verify Node.js installation
      command: node --version
      register: node_version

    - name: Print Node.js version
      debug:
        msg: "Node.js version installed: {{ node_version.stdout }}"
```

### Run it

```bash
ansible-playbook -i inventory.ini install-node.yaml
```

Expected output:
```
PLAY [Install Node.js on web servers] ******************************

TASK [Gathering Facts] *********************************************
ok: [node1]

TASK [Update apt cache] ********************************************
changed: [node1]

TASK [Install Node.js] *********************************************
changed: [node1]

TASK [Install npm] *************************************************
changed: [node1]

TASK [Verify Node.js installation] *********************************
changed: [node1]

TASK [Print Node.js version] ***************************************
ok: [node1] => {
    "msg": "Node.js version installed: v18.x.x"
}

PLAY RECAP ************************************
node1 : ok=6  changed=4  unreachable=0  failed=0
```

### Understanding the output: `ok` vs `changed`

| Status | Meaning |
|---|---|
| `ok` | Task ran — no change was needed (already in desired state) |
| `changed` | Task ran — something was actually modified on the server |
| `failed` | Task ran — it failed |
| `unreachable` | Ansible couldn't SSH in |

Run the playbook a **second time** without modifying anything:

```bash
ansible-playbook -i inventory.ini install-node.yaml
```

```
TASK [Install Node.js] ********************************************
ok: [node1]   ← not "changed" this time
```

Node.js is already installed — Ansible detects this and skips the actual work. This is **idempotency**: running the same playbook 10 times produces the same result as running it once. No side effects, no duplicates.

### Anatomy of a Playbook

```yaml
---                          # YAML document start marker
- name: Human-readable title  # The "play" — targets a group of hosts
  hosts: webservers           # Which group from inventory to run against
  become: yes                 # Use sudo (privilege escalation)

  tasks:                      # List of tasks to run in order
    - name: Task description  # Human-readable label (shows in output)
      apt:                    # The MODULE to use
        name: nodejs          # Module ARGUMENTS — what it should do
        state: present        # "present" = install, "absent" = uninstall
```

### Three Questions to Ask Trainees

**1. "Run the playbook twice. What's different the second time?"**
→ All tasks show `ok` instead of `changed`. Nothing was modified because the system was already in the desired state. This is idempotency — the core reliability guarantee of Ansible.

**2. "What does `become: yes` do? When would you leave it out?"**
→ It uses `sudo` to escalate privileges. You need it for system-level tasks (installing packages, modifying system files). You'd leave it out for tasks a regular user can do (creating files in their own home directory).

**3. "What's the difference between `state: present` and `state: latest`?"**
→ `present` installs the package if missing, but doesn't upgrade it if a newer version exists. `latest` always upgrades to the newest version. Use `present` for reproducibility; use `latest` cautiously in production.

---

## Step 4 — Variables, Templates, and Handlers

Real playbooks are parameterized — the same playbook deploys different configurations to different environments.

### Variables

```yaml
---
- name: Deploy Express app
  hosts: webservers
  become: yes
  vars:
    app_port: 3000
    app_name: hello-express
    node_env: production

  tasks:
    - name: Print deployment info
      debug:
        msg: "Deploying {{ app_name }} on port {{ app_port }} in {{ node_env }} mode"
```

`{{ variable_name }}` is Ansible's template syntax — Jinja2. Variables can be defined in:

| Location | Use case |
|---|---|
| `vars:` in the play | Quick inline variables |
| `vars_files:` | Separate YAML file for many variables |
| `group_vars/webservers.yaml` | Variables for a whole group — auto-loaded |
| `host_vars/node1.yaml` | Variables for one specific host — auto-loaded |
| Command line: `-e "app_port=8080"` | Override at runtime |

### Handlers

A **handler** is a task that runs only when notified — and only once, at the end of the play, even if notified multiple times.

The most common use: restart a service only when its config file actually changed.

```yaml
---
- name: Configure and start Nginx
  hosts: webservers
  become: yes

  tasks:
    - name: Install Nginx
      apt:
        name: nginx
        state: present

    - name: Copy Nginx config
      copy:
        src: nginx.conf
        dest: /etc/nginx/nginx.conf
      notify: Restart Nginx          # ← triggers the handler IF this task changed

  handlers:
    - name: Restart Nginx
      service:
        name: nginx
        state: restarted
```

If `nginx.conf` hasn't changed, the `copy` task is `ok` — the handler is never triggered. If you push a new config, `copy` is `changed` — and Nginx restarts once at the end of the play.

Without handlers you'd restart Nginx unconditionally every run. With handlers, restarts only happen when needed.

### Templates (Jinja2)

A **template** is a config file with variables in it. Ansible fills in the values and copies it to the server.

**`templates/app.env.j2`** (note the `.j2` extension):

```
NODE_ENV={{ node_env }}
PORT={{ app_port }}
APP_NAME={{ app_name }}
DB_HOST={{ db_host }}
```

**In the playbook:**

```yaml
    - name: Generate app environment file
      template:
        src: templates/app.env.j2
        dest: /opt/hello-express/.env
      notify: Restart app
```

Ansible fills in the variables and writes the final file to the server. Same template, different values per environment — one playbook deploys correctly to dev, staging, and production.

---

## Step 5 — Deploying Docker with Ansible

Now connect what you know. Use Ansible to install Docker on the managed nodes and run your Express container — automating what you did manually in the Docker module.

### Folder structure

```
ansible-docker/
  ├── inventory.ini
  ├── deploy-docker.yaml
  └── templates/
        └── docker-compose.j2
```

### `deploy-docker.yaml`

```yaml
---
- name: Install Docker and deploy Express app
  hosts: webservers
  become: yes
  vars:
    app_image: yourname/hello-express:1.0
    app_port: 3000

  tasks:
    - name: Install prerequisite packages
      apt:
        name:
          - apt-transport-https
          - ca-certificates
          - curl
          - software-properties-common
        state: present
        update_cache: yes

    - name: Add Docker GPG key
      apt_key:
        url: https://download.docker.com/linux/ubuntu/gpg
        state: present

    - name: Add Docker repository
      apt_repository:
        repo: "deb [arch=amd64] https://download.docker.com/linux/ubuntu {{ ansible_lsb.codename }} stable"
        state: present

    - name: Install Docker CE
      apt:
        name: docker-ce
        state: present

    - name: Start and enable Docker service
      service:
        name: docker
        state: started
        enabled: yes

    - name: Pull the app image from Docker Hub
      community.docker.docker_image:
        name: "{{ app_image }}"
        source: pull

    - name: Run the Express container
      community.docker.docker_container:
        name: hello-express
        image: "{{ app_image }}"
        state: started
        restart_policy: always
        ports:
          - "{{ app_port }}:3000"
```

### Run it

```bash
ansible-playbook -i inventory.ini deploy-docker.yaml
```

After it completes, the Express app is running on both web servers — pulled from Docker Hub, started automatically, configured to restart on reboot — without ever manually SSHing in.

### The Key Insight

> "This playbook is the bridge between your two previous modules. The Docker module taught you to build and push images. The Kubernetes module taught you to declare how they should run. Ansible automates the step before all of that — provisioning the machine itself. In a real CI/CD pipeline: Ansible sets up the server → Docker delivers the app → Kubernetes keeps it running."

---

## Step 6 — Roles: Reusable Automation

A playbook with 50 tasks becomes hard to read and impossible to share. **Roles** are Ansible's way of packaging related tasks into a reusable, structured module.

### Role Directory Structure

```
roles/
  install-docker/
    ├── tasks/
    │     └── main.yaml       ← tasks go here
    ├── handlers/
    │     └── main.yaml       ← handlers go here
    ├── templates/
    │     └── daemon.json.j2  ← templates go here
    ├── vars/
    │     └── main.yaml       ← role-specific variables
    └── defaults/
          └── main.yaml       ← default values (overridable)
```

### Create a role with `ansible-galaxy`

```bash
ansible-galaxy init roles/install-docker
ansible-galaxy init roles/deploy-app
```

This generates the full directory skeleton automatically.

### `roles/install-docker/tasks/main.yaml`

```yaml
---
- name: Install prerequisite packages
  apt:
    name:
      - apt-transport-https
      - ca-certificates
      - curl
    state: present
    update_cache: yes

- name: Add Docker GPG key
  apt_key:
    url: https://download.docker.com/linux/ubuntu/gpg
    state: present

- name: Install Docker CE
  apt:
    name: docker-ce
    state: present

- name: Start Docker
  service:
    name: docker
    state: started
    enabled: yes
```

### Use the role in a playbook

```yaml
---
- name: Provision web servers
  hosts: webservers
  become: yes

  roles:
    - install-docker
    - deploy-app
```

Clean. The playbook reads like a list of intentions. The details live inside the roles. The `install-docker` role can be shared across every playbook in every project.

### Ansible Galaxy — Community Roles

`ansible-galaxy` is also a registry of pre-built community roles — like npm for Node.js.

```bash
# Search for a role
ansible-galaxy search docker

# Install a well-known Docker role
ansible-galaxy install geerlingguy.docker

# Use it in your playbook
roles:
  - geerlingguy.docker
```

`geerlingguy.docker` handles Docker installation on Ubuntu, CentOS, Debian, and more — tested, maintained, and used by thousands of teams. Don't write from scratch what the community has already built.

### Three Questions to Ask Trainees

**1. "How is a Role similar to a function in programming?"**
→ Both encapsulate a reusable piece of logic with a clear name. `install-docker` is called like a function — you don't repeat the 10 tasks every time you need Docker installed.

**2. "What's the difference between `vars/main.yaml` and `defaults/main.yaml` in a role?"**
→ `defaults` has the lowest priority — any playbook, group_var, or command-line variable overrides it. `vars` has higher priority. Use `defaults` for values you expect users to override; use `vars` for internal role constants.

**3. "When would you publish a role to Ansible Galaxy?"**
→ When it solves a common problem (install Nginx, configure Java, set up monitoring) and is generic enough for others to use. Your organization can also run a private Galaxy for internal reuse.

---

## Step 7 — CI/CD Integration: Ansible in the Pipeline

Ansible's real power is as the **provisioning and deployment stage** in a CI/CD pipeline. Here is how it fits with the tools coming in the next modules.

### The Full DevOps Pipeline

```
Developer pushes code
        ↓
     GitHub
        ↓
    Jenkins (CI)
     - Run tests
     - docker build
     - docker push → Docker Hub
        ↓
    Ansible (CD)
     - ansible-playbook deploy.yaml
     - SSH into production servers
     - docker pull (new image)
     - Restart containers
        ↓
  Production Servers
  (running updated app)
```

### `deploy-update.yaml` — the CD playbook

This is the playbook Jenkins calls after a successful build:

```yaml
---
- name: Deploy updated application
  hosts: webservers
  become: yes
  vars:
    image_tag: "{{ lookup('env', 'IMAGE_TAG') }}"   # passed in by Jenkins

  tasks:
    - name: Pull the new image
      community.docker.docker_image:
        name: "yourname/hello-express:{{ image_tag }}"
        source: pull
        force_source: yes

    - name: Stop the old container
      community.docker.docker_container:
        name: hello-express
        state: stopped
      ignore_errors: yes

    - name: Remove the old container
      community.docker.docker_container:
        name: hello-express
        state: absent
      ignore_errors: yes

    - name: Start the new container
      community.docker.docker_container:
        name: hello-express
        image: "yourname/hello-express:{{ image_tag }}"
        state: started
        restart_policy: always
        ports:
          - "3000:3000"
```

### Calling Ansible from Jenkins (Jenkinsfile snippet)

```groovy
stage('Deploy') {
    steps {
        sh """
          ansible-playbook -i inventory.ini deploy-update.yaml \
            -e IMAGE_TAG=${BUILD_NUMBER}
        """
    }
}
```

Jenkins passes the build number as the image tag. Ansible pulls that exact image version and deploys it to production. Every deployment is traceable to a specific build.

---

## Ansible — Key Concepts Summary

### The Big Picture

Ansible solves one problem: **automating repetitive operations on many servers.** It replaces manual SSH + bash scripting with readable, version-controlled, idempotent YAML playbooks. In a DevOps pipeline it occupies the provisioning and deployment layer — below your application but above raw infrastructure.

---

### Core Concepts

**Inventory**
The list of servers Ansible manages. Can be a static `.ini` file or dynamically generated (from AWS, Azure, GCP). Groups hosts so you can target a class of server with one command.

**Playbook**
A YAML file containing one or more plays. A play maps a group of hosts to a list of tasks. Playbooks are the main unit of Ansible automation.

**Play**
A block within a playbook. Specifies `hosts` (which servers), `become` (privilege escalation), `vars` (variables), and `tasks` (what to do).

**Task**
A single call to a module with arguments. The basic unit of work. Each task has a name (shown in output) and a module.

**Module**
A built-in or community-provided tool that does one specific job. Examples: `apt` (install packages), `copy` (copy files), `service` (manage services), `template` (render Jinja2 files), `docker_container` (manage containers). Ansible ships with 3,000+ modules.

**Handler**
A task that only runs when explicitly notified by another task — and only once per play, regardless of how many times it was notified. Used for service restarts.

**Variable / `{{ }}`**
Named values injected into tasks and templates using Jinja2 `{{ variable }}` syntax. Can come from `vars:`, `group_vars/`, `host_vars/`, command line, or registered task output.

**Register**
Captures a task's output into a variable for use in later tasks. Example: capture the output of `node --version` and print it with `debug`.

**Idempotency**
Running the same playbook multiple times produces the same result. Ansible modules check current state before acting — if the desired state already exists, the task is `ok` (no change). This makes Ansible safe to run repeatedly.

**Role**
A structured, reusable bundle of tasks, handlers, templates, and variables for a specific purpose. Roles are composable — a playbook applies a list of roles. Shared via Ansible Galaxy.

**Ansible Galaxy**
A public registry of community roles. `ansible-galaxy install <role>` downloads a pre-built role. The equivalent of npm for Ansible automation.

---

### Core Modules Reference

| Module | What it does | Example |
|---|---|---|
| `apt` | Manage packages on Debian/Ubuntu | `name: nodejs state: present` |
| `yum` / `dnf` | Manage packages on RHEL/CentOS | `name: java-17 state: present` |
| `copy` | Copy a file to the remote server | `src: app.conf dest: /etc/app.conf` |
| `template` | Render a Jinja2 template and copy | `src: app.env.j2 dest: /opt/app/.env` |
| `service` | Start, stop, restart, enable services | `name: nginx state: restarted` |
| `command` | Run a command (not through a shell) | `command: node --version` |
| `shell` | Run a shell command (pipes, redirects) | `shell: cat /etc/os-release` |
| `file` | Create/delete files, directories, symlinks | `path: /opt/app state: directory` |
| `user` | Manage OS user accounts | `name: deploy state: present` |
| `git` | Clone or update a Git repository | `repo: https://github.com/... dest: /opt/app` |
| `debug` | Print a message or variable | `msg: "Version is {{ version }}"` |
| `docker_container` | Run/stop Docker containers | `name: app image: hello:1.0 state: started` |
| `docker_image` | Pull/build Docker images | `name: hello:1.0 source: pull` |

---

### Commands at a Glance

| What you want to do | Command |
|---|---|
| Check connectivity | `ansible all -i inventory.ini -m ping` |
| Run a one-off command | `ansible all -i inventory.ini -m shell -a "df -h"` |
| Run a playbook | `ansible-playbook -i inventory.ini playbook.yaml` |
| Dry run (check mode) | `ansible-playbook -i inventory.ini playbook.yaml --check` |
| Verbose output | `ansible-playbook ... -v` (use `-vv` or `-vvv` for more) |
| Run on one host only | `ansible-playbook ... --limit node1` |
| Pass extra variables | `ansible-playbook ... -e "app_port=8080"` |
| List hosts in a group | `ansible webservers -i inventory.ini --list-hosts` |
| Create a role skeleton | `ansible-galaxy init roles/my-role` |
| Install a Galaxy role | `ansible-galaxy install geerlingguy.docker` |
| Encrypt a variable file | `ansible-vault encrypt vars/secrets.yaml` |
| Edit an encrypted file | `ansible-vault edit vars/secrets.yaml` |
| Run playbook with vault | `ansible-playbook ... --ask-vault-pass` |

---

### One-Line Distinctions (commonly confused)

| These seem similar... | But... |
|---|---|
| `command` vs `shell` | `command` runs executables directly — no shell features. `shell` passes through `/bin/sh` — allows pipes, redirects, `&&`. Prefer `command` when you don't need shell features. |
| `copy` vs `template` | `copy` transfers a file as-is. `template` processes Jinja2 `{{ }}` expressions first. Use `template` whenever the file contains variables. |
| `vars:` vs `defaults:` in a role | `defaults` has the lowest precedence — easy to override. `vars` has high precedence — hard to override. Put user-tunable values in `defaults`. |
| `state: present` vs `state: latest` | `present` installs if missing, leaves the version alone. `latest` always upgrades. Use `present` for reproducibility. |
| Handler vs Task | A task always runs when reached. A handler only runs when `notify`ed, and only once per play. |
| Playbook vs Role | A Playbook is the executable entry point. A Role is a reusable module called from a playbook. |
| `ansible` vs `ansible-playbook` | `ansible` runs a single ad-hoc task against hosts. `ansible-playbook` runs a full YAML playbook file. |
| Ansible vs Terraform | Ansible = configuration management (what runs on servers). Terraform = infrastructure provisioning (what servers exist). They are complementary, not competing. |

---

### How Everything Connects

```
inventory.ini          playbook.yaml              roles/
(WHO to manage)   +   (WHAT to do)          +   (reusable tasks)
       ↓                    ↓                          ↓
                   ansible-playbook
                          ↓
              SSH into each managed node
                          ↓
              Run tasks using modules
                          ↓
              ┌──────────────────────────────┐
              │  Managed Node                │
              │  - Packages installed        │
              │  - Config files templated    │
              │  - Services started          │
              │  - Docker containers running │
              └──────────────────────────────┘
                          ↑
              Idempotent — safe to re-run
              Version-controlled — tracked in Git
              Auditable — every change logged
```

---

## Ansible Commands Reference

```bash
# Ad-hoc commands
ansible all -i inventory.ini -m ping
ansible webservers -i inventory.ini -m shell -a "uptime"
ansible dbservers -i inventory.ini -m apt -a "name=curl state=present" --become

# Playbook execution
ansible-playbook -i inventory.ini playbook.yaml
ansible-playbook -i inventory.ini playbook.yaml --check          # dry run
ansible-playbook -i inventory.ini playbook.yaml --diff           # show file diffs
ansible-playbook -i inventory.ini playbook.yaml -v               # verbose
ansible-playbook -i inventory.ini playbook.yaml --limit node1    # single host
ansible-playbook -i inventory.ini playbook.yaml -e "port=8080"   # extra vars
ansible-playbook -i inventory.ini playbook.yaml --tags install    # run tagged tasks only
ansible-playbook -i inventory.ini playbook.yaml --skip-tags test  # skip tagged tasks

# Inventory inspection
ansible all -i inventory.ini --list-hosts
ansible webservers -i inventory.ini --list-hosts
ansible all -i inventory.ini -m setup                            # gather all facts about hosts

# Roles
ansible-galaxy init roles/my-role
ansible-galaxy install geerlingguy.docker
ansible-galaxy list

# Vault (secrets management)
ansible-vault create vars/secrets.yaml
ansible-vault encrypt vars/secrets.yaml
ansible-vault decrypt vars/secrets.yaml
ansible-vault edit vars/secrets.yaml
ansible-vault view vars/secrets.yaml
ansible-playbook ... --ask-vault-pass
ansible-playbook ... --vault-password-file .vault_pass
```

---

## ToC Coverage Map

| Ansible Topic | Covered in |
|---|---|
| DevOps Principles and the Role of Ansible | Bridge section — where Ansible sits in the stack, agentless architecture |
| Ansible Components | Step 1 — architecture diagram; Step 2 — inventory; Step 3 — playbook anatomy |
| Inventory | Step 2 — `inventory.ini`, groups, host variables, ad-hoc ping |
| Ad-hoc Commands | Step 2 — `ansible all -m ping`, `-m shell`, `-m apt` |
| Playbooks | Step 3 — full playbook, `ok` vs `changed`, idempotency demo |
| Variables and Templates | Step 4 — `vars:`, Jinja2 `{{ }}`, `template` module, `group_vars` |
| Handlers | Step 4 — notify / handler pattern, conditional service restarts |
| Modules Reference | Core Modules Reference table — apt, copy, template, service, docker_container, etc. |
| Roles | Step 6 — role structure, `ansible-galaxy init`, `defaults` vs `vars`, Ansible Galaxy |
| CI/CD with Ansible | Step 7 — deploy playbook called from Jenkins, `IMAGE_TAG` variable injection |
| Ansible + Docker | Step 5 — install Docker via Ansible, pull image, run container |
| Ansible + Kubernetes | CI/CD section — Ansible provisions nodes; Kubernetes manages pods on top |

> **Next:** Jenkins — build the CI/CD pipeline that ties Git, Docker, Ansible, and Kubernetes together into an automated delivery workflow.
