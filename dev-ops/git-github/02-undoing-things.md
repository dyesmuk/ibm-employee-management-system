# Module 02 — Undoing Things

One of Git's most valuable features is the ability to undo almost anything. This module covers the full spectrum of "going back" — from simple staged-file removal to rewriting history.

> **Safety principle:** Git almost never permanently deletes data. Even "destructive" operations leave data in the reflog for 30–90 days.

---

## The Undo Landscape

```
Working Directory    Staging Area       Repository (commits)
─────────────────    ────────────       ────────────────────
  git restore          git restore        git commit --amend
                        --staged          git revert
                                          git reset
                                          git reflog
```

---

## 1. Discarding Working Directory Changes (`git restore`)

You've modified a file but haven't staged it yet, and you want to throw away those changes.

```bash
git restore README.md
```

> ⚠️ **This is irreversible.** The changes are gone — not staged, not in the reflog. Be sure before you run this.

```bash
# Discard all unstaged changes in the entire repo
git restore .

# Old syntax (still works, still seen in docs)
git checkout -- README.md
```

---

## 2. Unstaging Files (`git restore --staged`)

You've staged a file with `git add` but changed your mind about including it in the next commit.

```bash
git restore --staged README.md
```

The file stays modified in your working directory — only the staging is undone.

```bash
# Unstage everything
git restore --staged .

# Old syntax (equivalent)
git reset HEAD README.md
```

---

## 3. Amending the Last Commit (`git commit --amend`)

Use `--amend` to fix the most recent commit — before pushing it.

### Fix a typo in the commit message

```bash
git commit --amend -m "feat: add login form with email validation"
```

### Add a forgotten file to the last commit

```bash
git add forgotten-file.js
git commit --amend --no-edit   # --no-edit keeps the original message
```

### What `--amend` actually does

`--amend` **replaces** the last commit with a new one. The old commit is abandoned (though it stays in the reflog for a while). This is why:

> ⚠️ **Never amend a commit that has already been pushed to a shared remote.** Everyone who pulled that commit will have a different history, causing conflicts.

---

## 4. Reverting a Commit (`git revert`)

`git revert` is the **safe** way to undo a published commit. It doesn't delete history — it creates a new commit that applies the inverse of the target commit.

```bash
# Revert the latest commit
git revert HEAD

# Revert a specific commit
git revert a3f8c2d

# Revert without immediately committing (lets you inspect first)
git revert --no-commit a3f8c2d
```

### When to use `git revert`
- The commit has already been pushed to a shared branch
- You want a clear audit trail ("we intentionally reverted X")
- Working on `main` or any shared branch

### Example

```bash
# History:
# a3f8c2d  feat: add payment module   ← introduced a critical bug
# 9b2e1f3  docs: update README
# 7c1d4e5  feat: add login

git revert a3f8c2d
# Creates a new commit:
# f5d2a1b  Revert "feat: add payment module"
```

The payment module code is removed, but the history shows *both* commits — the original and the revert.

---

## 5. Resetting (`git reset`)

`git reset` moves the `HEAD` pointer (and optionally the branch pointer) to a different commit. It has three modes that affect what happens to your files.

### The three modes

```
         HEAD moves to <commit>
         ┌──────────────────┐
         ▼                  │
[older]──[target]──[c1]──[c2]──[HEAD]
                    ↑          ↑ before reset
                    └── after reset (HEAD now here)
```

| Mode | Staging area | Working directory |
|------|-------------|-------------------|
| `--soft` | Unchanged (changes are staged) | Unchanged |
| `--mixed` (default) | Reset to match target commit | Unchanged |
| `--hard` | Reset to match target commit | Reset to match target commit |

### `--soft` — "uncommit but keep staged"

```bash
git reset --soft HEAD~1
```

Moves HEAD back one commit. Your changes stay staged. Use this when you want to redo a commit with different content or a different message.

```bash
# Squash the last 3 commits into one
git reset --soft HEAD~3
git commit -m "feat: complete user authentication module"
```

### `--mixed` — "uncommit and unstage" (default)

```bash
git reset HEAD~1
# or explicitly:
git reset --mixed HEAD~1
```

Moves HEAD back. Changes are in your working directory but unstaged. Use this to "undo" a commit and start over with the changes.

### `--hard` — "throw everything away"

```bash
git reset --hard HEAD~1
```

Moves HEAD back AND discards all changes. The working directory and staging area are reset to exactly match the target commit.

> ⚠️ **`--hard` is the most dangerous Git command.** Changes are not staged or committed — they're gone. The reflog can recover commits, but not uncommitted work.

```bash
# Discard all local changes and reset to match the remote
git reset --hard origin/main
```

### Notation reference

| Notation | Means |
|----------|-------|
| `HEAD` | The current commit |
| `HEAD~1` or `HEAD~` | One commit before HEAD |
| `HEAD~3` | Three commits before HEAD |
| `HEAD^` | The first parent of HEAD (same as `~1` for linear history) |
| `a3f8c2d` | A specific commit by its hash |

---

