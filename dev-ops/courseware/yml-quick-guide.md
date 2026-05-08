# YAML / YML Quick Guide

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
name: Vaman
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
