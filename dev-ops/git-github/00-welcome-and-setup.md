# Module 00 — Welcome & Setup

Welcome to the **Git and GitHub** course. This course takes you from zero to confident — whether you've never typed a Git command or you've been winging it for years and want to finally understand what's happening under the hood.

---

## What You Will Learn

| Module | Topic |
|--------|-------|
| 00 | Welcome & Setup (this module) |
| 01 | Git Basics |
| 02 | Undoing Things |
| 03 | The Basics of GitHub |
| 04 | Working with Branches |
| 05 | Forking and Contributing |
| 06 | Collaboration |

---

## What is Version Control?

Imagine writing a report and saving copies every hour — `report_v1.docx`, `report_final.docx`, `report_FINAL_v2.docx`. You've been doing version control manually. Git formalises this idea and makes it powerful, fast, and collaborative.

**Version control** is a system that records changes to files over time so you can:
- See what changed, when, and who changed it
- Revert files or entire projects to a previous state
- Work on multiple ideas simultaneously without breaking anything
- Collaborate with others without overwriting each other's work

### Centralised vs. Distributed

| Type | How it works | Example |
|------|-------------|---------|
| Centralised | One server holds the history; clients check out snapshots | SVN, CVS |
| Distributed | Every clone is a full copy of the entire history | **Git**, Mercurial |

Git is **distributed**. You have the entire history locally. You can commit, branch, and review history without an internet connection. The remote (GitHub) is just a convenient shared copy.

---

## What is Git?

Git is a **free, open-source distributed version control system** created by Linus Torvalds in 2005 (to manage the Linux kernel after a dispute with the then-used tool BitKeeper).

Key characteristics:
- **Fast** — nearly all operations are local
- **Integrity** — everything is checksummed with SHA-1; nothing is lost silently
- **Non-destructive** — Git almost never deletes data; "undo" operations are safe
- **Branching is cheap** — creating a branch takes milliseconds

### What is GitHub?

GitHub is a **cloud-hosted platform** built on top of Git. It adds:
- A web interface for browsing repositories
- Pull Requests for code review
- Issues for bug tracking
- Actions for CI/CD automation
- Access control and team management

> **Git ≠ GitHub.** Git is the tool. GitHub is a website that hosts Git repositories and adds collaboration features. Alternatives include GitLab, Bitbucket, and Azure DevOps — they all speak Git.

---

## Installing Git

### macOS

macOS ships with an old Apple Git. Install the real thing via Homebrew:

```bash
# Install Homebrew first if you don't have it
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Then install Git
brew install git
```

Or install **Xcode Command Line Tools** (also installs Git):
```bash
xcode-select --install
```

### Windows

