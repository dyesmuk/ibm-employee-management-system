# Module 03 — The Basics of GitHub

GitHub turns your local Git repository into a shared, collaborative project. This module covers creating repositories, pushing and pulling code, and navigating the GitHub interface.

---

## 1. GitHub's Core Concepts

| Term | Definition |
|------|-----------|
| **Repository** | A project — all files and their history |
| **Remote** | A version of your repository hosted on a server (GitHub) |
| **Origin** | The conventional name for your primary remote |
| **Push** | Send local commits to the remote |
| **Pull** | Fetch remote commits and merge them into your local branch |
| **Fetch** | Download remote commits without merging |
| **Clone** | Download a full copy of a remote repository |
| **Fork** | A personal copy of someone else's repository |
| **Pull Request (PR)** | A proposal to merge your changes into another branch |

---

## 2. Creating a Repository on GitHub

### Via GitHub's web interface

1. Click the **"+"** icon in the top-right → **"New repository"**
2. Fill in:
   - **Repository name** — use `kebab-case` (e.g., `my-blog-app`)
   - **Description** — optional but helpful
   - **Public / Private** — public repos are visible to everyone
   - **Initialize with README** — check this if starting from scratch
   - **Add .gitignore** — choose your language/framework template
   - **License** — MIT is common for open source
3. Click **"Create repository"**

### Via GitHub CLI (optional, fast)

```bash
# Install GitHub CLI: https://cli.github.com
gh repo create my-blog-app --public --clone
```

---

## 3. Connecting a Local Repo to GitHub (Remotes)

### Scenario A: You created the repo on GitHub first

```bash
git clone git@github.com:yourname/my-blog-app.git
cd my-blog-app
# Remote 'origin' is already configured
```

### Scenario B: You have a local repo and want to connect it to GitHub

```bash
# On GitHub, create an empty repo (no README, no .gitignore)
# Then in your local terminal:

git remote add origin git@github.com:yourname/my-blog-app.git
git branch -M main
git push -u origin main
```

### Managing remotes

```bash
# List all remotes
git remote -v
# origin  git@github.com:yourname/my-blog-app.git (fetch)
# origin  git@github.com:yourname/my-blog-app.git (push)

# Show details about a remote
git remote show origin

# Rename a remote
git remote rename origin upstream

# Remove a remote
git remote remove origin

# Change a remote's URL
git remote set-url origin git@github.com:yourname/new-name.git
```

---

## 4. Pushing Changes (`git push`)

Push sends your local commits to GitHub.

```bash
# First push (sets the upstream tracking branch)
git push -u origin main

# Subsequent pushes (Git remembers where to push)
git push

# Push a specific branch
git push origin feature-branch

# Push all branches
git push --all origin

# Push tags (not pushed by default)
git push --tags
```

### What `-u` does

`-u` (or `--set-upstream`) creates a permanent link between your local branch and the remote branch. After the first push with `-u`, you can just type `git push` and Git knows where to send it.

### Force push (use with caution)

```bash
git push --force-with-lease
```

`--force-with-lease` is safer than `--force` — it refuses to push if the remote has commits you don't have locally (i.e., someone else pushed in the meantime).

> ⚠️ **Never force push to shared branches** (`main`, `develop`). Force pushing rewrites history and causes problems for everyone who has pulled the branch.

---

## 5. Pulling Changes (`git pull`)

Pull downloads remote changes and integrates them into your local branch.

```bash
git pull
```

This is actually two commands in one:
1. `git fetch` — download changes
2. `git merge` — merge into your current branch

### Pull with rebase (cleaner history)

```bash
git pull --rebase
```

Instead of creating a merge commit, this replays your local commits on top of the fetched commits. Results in a linear history.

```bash
# Make it the default
git config --global pull.rebase true
```

### Fetch first, then decide

```bash
# Just download — don't merge yet
git fetch origin

# See what came in
git log HEAD..origin/main --oneline

# Decide how to integrate
git merge origin/main
# or
git rebase origin/main
```

---

## 6. Navigating the GitHub Interface

### Repository tabs

| Tab | What you'll find |
|-----|-----------------|
| **Code** | File browser, README, clone URL |
| **Issues** | Bug reports and feature requests |
| **Pull requests** | Proposed code changes awaiting review |
| **Actions** | CI/CD workflows and automation |
| **Projects** | Kanban-style project boards |
| **Security** | Vulnerability alerts, security policies |
| **Insights** | Contributor stats, traffic, dependency graph |
| **Settings** | Repo configuration, access, webhooks |

### The Code tab

