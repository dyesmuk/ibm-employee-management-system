# Module 04 — Working with Branches

Branching is Git's killer feature. It lets you work on multiple things simultaneously without them interfering with each other. This module covers everything from creating branches to merging strategies and handling conflicts.

---

## 1. What is a Branch?

A branch is a lightweight, movable pointer to a commit. When you commit, the branch pointer advances to the new commit.

```
main: ──A──B──C──D (HEAD → main)

After branching:
main:    ──A──B──C──D
feature:             └──E──F (HEAD → feature)
```

Creating a branch in Git is instantaneous — it's just creating a 41-byte file containing a commit hash. It has virtually no overhead.

---

## 2. Basic Branch Commands

### List branches

```bash
# List local branches (* marks the current branch)
git branch

# List remote branches
git branch -r

# List all branches (local + remote)
git branch -a

# List with last commit info
git branch -v
```

### Create a branch

```bash
# Create a branch (doesn't switch to it)
git branch feature/login-form

# Create AND switch to it (the modern way)
git switch -c feature/login-form

# Old syntax (still works, widely used)
git checkout -b feature/login-form
```

### Switch branches

```bash
# Modern syntax
git switch feature/login-form

# Old syntax
git checkout feature/login-form

# Switch back to the previous branch
git switch -
```

### Delete a branch

```bash
# Delete a merged branch (safe)
git branch -d feature/login-form

# Force delete an unmerged branch
git branch -D feature/login-form

# Delete a remote branch
git push origin --delete feature/login-form
```

---

## 3. Branch Naming Conventions

Good branch names communicate purpose at a glance.

| Pattern | Example | Used for |
|---------|---------|---------|
| `feature/description` | `feature/user-authentication` | New features |
| `fix/description` | `fix/login-null-pointer` | Bug fixes |
| `hotfix/description` | `hotfix/payment-crash` | Urgent production fixes |
| `refactor/description` | `refactor/auth-module` | Code refactoring |
| `docs/description` | `docs/api-endpoints` | Documentation |
| `chore/description` | `chore/update-dependencies` | Maintenance tasks |
| `release/version` | `release/v2.1.0` | Release preparation |

---

## 4. The Typical Branch Workflow

```bash
# 1. Start from an up-to-date main
git switch main
git pull

# 2. Create a feature branch
git switch -c feature/user-dashboard

# 3. Do your work
# edit files...
git add .
git commit -m "feat: add user dashboard skeleton"

# edit more files...
git add .
git commit -m "feat: implement stats widgets"

# 4. Push the branch to GitHub
git push -u origin feature/user-dashboard

# 5. Open a Pull Request on GitHub
# (see Module 05 — Forking and Contributing)

# 6. After PR is merged, clean up
git switch main
git pull
git branch -d feature/user-dashboard
```

---

## 5. Merging (`git merge`)

Merging integrates changes from one branch into another.

### Fast-forward merge

When the target branch has no new commits since the feature branch was created, Git simply moves the pointer forward — no merge commit needed.

```
Before:
main:    ──A──B──C
feature:          └──D──E

After (git merge feature from main):
main:    ──A──B──C──D──E  (pointer just moved forward)
```

```bash
git switch main
git merge feature/login-form
# Fast-forward (no merge commit)
```

### Three-way merge (merge commit)

When both branches have diverged, Git finds their common ancestor and creates a new "merge commit."

```
Before:
main:    ──A──B──C──F
feature:          └──D──E

After:
main:    ──A──B──C──F──M  (M is the merge commit)
feature:          └──D──E─┘
```

```bash
git switch main
git merge feature/login-form
# Merge made by the 'ort' strategy.
```

### Controlling merge behaviour

```bash
# Always create a merge commit (even for fast-forward)
git merge --no-ff feature/login-form

# Fast-forward only (fail if a merge commit would be needed)
git merge --ff-only feature/login-form

# Preview what would be merged (dry run)
git merge --no-commit --no-ff feature/login-form
git merge --abort   # cancel the preview
```

> **Team convention:** Most teams use `--no-ff` to keep branch history visible. GitHub's "Merge pull request" button does this by default.

---

## 6. Rebasing (`git rebase`)

Rebasing moves (replays) your commits onto a different base commit, resulting in a linear history.

```
Before:
main:    ──A──B──C──F
feature:          └──D──E

After (git rebase main from feature):
main:    ──A──B──C──F
feature:              └──D'──E'  (replayed on top of F)
```

```bash
git switch feature/login-form
git rebase main
```

### Interactive rebase (`git rebase -i`)

Interactive rebase lets you rewrite, reorder, squash, or drop commits — your most powerful history-editing tool.

```bash
# Edit the last 4 commits
git rebase -i HEAD~4
```

This opens your editor with something like:

```
pick a3f8c2d feat: add login form HTML
pick 9b2e1f3 fix typo
pick 7c1d4e5 fix another typo
pick f2a3b4c feat: add form validation

# Commands:
# pick   = use commit as-is
# reword = use commit, but edit the message
# edit   = use commit, but stop for amending
# squash = melt into previous commit (keep message)
# fixup  = melt into previous commit (discard message)
# drop   = remove commit
```

