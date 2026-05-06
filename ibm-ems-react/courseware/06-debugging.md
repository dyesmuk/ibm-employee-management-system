# Module 06 — Debugging React Apps

## Learning Objectives
- Use React DevTools to inspect component trees, props, and state
- Apply systematic console debugging strategies
- Identify and fix the most common React bugs
- Implement Error Boundaries for graceful failure

---

## 6.1 React DevTools

The single most important debugging tool for React developers.

### Installation
- **Chrome** → search "React Developer Tools" in Chrome Web Store
- **Firefox** → search in Firefox Add-ons

After installing, open DevTools (`F12` or `Cmd+Option+I`) — you'll see two new tabs: **Components** and **Profiler**.

### Components Tab

```
▼ App                         ← click any component
  ▼ Header
  ▼ main
    ▼ EmployeeCard
        props
          employee: {id:1, name:"Alice"...}
          onRemove: ƒ()
        hooks
          State: [{id:1,name:...}, {id:2,...}]   ← useState value
          State: "All"                            ← filter state
```

**What you can do:**
- Click any component → see its current **props** and **state** on the right
- **Edit state live** — click the pencil icon next to a state value and change it
- **Search** for a component by name in the search bar
- **Highlight updates** — settings ⚙ → "Highlight updates" → components flash when they re-render

### Profiler Tab

1. Click the record button ●
2. Interact with your app (add employee, filter, etc.)
3. Stop recording
4. Each bar = one render; height = how long it took
5. Click a bar → see which component re-rendered and why

---

## 6.2 Console Strategies

```tsx
// Basic logging
console.log('employees:', employees);
console.log('filter:', filter, 'filtered:', filtered);

// Table format — great for arrays of objects
console.table(employees);

// Grouped logs
console.group('EmployeeCard render');
console.log('employee:', employee);
console.log('isActive:', employee.isActive);
console.groupEnd();

// Warnings and errors
console.warn('No employees found — showing empty state');
console.error('Failed to parse employee data:', raw);

// Time something
console.time('filter operation');
const result = employees.filter(/* ... */);
console.timeEnd('filter operation');

// Conditional — only log in dev
if (import.meta.env.DEV) {
  console.log('Debug:', someValue);
}
```

---

## 6.3 The `debugger` Statement

Execution pauses here when DevTools is open:

```tsx
const handleAdd = () => {
  debugger; // ← execution pauses, you can inspect variables
  if (!newName.trim()) return;
  // ...
};
```

You can also click any line number in DevTools → **Sources** tab to set a breakpoint there.

---

## 6.4 Common React Bugs — Catalogue

### Bug 1 — State update doesn't re-render

```tsx
// ❌ Mutating state directly — React doesn't detect the change
const handleToggle = (id: number) => {
  const emp = employees.find(e => e.id === id);
  if (emp) emp.isActive = !emp.isActive;  // mutates the object in place
  setEmployees(employees);                // same reference — React sees no change
};

// ✅ Create a new array with a new object
const handleToggle = (id: number) => {
  setEmployees(prev =>
    prev.map(e => e.id === id ? { ...e, isActive: !e.isActive } : e)
  );
};
```

### Bug 2 — Stale closure (event handler sees old state)

```tsx
// ❌ count is captured at render time; repeated calls all read same stale count
const handleTripleIncrement = () => {
  setCount(count + 1);
  setCount(count + 1);
  setCount(count + 1);
  // result: count + 1, not count + 3
};

// ✅ Functional update always uses latest value
const handleTripleIncrement = () => {
  setCount(prev => prev + 1);
  setCount(prev => prev + 1);
  setCount(prev => prev + 1);
  // result: count + 3 ✅
};
```

### Bug 3 — useEffect infinite loop

```tsx
// ❌ No dependency array → runs after EVERY render
// → sets state → triggers render → runs again → infinite loop
useEffect(() => {
  setEmployees(processEmployees(rawData));
});

// ❌ Object/array in deps → new reference every render → infinite loop
useEffect(() => {
  fetchEmployees(options); // options = {} defined inside component
}, [options]);

// ✅ Empty array → run once on mount
useEffect(() => {
  fetchData();
}, []);

// ✅ Stable primitive in deps
useEffect(() => {
  fetchEmployee(id);
}, [id]);

// ✅ Move object inside the effect
useEffect(() => {
  const options = { page: 1 };
  fetchEmployees(options);
}, []);
```

### Bug 4 — Calling event handler instead of passing it

```tsx
// ❌ handleRemove(emp.id) is CALLED during render, not on click
<button onClick={handleRemove(emp.id)}>Remove</button>

// ✅ Wrap in arrow function to defer the call
<button onClick={() => handleRemove(emp.id)}>Remove</button>

// ✅ Or: no arguments needed — pass reference directly
<button onClick={handleRemove}>Remove</button>
```

### Bug 5 — Reading state immediately after setting it

```tsx
// ❌ setCount is async — count still has the OLD value on next line
setCount(count + 1);
console.log(count); // still old value!
sendToServer(count); // sends wrong value

// ✅ Calculate the new value first, then use it in both places
const newCount = count + 1;
setCount(newCount);
sendToServer(newCount);

// ✅ Or react to state changes with useEffect
useEffect(() => {
  sendToServer(count);
}, [count]);
```

