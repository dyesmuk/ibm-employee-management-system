# Module 06 — Collaboration

Working with others introduces new challenges: staying in sync, avoiding stepping on each other's work, reviewing code effectively, and maintaining a healthy repository. This module covers team Git workflows end-to-end.

---

## 1. Team Workflow Overview

In a shared repository (where everyone has write access — as opposed to the fork model), the typical flow is:

```
1. Pull latest main
2. Create a feature branch
3. Commit on the branch
4. Push the branch to origin
5. Open a Pull Request
6. Code review
7. Merge to main
8. Pull and repeat
```

The difference from the fork model: everyone pushes branches to the **same** repository, rather than their own fork.

---

## 2. Repository Access and Permissions

### Inviting collaborators

Settings → Collaborators → Add people

| Role | Permissions |
|------|------------|
| **Read** | View and clone private repos |
| **Triage** | Manage issues and PRs (no code push) |
| **Write** | Push branches, manage issues/PRs |
| **Maintain** | Everything in Write + manage settings (no dangerous actions) |
| **Admin** | Full access including deletion and billing |

### Organisations and Teams

For companies and larger projects, use GitHub Organisations:
- Create **teams** (e.g., Frontend, Backend, DevOps)
- Assign teams to repositories with specific roles
- Manage access centrally as people join/leave

---

## 3. Keeping Up With a Moving Main Branch

On a busy team, `main` moves constantly. Your feature branch will get stale.

### Strategy A — Rebase onto main (recommended)

```bash
# Fetch latest
git fetch origin

# Rebase your branch onto the new main
git switch feature/user-dashboard
git rebase origin/main
```

Your commits are replayed on top of the latest `main`. Result: clean linear history, no merge commits.

### Strategy B — Merge main into your branch

```bash
git switch feature/user-dashboard
git merge origin/main
```

Creates a merge commit. History is less clean but shows exact timeline of events.

### Which to use?

| Situation | Recommendation |
|-----------|---------------|
| Solo feature branch, few commits | Rebase |
| Branch shared with others | Merge (rebasing rewrites shared history) |
| Long-running branch with many divergences | Merge (rebasing many commits gets complex) |

---

## 4. Handling Merge Conflicts in a Team Setting

Conflicts happen. They're not failures — they're a signal that two people edited related code.

### Prevention (reduce conflicts before they happen)

- **Pull before you start** — always begin from the latest `main`
- **Keep branches short-lived** — merge within days, not weeks
- **Communicate** — if Alice and Bob both need to touch `auth.js`, coordinate
- **Small, focused PRs** — large PRs conflict with everything

### Conflict resolution workflow

```bash
# 1. Fetch and rebase
git fetch origin
git rebase origin/main

# 2. Git pauses at each conflicting commit
# CONFLICT (content): Merge conflict in src/auth.js

# 3. Open the file and resolve
# Look for <<<<<<<, =======, >>>>>>>  markers

# 4. Stage the resolved file
git add src/auth.js

# 5. Continue the rebase
git rebase --continue

# 6. Repeat for each conflicting commit
# 7. Push (force-with-lease because rebase rewrote history)
git push --force-with-lease
```

### Tools for conflict resolution

**VS Code** — shows conflicts visually with "Accept Current", "Accept Incoming", "Accept Both" buttons.

**Command line:**
```bash
# Show conflicts only
git diff --name-only --diff-filter=U

# Use a 3-way merge tool
git mergetool
```

**Understanding the three versions in a conflict:**

```javascript
<<<<<<< HEAD (your version — current branch)
const timeout = 5000;
=======
const timeout = 3000;
>>>>>>> origin/main (incoming — what you're merging/rebasing onto)
```

- **Current (HEAD):** your changes on the feature branch
- **Incoming:** what's on the target branch (main)
- **Base:** the common ancestor (what the file looked like before both branches changed it)

---

## 5. Code Review Best Practices

### Requesting reviews

On GitHub, in a PR:
- **Reviewers** — assign specific people (required reviewers can be enforced)
- **Assignees** — who is responsible for the PR (usually the author)
- **Labels** — `ready for review`, `needs work`, `approved`, etc.

### Structuring a reviewable PR

Make reviewers' lives easy:
- **Small scope** — aim for 200–400 lines changed per PR
- **Clear description** — what, why, how to test
- **Self-review first** — go through your own diff before requesting review
- **Annotate complex changes** — add comments in the PR to explain non-obvious decisions

