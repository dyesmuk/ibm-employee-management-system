# Ansible — Hands-On Guide (DevOps Automation)

> **Prerequisites:** You have completed the Docker and Kubernetes modules. You are comfortable with YAML.
>
> **Environment:** Ansible **cannot run natively on Windows**. On Windows 11, Ansible runs inside **WSL2** (Windows Subsystem for Linux). All Ansible commands in this guide run inside a WSL2 terminal, not in PowerShell or CMD. Docker Desktop must be running on Windows for the lab nodes.

---

## WSL2 Setup (One-Time, Before Starting)

WSL2 gives you a full Linux environment running inside Windows 11. This is where Ansible lives.

### Install WSL2

Open **PowerShell as Administrator** and run:

```powershell
wsl --install
```

This installs WSL2 with Ubuntu 22.04 by default. Restart your laptop when prompted.

After restart, Ubuntu opens automatically and asks you to create a Linux username and password. Set these — you'll need them for `sudo` commands.

### Verify WSL2

```powershell
# In PowerShell — confirm WSL2 is running
wsl --list --verbose
```

Expected output:
```
  NAME      STATE           VERSION
* Ubuntu    Running         2
```

> **Note:** `wsl --list --verbose` is a PowerShell command. Once you are inside a WSL2 terminal (a Bash shell), this command will not work — you will see "command not found". That is normal. Run it only from PowerShell or CMD.

### Open a WSL2 terminal

In Windows Terminal, click the dropdown arrow next to the `+` tab button → select **Ubuntu**. This opens a Linux bash shell. All Ansible work happens here.

> **Quick access:** Pin the Ubuntu profile in Windows Terminal. You can also open WSL2 from any PowerShell/CMD window by typing `wsl`.

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

## Step 1 — Installing Ansible in WSL2

All commands below run inside the **WSL2 / Ubuntu terminal**, not PowerShell.

```bash
sudo apt update
sudo apt install ansible sshpass -y

# Verify
ansible --version
```

Expected output:
```
ansible [core 2.16.x]
  python version = 3.12.x
  ...
```

> **`sshpass` is installed here** alongside Ansible. It is required for password-based SSH and saves a separate step later.

### The Three-Part Architecture

```
Control Node                    Managed Nodes
(WSL2 on your Windows 11 PC —  (target servers —
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
| **Control Node** | WSL2 Ubuntu — where Ansible is installed and all commands run |
| **Managed Node** | Any server Ansible manages. Only needs Python and SSH. |
| **Inventory** | A list of managed nodes — the "who to run against" |
| **Playbook** | A YAML file describing automation tasks — the "what to do" |
| **Task** | A single unit of work (install a package, copy a file, start a service) |
| **Module** | The built-in tool that executes a task (`apt`, `copy`, `service`, `docker_container`, etc.) |
| **Role** | A reusable, structured bundle of tasks for a specific purpose (e.g. "install Java") |

---

## Step 2 — Lab Setup: Two Managed Nodes as Docker Containers

For the hands-on lab, use Docker containers as simulated servers. Docker Desktop on Windows exposes containers to WSL2 automatically.

### Important: Where to create your lab folder

> **WSL2 lab files must live inside the WSL2 filesystem, not on a Windows drive.**
>
> Creating the lab folder under `/mnt/d/` or `/mnt/c/` (Windows NTFS drives) causes a known Ansible error:
> ```
> [WARNING]: Ansible is being run in a world writable directory, ignoring it as an ansible.cfg source.
> ```
> NTFS mounts are world-writable by design, and Ansible refuses to load `ansible.cfg` from them as a security measure. Avoid this entirely by working inside your WSL2 home directory.

**From your WSL2 terminal**, create the lab folder inside your Linux home:

```bash
mkdir ansible-lab && cd ansible-lab
```

### Create `docker-compose.yml`

```bash
nano docker-compose.yml
```

**Note:** `nano` opens the file in editor. Write or paste stuff here. To save ctrl + O , enter, Ctrl + X.  


```yaml
services:
  node1:
    image: ubuntu:22.04
    container_name: ansible-node1
    command: >
      /bin/bash -c "
        apt-get update &&
        apt-get install -y openssh-server python3 &&
        mkdir /run/sshd &&
        echo 'root:password' | chpasswd &&
        sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config &&
        /usr/sbin/sshd -D
      "
    ports:
      - "2221:22"

  node2:
    image: ubuntu:22.04
    container_name: ansible-node2
    command: >
      /bin/bash -c "
        apt-get update &&
        apt-get install -y openssh-server python3 &&
        mkdir /run/sshd &&
        echo 'root:password' | chpasswd &&
        sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config &&
        /usr/sbin/sshd -D
      "
    ports:
      - "2222:22"
