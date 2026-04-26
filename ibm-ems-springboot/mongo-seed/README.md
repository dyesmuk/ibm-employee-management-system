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

## Import with mongoimport

Run these commands from this directory against a running MongoDB instance:

```bash
DB=ems_db
HOST=localhost:27017

mongoimport --host $HOST --db $DB --collection roles           --file roles.json           --jsonArray
mongoimport --host $HOST --db $DB --collection departments     --file departments.json     --jsonArray
mongoimport --host $HOST --db $DB --collection employees       --file employees.json       --jsonArray
mongoimport --host $HOST --db $DB --collection projects        --file projects.json        --jsonArray
mongoimport --host $HOST --db $DB --collection employee_projects --file employee_projects.json --jsonArray
```

> **Order matters**: import `roles` and `departments` before `employees`,
> and `employees` + `projects` before `employee_projects`.

## Import with Docker Compose

If your MongoDB is running via `docker compose`, copy the files into the container first:

```bash
docker cp . ibm-ems-mongodb:/seed/

docker exec ibm-ems-mongodb mongoimport --db ems_db --collection roles           --file /seed/roles.json           --jsonArray
docker exec ibm-ems-mongodb mongoimport --db ems_db --collection departments     --file /seed/departments.json     --jsonArray
docker exec ibm-ems-mongodb mongoimport --db ems_db --collection employees       --file /seed/employees.json       --jsonArray
docker exec ibm-ems-mongodb mongoimport --db ems_db --collection projects        --file /seed/projects.json        --jsonArray
docker exec ibm-ems-mongodb mongoimport --db ems_db --collection employee_projects --file /seed/employee_projects.json --jsonArray
```

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
