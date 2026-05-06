# Module 07 — Diving Deeper into Components & React Internals

## Learning Objectives
- Master `useEffect`, `useRef`, `useCallback`, `useMemo`
- Understand when and why React re-renders
- Prevent unnecessary re-renders with `React.memo`
- Build reusable custom hooks
- Share state across the tree with Context API
- Apply to EMS: `useEmployees` custom hook, theme context

---

## 7.1 The Component Lifecycle (function component view)

Every function component goes through three phases:

```
MOUNT              UPDATE                   UNMOUNT
  │                   │                        │
  ▼                   ▼                        ▼
Component          Props or state         Component removed
first renders      changed — function     from the DOM
to DOM             runs again
  │                   │                        │
  ▼                   ▼                        ▼
useEffect()        useEffect()            Cleanup function
([] — once)        ([dep] — on change)    returned from useEffect
```

---

## 7.2 `useEffect` — In Depth

`useEffect` runs **after** the browser has painted the screen. It's the right place for side effects: data fetching, subscriptions, DOM manipulation.

### Dependency array rules

```tsx
// 1 — No array: runs after EVERY render
useEffect(() => {
  document.title = `EMS — ${employees.length} employees`;
});

// 2 — Empty array []: runs ONCE after mount
useEffect(() => {
  console.log('Component mounted');
  loadSavedFilter();
}, []);

// 3 — With deps: runs when any dep changes
useEffect(() => {
  filterEmployees(filter);
}, [filter]);

// 4 — Multiple deps: runs when ANY changes
useEffect(() => {
  fetchPage(page, department);
}, [page, department]);
```

### Cleanup function

The function you return runs:
- Before the effect runs again (when deps change)
- When the component unmounts

```tsx
// Clean up subscriptions
useEffect(() => {
  const handler = (e: KeyboardEvent) => {
    if (e.key === 'Escape') closeModal();
  };
  document.addEventListener('keydown', handler);
  return () => document.removeEventListener('keydown', handler); // cleanup
}, []);

// Cancel in-flight HTTP requests
useEffect(() => {
  const controller = new AbortController();

  fetch('/api/employees', { signal: controller.signal })
    .then(r => r.json())
    .then(setEmployees)
    .catch(err => {
      if (err.name !== 'AbortError') console.error(err);
    });

  return () => controller.abort(); // cancel if component unmounts
}, []);

// Clear timers
useEffect(() => {
  const id = setInterval(() => setTick(t => t + 1), 1000);
  return () => clearInterval(id);
}, []);
```

---

## 7.3 `useRef`

`useRef` stores a mutable value that:
1. **Persists** across renders (not reset on re-render)
2. Does **NOT** trigger a re-render when changed
3. Can hold a DOM element reference

### DOM access

```tsx
function SearchBar() {
  const inputRef = useRef<HTMLInputElement>(null);

  // Auto-focus on mount
  useEffect(() => {
    inputRef.current?.focus();
  }, []);

  // Focus on "/" keypress
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === '/' && document.activeElement !== inputRef.current) {
        e.preventDefault();
        inputRef.current?.focus();
      }
    };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, []);

  return <input ref={inputRef} placeholder="Search employees (Press '/')" />;
}
```

### Mutable value without re-render

```tsx
function AutoSave({ data }: { data: Employee[] }) {
  const timerRef = useRef<ReturnType<typeof setTimeout>>();

  useEffect(() => {
    // Debounce: cancel previous timer, start new one
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => {
      localStorage.setItem('employees', JSON.stringify(data));
    }, 500);

    return () => clearTimeout(timerRef.current);
  }, [data]);

  return null;
}
```

---

## 7.4 `useMemo` — Memoize Computed Values

`useMemo` caches the result of a calculation. It only recalculates when its dependencies change.

```tsx
// Without useMemo — recalculates on EVERY render (even unrelated state changes)
const stats = {
  total: employees.length,
  active: employees.filter(e => e.isActive).length,
  avgSalary: employees.reduce((s, e) => s + e.salary, 0) / employees.length,
};

// With useMemo — recalculates only when employees changes
const stats = useMemo(() => ({
  total: employees.length,
  active: employees.filter(e => e.isActive).length,
  avgSalary: employees.reduce((s, e) => s + e.salary, 0) / employees.length || 0,
  departments: [...new Set(employees.map(e => e.department))].sort(),
}), [employees]);

// Filtered list memoized — expensive .filter().sort() doesn't repeat unnecessarily
const filtered = useMemo(() =>
  employees
    .filter(e => filter === 'All' || e.department === filter)
    .filter(e => showInactive || e.isActive)
    .filter(e => e.name.toLowerCase().includes(search.toLowerCase()))
    .sort((a, b) => a.name.localeCompare(b.name)),
  [employees, filter, showInactive, search]
);
```

