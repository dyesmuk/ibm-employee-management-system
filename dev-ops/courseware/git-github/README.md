# Git and GitHub — Course Materials

A practical, hands-on courseware covering Git and GitHub from setup through team collaboration.

---

## Modules

| # | Module | Topics Covered |
|---|--------|---------------|
| [00](./00-welcome-and-setup.md) | Welcome & Setup | What is Git, installation, first-time config, SSH keys, how Git stores data |
| [01](./01-git-basics.md) | Git Basics | init, clone, status, add, commit, log, diff, .gitignore, tags |
| [02](./02-undoing-things.md) | Undoing Things | restore, amend, revert, reset (soft/mixed/hard), reflog, stash, clean |
| [03](./03-github-basics.md) | The Basics of GitHub | Remotes, push, pull, fetch, GitHub UI, issues, README, settings |
| [04](./04-working-with-branches.md) | Working with Branches | Create, switch, merge, rebase, interactive rebase, conflicts, cherry-pick, branching strategies |
| [05](./05-forking-and-contributing.md) | Forking and Contributing | Fork workflow, upstream sync, PRs, review process, PR templates, squash vs merge |
| [06](./06-collaboration.md) | Collaboration | Team workflows, permissions, CI with GitHub Actions, Git hooks, Husky, security, releases |

---

## Prerequisites

- A computer running macOS, Windows, or Linux
- Basic comfort with a terminal / command line
- A free GitHub account ([github.com](https://github.com))

---

## How to Use This Course

Each module builds on the previous one. Work through them in order.
Every module includes:
- Conceptual explanations with diagrams
- Real command examples with expected output
- Common mistakes and how to avoid them
- A summary table for quick reference

---

## Quick Reference Card

```bash
# Daily workflow
git pull                          # start: get latest
git switch -c feature/my-thing    # create a branch
git add .                         # stage changes
git commit -m "feat: my change"   # commit
git push -u origin feature/my-thing  # push

# Inspection
git status                        # what changed?
git log --oneline --graph --all   # history overview
git diff                          # unstaged changes
git diff --staged                 # staged changes

# Undoing
git restore <file>                # discard working dir change
git restore --staged <file>       # unstage
git commit --amend --no-edit      # fix last commit
git revert HEAD                   # undo last commit (safe)
git stash / git stash pop         # temporarily shelve work

# Branches
git branch -a                     # list all branches
git switch -c new-branch          # create + switch
git merge feature-branch          # merge into current
git branch -d merged-branch       # clean up
```