Download the installer from [https://git-scm.com/download/win](https://git-scm.com/download/win).

During installation, the important options are:
- **"Git from the command line and also from 3rd-party software"** — choose this
- **"Use Bundled OpenSSH"** — recommended
- **Default branch name:** change `master` → `main`

This also installs **Git Bash**, a terminal emulator that gives you a Unix-like shell on Windows.

### Linux (Debian/Ubuntu)

```bash
sudo apt update
sudo apt install git
```

### Verify the Installation

```bash
git --version
# git version 2.44.0  (or similar)
```

---

## First-Time Configuration

Before you make your first commit, tell Git who you are. This information is embedded in every commit you make.

```bash
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

### Set your default editor

Git opens an editor for commit messages. Set it to something comfortable:

```bash
# VS Code (recommended)
git config --global core.editor "code --wait"

# Nano (easy terminal editor)
git config --global core.editor "nano"

# Vim
git config --global core.editor "vim"
```

### Set the default branch name

GitHub uses `main` as the default branch name. Configure Git to match:

```bash
git config --global init.defaultBranch main
```

### Set up line ending handling

Different operating systems use different line endings. Configure Git to handle them consistently:

```bash
# macOS / Linux
git config --global core.autocrlf input

# Windows
git config --global core.autocrlf true
```

### Review your configuration

```bash
git config --list
# Shows all settings

git config --global --list
# Shows only global settings
```

Configuration is stored in `~/.gitconfig`. You can open and edit it directly:

```bash
cat ~/.gitconfig
```

```ini
[user]
    name = Your Name
    email = you@example.com
[core]
    editor = code --wait
    autocrlf = input
[init]
    defaultBranch = main
```

---

## Setting Up GitHub

1. Go to [https://github.com](https://github.com) and create a free account.
2. Choose a username carefully — it appears in every URL you share.
3. Verify your email address.

### Connecting Git to GitHub via SSH (Recommended)

SSH keys let you push to GitHub without typing your password every time.

**Step 1 — Generate an SSH key pair:**
```bash
ssh-keygen -t ed25519 -C "you@example.com"
# Press Enter to accept the default file location
# Optionally set a passphrase (recommended for security)
```

This creates two files:
- `~/.ssh/id_ed25519` — your **private** key (never share this)
- `~/.ssh/id_ed25519.pub` — your **public** key (safe to share)

**Step 2 — Add the key to the SSH agent:**
```bash
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519
```

**Step 3 — Copy your public key:**
```bash
# macOS
pbcopy < ~/.ssh/id_ed25519.pub

# Linux
cat ~/.ssh/id_ed25519.pub
# Then copy the output manually

# Windows (Git Bash)
clip < ~/.ssh/id_ed25519.pub
```

**Step 4 — Add to GitHub:**
- Go to GitHub → Settings → SSH and GPG keys → New SSH key
- Paste your public key, give it a name (e.g., "My MacBook"), save.

**Step 5 — Test the connection:**
```bash
ssh -T git@github.com
# Hi yourname! You've successfully authenticated...
```

---

## Recommended Tools

### Terminal
- **macOS:** Terminal, iTerm2, or VS Code integrated terminal
- **Windows:** Git Bash (installed with Git), Windows Terminal, or VS Code integrated terminal
- **Linux:** Your distro's default terminal

### GUI Clients (Optional)
You don't need a GUI — the command line is more powerful and universally applicable. But if you prefer visual tools:

| Tool | Platform | Cost |
|------|----------|------|
| GitHub Desktop | macOS, Windows | Free |
| GitKraken | All | Freemium |
| Sourcetree | macOS, Windows | Free |
| VS Code (built-in) | All | Free |

> **Recommendation:** Learn the command line first. GUI tools hide what's happening and make it harder to debug problems. Once you understand Git deeply, a GUI becomes a productivity tool rather than a crutch.

---

## How Git Stores Data

Most version control systems store data as **changes** (deltas) per file. Git is different — it stores **snapshots** of your entire project at each commit.

```
Delta-based (most VCS):
Version 1:  A.txt ──────────────
Version 2:          +change1 ───
Version 3:                   +change2

Git (snapshot-based):
Version 1:  [snapshot of all files]
Version 2:  [snapshot of all files]  ← unchanged files are just pointers
Version 3:  [snapshot of all files]
```

Git is efficient because unchanged files in a new snapshot are stored as a link to the previous version — not a new copy.

---

## The Three States

Every file in a Git project lives in one of three states:

```
Working Directory    Staging Area (Index)    Repository (.git)
─────────────────    ────────────────────    ─────────────────
  [modified]    ──→      [staged]       ──→    [committed]
                git add               git commit
```

| State | What it means |
|-------|--------------|
| **Modified** | You changed the file but haven't told Git about it yet |
| **Staged** | You've marked the change to go into the next commit |
| **Committed** | The change is safely stored in the Git database |

This three-stage model gives you fine-grained control — you can change ten files but commit only three of them in a logical group.

---

## Summary

- Git is a distributed version control system; GitHub is a cloud hosting platform built on Git
- Install Git, configure your name/email/editor, and create a GitHub account
- Set up SSH keys to authenticate with GitHub without passwords
- Git stores **snapshots**, not deltas
- Files move through three states: Modified → Staged → Committed

**Next:** [01 — Git Basics →](./01-git-basics.md)
