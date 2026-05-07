# Module 01 — Git Basics

This module covers the everyday Git workflow — the commands you'll use dozens of times a day. By the end you'll be able to create repositories, track changes, and build a meaningful commit history.

---

## 1. Initialising a Repository

A **repository** (repo) is a directory that Git is tracking. There are two ways to get one.

### Option A — Start from scratch (`git init`)

```bash
mkdir my-project
cd my-project
git init
```

Output:
```
Initialized empty Git repository in /Users/you/my-project/.git/
```

Git creates a hidden `.git/` folder — this is the entire database of your project's history. **Never manually edit or delete this folder.**

```bash
ls -la
# .git/   ← Git's database lives here
```

### Option B — Clone an existing repository (`git clone`)

```bash
git clone https://github.com/username/repository-name.git
# or via SSH:
git clone git@github.com:username/repository-name.git
```

Clone downloads the entire repository history and creates a folder named after the repo.

```bash
# Clone into a custom folder name
git clone git@github.com:username/repo.git my-folder-name
```

---

## 2. Checking Status

`git status` is the most important command. Use it constantly.

```bash
git status
```

### Fresh repository (nothing yet)
```
On branch main

No commits yet

nothing to commit (create/copy files and use "git add" to track)
```

### After creating a file
```bash
echo "# My Project" > README.md
git status
```
```
On branch main

No commits yet

Untracked files:
  (use "git add <file>..." to include in what will be committed)
        README.md

nothing added to commit but untracked files present
```

**Untracked** means Git sees the file but isn't watching it yet.

### Short status

```bash
git status -s
# or
git status --short
```
```
?? README.md        ← untracked
M  app.js           ← modified, staged
 M style.css        ← modified, not staged
A  new-file.js      ← new file, staged
```

The two columns are: `[staging area][working directory]`

---

## 3. Staging Changes (`git add`)

Staging is how you select *which* changes go into the next commit.

```bash
# Stage a specific file
git add README.md

# Stage multiple files
git add README.md app.js

# Stage all changes in the current directory (and subdirectories)
git add .

# Stage all changes in the entire repo (from any location)
git add -A

# Stage parts of a file interactively (powerful!)
git add -p README.md
```

> **`git add .` vs `git add -A`:** In modern Git (v2+) they behave the same when run from the repo root. From a subdirectory, `git add .` only stages changes in that directory; `-A` stages everything.

### After staging
```
On branch main

No commits yet

Changes to be committed:
  (use "git rm --cached <file>..." to unstage)
        new file:   README.md
```

### Interactive staging (`-p`)

`git add -p` lets you stage individual *hunks* (chunks) within a file — useful when a file has two unrelated changes and you want separate commits.

```
Stage this hunk [y,n,q,a,d,/,e,?]?
  y = yes, stage this hunk
  n = no, skip this hunk
  s = split into smaller hunks
  e = manually edit the hunk
  q = quit
```

---

## 4. Making Commits (`git commit`)

A **commit** is a permanent snapshot of your staged changes.

```bash
git commit -m "Add README with project overview"
```

### The anatomy of a good commit message

```
<type>: <short summary in imperative mood, ≤72 chars>

[Optional body — explain WHAT and WHY, not HOW]
[Wrap at 72 characters]

[Optional footer — issue references, breaking changes]
```

**Common types (Conventional Commits):**
| Type | Use for |
|------|---------|
| `feat` | A new feature |
| `fix` | A bug fix |
| `docs` | Documentation only changes |
| `style` | Formatting, missing semicolons (no logic change) |
| `refactor` | Code change that isn't a fix or feature |
| `test` | Adding or fixing tests |
| `chore` | Build process, dependency updates |

**Good commit messages:**
```
feat: add user login form with email validation
fix: prevent crash when cart is empty at checkout
docs: update API endpoint documentation for v2
```

**Bad commit messages:**
```
fix stuff
WIP
changes
asdfgh
```

### Multi-line commit message (opens editor)

```bash
git commit
# Opens your configured editor
```

```
feat: add user authentication

Implement JWT-based authentication for the API. Users can now
register, log in, and receive a token valid for 24 hours.

Closes #42
```

### Shortcut: Stage and commit tracked files in one step

```bash
git commit -am "Fix typo in README"
```

> ⚠️ `-a` only stages **already-tracked** files. New (untracked) files still need `git add` first.

---

## 5. Viewing History (`git log`)

```bash
git log
```
```
commit a3f8c2d1e4b5f67890abcdef1234567890abcdef (HEAD -> main)
Author: Your Name <you@example.com>
Date:   Mon May 5 10:30:00 2025 +0530

    feat: add user login form with email validation

commit 9b2e1f3d4c5a6789012345678901234567890abc
Author: Your Name <you@example.com>
Date:   Sun May 4 16:15:00 2025 +0530

    docs: add README with project overview
```

### Useful `git log` options

```bash
# One line per commit
git log --oneline

# One line with branch graph
git log --oneline --graph --all

# Last N commits
git log -5

# Commits by a specific author
git log --author="Alice"

# Commits since/until a date
git log --since="2025-01-01"
git log --until="2025-03-31"

# Commits that changed a specific file
git log -- README.md

# Show patch (diff) for each commit
git log -p

# Show stats (files changed, insertions, deletions)
git log --stat
```

