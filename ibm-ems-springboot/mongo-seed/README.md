# MongoDB Seed Data

Initial data for the IBM EMS database. Each file maps directly to a MongoDB collection.

## Files

| File | Collection | Documents |
|------|-----------|-----------|
| `roles.json` | `roles` | 8 roles (Engineer → Manager ladder) |
| `departments.json` | `departments` | 5 departments |
| `employees.json` | `employees` | 10 employees across departments |
| `employee_projects.json` | `employee_projects` | 10 project assignments |
| `projects.json` | `projects` | 5 projects (various statuses) |

## Import into MongoDB

Import these json files into MongoDB using Compass. 

> **Order matters**: import `roles` , `departments` , `employees`, `projects` , `employee_projects` in this order.

## ID Reference Map

These fixed ObjectIds are used across files to wire up the relationships.

### Roles
| ID | Name |
|----|------|
| `664100000000000000000001` | SOFTWARE_ENGINEER |
| `664100000000000000000002` | SENIOR_ENGINEER |
| `664100000000000000000003` | TECH_LEAD |
| `664100000000000000000004` | ENGINEERING_MANAGER |
| `664100000000000000000005` | QA_ENGINEER |
| `664100000000000000000006` | DEVOPS_ENGINEER |
| `664100000000000000000007` | HR_MANAGER |
| `664100000000000000000008` | BUSINESS_ANALYST |

### Departments
| ID | Name |
|----|------|
| `664200000000000000000001` | Engineering |
| `664200000000000000000002` | Quality Assurance |
| `664200000000000000000003` | DevOps & Infrastructure |
| `664200000000000000000004` | Human Resources |
| `664200000000000000000005` | Business Analysis |

### Employees
| ID | Name |
|----|------|
| `664300000000000000000001` | Arjun Sharma |
| `664300000000000000000002` | Priya Nair |
| `664300000000000000000003` | Rohan Mehta |
| `664300000000000000000004` | Deepa Krishnan |
| `664300000000000000000005` | Karthik Iyer |
| `664300000000000000000006` | Sneha Patel |
| `664300000000000000000007` | Vikram Joshi |
| `664300000000000000000008` | Ananya Reddy |
| `664300000000000000000009` | Suresh Babu |
| `664300000000000000000010` | Meera Pillai |

### Projects
| ID | Name |
|----|------|
| `664400000000000000000001` | YONO 2.0 |
| `664400000000000000000002` | Cloud Migration Phase 2 |
| `664400000000000000000003` | HR Self-Service Portal |
| `664400000000000000000004` | API Gateway Consolidation |
| `664400000000000000000005` | AI Fraud Detection |
