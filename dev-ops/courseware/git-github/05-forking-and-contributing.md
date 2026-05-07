# Module 05 — Forking and Contributing

Forking is the foundation of open-source collaboration on GitHub. It lets you freely experiment with a project without affecting the original, and contribute back your changes through Pull Requests. This module covers the full fork-to-PR workflow.

---

## 1. What is a Fork?

A **fork** is a complete copy of someone else's repository, stored under your own GitHub account. Unlike cloning (which is just local), a fork exists on GitHub.

```
Original repo:          Your fork:               Your local machine:
github.com/org/repo ──► github.com/you/repo ──► /home/you/repo/
    (upstream)               (origin)
```

| Term | What it is |
|------|-----------|
| **upstream** | The original repository you forked from |
| **origin** | Your fork on GitHub |
| **local** | Your copy on your computer |

### Fork vs. Clone vs. Branch

| Action | When to use |
|--------|------------|
| **Fork** | Contributing to a project you don't have write access to |
| **Clone** | Working on a repo you already have access to |
| **Branch** | Working on a new feature in your own (or team's) repo |

---

## 2. Forking a Repository

### On GitHub

1. Navigate to the repository you want to fork
2. Click the **"Fork"** button (top right)
3. Choose your account (or an organisation) as the destination
4. GitHub creates `github.com/yourname/repo-name` instantly

### Clone your fork locally

```bash
# Clone your fork (origin)
git clone git@github.com:yourname/repo-name.git
cd repo-name
```

### Add the original repo as upstream

```bash
git remote add upstream git@github.com:original-org/repo-name.git

# Verify
git remote -v
# origin    git@github.com:yourname/repo-name.git (fetch)
# origin    git@github.com:yourname/repo-name.git (push)
# upstream  git@github.com:original-org/repo-name.git (fetch)
# upstream  git@github.com:original-org/repo-name.git (push)
```

This connection lets you pull in future changes from the original project.

---

## 3. Keeping Your Fork in Sync

The original project keeps moving after you fork it. Stay in sync:

```bash
# Fetch changes from upstream (doesn't modify your files)
git fetch upstream

# See what came in
git log HEAD..upstream/main --oneline

# Merge upstream changes into your local main
git switch main
git merge upstream/main

# Push the updated main to your fork on GitHub
git push origin main
```

### One-liner sync (if on main with no local changes)

```bash
git fetch upstream && git merge upstream/main && git push origin main
```

> **Do this before starting any new feature branch.** It ensures you're working from the latest code and minimises conflicts later.

---

## 4. Making a Contribution (End-to-End)

### Step 1 — Read the contribution guidelines

Almost every serious open-source project has a `CONTRIBUTING.md`. Read it before doing anything else. It tells you:
- How to set up the development environment
- Coding standards and linting rules
- How to run tests
- Branch naming conventions
- What kinds of PRs are welcome

```bash
cat CONTRIBUTING.md
```

### Step 2 — Find or create an issue

- Look for issues labelled `good first issue` or `help wanted`
- Comment on the issue: "I'd like to work on this" — avoid duplicating effort
- If reporting a new bug, open an issue before submitting a fix

### Step 3 — Sync your fork and create a branch

```bash
# Sync first
git fetch upstream
git switch main
git merge upstream/main

# Create a feature branch
git switch -c fix/correct-login-redirect
```

### Step 4 — Make your changes

```bash
# Edit files...
git add .
git commit -m "fix: redirect to dashboard after login instead of home"
```

Keep commits focused and logical. One concern per commit.

### Step 5 — Push to your fork

```bash
git push -u origin fix/correct-login-redirect
```

### Step 6 — Open a Pull Request

1. Go to your fork on GitHub
2. GitHub shows a banner: **"Compare & pull request"** — click it
3. Ensure:
   - **base repository:** `original-org/repo-name` | **base:** `main`
   - **head repository:** `yourname/repo-name` | **compare:** `fix/correct-login-redirect`
4. Write a clear PR description (see below)
5. Click **"Create pull request"**

---

## 5. Writing a Great Pull Request

A well-written PR is the difference between a quick merge and a weeks-long back-and-forth.

### PR title

Follow the same convention as commit messages:
```
fix: redirect to dashboard after login instead of home page
feat: add dark mode support with system preference detection
```

### PR description template

```markdown
## What does this PR do?
Fixes the login redirect so users land on the dashboard (not the
home page) after successful authentication.

## Why?
The current behaviour causes confusion — users log in and see
marketing content instead of their personalised dashboard.

## How was it tested?
- Tested manually: logged in as admin, standard user, and guest
- Existing auth tests pass: `npm test`
- Added a new test: `login redirects to /dashboard`

## Screenshots (if UI change)
Before | After
--- | ---
![before](url) | ![after](url)

## Related issues
Closes #87
```

### Checklist (add to your PR template)

```markdown
- [ ] Tests pass locally
- [ ] I've added tests for new functionality
- [ ] Documentation updated if needed
- [ ] No unrelated changes in this PR
- [ ] Commit messages follow the project convention
```

---

## 6. The Pull Request Review Process

### As the author

- **Respond to every comment** — even if just "Done" or "Good point, but I disagree because..."
- **Push new commits to the same branch** — the PR updates automatically
- **Don't force push after review has started** — reviewers lose context
- Mark resolved comments as **"Resolved"**

### As a reviewer

- **Review the purpose first** — does this PR solve the stated problem?
- **Be kind and specific** — "This could be refactored to X because Y" not "This is wrong"
- Use GitHub's suggestion feature for small fixes:

```suggestion
const user = await getUser(id);
```

- Approve when satisfied; request changes if something must be fixed first

### Review comment types

| Severity | Prefix | Meaning |
|----------|--------|---------|
| Must fix | `[blocking]` | PR cannot merge without this |
| Should fix | `[nit]` | Small improvement, author's call |
| Question | `[question]` | Seeking understanding, not a change |
| Optional | `[optional]` | Nice to have, truly optional |

---

## 7. Updating a PR After Review

```bash
# Make the requested changes
git add .
git commit -m "refactor: extract auth helper per review feedback"

# Push — the PR updates automatically
git push
```

### If you need to rebase before merging

```bash
# Sync with upstream main
git fetch upstream
git rebase upstream/main

# If conflicts occur, resolve them, then:
git rebase --continue

# Push (force required after rebase)
git push --force-with-lease
```

---

## 8. After the PR is Merged

```bash
# Switch back to main
git switch main

# Pull the merged changes
git pull upstream main

# Update your fork
git push origin main

# Delete the feature branch (locally and remotely)
git branch -d fix/correct-login-redirect
git push origin --delete fix/correct-login-redirect
```

---

## 9. Creating a PR Template for Your Project

Add a file at `.github/pull_request_template.md`:

```markdown
## Summary
<!-- What does this PR do? -->

## Type of change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## How to test
<!-- Steps to verify the change works -->

## Checklist
- [ ] Tests pass (`npm test`)
- [ ] Linting passes (`npm run lint`)
- [ ] Self-reviewed my own code
- [ ] Added/updated tests
- [ ] Updated documentation

## Related issues
<!-- Closes #issue_number -->
```

GitHub automatically pre-fills the PR description box with this template.

---

## 10. GitHub's Draft Pull Requests

Open a PR as a **draft** to signal it's a work-in-progress — not ready for review, but visible for early feedback.

```
Click "Create pull request" dropdown → "Create draft pull request"
```

A draft PR:
- Cannot be merged
- Won't trigger required reviewers
- Appears with a grey "Draft" badge
- Useful for sharing work-in-progress and running CI

When ready: **"Ready for review"** button converts it to a regular PR.

---

## 11. Squash and Merge vs. Merge Commit vs. Rebase

GitHub gives three merge options, configurable per-repo under Settings → Pull Requests:

### Merge commit (default)
```
main: ──A──B──C──────────M
feature:          D──E──F─┘
```
- Preserves all commits and branch context
- History can get noisy on large teams

### Squash and merge
```
main: ──A──B──C──S
```
- All feature commits squashed into one commit on main
- Clean linear main history
- Loses individual commit context
- Best for: small features, tidying up "WIP" commit histories

### Rebase and merge
```
main: ──A──B──C──D'──E'──F'
```
- Replays commits individually on top of main
- Linear history, individual commits preserved
- Best for: teams that want full history without merge commits

> **Common convention:** Use "Squash and merge" for most feature branches, "Rebase and merge" for large features with meaningful commit histories.

---

## 12. Contributing to Open Source: Etiquette

- **Start small.** Fix a typo, improve docs. Learn the project culture.
- **One PR, one concern.** Don't fix three unrelated bugs in one PR.
- **Be patient.** Maintainers are often volunteers. A week response time is normal.
- **Don't ping repeatedly.** One polite bump after 2 weeks is fine.
- **Accept rejection gracefully.** Not every change is needed. Learn and move on.
- **Credit others.** Use `Co-authored-by` for pair-programmed work:

```
Co-authored-by: Alice <alice@example.com>
```

---

## Summary

| Action | Command / Location |
|--------|-------------------|
| Fork a repo | GitHub → "Fork" button |
| Add upstream remote | `git remote add upstream <url>` |
| Sync fork with upstream | `git fetch upstream && git merge upstream/main` |
| Create feature branch | `git switch -c feature/description` |
| Push to fork | `git push -u origin feature/description` |
| Open a PR | GitHub → "Compare & pull request" |
| Update PR after review | Push new commits to same branch |
| Clean up after merge | Delete branch locally and on GitHub |

**Next:** [06 — Collaboration →](./06-collaboration.md)