### A beautiful one-liner alias

```bash
git log --oneline --graph --decorate --all
```

You can create an alias for this:
```bash
git config --global alias.lg "log --oneline --graph --decorate --all"
# Now just type:
git lg
```

---

## 6. Viewing Differences (`git diff`)

```bash
# Show unstaged changes (working dir vs staging area)
git diff

# Show staged changes (staging area vs last commit)
git diff --staged
# (alias: git diff --cached)

# Show all changes since last commit
git diff HEAD

# Diff between two commits
git diff abc1234 def5678

# Diff between branches
git diff main feature-branch

# Diff for a specific file only
git diff README.md
```

### Reading a diff

```diff
diff --git a/app.js b/app.js
index 83b042c..9f1e23a 100644
--- a/app.js        ← old version
+++ b/app.js        ← new version
@@ -10,7 +10,8 @@   ← line numbers changed
 function greet() {
-  console.log("Hello");       ← removed (red)
+  console.log("Hello, World!"); ← added (green)
+  return true;
 }
```

---

## 7. The `.gitignore` File

Some files should never be committed — secrets, build artifacts, editor files, OS files.

Create a `.gitignore` file in the root of your repository:

```bash
touch .gitignore
```

### Common `.gitignore` patterns

```gitignore
# Dependencies
node_modules/
vendor/

# Build output
dist/
build/
*.o
*.pyc
__pycache__/

# Environment files (contain secrets!)
.env
.env.local
.env.*.local

# OS files
.DS_Store        # macOS
Thumbs.db        # Windows

# Editor files
.vscode/
.idea/
*.swp
*.swo

# Logs
*.log
logs/

# Test coverage
coverage/
```

### Pattern syntax

| Pattern | Matches |
|---------|---------|
| `*.log` | Any file ending in `.log` |
| `logs/` | The `logs` directory (anywhere) |
| `/logs/` | The `logs` directory at the repo root only |
| `!important.log` | Exception — don't ignore this file |
| `**/*.log` | `.log` files in any subdirectory |

### GitHub's `.gitignore` templates

GitHub maintains templates for every language and framework:
[https://github.com/github/gitignore](https://github.com/github/gitignore)

When creating a new repository on GitHub, you can auto-generate a `.gitignore` for your language.

> **Already committed a file you want to ignore?**
> Adding it to `.gitignore` won't remove it from the repository. You need to untrack it:
> ```bash
> git rm --cached filename
> git commit -m "Remove accidentally committed file"
> ```

---

## 8. Viewing a Specific Commit (`git show`)

```bash
# Show the latest commit
git show

# Show a specific commit
git show a3f8c2d

# Show a file as it was in a specific commit
git show a3f8c2d:src/app.js
```

---

## 9. Tagging Releases (`git tag`)

Tags mark specific points in history as important — typically version releases.

```bash
# Lightweight tag (just a pointer)
git tag v1.0.0

# Annotated tag (recommended — includes message, author, date)
git tag -a v1.0.0 -m "Release version 1.0.0"

# Tag a specific past commit
git tag -a v0.9.0 9b2e1f3 -m "Beta release"

# List all tags
git tag
git tag -l "v1.*"   # wildcard filter

# Push tags to remote (tags are NOT pushed by default)
git push origin v1.0.0
git push origin --tags    # push all tags
```

---

## 10. Practical Workflow Example

Here's the complete workflow from scratch:

```bash
# 1. Create and enter project
mkdir blog-app && cd blog-app

# 2. Initialise Git
git init

# 3. Create a .gitignore
echo "node_modules/\n.env\ndist/" > .gitignore

# 4. Create your first file
echo "# Blog App" > README.md

# 5. Check what Git sees
git status

# 6. Stage the files
git add .

# 7. Check what's staged
git status
git diff --staged

# 8. Commit
git commit -m "feat: initial project setup"

# 9. Create more files and repeat
echo "console.log('hello');" > index.js
git add index.js
git commit -m "feat: add entry point"

# 10. Review history
git log --oneline
```

---

## Common Mistakes

### "I forgot to add a file to my last commit"
```bash
git add forgotten-file.js
git commit --amend --no-edit
# Adds the file to the previous commit without changing the message
```

> ⚠️ Only amend commits that haven't been pushed to a shared remote.

### "I staged the wrong file"
```bash
git restore --staged wrong-file.js
# Unstages the file; your changes are still in the working directory
```

### "I want to discard my uncommitted changes"
```bash
git restore wrong-file.js
# ⚠️ This is permanent — the changes are gone
```

---

## Summary

| Command | What it does |
|---------|-------------|
| `git init` | Start a new repo |
| `git clone <url>` | Copy an existing repo |
| `git status` | See what has changed |
| `git add <file>` | Stage changes |
| `git add .` | Stage all changes |
| `git commit -m "msg"` | Save a snapshot |
| `git log --oneline` | See compact history |
| `git diff` | See unstaged changes |
| `git diff --staged` | See staged changes |
| `git show <hash>` | Inspect a commit |
| `git tag -a v1.0.0` | Mark a release |

**Next:** [02 — Undoing Things →](./02-undoing-things.md)