> **When to use:** Use `useMemo` when you have a genuinely expensive calculation (complex sorts, large dataset filters). Don't over-use it — every `useMemo` has overhead itself.

---

## 7.5 `useCallback` — Memoize Functions

`useCallback` returns a memoized version of a callback function. The same function reference is returned unless deps change.

```tsx
// Without useCallback — new function reference every render
// → child components see a new prop → they re-render
const handleRemove = (id: number) => {
  setEmployees(prev => prev.filter(e => e.id !== id));
};

// With useCallback — same reference between renders
const handleRemove = useCallback((id: number) => {
  setEmployees(prev => prev.filter(e => e.id !== id));
}, []); // no deps — setEmployees never changes

// When does useCallback actually matter?
// Only when the function is passed to a React.memo component
// (see next section)
```

---

## 7.6 `React.memo` — Prevent Re-renders

By default, when a parent re-renders, ALL its children re-render too, even if their props didn't change. `React.memo` makes a component only re-render when its props actually change.

```tsx
// Without React.memo — re-renders whenever App re-renders
function EmployeeCard({ employee, onRemove }: Props) { /* ... */ }

// With React.memo — skips re-render if employee and onRemove are the same
const EmployeeCard = React.memo(function EmployeeCard({ employee, onRemove }: Props) {
  console.log('EmployeeCard rendered:', employee.name);
  return (/* ... */);
});

export default EmployeeCard;
```

```tsx
// In App.tsx — now you need useCallback for onRemove to keep its reference stable
// Otherwise React.memo sees a new function prop on every render and still re-renders

const handleRemove = useCallback((id: number) => {
  setEmployees(prev => prev.filter(e => e.id !== id));
}, []);

// Now: typing in the search input re-renders App → filtered list changes
// → EmployeeCard components that are in the filtered list re-render
// → EmployeeCard components NOT in the filtered list DON'T re-render ✅
```

### When is React.memo worth it?

- Component renders visually expensive content (charts, large tables)
- Component appears many times in a list (e.g. 100+ cards)
- Component is proving to be a bottleneck in the Profiler

> Don't prematurely optimize — most apps work fine without `React.memo`.

---

## 7.7 Custom Hooks

A custom hook is a **function** whose name starts with `use` and that calls other hooks internally.

Custom hooks let you **extract stateful logic** and **reuse it** across multiple components.

### `useLocalStorage`

```tsx
// src/hooks/useLocalStorage.ts
import { useState, useEffect } from 'react';

function useLocalStorage<T>(key: string, initialValue: T) {
  const [value, setValue] = useState<T>(() => {
    // Lazy initializer — runs only on first render
    try {
      const stored = localStorage.getItem(key);
      return stored ? (JSON.parse(stored) as T) : initialValue;
    } catch {
      return initialValue;
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch {
      console.warn(`useLocalStorage: couldn't save key "${key}"`);
    }
  }, [key, value]);

  return [value, setValue] as const;
}

export default useLocalStorage;
```

### `useDebounce`

```tsx
// src/hooks/useDebounce.ts
import { useState, useEffect } from 'react';

function useDebounce<T>(value: T, delayMs: number): T {
  const [debounced, setDebounced] = useState<T>(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(timer);
  }, [value, delayMs]);

  return debounced;
}

export default useDebounce;
```

### `useEmployees` — EMS domain hook

This is the most important custom hook for our project. It extracts ALL employee-related logic out of `App.tsx`.

```tsx
// src/hooks/useEmployees.ts
import { useState, useMemo, useCallback } from 'react';
import { Employee, Department } from '../types';
import { INITIAL_EMPLOYEES } from '../data/employees';

let nextId = INITIAL_EMPLOYEES.length + 1;

interface UseEmployeesReturn {
  employees: Employee[];
  filtered: Employee[];
  filter: Department;
  search: string;
  showInactive: boolean;
  stats: { total: number; active: number; departments: number };
  setFilter: (dept: Department) => void;
  setSearch: (s: string) => void;
  setShowInactive: (v: boolean) => void;
  addEmployee: (name: string, department: Exclude<Department, 'All'>) => void;
  removeEmployee: (id: number) => void;
  toggleActive: (id: number) => void;
}

