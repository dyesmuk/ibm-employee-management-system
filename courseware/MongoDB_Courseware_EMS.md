# MongoDB Courseware
### Java Full Stack Development – IBM Training Program
**Duration:** 1.5 Days | **Level:** Beginner to Intermediate

---

## Table of Contents

1. [Course Navigation and Practice Tasks Overview](#1-course-navigation-and-practice-tasks-overview)
2. [Introduction to MongoDB](#2-introduction-to-mongodb)
3. [MongoDB Installation Options](#3-mongodb-installation-options)
4. [Installing MongoDB on Local Computer](#4-installing-mongodb-on-local-computer)
5. [Installing MongoDB on a Dedicated or VPS Server](#5-installing-mongodb-on-a-dedicated-or-vps-server)
6. [Using MongoDB as a Service (Cloud MongoDB)](#6-using-mongodb-as-a-service-cloud-mongodb)
7. [Installing GUI Tools for MongoDB Management](#7-installing-gui-tools-for-mongodb-management)
8. [Introduction to the MongoDB Shell](#8-introduction-to-the-mongodb-shell)
9. [Primary MongoDB Data Types](#9-primary-mongodb-data-types)
10. [CRUD Operations](#10-crud-operations)
11. [MongoDB Queries](#11-mongodb-queries)
12. [Updating Documents](#12-updating-documents)
13. [Delete Operations](#13-delete-operations)
14. [Aggregation Framework](#14-aggregation-framework)
15. [Indexes](#15-indexes)
16. [Utilities](#16-utilities)
17. [Wrap Up](#17-wrap-up)

---

## 1. Course Navigation and Practice Tasks Overview

### How to Use This Courseware

This courseware is designed for freshers joining the IBM Java Full Stack Development program. Each module contains:

- **Concept Explanation** – Theory in plain English
- **Syntax Reference** – Command structure
- **Code Examples** – Real, runnable examples
- **Practice Tasks** – Hands-on exercises to reinforce learning

### Prerequisites

- Basic understanding of JSON (JavaScript Object Notation)
- Java installed (JDK 11+)
- Familiarity with relational databases (helpful but not mandatory)

### Tools You Will Use

| Tool | Purpose |
|------|---------|
| MongoDB Community Server | Core database engine |
| MongoDB Shell (`mongosh`) | CLI to interact with MongoDB |
| MongoDB Compass | GUI for visual database management |
| MongoDB Atlas | Cloud-based MongoDB service |

### Practice Task Overview

Each section ends with practice tasks. Complete them in order. A final capstone exercise at the end ties everything together using an **Employee Management System** dataset — the same domain used in every example throughout this courseware.

---

## 2. Introduction to MongoDB

### What is MongoDB?

MongoDB is an open-source, **NoSQL document database** built for high availability, scalability, and flexibility. Unlike traditional relational databases (MySQL, Oracle) that store data in rows and tables, MongoDB stores data as **JSON-like documents**.

> **"MongoDB" comes from the word "humongous" — built to handle massive amounts of data.**

### Why MongoDB?

| Feature | Relational DB (MySQL) | MongoDB |
|---|---|---|
| Data Format | Rows & Columns (Tables) | Documents (JSON/BSON) |
| Schema | Fixed / Rigid | Flexible / Dynamic |
| Relationships | Foreign Keys & JOINs | Embedded documents or References |
| Scalability | Vertical (bigger server) | Horizontal (more servers) |
| Best For | Structured data | Semi/Unstructured, hierarchical data |

### Core Concepts

#### Database
A container for collections. Similar to a database in SQL.

#### Collection
A group of MongoDB documents. Equivalent to a **table** in SQL. Collections do not enforce a schema.

#### Document
The basic unit of data in MongoDB. A document is a **BSON** (Binary JSON) object — essentially a set of key-value pairs.

```json
{
  "_id": "ObjectId('64a7f2c3e4b0a1d2e3f4a5b6')",
  "firstName": "Ravi",
  "lastName": "Kumar",
  "age": 28,
  "email": "ravi.kumar@company.com",
  "skills": ["Java", "MongoDB", "Spring Boot"]
}
```

#### Field
A key-value pair inside a document. Equivalent to a **column** in SQL.

#### `_id`
Every MongoDB document has a unique `_id` field. If you don't provide one, MongoDB automatically generates an **ObjectId**.

### MongoDB vs SQL — Quick Mapping

| SQL Term | MongoDB Term |
|---|---|
| Database | Database |
| Table | Collection |
| Row | Document |
| Column | Field |
| Primary Key | `_id` |
| JOIN | `$lookup` (Aggregation) |
| INDEX | Index |

### Real-World Use Cases

- **HR & Employee Systems:** Employee profiles, org charts, payroll data
- **E-commerce:** Product catalogs with varying attributes
- **Social Media:** User profiles, posts, comments
- **IoT:** Sensor data with flexible schemas
- **Gaming:** Player profiles and game state
- **Content Management:** Articles, blogs, media

---

## 3. MongoDB Installation Options

MongoDB offers three primary installation paths:

### Option 1: Local Installation
Install MongoDB Community Edition directly on your machine (Windows/Mac/Linux). Best for **development and learning**.

### Option 2: Dedicated/VPS Server
Install MongoDB on a remote Linux server. Best for **staging or production environments**.

### Option 3: Cloud MongoDB (Atlas)
Use MongoDB's fully managed cloud service. Best for **quick start, team projects, and production** without managing infrastructure.

---

## 4. Installing MongoDB on Local Computer

### Installing on Windows

**Step 1:** Download the MongoDB Community Server
Visit: [https://www.mongodb.com/try/download/community](https://www.mongodb.com/try/download/community)
- Choose: **Windows** → **MSI** package → Download

**Step 2:** Run the Installer
- Accept the license agreement
- Choose **Complete** installation
- Check **"Install MongoDB as a Service"**
- Optionally install **MongoDB Compass** (GUI tool) — recommended

**Step 3:** Verify Installation
Open Command Prompt:
```bash
mongod --version
mongosh --version
```

**Step 4:** Start MongoDB Service
```bash
# Start the service
net start MongoDB

# Stop the service
net stop MongoDB
```

**Default data directory:** `C:\Program Files\MongoDB\Server\<version>\data`

---

### Installing on macOS

**Option A: Using Homebrew (Recommended)**

```bash
# Install Homebrew if not already installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Tap the MongoDB formula
brew tap mongodb/brew

# Install MongoDB Community Edition
brew install mongodb-community

# Start MongoDB as a background service
brew services start mongodb-community

# Verify
mongosh --version
```

**Option B: Manual Installation**
- Download `.tgz` from the official site
- Extract and move binaries to `/usr/local/bin`

---

### Installing on Linux (Ubuntu/Debian)

```bash
# Import MongoDB public GPG key
curl -fsSL https://www.mongodb.org/static/pgp/server-7.0.asc | \
   sudo gpg -o /usr/share/keyrings/mongodb-server-7.0.gpg --dearmor

# Create list file
echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg ] \
https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse" | \
sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list

# Update packages and install
sudo apt-get update
sudo apt-get install -y mongodb-org

# Start MongoDB
sudo systemctl start mongod
sudo systemctl enable mongod

# Verify
mongod --version
```

### Practice Task 1
- Install MongoDB on your local machine
- Verify the installation by running `mongod --version` and `mongosh --version`
- Take a screenshot of the version output

---

## 5. Installing MongoDB on a Dedicated or VPS Server

### Common VPS Providers
- AWS EC2
- DigitalOcean Droplets
- Google Cloud Compute Engine
- Azure Virtual Machines

### Steps (Linux VPS Example)

**Step 1: SSH into your server**
```bash
ssh username@your-server-ip
```

**Step 2: Install MongoDB** (same as Ubuntu steps above)

**Step 3: Configure MongoDB for Remote Access**

Edit the config file:
```bash
sudo nano /etc/mongod.conf
```

Change the `bindIp` to allow remote connections:
```yaml
net:
  port: 27017
  bindIp: 0.0.0.0   # Allow all IPs (restrict in production)
```

**Step 4: Enable Firewall Rules**
```bash
# Allow MongoDB port
sudo ufw allow 27017/tcp
sudo ufw reload
```

**Step 5: Enable Authentication (Security Best Practice)**
```bash
# Login to mongosh
mongosh

# Create admin user
use admin
db.createUser({
  user: "adminUser",
  pwd: "SecurePassword123",
  roles: [{ role: "userAdminAnyDatabase", db: "admin" }]
})
```

Update `mongod.conf`:
```yaml
security:
  authorization: enabled
```

Restart MongoDB:
```bash
sudo systemctl restart mongod
```

**Step 6: Connect Remotely**
```bash
mongosh "mongodb://adminUser:SecurePassword123@your-server-ip:27017/admin"
```

---

## 6. Using MongoDB as a Service (Cloud MongoDB)

### MongoDB Atlas — The Official Cloud Service

**Atlas** is MongoDB's Database-as-a-Service (DBaaS). It handles backups, scaling, and security automatically.

### Getting Started with Atlas (Free Tier)

**Step 1: Create an Account**
Visit [https://www.mongodb.com/atlas](https://www.mongodb.com/atlas) and sign up for free.

**Step 2: Create a Free Cluster**
- Click **"Build a Database"**
- Choose **Free (Shared)** tier
- Select a cloud provider (AWS/GCP/Azure) and region
- Click **"Create Cluster"**

**Step 3: Configure Access**
- **Database Access:** Create a database user with username and password
- **Network Access:** Add your IP address (or `0.0.0.0/0` to allow all for development)

**Step 4: Get Connection String**
- Click **"Connect"** → **"Connect your application"**
- Copy the connection string:
```
mongodb+srv://<username>:<password>@cluster0.xxxxx.mongodb.net/<dbname>?retryWrites=true&w=majority
```

**Step 5: Connect via MongoDB Shell**
```bash
mongosh "mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/ems_db"
```

**Step 6: Connect from Java (Spring Boot)**
```properties
# application.properties
spring.data.mongodb.uri=mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/ems_db
```

### Atlas Key Features
- **Automated Backups**
- **Performance Advisor** (query optimization suggestions)
- **Data Explorer** (visual browsing)
- **Charts** (built-in data visualization)
- **Global Clusters** (multi-region replication)

### Practice Task 2
- Create a free MongoDB Atlas account
- Set up a free cluster
- Connect to your cluster via `mongosh`

---

## 7. Installing GUI Tools for MongoDB Management

### MongoDB Compass (Official GUI)

**MongoDB Compass** is the official GUI for MongoDB. It lets you visually explore data, build queries, and analyze performance.

**Download:** [https://www.mongodb.com/products/compass](https://www.mongodb.com/products/compass)

**Key Features:**
- Visual query builder
- Document editor (no query syntax needed)
- Index management
- Aggregation pipeline builder
- Schema analysis
- Real-time performance monitoring

**Connecting Compass to MongoDB:**
1. Open Compass
2. Paste your connection string: `mongodb://localhost:27017`
3. Click **Connect**

---

### Other Popular GUI Tools

| Tool | Platform | Cost | Best For |
|------|----------|------|---------|
| MongoDB Compass | Win/Mac/Linux | Free | Official, most features |
| Studio 3T | Win/Mac/Linux | Free/Paid | SQL-like querying of MongoDB |
| NoSQLBooster | Win/Mac/Linux | Free/Paid | Code completion, scripting |
| Robo 3T (Robomongo) | Win/Mac/Linux | Free | Lightweight and fast |
| TablePlus | Mac/Win | Free/Paid | Multi-DB support |

### Practice Task 3
- Install MongoDB Compass
- Connect it to your local MongoDB instance
- Explore the default databases (`admin`, `local`, `config`)

---

## 8. Introduction to the MongoDB Shell

### What is `mongosh`?

`mongosh` (MongoDB Shell) is the official CLI tool for interacting with MongoDB. It's a JavaScript-based interactive shell where you type commands to manage databases and documents.

### Starting the Shell

```bash
# Connect to local MongoDB
mongosh

# Connect to a specific database
mongosh "mongodb://localhost:27017/ems_db"

# Connect to Atlas
mongosh "mongodb+srv://user:pass@cluster0.mongodb.net/ems_db"
```

### Shell Basics

```javascript
// Show current database
db

// List all databases
show dbs

// Switch to or create a database
use ems_db

// Show all collections in current database
show collections

// Get help
help
db.help()

// Clear the screen
cls

// Exit the shell
exit
```

### Running JavaScript in the Shell

`mongosh` is a full JavaScript environment:

```javascript
// Variables
let dbName = "ems_db"
print("Connected to: " + dbName)

// Loops — insert 5 test employees
for (let i = 1; i <= 5; i++) {
  print("Employee " + i)
}

// Functions
function greetEmployee(name) {
  return "Welcome aboard, " + name + "!"
}
greetEmployee("Ravi Kumar")
```

### Shell Tips

```javascript
// Pretty print results
db.employees.find().pretty()

// Limit output
db.employees.find().limit(5)

// Count documents
db.employees.countDocuments()

// Check MongoDB server status
db.serverStatus()
```

### Practice Task 4
- Open `mongosh` and connect to local MongoDB
- Run `show dbs` — note the system databases
- Create a new database called `ems_db` using `use ems_db`
- Run `db` to confirm the current database

---

## 9. Primary MongoDB Data Types

MongoDB uses **BSON** (Binary JSON) which extends JSON with additional data types.

### Data Types Reference Table

| BSON Type | Example | Description |
|---|---|---|
| String | `"firstName": "Ravi"` | UTF-8 text |
| Integer (32-bit) | `"age": 28` | Whole numbers |
| Double | `"performanceScore": 8.5` | Floating point numbers |
| Boolean | `"isRemote": true` | true / false |
| Date | `"joiningDate": ISODate("2022-03-14")` | Date and time |
| ObjectId | `"_id": ObjectId("...")` | Unique 12-byte ID |
| Array | `"skills": ["Java","MongoDB"]` | List of values |
| Embedded Document | `"address": {"city": "Mumbai"}` | Nested document |
| Null | `"middleName": null` | No value |
| Regular Expression | `"pattern": /^Ravi/` | Regex |
| Binary Data | `"profilePic": BinData(...)` | Binary content |
| Timestamp | Internal MongoDB use | |
| Long (64-bit Integer) | `"employeeId": NumberLong("202400123456")` | Large integers |
| Decimal128 | `"salary": NumberDecimal("1200000.00")` | High precision decimals |

### Examples in Practice

```javascript
// Insert an employee document showcasing multiple data types
db.employees.insertOne({
  firstName: "Priya",                              // String
  lastName: "Sharma",                              // String
  age: 27,                                         // Integer
  salary: 1500000,                                 // Integer (INR, annual)
  performanceScore: 9.1,                           // Double
  isRemote: true,                                  // Boolean
  joiningDate: new Date("2022-07-01"),             // Date
  skills: ["Java", "Spring Boot", "MongoDB"],      // Array
  address: {                                       // Embedded Document
    street: "12 MG Road",
    city: "Bengaluru",
    pincode: "560001"
  },
  profilePic: null,                                // Null
  employeeId: NumberLong("202200123456")           // Long Integer
})
```

### The ObjectId

ObjectId is a 12-byte unique identifier automatically assigned to `_id`:

```
ObjectId("64a7f2c3e4b0a1d2e3f4a5b6")
           |------| |----| |--|  |--|
           4-byte    3-byte  2    3-byte
           timestamp machine  PID  random
```

```javascript
// Create an ObjectId manually
let id = new ObjectId()
print(id)
print(id.getTimestamp())   // Extract creation timestamp
```

### Practice Task 5
- Create a collection `employees` in the `ems_db` database
- Insert one employee document with at least 6 different data types
- Use `db.employees.find().pretty()` to view the result

---

## 10. CRUD Operations

CRUD stands for **Create, Read, Update, Delete** — the four fundamental operations of any database.

---

### Setup: Sample Dataset

Before we begin, let's create our practice collection. We are using the `ems_db` database with the `employees` collection throughout this courseware.

```javascript
use ems_db

db.employees.insertMany([
  {
    firstName: "Ravi", lastName: "Kumar",
    age: 28, salary: 1200000, status: "active",
    "address.city": "Mumbai",
    skills: ["Java", "MongoDB"]
  },
  {
    firstName: "Priya", lastName: "Sharma",
    age: 27, salary: 1500000, status: "active",
    "address.city": "Bengaluru",
    skills: ["Python", "Django"]
  },
  {
    firstName: "Amit", lastName: "Singh",
    age: 30, salary: 980000, status: "active",
    "address.city": "Delhi",
    skills: ["Java", "Spring Boot"]
  },
  {
    firstName: "Neha", lastName: "Patel",
    age: 25, salary: 1350000, status: "active",
    "address.city": "Pune",
    skills: ["Java", "MongoDB", "React"]
  },
  {
    firstName: "Suresh", lastName: "Reddy",
    age: 32, salary: 900000, status: "on_leave",
    "address.city": "Hyderabad",
    skills: ["Node.js", "MongoDB"]
  }
])
```

> **Note:** For the full 100-document dataset used in all practice tasks, import `employees.json` using `mongoimport` (covered in the Utilities section).

---

### CREATE — Inserting Documents

#### `insertOne()` — Insert a Single Document

```javascript
db.employees.insertOne({
  firstName: "Kavitha",
  lastName: "Nair",
  age: 26,
  salary: 1100000,
  status: "active",
  address: { city: "Chennai", state: "Tamil Nadu", pincode: "600001" },
  skills: ["Java", "AWS"]
})
```

**Response:**
```json
{
  "acknowledged": true,
  "insertedId": ObjectId("64a7f2c3e4b0a1d2e3f4a5b6")
}
```

#### `insertMany()` — Insert Multiple Documents

```javascript
db.employees.insertMany([
  {
    firstName: "Rahul", lastName: "Verma",
    age: 29, salary: 1050000, status: "active",
    address: { city: "Jaipur", state: "Rajasthan", pincode: "302001" },
    skills: ["Java", "Kubernetes"]
  },
  {
    firstName: "Sneha", lastName: "Joshi",
    age: 24, salary: 1800000, status: "active",
    address: { city: "Kolkata", state: "West Bengal", pincode: "700001" },
    skills: ["MongoDB", "Express", "Node.js"]
  }
])
```

#### Insert Options

```javascript
// Insert with a custom _id
db.employees.insertOne({
  _id: "EMP001",
  firstName: "Custom",
  lastName: "Employee",
  age: 30
})

// Ordered vs Unordered inserts
// ordered: true (default) — stops on first error
// ordered: false — continues past errors
db.employees.insertMany(
  [
    { firstName: "A", lastName: "Test" },
    { firstName: "B", lastName: "Test" },
    { firstName: "C", lastName: "Test" }
  ],
  { ordered: false }
)
```

---

### READ — Finding Documents

#### `find()` — Retrieve Documents

```javascript
// Find all employees
db.employees.find()

// Pretty formatted output
db.employees.find().pretty()

// Find employees from Mumbai
db.employees.find({ "address.city": "Mumbai" })

// Find with multiple conditions (age = 28 AND city = Mumbai)
db.employees.find({ age: 28, "address.city": "Mumbai" })
```

#### `findOne()` — Retrieve First Matching Document

```javascript
db.employees.findOne({ firstName: "Ravi", lastName: "Kumar" })
```

#### Projection — Select Specific Fields

```javascript
// Show only name and city (1 = include, 0 = exclude)
db.employees.find({}, { firstName: 1, lastName: 1, "address.city": 1, _id: 0 })

// Exclude a specific field
db.employees.find({}, { salary: 0 })
```

#### Cursor Methods

```javascript
// Limit results
db.employees.find().limit(3)

// Skip documents
db.employees.find().skip(2)

// Sort — 1 = Ascending, -1 = Descending
db.employees.find().sort({ salary: -1 })              // Highest salary first
db.employees.find().sort({ firstName: 1 })            // Alphabetical by first name

// Chain methods
db.employees.find().sort({ salary: -1 }).limit(3)

// Count employees in a specific city
db.employees.countDocuments({ "address.city": "Bengaluru" })
```

### Practice Task 6 (CRUD – Create & Read)
1. Insert 3 more employees of your choice into the `employees` collection
2. Find all employees whose city is `"Delhi"`
3. Retrieve only the `firstName`, `lastName`, and `salary` fields for all employees, sorted by `salary` descending
4. Find the highest-paid employee using `sort` and `limit`

---

## 11. MongoDB Queries

### Comparison Operators

```javascript
// $eq — Equal to (default behavior)
db.employees.find({ age: { $eq: 28 } })

// $ne — Not equal
db.employees.find({ status: { $ne: "inactive" } })

// $gt — Greater than
db.employees.find({ salary: { $gt: 1200000 } })

// $gte — Greater than or equal
db.employees.find({ salary: { $gte: 1200000 } })

// $lt — Less than
db.employees.find({ age: { $lt: 28 } })

// $lte — Less than or equal
db.employees.find({ age: { $lte: 28 } })

// $in — Value matches any in array
db.employees.find({ "address.city": { $in: ["Mumbai", "Pune", "Bengaluru"] } })

// $nin — Value does not match any in array
db.employees.find({ "address.city": { $nin: ["Delhi", "Hyderabad"] } })
```

### Logical Operators

```javascript
// $and — All conditions must be true
db.employees.find({
  $and: [
    { age: { $gte: 25 } },
    { salary: { $gte: 1000000 } }
  ]
})

// $or — At least one condition must be true
db.employees.find({
  $or: [
    { "address.city": "Mumbai" },
    { salary: { $gte: 1500000 } }
  ]
})

// $not — Negates a condition
db.employees.find({ salary: { $not: { $gte: 1000000 } } })

// $nor — All conditions must be false
db.employees.find({
  $nor: [
    { "address.city": "Delhi" },
    { status: "inactive" }
  ]
})
```

### Element Operators

```javascript
// $exists — Field exists
db.employees.find({ performanceScore: { $exists: true } })
db.employees.find({ certifications: { $exists: false } })

// $type — Field is of a specific type
db.employees.find({ age: { $type: "int" } })
db.employees.find({ firstName: { $type: "string" } })
```

### Array Operators

```javascript
// $all — Array contains all specified values
db.employees.find({ skills: { $all: ["Java", "MongoDB"] } })

// $elemMatch — At least one element matches conditions
// (useful when array elements are objects)
db.projects.find({ teamMemberIds: { $elemMatch: { $exists: true } } })

// $size — Array has exact number of elements
db.employees.find({ skills: { $size: 3 } })

// Query for specific element in array
db.employees.find({ skills: "Java" })   // skills array contains "Java"
```

### Evaluation Operators

```javascript
// $regex — Pattern match
db.employees.find({ firstName: { $regex: /^Ravi/i } })       // Starts with "Ravi"
db.employees.find({ lastName: { $regex: "Kumar$" } })        // Ends with "Kumar"
db.employees.find({ email: { $regex: ".*@company\\.com" } }) // Company email

// $where — JavaScript expression (use sparingly, slow)
db.employees.find({ $where: "this.age > 28 && this.salary > 1000000" })

// $expr — Use aggregation expressions in queries
db.employees.find({ $expr: { $gt: ["$salary", 1200000] } })
```

### Querying Embedded Documents

```javascript
// Employee document with embedded address
db.employees.insertOne({
  firstName: "Arjun",
  lastName: "Mehta",
  address: { city: "Mumbai", state: "Maharashtra", pincode: "400001" }
})

// Exact match on embedded document (field order matters!)
db.employees.find({
  address: { city: "Mumbai", state: "Maharashtra", pincode: "400001" }
})

// Dot notation — query nested fields (recommended)
db.employees.find({ "address.city": "Mumbai" })
db.employees.find({ "address.pincode": { $regex: /^400/ } })
```

### Practice Task 7 (Queries)
1. Find all employees with a salary between `1000000` and `1500000` (inclusive)
2. Find employees from either `"Mumbai"` or `"Bengaluru"` with a salary above `1200000`
3. Find employees whose `skills` array contains both `"Java"` and `"MongoDB"`
4. Find all employees whose `firstName` starts with the letter `"R"`
5. Find employees whose `skills` array has exactly 3 elements
6. Find employees who do **not** have a `certifications` field (use `$exists`)

---

## 12. Updating Documents

### Update Operators Overview

| Operator | Description |
|---|---|
| `$set` | Sets the value of a field |
| `$unset` | Removes a field |
| `$inc` | Increments a field by a value |
| `$mul` | Multiplies a field by a value |
| `$rename` | Renames a field |
| `$min` | Updates only if new value is less |
| `$max` | Updates only if new value is greater |
| `$push` | Adds an element to an array |
| `$pop` | Removes first or last element from array |
| `$pull` | Removes elements matching a condition |
| `$addToSet` | Adds element only if not already present |
| `$currentDate` | Sets field to current date |

---

### `updateOne()` — Update First Matching Document

```javascript
// Update an employee's salary
db.employees.updateOne(
  { firstName: "Ravi", lastName: "Kumar" },   // Filter
  { $set: { salary: 1400000 } }               // Update
)

// Set multiple fields at once
db.employees.updateOne(
  { firstName: "Ravi", lastName: "Kumar" },
  {
    $set: {
      salary: 1400000,
      "address.city": "Pune",
      updatedAt: new Date()
    }
  }
)
```

### `updateMany()` — Update All Matching Documents

```javascript
// Mark all employees earning above 1500000 as "senior"
db.employees.updateMany(
  { salary: { $gte: 1500000 } },
  { $set: { grade: "senior" } }
)

// Give a 5% salary hike to all employees in Bengaluru
db.employees.updateMany(
  { "address.city": "Bengaluru" },
  { $mul: { salary: 1.05 } }
)
```

### `replaceOne()` — Replace Entire Document

```javascript
// Replaces the ENTIRE document (except _id)
db.employees.replaceOne(
  { firstName: "Suresh", lastName: "Reddy" },
  {
    firstName: "Suresh",
    lastName: "Reddy",
    age: 33,
    salary: 1100000,
    status: "active",
    address: { city: "Chennai", state: "Tamil Nadu", pincode: "600001" },
    skills: ["Java", "Kafka"]
  }
)
```

> ⚠️ **Warning:** `replaceOne` removes all existing fields and replaces with the new document. Use `updateOne` with `$set` to update specific fields only.

### Field Operators

```javascript
// $unset — Remove a field
db.employees.updateOne(
  { firstName: "Ravi", lastName: "Kumar" },
  { $unset: { grade: "" } }
)

// $rename — Rename a field
db.employees.updateMany({}, { $rename: { "address.city": "city" } })

// $inc — Increment (e.g., annual performance review increases age by 1)
db.employees.updateOne(
  { firstName: "Priya", lastName: "Sharma" },
  { $inc: { age: 1, performanceScore: 0.2 } }
)

// $mul — Multiply (apply a 10% salary hike to one employee)
db.employees.updateOne(
  { firstName: "Amit", lastName: "Singh" },
  { $mul: { salary: 1.10 } }
)

// $min / $max — Conditional update
// Only lower the performance score if the current score is above 7.0
db.employees.updateOne(
  { firstName: "Neha", lastName: "Patel" },
  { $min: { performanceScore: 7.0 } }
)

// $currentDate — Set to today's date
db.employees.updateOne(
  { firstName: "Ravi", lastName: "Kumar" },
  { $currentDate: { lastModified: true } }
)
```

### Array Update Operators

```javascript
// $push — Add a skill to an employee
db.employees.updateOne(
  { firstName: "Ravi", lastName: "Kumar" },
  { $push: { skills: "Docker" } }
)

// $push with $each — Add multiple skills at once
db.employees.updateOne(
  { firstName: "Priya", lastName: "Sharma" },
  { $push: { skills: { $each: ["Kubernetes", "AWS"] } } }
)

// $addToSet — Add skill only if not already present (avoids duplicates)
db.employees.updateOne(
  { firstName: "Amit", lastName: "Singh" },
  { $addToSet: { skills: "Java" } }   // "Java" won't be added if it already exists
)

// $pop — Remove first (-1) or last (1) element from skills array
db.employees.updateOne(
  { firstName: "Neha", lastName: "Patel" },
  { $pop: { skills: 1 } }   // Remove last skill
)

// $pull — Remove a specific skill from the array
db.employees.updateOne(
  { firstName: "Ravi", lastName: "Kumar" },
  { $pull: { skills: "Docker" } }
)

// $pullAll — Remove multiple specific skills
db.employees.updateOne(
  { firstName: "Suresh", lastName: "Reddy" },
  { $pullAll: { skills: ["Node.js", "MongoDB"] } }
)
```

### Upsert — Update or Insert

If no document matches the filter, upsert inserts a new document:

```javascript
db.employees.updateOne(
  { firstName: "Manish", lastName: "Tiwari" },
  {
    $set: {
      firstName: "Manish",
      lastName: "Tiwari",
      age: 26,
      salary: 950000,
      status: "active",
      address: { city: "Nagpur", state: "Maharashtra", pincode: "440001" }
    }
  },
  { upsert: true }   // Creates the document if not found
)
```

### Practice Task 8 (Update)
1. Update Priya Sharma's salary to `1800000`
2. Add the skill `"DevOps"` to all employees from Mumbai
3. Remove the `grade` field from all documents where it exists
4. Apply a 5% salary hike (`$mul`) to all employees with `status: "active"`
5. Use upsert to insert a new employee `"Komal Saxena"` if they don't already exist

---

## 13. Delete Operations

### `deleteOne()` — Delete First Matching Document

```javascript
// Delete a specific employee
db.employees.deleteOne({ firstName: "Rahul", lastName: "Verma" })

// Delete by _id
db.employees.deleteOne({ _id: ObjectId("64a7f2c3e4b0a1d2e3f4a5b6") })
```

### `deleteMany()` — Delete All Matching Documents

```javascript
// Delete all inactive employees
db.employees.deleteMany({ status: "inactive" })

// Delete all employees from a specific city
db.employees.deleteMany({ "address.city": "Delhi" })

// ⚠️ Delete ALL documents in a collection (collection still exists)
db.employees.deleteMany({})
```

### `findOneAndDelete()` — Delete and Return the Document

```javascript
// Returns the deleted document before removal
const deletedEmployee = db.employees.findOneAndDelete(
  { firstName: "Sneha", lastName: "Joshi" }
)
print(deletedEmployee)
```

### Dropping a Collection vs Deleting Documents

```javascript
// Delete all documents — keeps the collection and its indexes
db.employees.deleteMany({})

// Drop the collection entirely — removes collection, indexes, and metadata
db.employees.drop()

// Drop the entire database
db.dropDatabase()
```

> ⚠️ **Important:** There is no "recycle bin" in MongoDB. Deleted data is gone permanently unless you have backups.

### Safe Delete Pattern

Always preview what you're about to delete with `find()` first:

```javascript
// Step 1: Preview — see who will be deleted
db.employees.find({ status: "inactive" })

// Step 2: Delete only after confirming the results look correct
db.employees.deleteMany({ status: "inactive" })
```

### Practice Task 9 (Delete)
1. Delete one employee of your choice by `firstName` and `lastName`
2. Delete all employees with `status: "on_leave"` from `"Jaipur"`
3. Use `findOneAndDelete` to delete an employee and print the returned document
4. Drop the `temp_employees` collection you may have created during earlier tasks

---

## 14. Aggregation Framework

### What is Aggregation?

Aggregation is MongoDB's powerful data processing pipeline. It processes documents through a series of **stages**, where each stage transforms the data.

Think of it like an **assembly line** — data enters at one end, goes through multiple processing steps, and comes out transformed.

```
[Input Documents] → [Stage 1] → [Stage 2] → [Stage 3] → [Result]
```

### Aggregation Syntax

```javascript
db.collection.aggregate([
  { $stage1: { /* options */ } },
  { $stage2: { /* options */ } },
  // ...
])
```

### Common Aggregation Stages

---

#### `$match` — Filter Documents (like `find()`)

```javascript
// Get only active employees with salary above 1 million
db.employees.aggregate([
  { $match: { status: "active", salary: { $gte: 1000000 } } }
])
```

---

#### `$group` — Group and Summarize

```javascript
// Count employees per city
db.employees.aggregate([
  {
    $group: {
      _id: "$address.city",            // Group by city
      count: { $sum: 1 },              // Count per group
      avgSalary: { $avg: "$salary" }   // Average salary per city
    }
  }
])

// Overall stats — total, min, max salary
db.employees.aggregate([
  {
    $group: {
      _id: null,                               // null = entire collection
      totalEmployees: { $sum: 1 },
      avgSalary: { $avg: "$salary" },
      maxSalary: { $max: "$salary" },
      minSalary: { $min: "$salary" }
    }
  }
])
```

**Group Accumulator Operators:**

| Operator | Description |
|---|---|
| `$sum` | Sum of values |
| `$avg` | Average of values |
| `$min` | Minimum value |
| `$max` | Maximum value |
| `$count` | Count of documents |
| `$push` | Creates array of all values |
| `$addToSet` | Creates array of unique values |
| `$first` | First value in group |
| `$last` | Last value in group |

---

#### `$project` — Reshape Documents

```javascript
// Show name, salary, and a computed seniority label
db.employees.aggregate([
  {
    $project: {
      firstName: 1,
      lastName: 1,
      salary: 1,
      _id: 0,
      seniorityLevel: {
        $cond: {
          if: { $gte: ["$salary", 1500000] },
          then: "Senior",
          else: "Junior"
        }
      }
    }
  }
])
```

---

#### `$sort` — Sort Documents

```javascript
// Sort employees by salary, highest first
db.employees.aggregate([
  { $sort: { salary: -1 } }
])
```

---

#### `$limit` and `$skip`

```javascript
// Get employees ranked 4th to 6th highest salary
db.employees.aggregate([
  { $sort: { salary: -1 } },
  { $skip: 3 },
  { $limit: 3 }
])
```

---

#### `$unwind` — Deconstruct Array Fields

```javascript
// Creates one document per skill per employee
db.employees.aggregate([
  { $unwind: "$skills" }
])

// Count employees per skill (find the most in-demand skill)
db.employees.aggregate([
  { $unwind: "$skills" },
  { $group: { _id: "$skills", employeeCount: { $sum: 1 } } },
  { $sort: { employeeCount: -1 } }
])
```

---

#### `$lookup` — Join Collections (like SQL JOIN)

```javascript
// Join employees with their department details
db.employees.aggregate([
  {
    $lookup: {
      from: "departments",          // Collection to join
      localField: "departmentId",   // Field in employees
      foreignField: "_id",          // Field in departments
      as: "departmentDetails"       // Output array field
    }
  }
])

// Join employees with their assigned role
db.employees.aggregate([
  {
    $lookup: {
      from: "roles",
      localField: "roleId",
      foreignField: "_id",
      as: "roleDetails"
    }
  },
  {
    $project: {
      firstName: 1, lastName: 1, salary: 1,
      "roleDetails.title": 1,
      "roleDetails.level": 1
    }
  }
])
```

---

#### `$addFields` — Add New Fields

```javascript
db.employees.aggregate([
  {
    $addFields: {
      ageInMonths: { $multiply: ["$age", 12] },
      fullName:    { $concat: ["$firstName", " ", "$lastName"] },
      annualBonus: { $multiply: ["$salary", 0.10] }  // 10% bonus
    }
  }
])
```

---

#### `$count` — Count Documents

```javascript
// Count high-earning employees
db.employees.aggregate([
  { $match: { salary: { $gte: 1500000 } } },
  { $count: "highEarners" }
])
```

---

#### Complete Pipeline Example

```javascript
// Top 3 cities by average salary, considering only active employees

db.employees.aggregate([
  { $match: { status: "active" } },                     // Stage 1: Active employees only
  {
    $group: {                                            // Stage 2: Group by city
      _id: "$address.city",
      avgSalary:     { $avg: "$salary" },
      employeeCount: { $sum: 1 },
      names: { $push: { $concat: ["$firstName", " ", "$lastName"] } }
    }
  },
  { $sort: { avgSalary: -1 } },                        // Stage 3: Highest average first
  { $limit: 3 },                                        // Stage 4: Top 3 cities only
  {
    $project: {                                          // Stage 5: Reshape output
      city:          "$_id",
      avgSalary:     { $round: ["$avgSalary", 0] },
      employeeCount: 1,
      names:         1,
      _id:           0
    }
  }
])
```

### Practice Task 10 (Aggregation)
1. Find the total number of employees in each city
2. Calculate the average, minimum, and maximum salary across all employees
3. Find the most common skill across all employees (use `$unwind` + `$group`)
4. List all employees with `salary >= 1500000`, showing only `firstName`, `lastName`, and `salary`, sorted by salary descending
5. Use `$lookup` to join `employees` with the `departments` collection and display each employee's department name alongside their name and salary
6. Find which department has the highest average salary using `$group` and `$sort`

---

## 15. Indexes

### What are Indexes?

An **index** is a data structure that stores a small portion of the collection's data in an easy-to-traverse form. Without indexes, MongoDB must perform a **collection scan** (read every document) to find matches.

> **Analogy:** An index in MongoDB is like the index at the back of a book — instead of reading every page, you jump directly to the right page.

### How to Check if an Index is Being Used

```javascript
// Use explain() to see the query execution plan
db.employees.find({ "address.city": "Mumbai" }).explain("executionStats")

// Look for: "COLLSCAN" (no index — bad) vs "IXSCAN" (index used — good)
```

---

### Creating Indexes

#### Single Field Index

```javascript
// Index on the city field inside the embedded address document
db.employees.createIndex({ "address.city": 1 })   // 1 = Ascending, -1 = Descending

// Index on salary for fast range queries
db.employees.createIndex({ salary: -1 })
```

#### Compound Index (Multiple Fields)

```javascript
// Index frequently queried together: city + salary
db.employees.createIndex({ "address.city": 1, salary: -1 })

// Index for status + performanceScore queries
db.employees.createIndex({ status: 1, performanceScore: -1 })
```

#### Unique Index

```javascript
// Ensure no two employees share the same email address
db.employees.createIndex({ email: 1 }, { unique: true })
```

#### Text Index (Full-Text Search)

```javascript
// Create a text index on firstName, lastName, and skills
db.employees.createIndex({ firstName: "text", lastName: "text" })

// Use text search — find employees named "Ravi" or from a search term
db.employees.find({ $text: { $search: "Ravi Kumar" } })
db.employees.find({ $text: { $search: "\"Priya Sharma\"" } })   // Exact phrase
```

#### Sparse Index

Indexes only documents that have the indexed field — useful for optional fields:

```javascript
// certifications field is optional — only index employees who have it
db.employees.createIndex({ certifications: 1 }, { sparse: true })
```

#### TTL Index (Time-To-Live)

Automatically deletes documents after a specified time — useful for temporary records like audit logs or session tokens:

```javascript
// Automatically remove employee session tokens after 1 hour
db.employeeSessions.createIndex({ createdAt: 1 }, { expireAfterSeconds: 3600 })
```

---

### Managing Indexes

```javascript
// List all indexes on the employees collection
db.employees.getIndexes()

// Drop a specific index
db.employees.dropIndex({ "address.city": 1 })

// Drop index by name
db.employees.dropIndex("address.city_1")

// Drop all indexes except _id
db.employees.dropIndexes()
```

---

### Index Best Practices

1. **Index fields used in `find()`, `sort()`, and `$match`** — such as `email`, `address.city`, `status`, `salary`
2. **Avoid over-indexing** — each index slows down writes
3. **Compound index field order matters** — put equality fields first (`status`), range fields last (`salary`)
4. **Use `explain()` to verify indexes are being used**
5. **Use sparse indexes** for optional fields like `certifications` or `performanceScore` to save space

---

### Covered Query

A query is "covered" when all fields needed are in the index — MongoDB doesn't need to read documents at all:

```javascript
// Create compound index
db.employees.createIndex({ "address.city": 1, salary: 1, firstName: 1 })

// This query is fully covered by the index (no document fetch needed)
db.employees.find(
  { "address.city": "Mumbai" },
  { salary: 1, firstName: 1, _id: 0 }
)
```

### Practice Task 11 (Indexes)
1. Create a single-field index on `salary`
2. Create a compound index on `address.city` and `salary`
3. Use `explain()` to compare query performance before and after creating the index
4. Create a unique index on the `email` field
5. Create a sparse index on `certifications` (since not all employees have this field)
6. List all indexes on the `employees` collection using `getIndexes()`

---

## 16. Utilities

MongoDB comes with several command-line utilities for database administration.

### mongodump — Backup Database

```bash
# Backup entire MongoDB instance
mongodump --out /backup/mongodb/

# Backup the EMS database
mongodump --db ems_db --out /backup/

# Backup only the employees collection
mongodump --db ems_db --collection employees --out /backup/

# Backup from Atlas
mongodump --uri "mongodb+srv://user:pass@cluster0.mongodb.net/ems_db" --out /backup/
```

### mongorestore — Restore Database

```bash
# Restore entire backup
mongorestore /backup/mongodb/

# Restore the EMS database
mongorestore --db ems_db /backup/ems_db/

# Drop existing data before restoring
mongorestore --drop /backup/mongodb/
```

### mongoexport — Export to JSON/CSV

```bash
# Export employees collection to JSON
mongoexport --db ems_db --collection employees --out employees.json

# Export to CSV with specific fields
mongoexport --db ems_db --collection employees --type=csv \
  --fields firstName,lastName,salary,"address.city",status --out employees.csv

# Export with a query filter — only active, high-earning employees
mongoexport --db ems_db --collection employees \
  --query '{"status": "active", "salary": {"$gte": 1500000}}' --out senior_employees.json
```

### mongoimport — Import JSON/CSV

```bash
# Import employees JSON file
mongoimport --db ems_db --collection employees --file employees.json

# Import from a CSV file
mongoimport --db ems_db --collection employees --type=csv \
  --headerline --file employees.csv

# Drop existing collection before import (clean slate)
mongoimport --db ems_db --collection employees --drop --file employees.json

# Import all four EMS collections
mongoimport --db ems_db --collection employees   --jsonArray --file employees.json
mongoimport --db ems_db --collection departments --jsonArray --file departments.json
mongoimport --db ems_db --collection roles       --jsonArray --file roles.json
mongoimport --db ems_db --collection projects    --jsonArray --file projects.json
```

### mongostat — Real-Time Server Statistics

```bash
# View real-time MongoDB server stats
mongostat

# Update every 2 seconds
mongostat 2
```

Output includes: inserts/sec, queries/sec, updates/sec, connections, memory usage.

### mongotop — Collection-Level Activity

```bash
# View read/write activity per collection
mongotop

# Update every 5 seconds
mongotop 5
```

### MongoDB Shell Utilities

```javascript
// Database stats
db.stats()

// Collection stats
db.employees.stats()

// Current operations
db.currentOp()

// Kill a long-running operation
db.killOp(opid)

// Validate a collection
db.employees.validate()

// Compact a collection (reclaim disk space)
db.runCommand({ compact: "employees" })
```

### Monitoring with Atlas

If using Atlas:
- **Performance Advisor:** Automatic slow query detection
- **Real-Time Performance Panel:** Live metrics dashboard
- **Alerts:** Set up CPU/memory/storage alerts
- **Profiler:** Log slow queries

### Practice Task 12 (Utilities)
1. Export the `employees` collection to a JSON file using `mongoexport`
2. Create a new collection `employees_backup` and import the exported JSON file into it
3. Use `db.stats()` and `db.employees.stats()` in `mongosh` to view database and collection statistics
4. Export only `active` employees to a CSV file with `firstName`, `lastName`, `salary`, and `address.city` fields

---

## 17. Wrap Up

### What You've Learned

Congratulations on completing the MongoDB module! Here's a summary of everything covered:

| Topic | Key Takeaways |
|---|---|
| Introduction | MongoDB is a NoSQL document database using BSON/JSON |
| Installation | Local, VPS, and Atlas (Cloud) options available |
| Shell | `mongosh` is a JavaScript-based CLI to interact with MongoDB |
| Data Types | BSON supports strings, integers, doubles, dates, arrays, embedded docs, ObjectId, etc. |
| CRUD | `insertOne/Many`, `find/findOne`, `updateOne/Many`, `deleteOne/Many` |
| Queries | Comparison, logical, array, regex, and element operators |
| Updates | `$set`, `$inc`, `$mul`, `$push`, `$pull`, `$unset`, `$addToSet` |
| Aggregation | Pipeline stages: `$match`, `$group`, `$sort`, `$project`, `$lookup`, `$unwind` |
| Indexes | Speed up queries; types: single, compound, unique, text, sparse, TTL |
| Utilities | `mongodump`, `mongorestore`, `mongoexport`, `mongoimport`, `mongostat` |

---

### Capstone Project: Employee Management System

Build a complete Employee Management System using all four collections.

#### Requirements:

**1. Database Setup**
- Database: `ems_db`
- Collections: `employees`, `departments`, `roles`, `projects`

**2. Insert Data**
- 10 employees with `firstName`, `lastName`, `age`, `email`, `salary`, `status`, `skills` (array), and `address` (embedded)
- 3 departments with `name`, `location`, `annualBudget`, and `managerId`
- 3 roles with `title`, `level`, `baseSalary`, and `permissions` (array)
- 3 projects with `name`, `status`, `techStack` (array), `teamMemberIds`, and `leadId`

**3. Queries**
- Find all employees with salary >= `1500000`
- Find employees from `"Mumbai"` or `"Bengaluru"` whose `skills` include `"Java"`
- Find employees with `status: "active"` who have more than 4 skills (`$size` or `$exists`)

**4. Updates**
- Give a 10% salary hike to all active employees
- Add `"Docker"` to the skills of all employees currently in `"Bengaluru"`
- Update a specific employee's `status` to `"inactive"` and record the `updatedAt` date

**5. Aggregation**
- Average salary per city
- Most common skill across all employees (`$unwind` + `$group`)
- Top 3 employees by salary
- Join employees with departments using `$lookup` to display `firstName`, `lastName`, `salary`, and `departmentName`

**6. Indexes**
- Create a unique index on `email`
- Create a compound index on `address.city` and `salary`
- Create a sparse index on `certifications`
- Use `explain()` to verify index usage on a salary query

**7. Export**
- Export the `employees` collection to CSV with fields: `firstName`, `lastName`, `salary`, `address.city`, `status`

---

### MongoDB with Java — Quick Reference

```xml
<!-- Maven Dependency -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

```java
// Employee Entity
@Document(collection = "employees")
public class Employee {

    @Id
    private String id;

    private String firstName;
    private String lastName;
    private int age;
    private String email;
    private long salary;
    private String status;
    private List<String> skills;
    private Address address;          // Embedded document
    private double performanceScore;
    private boolean isRemote;

    // Getters and setters
}

// Embedded Address class
public class Address {
    private String street;
    private String city;
    private String state;
    private String pincode;
    private String country;
    // Getters and setters
}

// Repository
public interface EmployeeRepository extends MongoRepository<Employee, String> {
    List<Employee> findByAddressCity(String city);
    List<Employee> findBySalaryGreaterThan(long salary);
    List<Employee> findByStatus(String status);
    List<Employee> findBySkillsContaining(String skill);
}

// Service
@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> getHighEarners(long minSalary) {
        return employeeRepository.findBySalaryGreaterThan(minSalary);
    }

    public List<Employee> getEmployeesByCity(String city) {
        return employeeRepository.findByAddressCity(city);
    }

    public List<Employee> getEmployeesBySkill(String skill) {
        return employeeRepository.findBySkillsContaining(skill);
    }
}
```

```properties
# application.properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=ems_db
```

---

### Further Learning Resources

| Resource | Link |
|---|---|
| Official Documentation | https://docs.mongodb.com |
| MongoDB University (Free Courses) | https://university.mongodb.com |
| MongoDB Atlas | https://www.mongodb.com/atlas |
| MongoDB Developer Blog | https://www.mongodb.com/developer |
| Spring Data MongoDB | https://spring.io/projects/spring-data-mongodb |

---

### Quick Reference Cheat Sheet

```javascript
// ===== DATABASE =====
show dbs                          // List databases
use ems_db                        // Switch to EMS database
db.dropDatabase()                 // Drop current database

// ===== COLLECTIONS =====
show collections                  // List collections (employees, departments, roles, projects)
db.createCollection("employees")  // Create collection explicitly
db.employees.drop()               // Drop collection

// ===== CREATE =====
db.employees.insertOne({})
db.employees.insertMany([{},{}])

// ===== READ =====
db.employees.find()
db.employees.find({ "address.city": "Mumbai" })
db.employees.findOne({ email: "ravi.kumar@company.com" })
db.employees.find().sort({ salary: -1 }).limit(5).skip(2)
db.employees.countDocuments({ status: "active" })

// ===== UPDATE =====
db.employees.updateOne({ email: "ravi@co.com" }, { $set: { salary: 1400000 } })
db.employees.updateMany({ status: "active" }, { $mul: { salary: 1.05 } })
db.employees.replaceOne({ filter }, { newDoc })
db.employees.updateOne({ filter }, { $set: {} }, { upsert: true })

// ===== DELETE =====
db.employees.deleteOne({ email: "ravi@co.com" })
db.employees.deleteMany({ status: "inactive" })
db.employees.findOneAndDelete({ firstName: "Rahul" })

// ===== INDEXES =====
db.employees.createIndex({ email: 1 }, { unique: true })
db.employees.createIndex({ "address.city": 1, salary: -1 })
db.employees.createIndex({ certifications: 1 }, { sparse: true })
db.employees.getIndexes()
db.employees.dropIndex({ "address.city": 1 })

// ===== AGGREGATION =====
db.employees.aggregate([
  { $match: { status: "active" } },
  { $group: { _id: "$address.city", avgSalary: { $avg: "$salary" }, count: { $sum: 1 } } },
  { $sort: { avgSalary: -1 } },
  { $limit: 5 }
])

// ===== QUERY OPERATORS =====
// Comparison: $eq $ne $gt $gte $lt $lte $in $nin
// Logical:    $and $or $not $nor
// Element:    $exists $type
// Array:      $all $elemMatch $size
// Evaluation: $regex $expr $where
```

---

*Courseware prepared for IBM Java Full Stack Development Training Program*
*Module: MongoDB | Domain: Employee Management System | Duration: 1.5 Days*
