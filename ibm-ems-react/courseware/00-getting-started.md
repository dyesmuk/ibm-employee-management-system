# Module 00 — Getting Started

## Learning Objectives
- Understand what React is and why we use it
- Scaffold a React + TypeScript project with Vite
- Understand every file in the generated project
- Trace the full execution flow from browser load to "Hello World"
- Clean the boilerplate and print your first output

---

## 0.1 What Is React?

React is a **JavaScript library** created by Meta for building user interfaces.

### The key ideas behind React

| Idea | What it means |
|------|--------------|
| **Component-based** | UI is split into small, reusable, self-contained pieces |
| **Declarative** | You describe *what* the UI should look like; React figures out *how* to update the DOM |
| **Virtual DOM** | React maintains a lightweight copy of the real DOM and only patches what actually changed |
| **Unidirectional data flow** | Data flows parent → child; bugs are easier to trace |

### React vs a framework

React handles only the **View** layer. Everything else (routing, state, HTTP) is handled by separate libraries you choose. That's why React is called a *library*, not a framework.

---

## 0.2 Prerequisites

| Tool | Minimum version | Check |
|------|----------------|-------|
| Node.js | 18 LTS | `node -v` |
| npm | 9 | `npm -v` |
| VS Code | latest | — |
| Git | any | `git --version` |

