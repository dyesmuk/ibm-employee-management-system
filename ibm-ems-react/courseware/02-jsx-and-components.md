# Module 02 — JSX and Components

## Learning Objectives
- Understand what JSX really is (and what it compiles to)
- Know all the JSX rules
- Write function components
- Compose components together
- Apply to EMS: create the first `EmployeeCard` component

---

## 2.1 What Is JSX?

JSX looks like HTML inside JavaScript, but it is **not HTML**. It is a syntax extension that Vite/TypeScript compiles into regular JavaScript function calls.

```tsx
// What you write (JSX)
const element = <h1 className="title">Hello World</h1>;

// What it compiles to (JS)
const element = React.createElement('h1', { className: 'title' }, 'Hello World');
```

That `React.createElement` call returns a plain JavaScript object (the "virtual DOM node"):

```js
{
  type: 'h1',
  props: { className: 'title', children: 'Hello World' },
  key: null,
  ref: null
}
```

React uses these objects to build the Virtual DOM tree, then figures out what real DOM changes to make.

> You never call `React.createElement` yourself — you write JSX and the compiler does it.

---

## 2.2 JSX Rules

These are the rules you **must** follow or you'll get compiler errors.

### Rule 1 — Return a single root element

```tsx
// ❌ Error — two root elements
function Bad() {
  return (
    <h1>Title</h1>
    <p>Body</p>
  );
}

// ✅ Wrap in a div
function Good() {
  return (
    <div>
      <h1>Title</h1>
      <p>Body</p>
    </div>
  );
}

// ✅ Or use a Fragment (no extra DOM node)
function Good() {
  return (
    <>
      <h1>Title</h1>
      <p>Body</p>
    </>
  );
}

// ✅ Or use explicit <Fragment> (when you need a key prop)
import { Fragment } from 'react';
function Good() {
  return (
    <Fragment>
      <h1>Title</h1>
    </Fragment>
  );
}
```

### Rule 2 — All tags must be closed

```tsx
// ❌ Error — img, input, br not closed
<img src="photo.jpg">
<input type="text">

// ✅ Self-close them
<img src="photo.jpg" />
<input type="text" />
<br />
```

### Rule 3 — `class` → `className`, `for` → `htmlFor`

```tsx
// ❌ 'class' is a reserved JS keyword
<div class="container">

// ✅ use className
<div className="container">

// ❌ 'for' is reserved too
<label for="name">

// ✅ use htmlFor
<label htmlFor="name">
```

### Rule 4 — camelCase for all HTML attributes

```tsx
// ❌ Wrong (HTML style)
<div onclick="fn()" tabindex="0" data-value="x">

// ✅ Correct (JSX style)
<div onClick={fn} tabIndex={0} data-value="x">
//                             ↑ data-* and aria-* stay hyphenated
```

### Rule 5 — JavaScript expressions go inside `{}`

```tsx
const name = 'Alice';
const isAdmin = true;

// ✅ Variables, expressions, function calls all work inside {}
<p>{name}</p>
<p>{2 + 2}</p>
<p>{name.toUpperCase()}</p>
<p>{isAdmin ? 'Admin' : 'User'}</p>
<img src={employee.avatarUrl} />
```

### Rule 6 — `{}` expects an expression, not a statement

```tsx
// ❌ if statement is not an expression
<p>{if (x > 0) { ... }}</p>

// ✅ ternary is an expression
<p>{x > 0 ? 'positive' : 'zero'}</p>

// ✅ short-circuit works
<p>{isAdmin && <span>Admin</span>}</p>
```

### Full comparison — HTML vs JSX

| HTML | JSX |
|------|-----|
| `class="..."` | `className="..."` |
| `for="..."` | `htmlFor="..."` |
| `onclick="..."` | `onClick={fn}` |
| `tabindex="0"` | `tabIndex={0}` |
| `style="color:red"` | `style={{ color: 'red' }}` |
| `<img>` | `<img />` |
| `<!-- comment -->` | `{/* comment */}` |

---

## 2.3 Expressions vs Statements in JSX

| Expression (✅ allowed in {}) | Statement (❌ not allowed in {}) |
|-------------------------------|----------------------------------|
| `name` | `let name = 'Alice'` |
| `2 + 2` | `if (x > 0) { }` |
| `arr.map(...)` | `for (let i ...)` |
| `condition ? A : B` | `switch (...)` |
| `condition && <El />` | function declarations |
| function calls | `return ...` |

---

## 2.4 Function Components

A React component is a **function** that:
1. Starts with an **uppercase letter**
2. Returns **JSX** (or `null`)