```

> **Note:** The `version:` key at the top of `docker-compose.yml` is obsolete in current Docker Compose versions. It is intentionally omitted here to avoid a warning.

```bash
docker compose up -d
```

**Note:** Docker Desktop is not available in WSL by default; it needs to be enabled. 

Go to Docker Desktop -> Settings -> Resources -> WSL Integration. 

Check box **Enable integration with my default WSL distro** 
Toggle **Ubuntu** to select. 


You now have two "servers" (containers) that Ansible will manage.

### Create `ansible.cfg`

> **Create this file before doing anything else with Ansible.** It disables SSH host key checking, which is required for a Docker-based lab where containers regenerate their SSH keys on every restart.

```bash
nano ansible.cfg
```

```ini
[defaults]
host_key_checking = False

[ssh_connection]
ssh_args = -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null
```

**Why both settings?**

| Setting | What it does |
|---|---|
| `host_key_checking = False` | Tells Ansible not to check SSH host keys |
| `StrictHostKeyChecking=no` | Tells the underlying SSH client not to block on unknown hosts |
| `UserKnownHostsFile=/dev/null` | Stops SSH from writing or reading `~/.ssh/known_hosts` — prevents stale key conflicts when containers are recreated |

> **Trainer note:** In production, host key checking should be enabled. For this lab, disabling it avoids repeated SSH fingerprint errors as containers restart.

---

## Step 3 — Inventory: Telling Ansible What to Manage

The **inventory** is a file listing the servers Ansible will work on.

### Create `inventory.ini`

```bash
nano inventory.ini
```

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
| `[webservers]` | A group name — run playbooks against groups, not just individual hosts |
| `ansible_host` | The actual IP to connect to |
| `ansible_port` | SSH port |
| `ansible_user` | SSH user |
| `ansible_password` | SSH password (use SSH keys in production) |
| `[all:vars]` | Variables that apply to every host |

> **Important:** Keep `ansible.cfg` and `inventory.ini` as separate files. Ansible configuration directives like `[defaults]` and `host_key_checking` belong only in `ansible.cfg`. Placing them in `inventory.ini` causes a parse error.

### Your lab folder should now look like this

```
~/ansible-lab/
  ├── ansible.cfg
  ├── docker-compose.yml
  └── inventory.ini
```

### Test connectivity with an ad-hoc command

```bash
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

> **Ansible's `ping` module is not ICMP ping.** It SSHes into the host, runs a small Python check, and returns `pong`. It is Ansible's SSH connectivity test.

### More ad-hoc commands

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
→ Run one command against `[webservers]` or `[dbservers]` without listing every host individually.

**2. "The `-m ping` module — is this the same as the `ping` command in your terminal?"**
→ No. Ansible's `ping` module SSHes into the host, runs a small Python check, and returns `pong`. Network ICMP ping is a different thing.

**3. "Why is Ansible agentless? What's the practical benefit?"**
→ Nothing needs to be pre-installed on managed nodes. Standard SSH + Python is enough. You can start managing an existing server immediately.

---

## Step 4 — Your First Playbook

An ad-hoc command runs one task. A **Playbook** runs many tasks in sequence — repeatable, version-controlled, readable.

### Create `install-node.yaml`

```bash
nano install-node.yaml
```

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
    "msg": "Node.js version installed: v12.x.x"
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

