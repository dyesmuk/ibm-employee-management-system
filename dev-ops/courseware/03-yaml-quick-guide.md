# 03 — YAML Quick Reference Guide

> **Series:** DevOps Hands-On | **Module:** 3 of 6 | **Type:** Reference Guide

## About This Guide

This is the third module in the DevOps series — and the shortest. It is a focused reference, not a project-based walkthrough.

**Why YAML gets its own module:**

Every tool from this point forward — Kubernetes, Ansible, GitHub Actions, and Jenkins pipelines — is configured almost entirely in YAML. A syntax error in a single YAML file will break a Kubernetes Deployment, fail an Ansible playbook, or cause a Jenkins pipeline to refuse to start. Understanding YAML well before diving into those tools saves hours of debugging.

**What you will learn:**
- YAML syntax: key-value pairs, lists, nested objects, booleans, numbers, strings
- The one rule that breaks everything: indentation (spaces only, never tabs)
- Comments, multi-line strings, and document separators
- Real examples pulled from Docker Compose, Kubernetes, and Ansible files you have already seen or will see shortly

**How this fits into the series:**
```
01 Git & GitHub  — version-controlled the app
02 Docker        — introduced docker-compose.yml (your first YAML file)
03 YAML          ← YOU ARE HERE — understand the language before going deeper
04 Kubernetes    — every object (Pod, Deployment, Service) is a YAML file
05 Ansible       — every playbook, inventory var, and role is YAML
06 Jenkins       — Jenkinsfile uses YAML for some pipeline configurations
```

**How to use this guide:** Read it once before starting Module 04. Keep it open as a reference while writing Kubernetes and Ansible YAML files. When something breaks, check indentation first.

---

## What is YAML?

YAML stands for:

```text
YAML Ain't Markup Language
```

YAML is a human-readable configuration language commonly used in:

- Docker Compose
- Kubernetes
- Ansible
- GitHub Actions
- CI/CD Pipelines
- Jenkins

---

# `.yml` vs `.yaml`

There is NO functional difference.

| Extension | Meaning |
|---|---|
| `.yaml` | Official/full extension |
| `.yml` | Shorter extension |

Both work the same way.

Example:

```text
docker-compose.yml
docker-compose.yaml
```

---

# YAML Basics

YAML is:

- indentation-based
- key-value oriented
- human-readable

---

# Important Rule

## Spaces matter!

Use spaces for indentation.

Avoid tabs.

---

# Key-Value Example

```yaml
name: Sonu
age: 47
city: Hyderabad
```

---

# Lists

```yaml
skills:
  - Java
  - Angular
  - Docker
  - Kubernetes
```

---

# Nested Objects

```yaml
employee:
  id: 101
  name: Sonu
  department: IT
```

---

# Nested + Lists Together

```yaml
employees:
  - id: 101
    name: Sonu

  - id: 102
    name: Monu
```

---

# Boolean Values

```yaml
isActive: true
isDeleted: false
```

---

# Numbers

```yaml
salary: 50000
rating: 4.5
```

---

# Strings

```yaml
message: Hello World
```

Quoted strings:

```yaml
message: "Hello World"
```

---

# Comments

```yaml
# This is a comment
name: Sonu
```

---

# Docker Compose Example

```yaml
version: '3'

services:

  frontend:
    image: angular-app
    ports:
      - "4200:80"

  backend:
    image: spring-app
    ports:
      - "8080:8080"

  mongodb:
    image: mongo
```

---

# Kubernetes Example

```yaml
apiVersion: v1
kind: Pod

metadata:
  name: nginx-pod

spec:
  containers:
    - name: nginx
      image: nginx
```

---

# Ansible Example

```yaml
---
- hosts: servers

  tasks:
    - name: Install nginx
      apt:
        name: nginx
        state: present
```

---

# Common YAML Mistakes

## Wrong Indentation

INVALID:

```yaml
services:
backend:
image: nginx
```

VALID:

```yaml
services:
  backend:
    image: nginx
```

---

# Important Symbols

| Symbol | Meaning |
|---|---|
| `:` | key-value separator |
| `-` | list item |
| `#` | comment |

---

# YAML Learning Tips

- Maintain proper indentation
- Use spaces consistently
- Read from top to bottom
- Think in hierarchy/tree structure

---

# Where YAML is Used in DevOps

| Tool | YAML Usage |
|---|---|
| Docker Compose | Yes |
| Kubernetes | Heavy |
| Ansible | Heavy |
| GitHub Actions | Yes |
| Jenkins | Sometimes |

---

# Quick Memory Trick

```text
YAML = Configuration Language for DevOps Tools
```

---

# Final Summary

```text
YAML files define configurations using indentation-based key-value structures.

.yml and .yaml are the same.
```

---

## The Bridge to Kubernetes

You have already written YAML in the Docker module — `docker-compose.yml`. Kubernetes takes YAML further: every single object in the cluster (Pod, Deployment, Service, ConfigMap, Secret) is declared in a YAML file and applied with `kubectl apply -f`.

The difference:

```yaml
# Docker Compose YAML (what you've already written)
services:
  app:
    image: yourname/hello-express:1.0
    ports:
      - "3000:3000"

# Kubernetes YAML (what you will write next)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app-deployment
spec:
  replicas: 3
  template:
    spec:
      containers:
        - name: app
          image: yourname/hello-express:1.0
```

Same concepts — image, container, port — but Kubernetes YAML is more verbose because it supports far more configuration. The structure you learned in this guide (key-value, lists with `-`, nesting with indentation) applies to every line of it.

> **Next → 04 Kubernetes** — deploy the Docker image from Module 02 to a Kubernetes cluster. Every Pod, Deployment, and Service you create will be a YAML file applied with `kubectl apply -f`.