- Browse files and directories
- Click a file to view its content
- Press `.` on any GitHub page to open the repo in a web-based VS Code editor
- Press `t` to activate the file finder
- The README.md at the root is automatically rendered below the file list

### Blame view

Click any file → click **"Blame"** to see which commit last modified each line, and who made it.

```bash
# Also works locally:
git blame README.md
```

---

## 7. GitHub Issues

Issues are GitHub's built-in task tracker. Use them for bug reports, feature requests, and discussions.

### Creating an issue

1. Go to **Issues** tab → **New issue**
2. Write a clear title and description
3. Add **labels** (bug, enhancement, documentation, etc.)
4. Assign to a team member
5. Link to a **milestone** (a collection of issues for a release)

### Issue templates

Create `.github/ISSUE_TEMPLATE/bug_report.md` to give contributors a structured form:

```markdown
---
name: Bug Report
about: Report a bug to help us improve
labels: bug
---

## Describe the bug
A clear and concise description of what the bug is.

## Steps to reproduce
1. Go to '...'
2. Click on '...'
3. See error

## Expected behaviour
What you expected to happen.

## Screenshots
If applicable, add screenshots.

## Environment
- OS: [e.g. macOS 14]
- Browser: [e.g. Chrome 120]
- Version: [e.g. v1.2.0]
```

### Closing issues from commits

Include special keywords in commit messages or PR descriptions to automatically close issues:

```
fix: prevent crash on empty cart

Fixes #42
```

Keywords: `closes`, `fixes`, `resolves` (all case-insensitive).

---

## 8. README Files

A good README is the front page of your project. It's rendered on the repository's home page.

### README structure

````markdown
# Project Name

One-line description of what this project does.

## Demo

[Live link](https://yourapp.com) | Screenshot here

## Features

- Feature 1
- Feature 2
- Feature 3

## Getting Started

### Prerequisites
- Node.js 18+
- npm 9+

### Installation

```bash
git clone https://github.com/yourname/project.git
cd project
npm install
npm run dev
```

## Usage

Explain how to use the project.

## Contributing

Pull requests are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[MIT](LICENSE)
````

### Badges (optional but professional)

```markdown
![Build](https://github.com/user/repo/actions/workflows/ci.yml/badge.svg)
![License](https://img.shields.io/github/license/user/repo)
![Version](https://img.shields.io/github/v/release/user/repo)
```

---

## 9. Repository Settings

Found under **Settings** tab.

### Key settings to know

| Setting | Where | What |
|---------|-------|------|
| Default branch | General | Change from `master` to `main` |
| Branch protection | Branches | Prevent direct pushes to `main` |
| Collaborators | Collaborators & teams | Invite contributors |
| Deploy keys | Deploy keys | SSH keys for CI/CD servers |
| Webhooks | Webhooks | Trigger external services on events |
| GitHub Pages | Pages | Host static site from your repo |
| Archive repository | Danger Zone | Make read-only |
| Delete repository | Danger Zone | Permanent deletion |

### Branch protection rules

For any important branch, enable protection rules under Settings → Branches:

- ✅ **Require a pull request before merging** — no direct pushes
- ✅ **Require approvals** — at least 1 reviewer must approve
- ✅ **Require status checks to pass** — tests must be green
- ✅ **Include administrators** — even admins must follow the rules

---

## 10. GitHub Notifications

By default, GitHub notifies you about activity on repos you watch and things that mention you.

### Notification settings

- **Watch** a repo to get all notifications
- **Unwatch** a busy repo and just get mentions
- Go to [github.com/notifications](https://github.com/notifications) to manage the firehose

### The `@mention` system

- `@username` — notify a specific person
- `@teamname` — notify everyone in a team
- Works in issues, PR comments, and commit messages

---

## 11. Full Push/Pull Workflow

```bash
# Start your day — pull the latest
git pull

# Make changes
echo "New feature code" >> app.js

# Stage and commit
git add app.js
git commit -m "feat: add dark mode toggle"

# Push to GitHub
git push

# --- Meanwhile, a teammate pushed changes ---

# Next morning — pull again before starting
git pull

# Conflict? Resolve it (see Module 06 — Collaboration)
```

---

## Summary

| Command | What it does |
|---------|-------------|
| `git remote add origin <url>` | Connect local repo to GitHub |
| `git remote -v` | List configured remotes |
| `git push -u origin main` | First push, set upstream |
| `git push` | Push commits to remote |
| `git pull` | Fetch + merge from remote |
| `git fetch` | Download without merging |

**Next:** [04 — Working with Branches →](./04-working-with-branches.md)