All tasks show `ok` — not `changed`. Node.js is already installed, Ansible detects this and skips the work. This is **idempotency**: running the same playbook 10 times produces the same result as running it once.

### Anatomy of a Playbook

```yaml
---                          # YAML document start
- name: Human-readable title  # The "play" — targets a group of hosts
  hosts: webservers           # Which group from inventory to run against
  become: yes                 # Use sudo (privilege escalation)

  tasks:
    - name: Task description  # Human-readable label (shown in output)
      apt:                    # The MODULE to use
        name: nodejs          # Module ARGUMENTS
        state: present        # "present" = install, "absent" = uninstall
```

### Three Questions to Ask Trainees

**1. "Run the playbook twice. What's different the second time?"**
→ All tasks show `ok` instead of `changed`. This is idempotency — the core reliability guarantee.

**2. "What does `become: yes` do? When would you leave it out?"**
→ It uses `sudo` to escalate privileges. Needed for system-level tasks. Leave it out for tasks a regular user can do.

**3. "What's the difference between `state: present` and `state: latest`?"**
→ `present` installs if missing but doesn't upgrade. `latest` always upgrades. Use `present` for reproducibility.

---

## Step 5 — Variables, Templates, and Handlers

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

`{{ variable_name }}` is Ansible's template syntax — Jinja2.

| Variable location | Use case |
|---|---|
| `vars:` in the play | Quick inline variables |
| `vars_files:` | Separate YAML file for many variables |
| `group_vars/webservers.yaml` | Variables for a whole group — auto-loaded |
| `host_vars/node1.yaml` | Variables for one specific host — auto-loaded |
| Command line: `-e "app_port=8080"` | Override at runtime |

### Handlers

A **handler** is a task that runs only when notified — and only once at the end of the play.

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
      notify: Restart Nginx

  handlers:
    - name: Restart Nginx
      service:
        name: nginx
        state: restarted
```

If `nginx.conf` hasn't changed, `copy` is `ok` — handler never fires. If you push a new config, `copy` is `changed` — Nginx restarts once at the end.

### Templates (Jinja2)

**`templates/app.env.j2`:**

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

Same template, different values per environment — one playbook deploys to dev, staging, and production.

---

## Step 6 — Deploying Docker with Ansible

Use Ansible to install Docker on the managed nodes and run your Express container.

### Folder structure

```
~/ansible-lab/
  ├── ansible.cfg
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
    app_image: vamandeshmukh/hello-express:1.0
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
        repo: "deb [arch=amd64] https://download.docker.com/linux/ubuntu {{ ansible_facts['lsb']['codename'] }} stable"
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

> **Note on `ansible_facts` syntax:** The playbook above uses `ansible_facts['lsb']['codename']` — the recommended syntax in current Ansible versions. The older shorthand `ansible_lsb.codename` produces a deprecation warning in Ansible 2.17+ and will be removed in a future release. Always use the `ansible_facts` dictionary form.

```bash
ansible-playbook -i inventory.ini deploy-docker.yaml
```

---

## Step 7 — Roles: Reusable Automation

### Create a role

```bash
ansible-galaxy init roles/install-docker
ansible-galaxy init roles/deploy-app
```

### Role directory structure

```
roles/
  install-docker/
    ├── tasks/
    │     └── main.yaml
    ├── handlers/
    │     └── main.yaml
    ├── templates/
    │     └── daemon.json.j2
    ├── vars/
    │     └── main.yaml
    └── defaults/
          └── main.yaml
```

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

### Ansible Galaxy — Community Roles

```bash
ansible-galaxy search docker
ansible-galaxy install geerlingguy.docker

# Use in playbook
roles:
  - geerlingguy.docker
```

---

## Step 8 — CI/CD Integration: Ansible in the Pipeline

### The Full DevOps Pipeline

```
Developer pushes code
        ↓
     GitHub
        ↓
    Jenkins (CI) — runs on Windows via Docker
     - Run tests
     - docker build
     - docker push → Docker Hub
        ↓
    Ansible (CD) — runs inside WSL2
     - ansible-playbook deploy.yaml
     - SSH into production servers
     - docker pull (new image)
     - Restart containers
        ↓
  Production Servers
  (running updated app)
```