```tsx
// Minimal component
function Greeting() {
  return <h1>Hello, World!</h1>;
}

// Arrow function component (equally valid)
const Greeting = () => <h1>Hello, World!</h1>;

// With multi-line JSX — wrap in parentheses
const Greeting = () => (
  <div>
    <h1>Hello, World!</h1>
    <p>Welcome to EMS</p>
  </div>
);
```

### Why uppercase?

```tsx
// Lowercase → React treats it as an HTML element
<div />     // → creates a <div> DOM node
<greeting /> // → tries to create a <greeting> DOM node (unknown element)

// Uppercase → React treats it as a component function call
<Greeting /> // → calls Greeting() and uses the return value
```

### Returning null

A component can return `null` to render nothing:

```tsx
function DebugBanner() {
  if (!import.meta.env.DEV) return null; // shows nothing in production
  return <div className="debug-banner">Development Mode</div>;
}
```

---

## 2.5 Composing Components

Components are meant to be composed — you build complex UIs by nesting simple components.

```
App
└── Layout
    ├── Header
    │   ├── Logo
    │   └── NavBar
    ├── Main
    │   ├── PageTitle
    │   └── EmployeeGrid
    │       ├── EmployeeCard
    │       ├── EmployeeCard
    │       └── EmployeeCard
    └── Footer
```

```tsx
// Each of these is a separate file/function
function Logo() { return <span className="logo">🏢 IBM EMS</span>; }

function NavBar() { return <nav>...</nav>; }

function Header() {
  return (
    <header>
      <Logo />
      <NavBar />
    </header>
  );
}

function App() {
  return (
    <div>
      <Header />
      <main>...</main>
    </div>
  );
}
```

---

## 2.6 EMS Project — First Component

Let's create our first real component.

### File: `src/components/EmployeeCard.tsx`

Create the folder `src/components/` first.

```tsx
// src/components/EmployeeCard.tsx

function EmployeeCard() {
  return (
    <div style={{
      border: '1px solid #e0e0e0',
      borderRadius: '8px',
      padding: '20px',
      margin: '10px',
      background: 'white',
      maxWidth: '300px',
    }}>
      <h3 style={{ margin: '0 0 8px', color: '#161616' }}>Alice Johnson</h3>
      <p style={{ margin: '4px 0', color: '#525252', fontSize: '14px' }}>
        Department: Engineering
      </p>
      <p style={{ margin: '4px 0', color: '#525252', fontSize: '14px' }}>
        Email: alice@ibm.com
      </p>
      <p style={{ margin: '8px 0 0', fontWeight: '600', color: '#0062ff' }}>
        $95,000
      </p>
    </div>
  );
}

export default EmployeeCard;
```

### Update `src/App.tsx` to use it

```tsx
// src/App.tsx
import EmployeeCard from './components/EmployeeCard';

function App() {
  return (
    <div style={{ padding: '24px' }}>
      <h1>IBM Employee Management System</h1>
      <p style={{ color: '#525252', marginBottom: '24px' }}>
        Manage your organization's workforce
      </p>
      <EmployeeCard />
    </div>
  );
}

export default App;
```

You should see a white card with Alice's details on a grey background.

---

## 2.7 Folder and File Conventions

```
src/
├── components/          # Reusable, shared components
│   └── EmployeeCard.tsx
├── pages/               # Page-level components (used by router)
│   └── EmployeesPage.tsx
├── hooks/               # Custom hooks
│   └── useEmployees.ts
├── services/            # API calls
│   └── employeeService.ts
├── types/               # Shared TypeScript types
│   └── index.ts
├── utils/               # Pure utility functions
│   └── formatters.ts
├── App.tsx
├── main.tsx
└── index.css
```

**One component per file. File name matches component name.**

---

## 2.8 Export Patterns

```tsx
// Named export — component AND types together
export interface Employee { id: number; name: string; }
export function EmployeeCard() { return <div />; }

// Default export — one per file, the main thing
function EmployeeCard() { return <div />; }
export default EmployeeCard;

// Folder barrel — re-export everything from one place
// src/components/index.ts
export { default as EmployeeCard } from './EmployeeCard';
export { default as EmployeeList } from './EmployeeList';

// Import from barrel
import { EmployeeCard, EmployeeList } from './components';
```

---

## 2.9 JSX Comments

```tsx
function App() {
  return (
    <div>
      {/* This is a JSX comment — it does not appear in the output */}
      <h1>IBM EMS</h1>
      {/* TODO: Add employee count */}
    </div>
  );
}
```

---

## Summary

- JSX compiles to `React.createElement()` calls
- Every JSX element must be closed, one root, `className` not `class`
- Components are functions that start with uppercase and return JSX
- Compose big UIs from small, single-purpose components
- We created our first `EmployeeCard` component

**Next → [Module 03: Props, State, and Events](./03-props-state-events.md)**