**Squash the typo fixes into the first commit:**

```
pick a3f8c2d feat: add login form HTML
fixup 9b2e1f3 fix typo
fixup 7c1d4e5 fix another typo
pick f2a3b4c feat: add form validation
```

Save and close → Git replays and squashes. Two clean commits remain.

### Rebase vs. Merge: When to use which?

| Scenario | Use |
|---------|-----|
| Updating a feature branch with main | `rebase` — keeps history linear |
| Integrating a completed feature into main | `merge --no-ff` — preserves branch context |
| Cleaning up commits before a PR | `rebase -i` — squash, reorder, fix messages |
| Shared branch (others are using it) | **Never rebase** — use merge |

> **The golden rule of rebasing:** Never rebase commits that have been pushed to a shared remote branch.

---

## 7. Merge Conflicts

A conflict occurs when two branches modify the same part of a file differently. Git can't auto-resolve it and asks you to decide.

### What a conflict looks like

```bash
git merge feature/login-form
# Auto-merging src/App.js
# CONFLICT (content): Merge conflict in src/App.js
# Automatic merge failed; fix conflicts and then commit the result.
```

Open `src/App.js`:

```javascript
function getTitle() {
<<<<<<< HEAD
  return "My App";           // ← your version (current branch)
=======
  return "Awesome App";      // ← incoming version (feature branch)
>>>>>>> feature/login-form
}
```

### Resolving conflicts

**Step 1 — Find all conflicted files:**
```bash
git status
# both modified:   src/App.js
```

**Step 2 — Open and edit each conflicted file.** Remove the conflict markers and keep what you want:

```javascript
function getTitle() {
  return "Awesome App";    // chose the feature branch version
}
```

Or combine both:
```javascript
function getTitle(short = false) {
  return short ? "My App" : "Awesome App";
}
```

**Step 3 — Mark as resolved:**
```bash
git add src/App.js
```

**Step 4 — Complete the merge:**
```bash
git commit -m "merge: integrate feature/login-form into main"
```

### Aborting a merge

Changed your mind mid-conflict?
```bash
git merge --abort
```

### Using a merge tool

```bash
# Configure a visual tool (VS Code works well)
git config --global merge.tool vscode
git config --global mergetool.vscode.cmd 'code --wait $MERGED'

# Launch it during a conflict
git mergetool
```

---

## 8. Cherry-Picking (`git cherry-pick`)

Apply a specific commit from one branch to another — without merging the whole branch.

```bash
# Apply commit a3f8c2d to the current branch
git cherry-pick a3f8c2d

# Cherry-pick a range of commits
git cherry-pick a3f8c2d..f2a3b4c

# Cherry-pick without committing (review before finalising)
git cherry-pick --no-commit a3f8c2d
```

**Typical use case:** A fix was committed to `feature-branch`. You need it on `main` urgently before the whole feature is ready.

```bash
git switch main
git cherry-pick a3f8c2d  # the specific fix commit
```

---

## 9. Comparing Branches

```bash
# Commits in feature-branch NOT in main
git log main..feature-branch --oneline

# Commits in main NOT in feature-branch (what you'd be rebasing onto)
git log feature-branch..main --oneline

# Files that differ between branches
git diff main..feature-branch --name-only

# Full diff between branches
git diff main..feature-branch
```

---

## 10. Branching Strategies

### GitHub Flow (simple, recommended for most teams)

```
main ────────────────────────────────────► (always deployable)
         ↑ PR merge              ↑ PR merge
feature-1 ────────────
                    feature-2 ──────────────
```

1. `main` is always deployable
2. Branch off `main` for every feature/fix
3. Open a PR when ready
4. Review and merge to `main`
5. Deploy immediately after merge

### Git Flow (complex projects with scheduled releases)

```
main         ──────────────────────────── (production releases)
develop      ────────────────────────────
feature/X    ──────────
feature/Y           ──────────
release/1.0                    ────────
hotfix/crash                           ────
```

Branches:
- `main` — production-ready
- `develop` — integration branch
- `feature/*` — individual features (branch from `develop`)
- `release/*` — release preparation (branch from `develop`)
- `hotfix/*` — emergency fixes (branch from `main`)

> For most modern teams deploying continuously, **GitHub Flow is simpler and better.** Git Flow adds process for teams that need it (e.g., release cycles, multiple supported versions).

---

## Summary

| Command | What it does |
|---------|-------------|
| `git branch` | List local branches |
| `git switch -c <name>` | Create and switch to branch |
| `git switch <name>` | Switch branch |
| `git branch -d <name>` | Delete merged branch |
| `git merge <name>` | Merge branch into current |
| `git rebase main` | Replay commits on top of main |
| `git rebase -i HEAD~N` | Interactive rebase (squash, reorder) |
| `git cherry-pick <hash>` | Apply a specific commit |
| `git merge --abort` | Cancel a conflicted merge |

**Next:** [05 — Forking and Contributing →](./05-forking-and-contributing.md)
