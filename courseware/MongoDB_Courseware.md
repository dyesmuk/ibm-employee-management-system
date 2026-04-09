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

Each section ends with practice tasks. Complete them in order. A final capstone exercise at the end ties everything together using a **Student Management System** dataset.

---

## 2. Introduction to MongoDB

### What is MongoDB?

MongoDB is an open-source, **NoSQL document database** built for high availability, scalability, and flexibility. Unlike traditional relational databases (MySQL, Oracle) that store data in rows and tables, MongoDB stores data as **JSON-like documents**.

> **"MongoDB" comes from the word "humongous" — built to handle massive amounts of data."**

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
  "name": "Ravi Kumar",
  "age": 22,
  "email": "ravi@example.com",
  "courses": ["Java", "MongoDB", "Spring Boot"]
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
mongosh "mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/myFirstDatabase"
```

**Step 6: Connect from Java (Spring Boot)**
```properties
# application.properties
spring.data.mongodb.uri=mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/mydb
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
mongosh "mongodb://localhost:27017/mydb"

# Connect to Atlas
mongosh "mongodb+srv://user:pass@cluster0.mongodb.net/mydb"
```

### Shell Basics

```javascript
// Show current database
db

// List all databases
show dbs

// Switch to or create a database
use studentdb

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
let name = "MongoDB"
print("Hello, " + name)

// Loops
for (let i = 1; i <= 5; i++) {
  print("Student " + i)
}

// Functions
function greet(user) {
  return "Welcome, " + user
}
greet("Ravi")
```

### Shell Tips

```javascript
// Pretty print results
db.students.find().pretty()

// Limit output
db.students.find().limit(5)

// Count documents
db.students.countDocuments()

