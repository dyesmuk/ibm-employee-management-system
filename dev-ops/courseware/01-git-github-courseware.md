# 01 — Git and GitHub

> **Series:** DevOps Hands-On | **Module:** 1 of 6 | **Project:** Node.js Express app

> **Environment:** Windows 11. All commands run in **Windows Terminal (PowerShell or Git Bash)**. Git Bash ships with Git for Windows and is recommended — it gives you Unix-style commands that match every Git tutorial on the internet.
>
> **Project used throughout:** A simple Node.js Express app — the same one carried forward into Docker, Kubernetes, Ansible, and Jenkins.

---

## About This Guide

This is the first module in a six-part DevOps series. Before any container is built, any server is automated, or any pipeline is triggered — code needs to be tracked and shared. That is what this module covers.

**What you will learn:**
- Track changes to your project using Git
- Understand what `.gitignore` does and why `node_modules` is never committed
- Undo mistakes safely at any stage
- Push code to GitHub and collaborate using Pull Requests
- Use branches to work on features without breaking the main codebase

**How this fits into the series:**
```
01 Git & GitHub  ← YOU ARE HERE
02 Docker        — containerise the app you version-control here
03 YAML          — configuration language used by Docker, Kubernetes, Ansible
04 Kubernetes    — orchestrate the containers you build in Docker
05 Ansible       — automate provisioning of the servers your cluster runs on
06 Jenkins       — automate the entire pipeline end to end
```

**Project thread:** You will build a simple Node.js Express app in this module. The same project — same codebase, same GitHub repository — is carried forward into every subsequent module. By the end of the series it will be containerised, orchestrated, provisioned, and fully CI/CD automated.