### `deploy-update.yaml`

```yaml
---
- name: Deploy updated application
  hosts: webservers
  become: yes
  vars:
    image_tag: "{{ lookup('env', 'IMAGE_TAG') }}"

  tasks:
    - name: Pull the new image
      community.docker.docker_image:
        name: "vamandeshmukh/hello-express:{{ image_tag }}"
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
        image: "vamandeshmukh/hello-express:{{ image_tag }}"
        state: started
        restart_policy: always
        ports:
          - "3000:3000"
```

### Calling Ansible from Jenkins (Jenkinsfile snippet)

Jenkins runs on Windows via Docker. To call Ansible (which lives in WSL2), use the `wsl` command:

```groovy
stage('Deploy') {
    steps {
        // Call Ansible inside WSL2 from Jenkins running on Windows
        sh """
          wsl ansible-playbook -i inventory.ini deploy-update.yaml \
            -e IMAGE_TAG=${BUILD_NUMBER}
        """
    }
}
```

> **Alternative for production:** Run Jenkins agents as Linux VMs or containers where Ansible is installed natively — this is cleaner than calling WSL2 from Jenkins.

---

## Ansible — Key Concepts Summary

### The Big Picture

Ansible solves one problem: **automating repetitive operations on many servers.** On Windows 11, Ansible runs inside WSL2 — the control node is your Ubuntu environment inside Windows. Target servers are Linux machines (or Docker containers) reachable over SSH.

---

### Core Concepts

**Inventory** — The list of servers Ansible manages. Groups hosts so you can target a class of server with one command.

**Playbook** — A YAML file containing one or more plays. A play maps a group of hosts to a list of tasks.

**Task** — A single call to a module with arguments. Each task has a name (shown in output) and a module.

**Module** — A built-in or community tool that does one specific job: `apt`, `copy`, `service`, `template`, `docker_container`, etc. Ansible ships with 3,000+ modules.

**Handler** — A task that only runs when explicitly notified by another task — and only once per play. Used for service restarts.

**Idempotency** — Running the same playbook multiple times produces the same result. Safe to re-run repeatedly.

**Role** — A structured, reusable bundle of tasks, handlers, templates, and variables. Shared via Ansible Galaxy.

**Ansible Galaxy** — A public registry of community roles. `ansible-galaxy install <role>` downloads a pre-built role.

---

### Core Modules Reference

| Module | What it does | Example |
|---|---|---|
| `apt` | Manage packages on Debian/Ubuntu | `name: nodejs state: present` |
| `yum` / `dnf` | Manage packages on RHEL/CentOS | `name: java-17 state: present` |
| `copy` | Copy a file to the remote server | `src: app.conf dest: /etc/app.conf` |
| `template` | Render a Jinja2 template and copy | `src: app.env.j2 dest: /opt/app/.env` |
| `service` | Start, stop, restart, enable services | `name: nginx state: restarted` |
| `command` | Run a command (no shell features) | `command: node --version` |
| `shell` | Run a shell command (pipes, redirects) | `shell: cat /etc/os-release` |
| `file` | Create/delete files, directories, symlinks | `path: /opt/app state: directory` |
| `user` | Manage OS user accounts | `name: deploy state: present` |
| `git` | Clone or update a Git repository | `repo: https://github.com/... dest: /opt/app` |
| `debug` | Print a message or variable | `msg: "Version is {{ version }}"` |
| `docker_container` | Run/stop Docker containers | `name: app image: hello:1.0 state: started` |
| `docker_image` | Pull/build Docker images | `name: hello:1.0 source: pull` |

---

### Commands at a Glance