Download Node from [https://nodejs.org](https://nodejs.org) → choose **LTS**.

### VS Code extensions (install these now)

1. **ESLint** — flags code problems inline
2. **Prettier – Code formatter** — auto-formats on save
3. **ES7+ React/Redux/React-Native snippets** — type `rfc` → full component
4. **Auto Rename Tag** — renames closing JSX tag when you edit opening
5. **TypeScript Importer** — auto-adds import statements

---

## 0.3 Creating the Project

```bash
npm create vite@latest ibm-ems-app -- --template react-ts
```

### Command breakdown

| Part | Meaning |
|------|---------|
| `npm create vite@latest` | Run the latest Vite project scaffolder |
| `ibm-ems-app` | Name of the folder to create |
| `--` | Separator — everything after goes to the scaffolder |
| `--template react-ts` | Use the React + TypeScript template |

Run it:

```
✔ Scaffolding project in ./ibm-ems-app...
Done. Now run:

  cd ibm-ems-app
  npm install
  npm run dev
```

```bash
cd ibm-ems-app
npm install
npm run dev
```

You should see:

```
  VITE v6.x.x  ready in 300 ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
  ➜  press h + enter to show help
```

Open **http://localhost:5173** — you'll see the default Vite + React welcome page.

---

## 0.4 Project File Anatomy

```
ibm-ems-app/
│
├── public/                  ← Static files served unchanged
│   └── vite.svg
│
├── src/                     ← All your application code lives here
│   ├── assets/              ← Images, icons, fonts
│   │   └── react.svg
│   ├── App.css              ← Styles scoped to App
│   ├── App.tsx              ← Root component of the app
│   ├── index.css            ← Global stylesheet
│   └── main.tsx             ← Entry point — bootstraps React
│
├── index.html               ← The one HTML file (SPA shell)
├── package.json             ← Dependencies + scripts
├── tsconfig.json            ← TypeScript compiler config
├── tsconfig.node.json       ← TypeScript config for Vite itself
└── vite.config.ts           ← Vite build configuration
```

### Each file explained

#### `index.html`

```html
<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Vite + React + TS</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
```

- The entire app lives inside `<div id="root"></div>`
- The `<script type="module">` tag loads your React code
- This is why it's called a **Single-Page Application** — one HTML file, React handles the rest

#### `src/main.tsx`

```tsx
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
```

This is the **entry point**. It:
1. Finds the `<div id="root">` in the HTML
2. Creates a React root attached to that div
3. Renders your `<App />` component inside `<StrictMode>`

#### `src/App.tsx`

The root component. Everything your user sees is a child of this.

#### `vite.config.ts`

```ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
})
```

Vite is your build tool. The `react()` plugin enables JSX transformation and React Fast Refresh (hot reload).

#### `tsconfig.json`

Tells TypeScript how strict to be, which files to include, and what features to allow.

---

## 0.5 Full Execution Flow (Browser → Screen)

Understanding this flow will help you debug and reason about React apps.

```
① Browser requests http://localhost:5173
        │
        ▼
② Vite dev server responds with index.html
        │
        ▼
③ Browser parses index.html
   — finds <div id="root"> (empty)
   — finds <script src="/src/main.tsx">
        │
        ▼
④ Browser downloads main.tsx
   Vite transforms it:
   — TypeScript → JavaScript
   — JSX → React.createElement() calls
        │
        ▼
⑤ main.tsx runs:
   createRoot(div#root).render(<App />)
        │
        ▼
⑥ React calls the App() function
   App() returns JSX
        │
        ▼
⑦ React converts JSX → Virtual DOM tree
        │
        ▼
⑧ React diffs Virtual DOM vs real DOM
   (first render: everything is new)
        │
        ▼
⑨ React writes actual DOM nodes into div#root
        │
        ▼
⑩ Browser paints the screen ← user sees the UI
```

When you **change state or props**, steps ⑥ → ⑩ repeat, but React only updates the DOM nodes that actually changed (the diff in step ⑧ makes this efficient).

---

## 0.6 Available Scripts

```bash
npm run dev       # Start dev server (hot reload enabled)
npm run build     # Compile + bundle for production → dist/
npm run preview   # Serve the dist/ folder locally
npm run lint      # Run ESLint
```

---

## 0.7 Cleaning the Boilerplate — Hello World

Let's wipe the generated boilerplate and start from zero.

### Step 1 — Replace `src/App.tsx`

Delete everything and replace with:

```tsx
function App() {
  return (
    <div>
      <h1>Hello World</h1>
    </div>
  );
}

export default App;
```

### Step 2 — Replace `src/index.css`

```css
/* Global reset */
*, *::before, *::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
    'Helvetica Neue', Arial, sans-serif;
  font-size: 16px;
  line-height: 1.6;
  color: #161616;
  background-color: #f4f4f4;
}
```

### Step 3 — Delete `src/App.css`

Remove the file. Also remove `import './App.css'` from `App.tsx` if you see it.

### Step 4 — Delete `src/assets/react.svg` and `public/vite.svg`

Not needed for our project.

Save all files. Your browser should hot-reload and show a plain **Hello World** heading.

---

## 0.8 Understanding StrictMode

In `main.tsx`, `<StrictMode>` wraps your app:

```tsx
<StrictMode>
  <App />
</StrictMode>
```

**What it does in development:**
- Renders every component **twice** to catch bugs where rendering has side effects
- Runs every `useEffect` **twice** to check cleanup works correctly
- Warns about deprecated APIs

**In production:** StrictMode is a no-op — no double rendering, no performance cost.

> If you notice something renders twice in the console during development, that's StrictMode working correctly. Don't worry about it.

---

## 0.9 EMS Project Plan

Throughout this course we'll build an **Employee Management System**. Here's what we're going to add, module by module:

| Module | What we add to EMS |
|--------|-------------------|
| 02 | `<EmployeeCard />` component |
| 03 | Employee data as state, add/remove |
| 04 | List of employees, filter by department |
| 05 | Styled layout, cards, badges |
| 07 | Custom hooks, `useEmployees` |
| 08 | Fetch employees from API |
| 09 | Pages: `/`, `/employees`, `/employees/:id` |
| 10 | Create / Edit form with validation |
| 11 | Redux for global state |
| 12 | Login page, protected routes |
| 13 | Tests for all features |
| 14 | Deploy to Vercel |

---

## Summary

- React is a component-based UI library, not a full framework
- Vite is our build tool — fast, modern, zero config
- `index.html` → `main.tsx` → `App.tsx` is the bootstrap chain
- `<StrictMode>` double-renders in dev to catch bugs
- The Virtual DOM + diffing is what makes React performant

**Next → [Module 01: JavaScript & TypeScript Refresh](./01-js-ts-refresh.md)**