// Check MongoDB server status
db.serverStatus()
```

### Practice Task 4
- Open `mongosh` and connect to local MongoDB
- Run `show dbs` — note the system databases
- Create a new database called `ibmtraining` using `use ibmtraining`
- Run `db` to confirm the current database

---

## 9. Primary MongoDB Data Types

MongoDB uses **BSON** (Binary JSON) which extends JSON with additional data types.

### Data Types Reference Table

| BSON Type | Example | Description |
|---|---|---|
| String | `"name": "Ravi"` | UTF-8 text |
| Integer (32-bit) | `"age": 22` | Whole numbers |
| Double | `"gpa": 8.5` | Floating point numbers |
| Boolean | `"active": true` | true / false |
| Date | `"dob": ISODate("2002-05-15")` | Date and time |
| ObjectId | `"_id": ObjectId("...")` | Unique 12-byte ID |
| Array | `"courses": ["Java","MongoDB"]` | List of values |
| Embedded Document | `"address": {"city": "Mumbai"}` | Nested document |
| Null | `"middleName": null` | No value |
| Regular Expression | `"pattern": /^Ravi/` | Regex |
| Binary Data | `"photo": BinData(...)` | Binary content |
| Timestamp | Internal MongoDB use | |
| Long (64-bit Integer) | `"bigNum": NumberLong("123456789012")` | Large integers |
| Decimal128 | `"price": NumberDecimal("19.99")` | High precision decimals |

### Examples in Practice

```javascript
// Insert a document showcasing multiple data types
db.students.insertOne({
  name: "Priya Sharma",                          // String
  age: 21,                                        // Integer
  gpa: 9.1,                                       // Double
  isEnrolled: true,                               // Boolean
  enrolledDate: new Date("2024-07-01"),           // Date
  courses: ["Java", "Spring", "MongoDB"],         // Array
  address: {                                      // Embedded Document
    street: "12 MG Road",
    city: "Bengaluru",
    pincode: "560001"
  },
  profilePic: null,                               // Null
  studentId: NumberLong("202400123456")           // Long Integer
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
- Create a collection `employees` in the `ibmtraining` database
- Insert one document with at least 6 different data types
- Use `db.employees.find().pretty()` to view the result

---

## 10. CRUD Operations

CRUD stands for **Create, Read, Update, Delete** — the four fundamental operations of any database.

---

### Setup: Sample Dataset

Before we begin, let's create our practice collection:

```javascript
use ibmtraining

db.students.insertMany([
  { name: "Ravi Kumar", age: 22, city: "Mumbai", gpa: 8.5, courses: ["Java", "MongoDB"] },
  { name: "Priya Sharma", age: 21, city: "Bengaluru", gpa: 9.1, courses: ["Python", "Django"] },
  { name: "Amit Singh", age: 23, city: "Delhi", gpa: 7.8, courses: ["Java", "Spring Boot"] },
  { name: "Neha Patel", age: 20, city: "Pune", gpa: 8.9, courses: ["Java", "MongoDB", "React"] },
  { name: "Suresh Reddy", age: 24, city: "Hyderabad", gpa: 7.5, courses: ["Node.js", "MongoDB"] }
])
```

---

### CREATE — Inserting Documents

#### `insertOne()` — Insert a Single Document

```javascript
db.students.insertOne({
  name: "Kavitha Nair",
  age: 22,
  city: "Chennai",
  gpa: 8.7,
  courses: ["Java", "AWS"]
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
db.students.insertMany([
  { name: "Rahul Verma", age: 23, city: "Jaipur", gpa: 8.0, courses: ["Java"] },
  { name: "Sneha Joshi", age: 21, city: "Kolkata", gpa: 9.3, courses: ["MongoDB", "Express"] }
])
```

#### Insert Options

```javascript
// Insert with a custom _id
db.students.insertOne({
  _id: "STU001",
  name: "Custom ID Student",
  age: 22
})

// Ordered vs Unordered inserts
// ordered: true (default) — stops on first error
// ordered: false — continues past errors
db.students.insertMany(
  [{ name: "A" }, { name: "B" }, { name: "C" }],
  { ordered: false }
)
```

---

### READ — Finding Documents

#### `find()` — Retrieve Documents

```javascript
// Find all documents
db.students.find()

// Pretty formatted output
db.students.find().pretty()

// Find with a filter (all students from Mumbai)
db.students.find({ city: "Mumbai" })

// Find with multiple conditions (age = 22 AND city = Mumbai)
db.students.find({ age: 22, city: "Mumbai" })
```

#### `findOne()` — Retrieve First Matching Document

```javascript
db.students.findOne({ name: "Ravi Kumar" })
```

#### Projection — Select Specific Fields

```javascript
// Show only name and city (1 = include, 0 = exclude)
db.students.find({}, { name: 1, city: 1, _id: 0 })

// Exclude a specific field
db.students.find({}, { gpa: 0 })
```

#### Cursor Methods

```javascript
// Limit results
db.students.find().limit(3)

// Skip documents
db.students.find().skip(2)

// Sort — 1 = Ascending, -1 = Descending
db.students.find().sort({ gpa: -1 })       // Sort by GPA descending
db.students.find().sort({ name: 1 })       // Sort by name alphabetically

// Chain methods
db.students.find().sort({ gpa: -1 }).limit(3)

// Count documents
db.students.countDocuments({ city: "Bengaluru" })
```

### Practice Task 6 (CRUD – Create & Read)
1. Insert 3 more students of your choice into the `students` collection
2. Find all students whose city is "Delhi"
3. Retrieve only the `name` and `gpa` fields for all students, sorted by `gpa` descending
4. Find the student with the highest GPA using `sort` and `limit`

---

## 11. MongoDB Queries

### Comparison Operators

```javascript
// $eq — Equal to (default behavior)
db.students.find({ age: { $eq: 22 } })

// $ne — Not equal
db.students.find({ city: { $ne: "Mumbai" } })

// $gt — Greater than
db.students.find({ gpa: { $gt: 8.5 } })

// $gte — Greater than or equal
db.students.find({ gpa: { $gte: 8.5 } })

// $lt — Less than
db.students.find({ age: { $lt: 22 } })

// $lte — Less than or equal
db.students.find({ age: { $lte: 22 } })

// $in — Value matches any in array
db.students.find({ city: { $in: ["Mumbai", "Pune", "Bengaluru"] } })

// $nin — Value does not match any in array
db.students.find({ city: { $nin: ["Delhi", "Hyderabad"] } })
```

### Logical Operators

```javascript
// $and — All conditions must be true
db.students.find({
  $and: [
    { age: { $gte: 21 } },
    { gpa: { $gte: 8.0 } }
  ]
})

// $or — At least one condition must be true
db.students.find({
  $or: [
    { city: "Mumbai" },
    { gpa: { $gte: 9.0 } }
  ]
})

// $not — Negates a condition
db.students.find({ gpa: { $not: { $gte: 8.0 } } })

// $nor — All conditions must be false
db.students.find({
  $nor: [
    { city: "Delhi" },
    { gpa: { $lt: 7.0 } }
  ]
})
```

### Element Operators

```javascript
// $exists — Field exists
db.students.find({ gpa: { $exists: true } })
db.students.find({ middleName: { $exists: false } })

// $type — Field is of a specific type
db.students.find({ age: { $type: "int" } })
db.students.find({ name: { $type: "string" } })
```

### Array Operators

```javascript
// $all — Array contains all specified values
db.students.find({ courses: { $all: ["Java", "MongoDB"] } })

// $elemMatch — At least one element matches conditions
db.students.find({ scores: { $elemMatch: { $gt: 85, $lt: 95 } } })

// $size — Array has exact number of elements
db.students.find({ courses: { $size: 3 } })

// Query for specific element in array
db.students.find({ courses: "Java" })   // courses array contains "Java"
```

### Evaluation Operators

```javascript
// $regex — Pattern match
db.students.find({ name: { $regex: /^Ravi/i } })     // Starts with "Ravi" (case-insensitive)
db.students.find({ name: { $regex: "Kumar$" } })     // Ends with "Kumar"
db.students.find({ name: { $regex: ".*Singh.*" } })  // Contains "Singh"

// $where — JavaScript expression (use sparingly, slow)
db.students.find({ $where: "this.age > 21 && this.gpa > 8" })

// $expr — Use aggregation expressions in queries
db.students.find({ $expr: { $gt: ["$gpa", 8.0] } })
```

### Querying Embedded Documents

```javascript
// Sample document with embedded address
db.employees.insertOne({
  name: "Arjun Mehta",
  address: { city: "Mumbai", state: "Maharashtra", pincode: "400001" }
})

// Exact match on embedded document (field order matters!)
db.employees.find({ address: { city: "Mumbai", state: "Maharashtra", pincode: "400001" } })

// Dot notation — query nested fields (recommended)
db.employees.find({ "address.city": "Mumbai" })
db.employees.find({ "address.pincode": { $regex: /^400/ } })
```

### Practice Task 7 (Queries)
1. Find all students with GPA between 8.0 and 9.0 (inclusive)
2. Find students from either "Mumbai" or "Bengaluru" who have GPA above 8.5
3. Find students whose `courses` array contains both "Java" and "MongoDB"
4. Find all students whose name starts with the letter "R"
5. Find students whose `courses` array has exactly 2 elements

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
// Update a student's GPA
db.students.updateOne(
  { name: "Ravi Kumar" },       // Filter
  { $set: { gpa: 9.0 } }       // Update
)

// Set multiple fields at once
db.students.updateOne(
  { name: "Ravi Kumar" },
  { $set: { gpa: 9.0, city: "Pune", updatedAt: new Date() } }
)
```

### `updateMany()` — Update All Matching Documents

```javascript
// Add a "status" field to all students with GPA >= 9.0
db.students.updateMany(
  { gpa: { $gte: 9.0 } },
  { $set: { status: "Merit" } }
)

// Increment age of all students by 1
db.students.updateMany({}, { $inc: { age: 1 } })
```

### `replaceOne()` — Replace Entire Document

```javascript
// Replaces the ENTIRE document (except _id)
db.students.replaceOne(
  { name: "Suresh Reddy" },
  {
    name: "Suresh Reddy",
    age: 25,
    city: "Chennai",
    gpa: 8.2,
    courses: ["Java", "Kafka"]
  }
)
```

> ⚠️ **Warning:** `replaceOne` removes all existing fields and replaces with the new document. Use `updateOne` with `$set` to update specific fields only.

### Field Operators

```javascript
// $unset — Remove a field
db.students.updateOne(
  { name: "Ravi Kumar" },
  { $unset: { status: "" } }
)

// $rename — Rename a field
db.students.updateMany({}, { $rename: { "city": "location" } })

// $inc — Increment
db.students.updateOne(
  { name: "Priya Sharma" },
  { $inc: { age: 1, gpa: 0.2 } }
)

// $mul — Multiply
db.students.updateOne(
  { name: "Amit Singh" },
  { $mul: { gpa: 1.1 } }   // Increase GPA by 10%
)

// $min / $max — Conditional update
db.students.updateOne(
  { name: "Neha Patel" },
  { $min: { gpa: 8.0 } }   // Only update if current gpa > 8.0
)

// $currentDate — Set to today's date
db.students.updateOne(
  { name: "Ravi Kumar" },
  { $currentDate: { lastModified: true } }
)
```

### Array Update Operators

```javascript
// $push — Add element to array
db.students.updateOne(
  { name: "Ravi Kumar" },
  { $push: { courses: "Docker" } }
)

// $push with $each — Add multiple elements
db.students.updateOne(
  { name: "Priya Sharma" },
  { $push: { courses: { $each: ["Kubernetes", "AWS"] } } }
)

// $addToSet — Add only if not already present (avoids duplicates)
db.students.updateOne(
  { name: "Amit Singh" },
  { $addToSet: { courses: "Java" } }   // "Java" won't be added again
)

// $pop — Remove first (-1) or last (1) element
db.students.updateOne(
  { name: "Neha Patel" },
  { $pop: { courses: 1 } }   // Remove last course
)

// $pull — Remove elements matching a condition
db.students.updateOne(
  { name: "Ravi Kumar" },
  { $pull: { courses: "Docker" } }   // Remove "Docker" from array
)

// $pullAll — Remove multiple specific values
db.students.updateOne(
  { name: "Suresh Reddy" },
  { $pullAll: { courses: ["Node.js", "MongoDB"] } }
)
```

### Upsert — Update or Insert

If no document matches the filter, upsert inserts a new document:

```javascript
db.students.updateOne(
  { name: "New Student" },
  { $set: { name: "New Student", age: 20, city: "Nagpur", gpa: 7.5 } },
  { upsert: true }   // Creates document if not found
)
```

### Practice Task 8 (Update)
1. Update Priya Sharma's GPA to 9.5
2. Add the course "DevOps" to all students from Mumbai
3. Remove the field `status` from all documents where it exists
4. Use `$inc` to increase the age of all students by 1
5. Use upsert to insert a new student "Manish Tiwari" if they don't already exist

---

## 13. Delete Operations

### `deleteOne()` — Delete First Matching Document

```javascript
// Delete a specific student
db.students.deleteOne({ name: "Rahul Verma" })

// Delete by _id
db.students.deleteOne({ _id: ObjectId("64a7f2c3e4b0a1d2e3f4a5b6") })
```

### `deleteMany()` — Delete All Matching Documents

```javascript
// Delete all students with GPA below 7.5
db.students.deleteMany({ gpa: { $lt: 7.5 } })

// Delete all students from Delhi
db.students.deleteMany({ city: "Delhi" })

// ⚠️ Delete ALL documents in a collection (collection still exists)
db.students.deleteMany({})
```

### `findOneAndDelete()` — Delete and Return the Document

```javascript
// Returns the deleted document
const deletedDoc = db.students.findOneAndDelete({ name: "Sneha Joshi" })
print(deletedDoc)
```

### Dropping a Collection vs Deleting Documents

```javascript
// Delete all documents — keeps the collection and its indexes
db.students.deleteMany({})

// Drop the collection entirely — removes collection, indexes, and metadata
db.students.drop()

// Drop the entire database
db.dropDatabase()
```

> ⚠️ **Important:** There is no "recycle bin" in MongoDB. Deleted data is gone permanently unless you have backups.

### Safe Delete Pattern

Always preview what you're about to delete with `find()` first:

```javascript
// Step 1: Preview
db.students.find({ gpa: { $lt: 7.0 } })

// Step 2: Delete (only after confirming)
db.students.deleteMany({ gpa: { $lt: 7.0 } })
```

### Practice Task 9 (Delete)
1. Delete one student of your choice by name
2. Delete all students from "Jaipur"
3. Use `findOneAndDelete` to delete a student and print the returned document
4. Drop the `employees` collection you created earlier

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
db.students.aggregate([
  { $match: { gpa: { $gte: 8.0 } } }
])
```

---

#### `$group` — Group and Summarize

```javascript
// Count students per city
db.students.aggregate([
  {
    $group: {
      _id: "$city",           // Group by city
      count: { $sum: 1 },     // Count per group
      avgGPA: { $avg: "$gpa" }  // Average GPA per city
    }
  }
])

// Total, min, max GPA
db.students.aggregate([
  {
    $group: {
      _id: null,               // null means no grouping (entire collection)
      totalStudents: { $sum: 1 },
      avgGPA: { $avg: "$gpa" },
      maxGPA: { $max: "$gpa" },
      minGPA: { $min: "$gpa" }
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
// Show only name and computed field
db.students.aggregate([
  {
    $project: {
      name: 1,
      gpa: 1,
      _id: 0,
      gradeCategory: {
        $cond: {
          if: { $gte: ["$gpa", 9.0] },
          then: "Distinction",
          else: "Pass"
        }
      }
    }
  }
])
```

---

#### `$sort` — Sort Documents

```javascript
db.students.aggregate([
  { $sort: { gpa: -1 } }   // Sort by GPA descending
])
```

---

#### `$limit` and `$skip`

```javascript
db.students.aggregate([
  { $sort: { gpa: -1 } },
  { $skip: 2 },
  { $limit: 3 }
])
```

---

#### `$unwind` — Deconstruct Array Fields

```javascript
// Creates one document per course per student
db.students.aggregate([
  { $unwind: "$courses" }
])

// Count students per course
db.students.aggregate([
  { $unwind: "$courses" },
  { $group: { _id: "$courses", studentCount: { $sum: 1 } } },
  { $sort: { studentCount: -1 } }
])
```

---

#### `$lookup` — Join Collections (like SQL JOIN)

```javascript
// Sample: join students with enrollments collection
db.enrollments.insertMany([
  { studentName: "Ravi Kumar", subject: "Advanced Java", semester: 3 },
  { studentName: "Priya Sharma", subject: "Machine Learning", semester: 3 }
])

db.students.aggregate([
  {
    $lookup: {
      from: "enrollments",           // Collection to join
      localField: "name",            // Field in students
      foreignField: "studentName",   // Field in enrollments
      as: "enrollmentDetails"        // Output array field
    }
  }
])
```

---

#### `$addFields` — Add New Fields

```javascript
db.students.aggregate([
  {
    $addFields: {
      ageInMonths: { $multiply: ["$age", 12] },
      fullLabel: { $concat: ["$name", " - ", "$city"] }
    }
  }
])
```

---

#### `$count` — Count Documents

```javascript
db.students.aggregate([
  { $match: { gpa: { $gte: 8.0 } } },
  { $count: "highPerformers" }
])
```

---

#### Complete Pipeline Example

```javascript
// Find top 3 cities by average GPA, considering only students with GPA >= 7.5
db.students.aggregate([
  { $match: { gpa: { $gte: 7.5 } } },                    // Stage 1: Filter
  {
    $group: {                                              // Stage 2: Group by city
      _id: "$city",
      avgGPA: { $avg: "$gpa" },
      studentCount: { $sum: 1 },
      names: { $push: "$name" }
    }
  },
  { $sort: { avgGPA: -1 } },                             // Stage 3: Sort
  { $limit: 3 },                                          // Stage 4: Top 3
  {
    $project: {                                            // Stage 5: Reshape output
      city: "$_id",
      avgGPA: { $round: ["$avgGPA", 2] },
      studentCount: 1,
      names: 1,
      _id: 0
    }
  }
])
```

### Practice Task 10 (Aggregation)
1. Find the total number of students in each city
2. Calculate the average, minimum, and maximum GPA across all students
3. Find the most popular course (the one enrolled in by the most students)
4. List all students with GPA >= 8.5, showing only name and GPA, sorted by GPA descending
5. Use `$lookup` to join `students` with a new `scores` collection you create

---

## 15. Indexes

### What are Indexes?

An **index** is a data structure that stores a small portion of the collection's data in an easy-to-traverse form. Without indexes, MongoDB must perform a **collection scan** (read every document) to find matches.

> **Analogy:** An index in MongoDB is like the index at the back of a book — instead of reading every page, you jump directly to the right page.

### How to Check if an Index is Being Used

```javascript
// Use explain() to see query execution plan
db.students.find({ city: "Mumbai" }).explain("executionStats")

// Look for: "COLLSCAN" (bad) vs "IXSCAN" (good)
```

---

### Creating Indexes

#### Single Field Index

```javascript
// Create index on the "city" field
db.students.createIndex({ city: 1 })   // 1 = Ascending, -1 = Descending
```

#### Compound Index (Multiple Fields)

```javascript
// Index on both city and gpa
db.students.createIndex({ city: 1, gpa: -1 })
```

#### Unique Index

```javascript
// Ensure no two documents have the same email
db.students.createIndex({ email: 1 }, { unique: true })
```

#### Text Index (Full-Text Search)

```javascript
// Create a text index on name and city
db.students.createIndex({ name: "text", city: "text" })

// Use text search
db.students.find({ $text: { $search: "Ravi Mumbai" } })
db.students.find({ $text: { $search: "\"Ravi Kumar\"" } })   // Exact phrase
```

#### Sparse Index

Indexes only documents that have the indexed field:

```javascript
db.students.createIndex({ email: 1 }, { sparse: true })
```

#### TTL Index (Time-To-Live)

Automatically deletes documents after a specified time:

```javascript
// Delete sessions after 3600 seconds (1 hour)
db.sessions.createIndex({ createdAt: 1 }, { expireAfterSeconds: 3600 })
```

---

### Managing Indexes

```javascript
// List all indexes on a collection
db.students.getIndexes()

// Drop a specific index
db.students.dropIndex({ city: 1 })

// Drop index by name
db.students.dropIndex("city_1")

// Drop all indexes except _id
db.students.dropIndexes()
```

---

### Index Best Practices

1. **Index fields used in `find()`, `sort()`, and `$match`**
2. **Avoid over-indexing** — each index slows down writes
3. **Compound index field order matters** — put equality fields first, range fields last
4. **Use `explain()` to verify indexes are being used**
5. **Use sparse indexes** for optional fields to save space

---

### Covered Query

A query is "covered" when all fields are in the index — MongoDB doesn't need to read documents at all:

```javascript
// Create compound index
db.students.createIndex({ city: 1, gpa: 1, name: 1 })

// This query is fully covered by the index
db.students.find(
  { city: "Mumbai" },
  { gpa: 1, name: 1, _id: 0 }
)
```

### Practice Task 11 (Indexes)
1. Create a single-field index on `gpa`
2. Create a compound index on `city` and `gpa`
3. Use `explain()` to compare query performance before and after creating an index
4. Create a unique index on an `email` field
5. List all indexes on the `students` collection using `getIndexes()`

---

## 16. Utilities

MongoDB comes with several command-line utilities for database administration.

### mongodump — Backup Database

```bash
# Backup entire MongoDB instance
mongodump --out /backup/mongodb/

# Backup a specific database
mongodump --db ibmtraining --out /backup/

# Backup a specific collection
mongodump --db ibmtraining --collection students --out /backup/

# Backup from Atlas
mongodump --uri "mongodb+srv://user:pass@cluster0.mongodb.net/ibmtraining" --out /backup/
```

### mongorestore — Restore Database

```bash
# Restore entire backup
mongorestore /backup/mongodb/

# Restore specific database
mongorestore --db ibmtraining /backup/ibmtraining/

# Drop existing data before restoring
mongorestore --drop /backup/mongodb/
```

### mongoexport — Export to JSON/CSV

```bash
# Export collection to JSON
mongoexport --db ibmtraining --collection students --out students.json

# Export to CSV with specific fields
mongoexport --db ibmtraining --collection students --type=csv \
  --fields name,age,city,gpa --out students.csv

# Export with a query filter
mongoexport --db ibmtraining --collection students \
  --query '{"gpa": {"$gte": 8.0}}' --out highperformers.json
```

### mongoimport — Import JSON/CSV

```bash
# Import JSON file
mongoimport --db ibmtraining --collection students --file students.json

# Import CSV file
mongoimport --db ibmtraining --collection students --type=csv \
  --headerline --file students.csv

# Drop existing collection before import
mongoimport --db ibmtraining --collection students --drop --file students.json
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
db.students.stats()

// Current operations
db.currentOp()

// Kill a long-running operation
db.killOp(opid)

// Validate a collection
db.students.validate()

// Compact a collection (reclaim disk space)
db.runCommand({ compact: "students" })
```

### Monitoring with Atlas

If using Atlas:
- **Performance Advisor:** Automatic slow query detection
- **Real-Time Performance Panel:** Live metrics dashboard
- **Alerts:** Set up CPU/memory/storage alerts
- **Profiler:** Log slow queries

### Practice Task 12 (Utilities)
1. Export the `students` collection to a JSON file using `mongoexport`
2. Create a new collection `students_backup` and import the JSON file into it
3. Use `db.stats()` and `db.students.stats()` in `mongosh` to view database/collection statistics

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
| Updates | `$set`, `$inc`, `$push`, `$pull`, `$unset`, `$addToSet` |
| Aggregation | Pipeline stages: `$match`, `$group`, `$sort`, `$project`, `$lookup`, `$unwind` |
| Indexes | Speed up queries; types: single, compound, unique, text, TTL |
| Utilities | `mongodump`, `mongorestore`, `mongoexport`, `mongoimport`, `mongostat` |

---

### Capstone Project: Student Management System

Build a complete Student Management System using MongoDB:

#### Requirements:

**1. Database Setup**
- Database: `sms_db`
- Collections: `students`, `courses`, `enrollments`, `faculty`

**2. Insert Data**
- 10 students with name, age, email, city, GPA, and address (embedded)
- 5 courses with name, duration, and faculty reference
- Enrollment records linking students to courses

**3. Queries**
- Find all students with GPA >= 8.5
- Find students enrolled in "Advanced Java"
- List cities with more than 2 students

**4. Updates**
- Update GPA for a specific student
- Add a new course to a student's enrollment

**5. Aggregation**
- Average GPA per city
- Most enrolled course
- Top 3 students by GPA

**6. Indexes**
- Create indexes on `email` (unique) and `gpa`
- Use `explain()` to verify usage

**7. Export**
- Export the `students` collection to CSV

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
// Student Entity
@Document(collection = "students")
public class Student {
    @Id
    private String id;
    private String name;
    private int age;
    private String city;
    private double gpa;
    private List<String> courses;
    // getters and setters
}

// Repository
public interface StudentRepository extends MongoRepository<Student, String> {
    List<Student> findByCity(String city);
    List<Student> findByGpaGreaterThan(double gpa);
}

// Service
@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;

    public List<Student> getTopStudents(double minGpa) {
        return studentRepository.findByGpaGreaterThan(minGpa);
    }
}
```

```properties
# application.properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=ibmtraining
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
use mydb                          // Switch database
db.dropDatabase()                 // Drop current database

// ===== COLLECTIONS =====
show collections                  // List collections
db.createCollection("name")       // Create collection
db.name.drop()                    // Drop collection

// ===== CREATE =====
db.col.insertOne({})
db.col.insertMany([{},{}])

// ===== READ =====
db.col.find()
db.col.find({ field: value })
db.col.findOne({ field: value })
db.col.find().sort({field:1}).limit(5).skip(2)
db.col.countDocuments({})

// ===== UPDATE =====
db.col.updateOne({ filter }, { $set: { field: val } })
db.col.updateMany({ filter }, { $set: { field: val } })
db.col.replaceOne({ filter }, { newDoc })
db.col.updateOne({ filter }, { $set: {} }, { upsert: true })

// ===== DELETE =====
db.col.deleteOne({ filter })
db.col.deleteMany({ filter })
db.col.findOneAndDelete({ filter })

// ===== INDEXES =====
db.col.createIndex({ field: 1 })
db.col.createIndex({ f1: 1, f2: -1 })
db.col.getIndexes()
db.col.dropIndex({ field: 1 })

// ===== AGGREGATION =====
db.col.aggregate([
  { $match: {} },
  { $group: { _id: "$field", count: { $sum: 1 } } },
  { $sort: { count: -1 } },
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
*Module: MongoDB | Duration: 1.5 Days*
