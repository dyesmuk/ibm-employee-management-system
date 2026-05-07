# Docker Hello World — Node.js

## The App (6 lines of Node.js)

Create a folder called `hello-docker` and put this inside as `app.js`:

```js
const http = require('http');

const server = http.createServer((req, res) => {
  res.end('Hello from inside Docker!');
});

server.listen(3000, () => console.log('Server running on port 3000'));
```

No npm install, no dependencies, no framework. Just Node's built-in `http` module.

---

## The Dockerfile

Same folder, file called `Dockerfile` (no extension):

```dockerfile
FROM node:18-alpine

WORKDIR /app

COPY app.js .

CMD ["node", "app.js"]
```

| Line | What it means |
|---|---|
| `FROM node:18-alpine` | Start from an existing image that has Node installed (alpine = tiny Linux) |
| `WORKDIR /app` | All commands from here run inside `/app` in the container |
| `COPY app.js .` | Copy your file from your laptop into the container |
| `CMD [...]` | The command to run when the container starts |

---

## The Four Commands to Run

Open a terminal inside the `hello-docker` folder:

```bash
# 1. Build the image — give it a name
docker build -t hello-docker .

# 2. Run it — map your laptop's port 3000 to the container's port 3000
docker run -p 3000:3000 hello-docker
```

Now open a browser → `http://localhost:3000`

You'll see: **Hello from inside Docker!**

Then open a second terminal and run:

```bash
# 3. See the running container
docker ps

# 4. Stop it
docker stop <container-id>
```

---

## The Mental Model

```
Your files          Image                    Container
(app.js +      →   (Node runtime +      →   (Running process,
Dockerfile)         your app, frozen)         isolated, alive)

   Recipe        Cake mould (reusable)       Actual cake
```

- `docker build` turns your files into an **image**
- `docker run` turns an image into a **container**

---

## Three Questions to Ask Trainees

**1. "What happens if you run `docker run -p 3000:3000 hello-docker` again in another terminal?"**
→ A second container starts. Same image, two independent running containers. This is how scaling works.

**2. "Can you change `app.js` and refresh the browser?"**
→ No. The change doesn't reflect. You have to `docker build` again. This teaches that an image is **immutable** — a snapshot, not a live folder.

**3. "Where is Node.js installed on your laptop?"**
→ It isn't (or it doesn't matter). Node lives *inside* the container. This is the isolation point — the whole runtime is bundled.

---

## Starting a Stopped Container

A stopped container still exists — it's just paused, not deleted.

```bash
# See all containers — including stopped ones
docker ps -a

# Start the same container again (use the container ID or name)
docker start <container-id>
```

Browser → `http://localhost:3000` will work again.

### The Full Container Lifecycle

| Command | What it does |
|---|---|
| `docker run` | Creates a **brand new** container from the image and starts it |
| `docker start` | Starts an **existing** (stopped) container |
| `docker stop` | Stops a running container (container still exists) |
| `docker rm` | **Deletes** the container permanently |

```
run → stop → start → stop → start ... → rm
```

> `docker run` is only used **once** per container. After that it's `start` and `stop`.

### Useful: Start with Logs Visible

```bash
# Start and see logs in the terminal (attached mode)
docker start -a <container-id>
```

Without `-a` it starts in the background silently. With `-a` you see the console output — useful when debugging.

---

## Step 2 — Adding Express and Seeing Image Layers

Still inside the same `hello-docker` folder. We're building on top of it.

### Step 1 — Update `app.js`

Replace the contents with this Express version:

```js
const express = require('express');
const app = express();

app.get('/', (req, res) => {
  res.send('Hello from Express inside Docker!');
});

app.listen(3000, () => console.log('Server running on port 3000'));
```

### Step 2 — Create `package.json`

Same folder, new file `package.json`:

```json
{
  "name": "hello-docker",
  "version": "1.0.0",
  "main": "app.js",
  "dependencies": {
    "express": "4.18.2"
  }
}
```

Your folder should now look like this:

```
hello-docker/
  ├── app.js
  ├── package.json
  └── Dockerfile
```

### Step 3 — Update the Dockerfile

```dockerfile
FROM node:18-alpine

WORKDIR /app

COPY package.json .

RUN npm install

COPY app.js .

CMD ["node", "app.js"]
```

> Notice the order — `package.json` is copied **before** `app.js`, and `RUN npm install` sits between them. This is intentional — see the Layer Caching section below.

### Step 4 — Build and Run

```bash
# Build with a new name to keep things clean
docker build -t hello-express .

# Run it
docker run -p 3000:3000 hello-express
```

Browser → `http://localhost:3000` → **Hello from Express inside Docker!**

---

## Image Layers and Caching

Run the build a second time without changing anything:

```bash
docker build -t hello-express .
```

You'll see output like this:

```
 => CACHED [2/5] WORKDIR /app
 => CACHED [3/5] COPY package.json .
 => CACHED [4/5] RUN npm install        ← pulled from cache, not re-run!
 => CACHED [5/5] COPY app.js .
```

Every line in the Dockerfile is a **layer**. Docker caches each layer. If nothing changed on that layer, it reuses the cache — making builds much faster.

Now change one word in `app.js` and build again:

```bash
docker build -t hello-express .
```

```
 => CACHED [3/5] COPY package.json .
 => CACHED [4/5] RUN npm install        ← still cached!
 => [5/5] COPY app.js .                 ← only this re-runs
```

`npm install` is skipped because `package.json` didn't change. Only the `app.js` layer rebuilds.

### Why Order Matters

Swap the Dockerfile so `COPY app.js .` comes before `RUN npm install`:

```dockerfile
# BAD ORDER — don't do this
COPY app.js .
COPY package.json .
RUN npm install
```

Change one word in `app.js` and build again — now `npm install` re-runs every single time, even though dependencies didn't change. Slow and wasteful.

**The rule:** copy what changes least, first. Dependencies before source code — always.

---

## What Trainees Have Learned Across Both Steps

| Concept | Where they saw it |
|---|---|
| Image is immutable | Had to rebuild after changing `app.js` |
| Container lifecycle | `run → stop → start → rm` |
| Dockerfile instructions | `FROM`, `WORKDIR`, `COPY`, `RUN`, `CMD` |
| Layer caching | `npm install` skipped on second build |
| Layer order matters | Bad order = slow builds |

---

## What to Do Next — Docker Compose

The next step is **Docker Compose** — running multiple containers together (e.g. the Express app + MongoDB) with a single `docker-compose.yml` file and one command: `docker compose up`.