export function useEmployees(): UseEmployeesReturn {
  const [employees, setEmployees] = useState<Employee[]>(INITIAL_EMPLOYEES);
  const [filter, setFilter]       = useState<Department>('All');
  const [search, setSearch]       = useState('');
  const [showInactive, setShowInactive] = useState(true);

  const filtered = useMemo(() =>
    employees
      .filter(e => filter === 'All' || e.department === filter)
      .filter(e => showInactive || e.isActive)
      .filter(e => e.name.toLowerCase().includes(search.toLowerCase()))
      .sort((a, b) => a.name.localeCompare(b.name)),
    [employees, filter, showInactive, search]
  );

  const stats = useMemo(() => ({
    total: employees.length,
    active: employees.filter(e => e.isActive).length,
    departments: new Set(employees.map(e => e.department)).size,
  }), [employees]);

  const addEmployee = useCallback((name: string, department: Exclude<Department, 'All'>) => {
    const emp: Employee = {
      id: nextId++,
      name: name.trim(),
      email: `${name.toLowerCase().replace(/\s+/g, '.')}@ibm.com`,
      department,
      salary: 70000,
      isActive: true,
      joinDate: new Date().toISOString().split('T')[0],
    };
    setEmployees(prev => [...prev, emp]);
  }, []);

  const removeEmployee = useCallback((id: number) => {
    setEmployees(prev => prev.filter(e => e.id !== id));
  }, []);

  const toggleActive = useCallback((id: number) => {
    setEmployees(prev =>
      prev.map(e => e.id === id ? { ...e, isActive: !e.isActive } : e)
    );
  }, []);

  return {
    employees, filtered, filter, search, showInactive, stats,
    setFilter, setSearch, setShowInactive,
    addEmployee, removeEmployee, toggleActive,
  };
}
```

### Now `App.tsx` is clean

```tsx
import { useEmployees } from './hooks/useEmployees';
// ... other imports

function App() {
  const {
    filtered, filter, search, showInactive, stats,
    setFilter, setSearch, setShowInactive,
    addEmployee, removeEmployee,
  } = useEmployees();

  // App.tsx only handles UI — all logic is in the hook
  return (/* ... */);
}
```

---

## 7.8 Context API — Share State Without Prop Drilling

**Prop drilling** happens when you pass props through many layers just to get them to a deeply nested component.

```
App (owns theme)
  └── Layout (just passes theme down)
        └── Sidebar (just passes theme down)
              └── NavItem (FINALLY uses theme)
```

Context lets any component read shared values without intermediate components knowing about them.

### Create a Theme Context

```tsx
// src/context/ThemeContext.tsx
import { createContext, useContext, useState, ReactNode } from 'react';

type Theme = 'light' | 'dark';

interface ThemeContextValue {
  theme: Theme;
  toggleTheme: () => void;
}

// 1 — Create the context (null as initial, safe with our hook check)
const ThemeContext = createContext<ThemeContextValue | null>(null);

// 2 — Provider component that holds the state
export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setTheme] = useState<Theme>('light');
  const toggleTheme = () => setTheme(t => t === 'light' ? 'dark' : 'light');

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}

// 3 — Custom hook for consuming the context (includes error check)
export function useTheme(): ThemeContextValue {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error('useTheme must be used inside <ThemeProvider>');
  return ctx;
}
```

```tsx
// src/main.tsx — wrap the app
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeProvider>
      <App />
    </ThemeProvider>
  </StrictMode>,
);
```

```tsx
// Any component anywhere in the tree can use it
import { useTheme } from '../context/ThemeContext';

function Header() {
  const { theme, toggleTheme } = useTheme();

  return (
    <header style={{
      background: theme === 'dark' ? '#161616' : '#0062ff',
      color: 'white',
    }}>
      <span>IBM EMS</span>
      <button onClick={toggleTheme}>
        {theme === 'dark' ? '☀️' : '🌙'}
      </button>
    </header>
  );
}
```

---

## 7.9 Final Folder Structure (End of Module 07)

```
src/
├── components/
│   ├── EmployeeCard.tsx
│   ├── EmployeeCard.module.css
│   └── ErrorBoundary.tsx
├── context/
│   └── ThemeContext.tsx
├── data/
│   └── employees.ts
├── hooks/
│   ├── useDebounce.ts
│   ├── useEmployees.ts
│   └── useLocalStorage.ts
├── types/
│   └── index.ts
├── App.module.css
├── App.tsx
├── index.css
└── main.tsx
```

---

## Summary

| Hook / Pattern | Use when |
|----------------|----------|
| `useEffect(fn, [])` | Run once on mount |
| `useEffect(fn, [dep])` | React to specific value changes |
| `useEffect` return | Clean up timers, listeners, requests |
| `useRef` | DOM access, mutable value with no re-render |
| `useMemo` | Expensive derived computation |
| `useCallback` | Stable function reference for memo'd children |
| `React.memo` | Skip re-render when props haven't changed |
| Custom hook | Extract + reuse stateful logic across components |
| Context API | Share state without prop drilling |

**Next → [Module 08: HTTP & Ajax with Axios](./08-http-ajax.md)**