### Reviewing effectively

```
1. Understand the context first — read the issue, PR description
2. Read the diff at a high level — does the approach make sense?
3. Check for correctness — does the logic work for edge cases?
4. Check for tests — is the new behaviour tested?
5. Check for style — does it match the codebase conventions?
6. Leave specific, actionable comments
7. Approve or request changes
```

### GitHub review comment keyboard shortcuts

| Shortcut | Action |
|----------|--------|
| `b` | Toggle blame view |
| `e` | Collapse/expand file |
| `p` | Previous comment |
| `n` | Next comment |

---

## 6. GitHub Actions for Team CI/CD

GitHub Actions automates testing, building, and deployment on every push and PR.

### A basic CI workflow

Create `.github/workflows/ci.yml`:

```yaml
name: CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'

      - name: Install dependencies
        run: npm ci

      - name: Run linter
        run: npm run lint

      - name: Run tests
        run: npm test

      - name: Build
        run: npm run build
```

This workflow:
- Runs on every push to `main` and every PR targeting `main`
- Installs dependencies with `npm ci` (deterministic, respects `package-lock.json`)
- Lints, tests, and builds

### Requiring CI to pass before merging

Settings → Branches → Branch protection → **"Require status checks to pass before merging"** → select your workflow's job name.

Now PRs cannot be merged until CI is green.

---

## 7. Git Hooks for Local Automation

Git hooks are scripts that run automatically at key points in your Git workflow. They live in `.git/hooks/`.

### Useful hooks

**`pre-commit`** — runs before a commit is created:
```bash
#!/bin/sh
# Run linter before every commit
npm run lint
if [ $? -ne 0 ]; then
  echo "❌ Linting failed. Fix errors before committing."
  exit 1
fi
```

**`commit-msg`** — validates commit message format:
```bash
#!/bin/sh
# Ensure commit message follows Conventional Commits
msg=$(cat "$1")
if ! echo "$msg" | grep -qE "^(feat|fix|docs|style|refactor|test|chore)(\(.+\))?: .+"; then
  echo "❌ Commit message must follow: type(scope): description"
  echo "   Example: feat: add login form"
  exit 1
fi
```

**`pre-push`** — runs before a push:
```bash
#!/bin/sh
# Run full test suite before pushing
npm test
if [ $? -ne 0 ]; then
  echo "❌ Tests failed. Fix before pushing."
  exit 1
fi
```

### Make hooks executable

```bash
chmod +x .git/hooks/pre-commit
```

### Sharing hooks with your team (Husky)

`.git/hooks/` is not committed to the repository — hooks are local only. Use **Husky** to share hooks via `package.json`:

```bash
npm install --save-dev husky
npx husky init
```

This creates a `.husky/` directory that IS committed. Add your hooks there:

```bash
# .husky/pre-commit
npm run lint
```

---

## 8. Managing Long-Running Projects

### Semantic versioning

Tag releases with **SemVer** (`MAJOR.MINOR.PATCH`):

| Version bump | When |
|-------------|------|
| `PATCH` (1.0.**1**) | Backwards-compatible bug fix |
| `MINOR` (1.**1**.0) | Backwards-compatible new feature |
| `MAJOR` (**2**.0.0) | Breaking change |

```bash
git tag -a v1.2.3 -m "Release v1.2.3: fix payment crash"
git push origin v1.2.3
```

### GitHub Releases

GitHub Releases turn a tag into a published release with:
- Release notes / changelog
- Binary assets (compiled executables, etc.)
- Automatic zip of source code

Go to **Releases → Draft a new release** → choose a tag → write notes → publish.

### CHANGELOG.md

Maintain a `CHANGELOG.md` at the project root:

```markdown
# Changelog

All notable changes to this project will be documented in this file.
Format: [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)

## [Unreleased]

## [1.2.3] - 2025-05-05
### Fixed
- Prevent crash when payment provider times out (#156)

## [1.2.0] - 2025-04-20
### Added
- Dark mode support with system preference detection (#142)
- Export data as CSV from the dashboard (#139)

### Changed
- Dashboard now loads 40% faster after caching improvements

## [1.1.0] - 2025-03-15
### Added
- User profile editing
```

---

## 9. Working with Multiple Remotes

Teams sometimes work with more than one remote — for example, a fork workflow alongside the shared repo, or deploying to multiple servers.