## 6. The Reflog — Your Safety Net (`git reflog`)

The **reflog** (reference log) records every position HEAD has been at, including commits you've "lost" with reset. It's Git's secret undo history.

```bash
git reflog
```

```
a3f8c2d (HEAD -> main) HEAD@{0}: commit: feat: add user auth
9b2e1f3 HEAD@{1}: reset: moving to HEAD~1
7c1d4e5 HEAD@{2}: commit: docs: update README
f2a3b4c HEAD@{3}: commit: feat: add login form
```

### Recovering a "lost" commit after `--hard` reset

```bash
# Oh no — I reset --hard and lost my work!
git reset --hard HEAD~3

# Use reflog to find the lost commit
git reflog
# HEAD@{1}: commit: feat: that important work I lost

# Recover it
git reset --hard HEAD@{1}
# or
git checkout -b recovery-branch HEAD@{1}
```

> The reflog is **local only** and expires after 90 days (by default). It's not pushed to GitHub.

---

## 7. Cleaning Untracked Files (`git clean`)

Remove files that Git doesn't track at all — build artifacts, generated files, etc.

```bash
# Preview what would be removed (dry run — always do this first)
git clean -n

# Remove untracked files
git clean -f

# Remove untracked files AND directories
git clean -fd

# Remove ignored files too (use with extreme caution)
git clean -fdx
```

> ⚠️ `git clean` cannot be undone with the reflog — these files were never tracked by Git.

---

## 8. Recovering Specific File Versions

### Restore a file to how it was in the last commit

```bash
git restore README.md
```

### Restore a file to how it was in a specific commit

```bash
git restore --source=a3f8c2d README.md
# The file is now in working directory as it was in that commit
# It's unstaged — you can review and then add+commit
```

### Retrieve a deleted file

```bash
# Find the commit that deleted the file
git log --all --full-history -- deleted-file.js

# Restore it from the commit just before deletion
git restore --source=a3f8c2d~1 deleted-file.js
```

---

## 9. Stashing (`git stash`)

Stash saves your current uncommitted work temporarily, leaving you a clean working directory. Perfect when you need to quickly switch context.

```bash
# Stash current changes
git stash

# Stash with a descriptive message
git stash push -m "WIP: refactoring auth module"

# List all stashes
git stash list
```
```
stash@{0}: WIP on main: a3f8c2d feat: add user auth
stash@{1}: On feature-branch: WIP: refactoring auth module
```

```bash
# Apply the most recent stash (keeps it in the stash list)
git stash apply

# Apply a specific stash
git stash apply stash@{1}

# Apply AND remove from stash list
git stash pop

# Remove a specific stash
git stash drop stash@{1}

# Remove all stashes
git stash clear

# Create a branch from a stash
git stash branch new-feature stash@{0}
```

### Stash untracked files too

```bash
git stash -u          # include untracked files
git stash -a          # include untracked AND ignored files
```

### Practical stash workflow

```bash
# You're mid-feature and a critical bug report comes in
git stash push -m "WIP: user dashboard feature"

# Switch to main, fix the bug
git checkout main
git checkout -b hotfix/critical-bug
# ... fix the bug ...
git commit -m "fix: prevent null pointer in payment processing"
git checkout main
git merge hotfix/critical-bug

# Return to your feature
git checkout feature/user-dashboard
git stash pop
# Continue where you left off
```

---

## 10. Decision Tree: Which Undo Command?

```
Did I commit the change?
├── NO
│   ├── Is it staged?
│   │   ├── YES → git restore --staged <file>
│   │   └── NO  → git restore <file>  ⚠️ irreversible
│   └── Is it an untracked file?
│           └── YES → git clean -f  ⚠️ irreversible
└── YES
    ├── Was it pushed to shared remote?
    │   ├── YES → git revert <commit>  ✅ safe
    │   └── NO
    │       ├── Just the commit message → git commit --amend
    │       ├── Keep changes staged    → git reset --soft HEAD~1
    │       ├── Keep changes unstaged  → git reset HEAD~1
    │       └── Discard changes too   → git reset --hard HEAD~1  ⚠️
    └── I lost something with reset --hard
            └── git reflog  →  git reset --hard HEAD@{N}
```

---

## Summary

| Command | What it does | Safe to push? |
|---------|-------------|---------------|
| `git restore <file>` | Discard working dir changes | N/A |
| `git restore --staged <file>` | Unstage a file | N/A |
| `git commit --amend` | Fix last commit | ❌ (if already pushed) |
| `git revert <commit>` | Undo via new commit | ✅ always safe |
| `git reset --soft HEAD~1` | Uncommit, keep staged | ❌ (if already pushed) |
| `git reset HEAD~1` | Uncommit, keep unstaged | ❌ (if already pushed) |
| `git reset --hard HEAD~1` | Uncommit + discard changes | ❌ |
| `git reflog` | View all HEAD positions | N/A |
| `git stash` | Temporarily save work | N/A |
| `git clean -fd` | Delete untracked files | N/A |

**Next:** [03 — The Basics of GitHub →](./03-github-basics.md)
