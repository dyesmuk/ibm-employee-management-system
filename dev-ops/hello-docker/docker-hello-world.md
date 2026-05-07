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

## What to Do Next

Add a `package.json` and use `npm install` to bring in `express`. The Dockerfile will now need a `RUN npm install` line, and you'll see **image layers** form naturally. That's the segue into the full Docker topic.