> All commands run inside **WSL2 Ubuntu terminal**, not PowerShell.

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
| `command` vs `shell` | `command` runs executables directly. `shell` passes through `/bin/sh` — allows pipes, redirects. |
| `copy` vs `template` | `copy` transfers a file as-is. `template` processes Jinja2 `{{ }}` first. |
| `vars:` vs `defaults:` in a role | `defaults` is easiest to override. `vars` has high priority. |
| `state: present` vs `state: latest` | `present` installs if missing. `latest` always upgrades. |
| Handler vs Task | A task always runs. A handler only runs when notified, once per play. |
| Playbook vs Role | Playbook is the entry point. Role is a reusable module called from a playbook. |
| `ansible` vs `ansible-playbook` | `ansible` runs a single ad-hoc task. `ansible-playbook` runs a full YAML file. |
| WSL2 vs native Linux | WSL2 is a Linux VM inside Windows. Ansible runs inside WSL2. Commands look identical to native Linux. |
| `ansible.cfg` vs `inventory.ini` | `ansible.cfg` controls how Ansible behaves. `inventory.ini` lists the hosts to manage. Never mix them. |

---

## Common Errors and Fixes (WSL2 Lab Reference)

This table captures errors specific to running Ansible on WSL2 with Docker containers. Share it with trainees at the start of the lab session.

| Error message | Root cause | Fix |
|---|---|---|
| `Host Key checking is enabled` | `ansible.cfg` is missing or not being loaded | Create `ansible.cfg` in `~/ansible-lab/` with `host_key_checking = False` |
| `Expected key=value host variable assignment, got: False` | `[defaults]` block was placed inside `inventory.ini` | Move `[defaults]` to `ansible.cfg` — it is not valid INI inventory syntax |
| `REMOTE HOST IDENTIFICATION HAS CHANGED` | Old SSH fingerprints in `~/.ssh/known_hosts` from a previous container run | Run `ssh-keygen -R '[127.0.0.1]:2221'` and `ssh-keygen -R '[127.0.0.1]:2222'`, or set `UserKnownHostsFile=/dev/null` in `ansible.cfg` |
| `world writable directory, ignoring ansible.cfg` | Lab folder is on `/mnt/d/` or `/mnt/c/` (Windows NTFS drive, world-writable) | Create the lab folder inside WSL2 home: `~/ansible-lab/` |
| `INJECT_FACTS_AS_VARS default to True is deprecated` | Playbook uses `ansible_lsb.codename` shorthand | Replace with `ansible_facts['lsb']['codename']` |
| `version is obsolete` warning in `docker compose up` | `docker-compose.yml` has a top-level `version:` key | Remove the `version:` line — it is not needed in current Docker Compose |

---

## ToC Coverage Map

| Ansible Topic | Covered in |
|---|---|
| DevOps Principles and the Role of Ansible | Bridge section — where Ansible sits in the stack, agentless architecture |
| Windows 11 Setup | WSL2 install section — `wsl --install`, Ubuntu terminal, Docker Desktop integration |
| Ansible Components | Step 1 — architecture diagram; Step 3 — inventory; Step 4 — playbook anatomy |
| Inventory | Step 3 — `inventory.ini`, groups, `sshpass`, SSH host key fix |
| Ad-hoc Commands | Step 3 — `ansible all -m ping`, `-m shell`, `-m apt` |
| Playbooks | Step 4 — full playbook, `ok` vs `changed`, idempotency demo |
| Variables and Templates | Step 5 — `vars:`, Jinja2 `{{ }}`, `template` module, `group_vars` |
| Handlers | Step 5 — notify / handler pattern, conditional service restarts |
| Modules Reference | Core Modules Reference table |
| Roles | Step 7 — role structure, `ansible-galaxy init`, `defaults` vs `vars`, Ansible Galaxy |
| CI/CD with Ansible | Step 8 — deploy playbook, `wsl ansible-playbook` from Jenkins on Windows |
| Ansible + Docker | Step 6 — install Docker via Ansible, pull image, run container |
| Common Errors | Common Errors and Fixes table — WSL2 + Docker lab specific |

> **Next:** Jenkins — build the CI/CD pipeline that ties Git, Docker, Ansible, and Kubernetes together into an automated delivery workflow.