**Tools needed for this module:** Git for Windows ([git-scm.com](https://git-scm.com)), VS Code, Node.js, a GitHub account.

---

## Welcome — Why Git?

Every file you've ever accidentally overwritten, every "final_v2_FINAL_actual.js" you've created, every time you broke something and couldn't get back — Git solves all of that.

Git is a **version control system**. It tracks every change ever made to every file in a project. You can go back to any point in history, work on experimental features without breaking the main code, and collaborate with a team without stepping on each other's work.

GitHub is where Git repositories live on the internet — a platform for sharing, collaborating, and connecting Git to CI/CD pipelines.

```
Git     = the tool on your machine that tracks changes
GitHub  = the website that hosts repositories and enables collaboration
```

The mental model:

```
Without Git:                    With Git:
final.js                        Every save is a snapshot.
final_v2.js                     You describe what changed.
final_v2_fixed.js               You can jump to any snapshot.
final_v2_fixed_REAL.js          Your whole team works from the same history.
final_USE_THIS_ONE.js
```

---

## Step 1 — Git Basics

### Install Git on Windows 11

Download from [git-scm.com](https://git-scm.com). During install:
- Choose **Git Bash** as the default terminal — recommended
- Choose **VS Code** as the default editor
- Leave all other options as default

Verify:

```bash
git --version
```

Expected: `git version 2.4x.x.windows.x`

### Configure your identity

Git records who made each change. Set this once:

```bash
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
git config --global core.editor "code --wait"   # use VS Code as Git editor

# Verify
git config --list
```

### Create your first repository

A **repository** (repo) is a folder that Git tracks.

```bash
# Create the project folder
mkdir hello-git
cd hello-git

# Initialise Git — creates a hidden .git folder
git init
```

`git init` creates a `.git` folder inside your project. This folder **is** the repository — it stores all the history. Never delete it.

### Your first Node.js project

Create `app.js`:

```js
const express = require('express');
const app = express();

app.get('/', (req, res) => {
  res.send('Hello from Git and Express!');
});

app.listen(3000, () => console.log('Server running on port 3000'));
```

Create `package.json`:

```json
{
  "name": "hello-git",
  "version": "1.0.0",
  "main": "app.js",
  "dependencies": {
    "express": "4.18.2"
  }
}
```

Install dependencies:

```bash
npm install
```

This creates a `node_modules/` folder with ~50MB of files. You do **not** want to commit this to Git.

### `.gitignore` — telling Git what to ignore

Before making your first commit, tell Git to ignore `node_modules` and other generated files.

Create `.gitignore` in the project root:

```
# Node.js dependencies — installed by npm, never committed
node_modules/

# Environment variables — contains secrets, never committed
.env

# OS files
.DS_Store
Thumbs.db

# Logs
*.log
npm-debug.log*

# Build output
dist/
build/
```

**Why `node_modules` must be ignored:**
- It contains thousands of files (often 50–200MB)
- It's always reproducible with `npm install`
- It's specific to the OS/architecture — a `node_modules` committed on Windows may break on Linux
- Anyone cloning the repo runs `npm install` and gets a fresh, correct copy

**The rule:** never commit anything that can be generated. Commit the *recipe*, not the *output*.

Verify Git is ignoring it:

```bash
git status
```

Expected — `node_modules` should NOT appear:
```
On branch main

No commits yet

Untracked files:
  (use "git add <file>..." to include in what will be committed)
        .gitignore
        app.js
        package.json
        package-lock.json
```

### The Three Areas of Git

```
Working Directory        Staging Area          Repository (.git)
(your files on disk)  →  (what will be     →   (permanent history,
                          in the commit)         all commits stored here)

     git add                                     git commit
```

| Area | What it is |
|---|---|
| Working Directory | Your files as they are right now on disk |
| Staging Area (Index) | A holding area — you choose exactly what goes into the next commit |
| Repository | The permanent record — every commit ever made |

### Making your first commit

```bash
# Stage all files
git add .

# Check what's staged
git status

# Commit with a message
git commit -m "Initial commit: Express app with gitignore"
```

Expected:
```
[main (root-commit) a3f9c12] Initial commit: Express app with gitignore
 4 files changed, 15 insertions(+)
 create mode 100644 .gitignore
 create mode 100644 app.js
 create mode 100644 package.json
 create mode 100644 package-lock.json
```

### Viewing history

```bash
# Full log
git log

# Compact one-line log
git log --oneline

# See what changed in a specific commit
git show a3f9c12
```

### Making more commits

Update `app.js`:

```js
app.get('/', (req, res) => {
  res.send('Hello from Git and Express! v2');
});
```

```bash
# See what changed
git diff

# Stage and commit
git add app.js
git commit -m "Update home route response to v2"

# See the history
git log --oneline
```

```
b1e2d34 Update home route response to v2
a3f9c12 Initial commit: Express app with gitignore
```

### Three Questions to Ask Trainees

**1. "Why do we have a staging area? Why not just `git commit` directly?"**
→ The staging area lets you be selective. You might have changed 5 files but only want to commit changes to 2. `git add` lets you choose exactly what goes into each commit, keeping history meaningful.

**2. "What happens if you don't create `.gitignore` before the first commit?"**
→ `node_modules` goes into the repository. It's thousands of files, the repo becomes huge, and pushing to GitHub is very slow. Fixing it later requires rewriting history. Always create `.gitignore` first.

**3. "What's the difference between `git add .` and `git add app.js`?"**
→ `git add .` stages everything changed. `git add app.js` stages only that file. Precise staging = precise commits = readable history.

---

## Step 2 — Undoing Things

Git's real power is the ability to go back. There are several ways depending on how far back you need to go.

### Undo before staging — `git restore`

You've changed `app.js` but haven't staged it yet. You want to throw away the change:

```bash
git restore app.js
```

The file goes back to exactly what it was at the last commit. **This is permanent** — the change is gone.

### Undo after staging — unstage a file

You ran `git add app.js` but changed your mind:

```bash
git restore --staged app.js
```

The file stays changed on disk — it's just removed from the staging area. You can then edit it further or commit it later.

### Undo a commit — `git revert`

You committed something wrong. `git revert` creates a **new** commit that undoes it — safe for shared history:

```bash
# See the commit hash to revert
git log --oneline

# Revert a specific commit
git revert b1e2d34
```

Git opens your editor to write a revert message. Save and close. A new commit appears that reverses the changes of `b1e2d34`. The original commit stays in history — nothing is erased.

### Reset — rewriting local history

`git reset` moves the branch pointer backward. Three modes:

```bash
# Soft reset — undo commit, keep changes staged
git reset --soft HEAD~1

# Mixed reset (default) — undo commit, unstage changes, keep files
git reset HEAD~1

# Hard reset — undo commit AND discard all changes (destructive)
git reset --hard HEAD~1
```

`HEAD~1` means "one commit before HEAD (the current commit)".

| Command | Commit undone? | Staging cleared? | Files changed? |
|---|---|---|---|
| `reset --soft` | ✅ | ❌ | ❌ |
| `reset --mixed` | ✅ | ✅ | ❌ |
| `reset --hard` | ✅ | ✅ | ✅ (gone!) |

> **Rule of thumb:** Use `git revert` on commits that have been pushed to GitHub (shared history). Use `git reset` only on local commits that nobody else has seen.

### See a previous version without changing anything

```bash
# Look at any file at any commit
git show a3f9c12:app.js

# Temporarily go back to a commit (detached HEAD)
git checkout a3f9c12
git checkout main       # come back to the present
```

### Three Questions to Ask Trainees

**1. "What's the difference between `git revert` and `git reset`?"**
→ `revert` adds a new commit that undoes the change — safe for shared history. `reset` moves the branch pointer back — rewrites history, dangerous on shared branches.

**2. "You staged a file accidentally with `git add`. How do you unstage it?"**
→ `git restore --staged filename`. The file stays changed on disk, it's just removed from the staging area.

**3. "What does `HEAD` mean?"**
→ `HEAD` is a pointer to the current commit — where you are right now in history. `HEAD~1` is one commit before that. `HEAD~3` is three commits back.

---

## Step 3 — The Basics of GitHub

### Create a GitHub account

Go to [github.com](https://github.com) and create a free account. Your username will appear in every repository URL and commit you push.

### Create a repository on GitHub

1. Click **+** → **New repository**
2. Repository name: `hello-git`
3. Visibility: **Public** (free) or Private
4. **Do NOT** initialise with README, .gitignore, or license — you already have a local repo
5. Click **Create repository**

GitHub shows you the commands to connect your local repo.

### Connect local repo to GitHub

```bash
# Add the remote — "origin" is the conventional name for your primary remote
git remote add origin https://github.com/yourname/hello-git.git

# Verify the remote was added
git remote -v

# Push your commits to GitHub
git push -u origin main
```

`-u origin main` sets the upstream — after this, you can just `git push` without specifying where.

Browser → `https://github.com/yourname/hello-git` — your code is now on GitHub.

### Authenticate with GitHub — Personal Access Token

GitHub no longer accepts passwords for `git push`. Use a **Personal Access Token (PAT)**:

1. GitHub → **Settings → Developer settings → Personal access tokens → Tokens (classic)**
2. **Generate new token**
3. Select scopes: `repo` (full control of private repositories)
4. Copy the token — you won't see it again

When Git asks for a password during `git push`, paste the token.

**Save the token in Windows Credential Manager** so you don't type it every time:

```bash
git config --global credential.helper manager
```

Git Credential Manager (installed with Git for Windows) stores it automatically after the first successful push.

### The everyday workflow

```bash
# Make changes to files
# ...edit app.js...

# Stage changes
git add .

# Commit
git commit -m "Add new endpoint"

# Push to GitHub
git push

# Pull latest from GitHub (before starting work each day)
git pull
```

### Clone an existing repository

```bash
# Clone someone else's repo (or your own on a new machine)
git clone https://github.com/yourname/hello-git.git

cd hello-git
npm install      # restore node_modules (not in Git, must be installed)
```

`git clone` creates a new folder, downloads all files and history, and sets up the `origin` remote automatically.

### Three Questions to Ask Trainees

**1. "Why is `node_modules` not in the cloned repo? How does it work after cloning?"**
→ It's in `.gitignore`. After cloning you run `npm install` which reads `package.json` and installs everything fresh. The `package-lock.json` (which IS committed) ensures the exact same versions are installed.

**2. "What does `git push -u origin main` mean? Why `-u`?"**
→ `-u` sets the upstream tracking relationship. After this, `git push` and `git pull` know which remote branch to use without being told explicitly.

**3. "Can two people push to the same repository at the same time?"**
→ Yes — but if their changes conflict, one person's push will be rejected. They need to `git pull` first, resolve any merge conflicts, then push. This is what branches solve — covered next.

---

## Step 4 — Working with Branches

Branches let you work on a feature or fix in isolation without affecting the main codebase. This is the foundation of all team collaboration workflows.

### The mental model

```
main branch:     A --- B --- C                    (stable, always works)
                              \
feature branch:               D --- E --- F       (your work in progress)
```

When the feature is complete and tested, you merge it back into `main`.

### Create and switch branches

```bash
# Create a new branch and switch to it
git checkout -b feature/add-about-page

# Or — the modern way (Git 2.23+)
git switch -c feature/add-about-page

# See all branches (* marks the current branch)
git branch

# Switch between branches
git checkout main
git switch main
```

### Work on the feature branch

Add a new route to `app.js`:

```js
app.get('/about', (req, res) => {
  res.send('About page — Hello Git project');
});
```

```bash
git add app.js
git commit -m "Add /about route"
```

This commit exists only on `feature/add-about-page`. The `main` branch is untouched.

### Push the branch to GitHub

```bash
git push -u origin feature/add-about-page
```

GitHub now has the branch. Other team members can see it, check it out, and collaborate on it.

### Merge the branch into main

When the feature is complete:

```bash
# Switch to main
git switch main

# Merge the feature branch into main
git merge feature/add-about-page
```

```
Fast-forward merge:

main:     A --- B --- C
                       \
feature:               D --- E --- F
                                    ↑
After merge:
main:     A --- B --- C --- D --- E --- F
```

A **fast-forward merge** happens when main hasn't changed since the branch was created — Git just moves the pointer forward.

### Delete the branch after merging

```bash
# Delete locally
git branch -d feature/add-about-page

# Delete on GitHub
git push origin --delete feature/add-about-page
```

### Merge conflicts

A conflict happens when the same line was changed on both branches. Git marks the conflict in the file:

```
<<<<<<< HEAD
  res.send('Hello from Git and Express! v2');
=======
  res.send('Hello from the feature branch!');
>>>>>>> feature/add-about-page
```

To resolve:
1. Edit the file — delete the markers and keep what you want
2. `git add app.js`
3. `git commit -m "Resolve merge conflict in app.js"`

### Common Branch Naming Conventions

| Pattern | Use case |
|---|---|
| `feature/user-login` | New features |
| `fix/null-pointer-error` | Bug fixes |
| `hotfix/security-patch` | Urgent production fixes |
| `release/v2.0` | Release preparation |
| `chore/update-deps` | Maintenance (no feature/fix) |

### Three Questions to Ask Trainees

**1. "Why should you never commit directly to `main`?"**
→ `main` should always be in a deployable state. Unfinished work on main means the CI/CD pipeline might deploy broken code. Branches keep main clean.

**2. "You created a branch, committed, then switched back to main. Where did your changes go?"**
→ They're still there — on the feature branch. Switching branches changes your working directory to match that branch's state. This surprises people the first time.

**3. "What's the difference between `merge` and `rebase`?"**
→ `merge` creates a merge commit and preserves the full history. `rebase` replays your commits on top of the target branch — creates a linear history. `merge` is safer for shared branches; `rebase` is preferred for keeping feature branch history clean before merging.

---

## Step 5 — Forking and Contributing

### What is a fork?

A **fork** is your personal copy of someone else's repository on GitHub. You can make any changes without affecting the original. When ready, you propose your changes back via a **Pull Request**.

```
Original repo (owner's GitHub)
        ↓  Fork
Your fork (your GitHub)
        ↓  Clone
Your local machine
        ↓  Push changes to your fork
Your fork (your GitHub)
        ↓  Pull Request
Original repo (owner reviews and merges)
```

### Fork a repository

1. Go to any GitHub repository
2. Click **Fork** (top right) → **Create fork**
3. GitHub creates a copy at `https://github.com/yourname/original-repo-name`

### Clone your fork

```bash
git clone https://github.com/yourname/original-repo-name.git
cd original-repo-name
npm install
```

### Add the original repo as `upstream`

So you can pull in updates from the original:

```bash
git remote add upstream https://github.com/originalowner/original-repo-name.git

# Verify
git remote -v
```

You now have two remotes:
- `origin` → your fork
- `upstream` → the original repo

### Make changes, push to your fork, open a Pull Request

```bash
git checkout -b fix/typo-in-readme

# make changes...

git add .
git commit -m "Fix typo in README"
git push origin fix/typo-in-readme
```

On GitHub:
1. A banner appears: **"Compare & pull request"** — click it
2. Write a description of what you changed and why
3. Click **Create pull request**

The original repo owner reviews, comments, requests changes, and eventually merges (or closes) your PR.

### Keep your fork in sync

```bash
# Pull updates from the original repo
git fetch upstream

# Merge them into your local main
git checkout main
git merge upstream/main

# Push to your fork
git push origin main
```

---

## Step 6 — Collaboration

### Pull Requests (PRs) — the core collaboration unit

A **Pull Request** is a proposal to merge one branch into another. On a team:

1. Developer creates a feature branch and pushes it
2. Developer opens a PR on GitHub targeting `main`
3. Team reviews the code — leaves comments, requests changes
4. Developer pushes more commits to address review comments
5. PR is approved and merged
6. Feature branch is deleted

PRs are **not a Git feature** — they're a GitHub feature. They add review, discussion, and approval on top of Git's merge.

### Protected branches

In a team setting, `main` is protected — nobody can push directly to it. All changes go through PRs.

**On GitHub:** Repository → **Settings → Branches → Add branch protection rule**

Common rules:
- Require pull request before merging
- Require at least 1 approving review
- Require status checks (CI pipeline) to pass before merging
- Restrict who can push to main

### GitHub Issues

**Issues** are used to track bugs, feature requests, and tasks.

- Every issue gets a number: `#42`
- Reference issues in commits: `git commit -m "Fix login bug, closes #42"`
- When the PR is merged, GitHub automatically closes issue `#42`

### GitHub Flow — the standard team workflow

```
1. Create a branch from main
         ↓
2. Add commits (write code)
         ↓
3. Push branch to GitHub
         ↓
4. Open a Pull Request
         ↓
5. Review and discuss
         ↓
6. Merge to main
         ↓
7. Deploy (CI/CD pipeline triggers automatically)
```

This is the workflow used by most software teams in the world. Every step maps to something you'll see in the Jenkins CI/CD pipeline later.

### Resolving conflicts in a PR

If two PRs both modify the same file and one is merged first, the second gets a conflict. GitHub shows a **"Resolve conflicts"** button in the PR. You can resolve in the browser or locally:

```bash
git checkout main
git pull
git checkout my-feature-branch
git merge main       # bring in the latest main
# resolve conflicts in files
git add .
git commit -m "Resolve merge conflicts with main"
git push
```

The PR automatically updates.

---

## Git — Key Concepts Summary

### Core Concepts

**Repository** — A folder tracked by Git. Contains all files and the complete history of every change. The `.git` subfolder is the repository database.

**Commit** — A snapshot of the project at a point in time. Has a unique SHA hash, a message, an author, and a timestamp. The atomic unit of Git history.

**Branch** — A lightweight pointer to a commit. Creating a branch is free and instant. The default branch is called `main` (formerly `master`).

**HEAD** — A pointer to the current commit (or branch). "Where you are right now."

**Remote** — A repository hosted elsewhere (GitHub, GitLab, Bitbucket). `origin` is the conventional name for the primary remote.

**Clone** — Download a full copy of a repository including all history. Sets up `origin` automatically.

**Fork** — A personal GitHub copy of someone else's repository. Independent until you open a PR.

**Pull Request (PR)** — A GitHub proposal to merge one branch into another. The mechanism for code review and collaboration.

**Merge** — Combine changes from one branch into another.

**Merge Conflict** — When the same part of a file was changed differently on two branches being merged. Must be resolved manually.

**`.gitignore`** — A file listing paths Git should not track. Generated files (`node_modules`, `dist`, `.env`) are always ignored.

**Staging Area** — The intermediate step between editing files and committing them. Lets you craft precise commits.

---

### Commands at a Glance

| What you want to do | Command |
|---|---|
| Initialise a repo | `git init` |
| Clone a repo | `git clone <url>` |
| Check status | `git status` |
| Stage files | `git add .` or `git add <file>` |
| Commit | `git commit -m "message"` |
| See history | `git log --oneline` |
| See diff | `git diff` |
| Create + switch branch | `git switch -c branch-name` |
| Switch branch | `git switch branch-name` |
| List branches | `git branch` |
| Merge branch | `git merge branch-name` |
| Delete branch | `git branch -d branch-name` |
| Add remote | `git remote add origin <url>` |
| Push | `git push` |
| Pull | `git pull` |
| Fetch | `git fetch` |
| Undo unstaged change | `git restore <file>` |
| Unstage a file | `git restore --staged <file>` |
| Undo last commit (keep files) | `git reset HEAD~1` |
| Undo a commit safely | `git revert <hash>` |
| Stash work in progress | `git stash` |
| Restore stash | `git stash pop` |

---

### One-Line Distinctions (commonly confused)

| These seem similar... | But... |
|---|---|
| `git fetch` vs `git pull` | `fetch` downloads remote changes but doesn't apply them. `pull` = `fetch` + `merge`. Use `fetch` to see what's changed before merging. |
| `git merge` vs `git rebase` | `merge` preserves history with a merge commit. `rebase` replays commits to create a linear history. |
| `git reset` vs `git revert` | `reset` rewrites history (local only). `revert` creates a new undo commit (safe for shared branches). |
| `git stash` vs a branch | `stash` temporarily shelves uncommitted changes. A branch is a permanent snapshot of committed work. |
| Fork vs Clone | Fork = your copy on GitHub. Clone = download to your machine. You fork then clone. |
| PR vs Merge | A PR is a GitHub review process. A merge is a Git operation. PRs result in merges, but a merge doesn't require a PR. |
| `.gitignore` vs `.gitkeep` | `.gitignore` tells Git to ignore files. `.gitkeep` is a convention to track empty folders (Git ignores empty directories by default). |

---

### How Everything Connects

```
Your Machine                           GitHub
─────────────────────────────          ─────────────────────────
Working Dir → Staging → Local Repo     Remote Repo (origin)
                          ↑                     ↑
                     git commit           git push / pull
                                                 ↑
                                        Team members
                                        clone / fork / PR
                                                 ↓
                                        CI/CD Pipeline
                                    (Jenkins watches for pushes,
                                     triggers build automatically)
```

---

## Git Commands Reference

```bash
# Setup
git config --global user.name "Name"
git config --global user.email "email"
git config --global credential.helper manager

# Repository
git init
git clone <url>
git remote add origin <url>
git remote -v

# Daily workflow
git status
git diff
git add .
git add <file>
git commit -m "message"
git push
git pull
git fetch

# Branches
git branch
git switch -c new-branch
git switch branch-name
git merge branch-name
git branch -d branch-name
git push origin branch-name
git push origin --delete branch-name

# History
git log
git log --oneline
git log --oneline --graph --all
git show <hash>
git diff <hash1> <hash2>

# Undoing
git restore <file>              # undo unstaged changes
git restore --staged <file>     # unstage
git reset HEAD~1                # undo last commit, keep files
git reset --hard HEAD~1         # undo last commit, discard files
git revert <hash>               # safe undo (creates new commit)

# Stash
git stash
git stash list
git stash pop
git stash drop

# Tags (for releases)
git tag v1.0
git tag -a v1.0 -m "Release version 1.0"
git push origin v1.0
git push origin --tags
```

---

## ToC Coverage Map

| Git / GitHub Topic | Covered in |
|---|---|
| Welcome — Why Git? | Welcome section — the mental model, before/after comparison |
| Git Basics | Step 1 — `git init`, `.gitignore`, staging area, `git add`, `git commit`, `git log` |
| `.gitignore` with Node.js | Step 1 — `node_modules`, `.env`, why generated files are excluded, `package-lock.json` |
| Undoing Things | Step 2 — `git restore`, `git reset` (soft/mixed/hard), `git revert`, HEAD explained |
| The Basics of GitHub | Step 3 — account, create repo, remote, push, PAT auth, Windows Credential Manager, clone |
| Working with Branches | Step 4 — `git switch -c`, `git merge`, fast-forward, conflict resolution, branch naming |
| Forking and Contributing | Step 5 — fork, upstream remote, PR workflow, keeping fork in sync |
| Collaboration | Step 6 — PRs, protected branches, GitHub Issues, GitHub Flow, conflict resolution in PRs |

> **Next → 02 Docker** — the `hello-git` project is now version-controlled. The next module containerises it using Docker, so it runs identically on any machine. You will push the Docker image to Docker Hub the same way you pushed code to GitHub.