```bash
# View all remotes
git remote -v

# Add a deployment remote (e.g., Heroku)
git remote add heroku https://git.heroku.com/your-app.git

# Push to Heroku for deployment
git push heroku main

# Fetch from upstream (the original project you forked)
git fetch upstream
git rebase upstream/main
```

---

## 10. Git Aliases for Team Productivity

Store these in your `~/.gitconfig`:

```ini
[alias]
    # Short status
    st = status -s

    # Compact log with graph
    lg = log --oneline --graph --decorate --all

    # Undo last commit (keep changes staged)
    undo = reset --soft HEAD~1

    # Show what you changed today
    today = log --since=midnight --author="$(git config user.name)" --oneline

    # List branches by most recently used
    recent = branch --sort=-committerdate

    # Amend last commit without editing message
    oops = commit --amend --no-edit

    # Show aliases
    aliases = config --get-regexp alias

    # Sync: pull with rebase + push
    sync = !git pull --rebase && git push
```

---

## 11. Common Team Scenarios

### "Someone pushed directly to main"

If your branch is protected, this shouldn't happen. If it did:

```bash
# Find the rogue commit
git log main --oneline -10

# Revert it
git revert <hash>
git push
```

### "Two people edited the same file"

This causes a conflict. See Section 4. It's normal — resolve it, don't panic.

### "I need to see what my teammate is working on"

```bash
# Fetch all remote branches
git fetch origin

# List remote branches
git branch -r

# Check out their branch locally (read-only inspection)
git switch --track origin/feature/their-branch
```

### "A commit broke main and needs to be reverted immediately"

```bash
git switch main
git pull

# Find the bad commit hash
git log --oneline -10

# Revert it (creates a new commit — safe for shared branch)
git revert <bad-commit-hash>
git push

# Notify the team immediately
```

### "We need to cherry-pick a fix from a feature branch to main"

```bash
# Find the specific commit hash
git log feature/new-payments --oneline

# Cherry-pick to main
git switch main
git cherry-pick <hash>
git push
```

---

## 12. Security Practices

### Never commit secrets

- API keys, passwords, tokens → **never** in Git
- Use `.env` files and add them to `.gitignore`
- Use GitHub Secrets for CI/CD credentials (Settings → Secrets and variables → Actions)

### If you accidentally commit a secret

```bash
# Remove it from history using git filter-repo
pip install git-filter-repo
git filter-repo --path config/secrets.json --invert-paths

# Force push (coordinate with team)
git push origin --force --all

# Rotate the compromised secret immediately
# (assume it's been scraped — bots watch GitHub in real-time)
```

### Sign your commits (optional but increasingly required)

```bash
# Generate a GPG key
gpg --full-generate-key

# Get the key ID
gpg --list-secret-keys --keyid-format LONG

# Configure Git to sign commits
git config --global user.signingkey YOUR_KEY_ID
git config --global commit.gpgsign true

# Add the public key to GitHub (Settings → SSH and GPG keys)
gpg --armor --export YOUR_KEY_ID
```

Signed commits show a **"Verified"** badge on GitHub.

---

## Summary

| Topic | Key Takeaway |
|-------|-------------|
| Permissions | Use the minimum role needed |
| Sync strategy | Rebase for solo branches; merge for shared ones |
| Conflict resolution | Fetch, rebase, resolve, continue |
| Code review | Small PRs, specific feedback, respond to all comments |
| CI/CD | Automate tests with GitHub Actions; require green CI to merge |
| Hooks | Use Husky to share pre-commit and commit-msg hooks |
| Secrets | Never commit them; rotate immediately if you do |
| Releases | Tag with SemVer; maintain a CHANGELOG |

---

## Full Course Recap

| Module | Core skill |
|--------|-----------|
| 00 — Welcome & Setup | Install Git, configure identity, set up GitHub + SSH |
| 01 — Git Basics | init, add, commit, log, diff, .gitignore |
| 02 — Undoing Things | restore, revert, reset, reflog, stash |
| 03 — GitHub Basics | remotes, push, pull, issues, README |
| 04 — Branches | create, merge, rebase, conflicts, cherry-pick |
| 05 — Forking & Contributing | fork, upstream, PRs, review workflow |
| 06 — Collaboration | team workflows, CI/CD, hooks, security |

You now have the full toolkit to work effectively with Git and GitHub — alone or on a team.