### Bug 6 — Missing key prop (or using index as key)

```tsx
// ❌ Missing key → warning + incorrect re-rendering
{employees.map(emp => <EmployeeCard employee={emp} />)}

// ❌ Index key → broken when list reorders/filters
{employees.map((emp, i) => <EmployeeCard key={i} employee={emp} />)}

// ✅ Stable unique ID
{employees.map(emp => <EmployeeCard key={emp.id} employee={emp} />)}
```

### Bug 7 — The infamous `{0 && ...}` gotcha

```tsx
// ❌ If count is 0, renders the number "0" on screen!
{count && <p>You have {count} items</p>}

// ✅ Use explicit boolean check
{count > 0 && <p>You have {count} items</p>}

// ✅ Or ternary
{count ? <p>You have {count} items</p> : null}

// ✅ Or double negation to coerce to boolean
{!!count && <p>You have {count} items</p>}
```

---

## 6.5 TypeScript as a Debugger

TypeScript catches whole categories of bugs before they run:

```tsx
// TS error: Property 'naem' does not exist on type Employee
<p>{employee.naem}</p>  // ← red squiggle in VS Code

// TS error: Argument of type 'string' is not assignable to type 'number'
setCount('five');

// TS error: Missing required prop 'onRemove'
<EmployeeCard employee={emp} /> // ← if onRemove is required
```

Lean on TypeScript — the red squiggles are your friends.

---

## 6.6 Error Boundaries

By default, a JavaScript error inside a component crashes the **entire app** — the user sees a blank white screen. Error Boundaries catch the error and show a fallback UI instead.

```
Without Error Boundary:
  Component throws → React unmounts entire tree → blank screen

With Error Boundary:
  Component throws → Error Boundary catches it → shows fallback
  → rest of the app still works
```

Error Boundaries must be **class components** (this is the one place class components are still required in modern React):

```tsx
// src/components/ErrorBoundary.tsx
import { Component, ErrorInfo, ReactNode } from 'react';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
  onError?: (error: Error, info: ErrorInfo) => void;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false, error: null };

  // Called when a descendant throws during rendering
  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  // Called after the error is captured — good for logging
  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('ErrorBoundary caught:', error, info.componentStack);
    this.props.onError?.(error, info);
    // In production: Sentry.captureException(error, { extra: info });
  }

  handleRetry = () => this.setState({ hasError: false, error: null });

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) return this.props.fallback;

      return (
        <div style={{
          padding: '24px',
          border: '1px solid #da1e28',
          borderRadius: '8px',
          background: '#fff1f1',
          color: '#a2191f',
          margin: '16px',
        }}>
          <h3>Something went wrong</h3>
          <p style={{ fontSize: '14px', marginTop: '8px', fontFamily: 'monospace' }}>
            {this.state.error?.message}
          </p>
          <button
            onClick={this.handleRetry}
            style={{
              marginTop: '12px', padding: '6px 16px',
              background: '#da1e28', color: 'white',
              border: 'none', borderRadius: '4px', cursor: 'pointer',
            }}
          >
            Try Again
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
```

### Using Error Boundaries

```tsx
// Wrap the entire app for a global catch-all
// src/main.tsx
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ErrorBoundary>
      <App />
    </ErrorBoundary>
  </StrictMode>,
);

// Or wrap individual sections so one failure doesn't kill everything
function Dashboard() {
  return (
    <div>
      <Header />
      <ErrorBoundary fallback={<p>Stats unavailable</p>}>
        <StatsDashboard />
      </ErrorBoundary>
      <ErrorBoundary fallback={<p>Employee list failed to load</p>}>
        <EmployeeList />
      </ErrorBoundary>
    </div>
  );
}
```

> **Note:** Error Boundaries catch errors during **rendering** and **lifecycle methods**. They do NOT catch errors in:
> - Event handlers (use try/catch there)
> - Async code (use try/catch in the async function)
> - The Error Boundary component itself

---

## 6.7 Debugging Checklist

When something isn't working, go through this list:

```
1. Is there a console error or warning? Read it carefully.
2. Open React DevTools → check props and state on the component.
3. Add console.log() before and after the suspicious line.
4. Is state being mutated directly? Use spread/filter/map.
5. Is the useEffect dependency array correct?
6. Is an event handler being called instead of passed?
7. Does TypeScript show a red squiggle? Trust it.
8. Is the component re-rendering unexpectedly? Check Profiler.
9. Is there a missing key in a list?
10. Add a debugger statement and step through line by line.
```

---

## Summary

| Tool | Use for |
|------|---------|
| React DevTools Components tab | Inspect props/state, edit live |
| React DevTools Profiler | Find unnecessary re-renders |
| `console.table(arr)` | Debug arrays of objects |
| `debugger` | Pause execution and inspect |
| Error Boundary | Catch render errors, show fallback |
| TypeScript | Catch type errors before runtime |

**Next → [Module 07: Deep Dive into Components & React Internals](./07-deep-dive-components.md)**
