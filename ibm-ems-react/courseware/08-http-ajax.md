# Module 08 — Reaching Out to the Web: HTTP / Ajax with Axios

## Learning Objectives
- Understand why Axios is preferred over raw `fetch`
- Set up a centralised Axios instance with interceptors
- Build a typed service layer
- Handle loading, error, and success states cleanly
- Apply to EMS: replace seed data with real API calls to JSONPlaceholder

---

## 8.1 Why Axios?

| Feature | `fetch` | `axios` |
|---------|---------|---------|
| Auto JSON parse | ❌ manual `.json()` | ✅ |
| Throws on 4xx/5xx | ❌ only network errors | ✅ |
| Request interceptors | ❌ | ✅ |
| Response interceptors | ❌ | ✅ |
| Base URL config | ❌ | ✅ |
| Request cancellation | AbortController | ✅ built-in |
| Request timeout | ❌ manual | ✅ |
| TypeScript generics | minimal | ✅ excellent |
| Upload progress | ❌ | ✅ |

---

## 8.2 Install Axios

```bash
npm install axios
```

---

## 8.3 Centralised Axios Instance

Never use `axios.get(...)` directly. Create one configured instance, import it everywhere.

```ts
// src/services/api.ts
import axios, { AxiosError } from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'https://jsonplaceholder.typicode.com',
  timeout: 10_000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

// ── Request interceptor ──────────────────────────────────────────────────────
// Runs before every request — attach token, add correlation IDs, etc.
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response interceptor ─────────────────────────────────────────────────────
// Runs after every response — handle global errors (401, 500, etc.)
api.interceptors.response.use(
  (response) => response,   // 2xx — pass through
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('auth_token');
      window.location.href = '/login';
    }
    if (error.response?.status === 500) {
      console.error('Server error:', error.response.data);
    }
    return Promise.reject(error);
  }
);

export default api;
```

---

## 8.4 Environment Variables

```bash
# .env (development)
VITE_API_BASE_URL=https://jsonplaceholder.typicode.com

# .env.production (production)
VITE_API_BASE_URL=https://api.ibm-ems.com/v1
```

> **Rules:**
> - Prefix all variables with `VITE_` — Vite only exposes those to client code
> - Never put secrets in `.env` files in the frontend (they're visible in the bundle)
> - Add `.env.local` to `.gitignore`

```ts
// Access in code
const baseURL = import.meta.env.VITE_API_BASE_URL;
const isDev   = import.meta.env.DEV;     // boolean
const mode    = import.meta.env.MODE;    // 'development' | 'production'
```

---

## 8.5 Service Layer

Each feature gets its own service file. Services make API calls and return typed data.

### Employee types (updated)

```ts
// src/types/index.ts
export interface Employee {
  id: number;
  name: string;
  username: string;
  email: string;
  phone: string;
  website: string;
  department: string;   // we'll map from JSONPlaceholder's company
  salary: number;       // we'll generate this
  isActive: boolean;    // we'll generate this
  joinDate: string;     // we'll generate this
  address?: {
    street: string;
    city: string;
    zipcode: string;
  };
  company?: {
    name: string;
    catchPhrase: string;
  };
}

export interface CreateEmployeeDto {
  name: string;
  email: string;
  department: string;
  salary: number;
}

export interface UpdateEmployeeDto extends Partial<CreateEmployeeDto> {}

export type Department = 'All' | 'Engineering' | 'Marketing' | 'HR' | 'Finance' | 'Sales';
```

### Employee service

```ts
// src/services/employeeService.ts
import api from './api';
import { Employee, CreateEmployeeDto, UpdateEmployeeDto } from '../types';

// JSONPlaceholder /users endpoint structure
interface JsonPlaceholderUser {
  id: number;
  name: string;
  username: string;
  email: string;
  phone: string;
  website: string;
  address: { street: string; suite: string; city: string; zipcode: string };
  company: { name: string; catchPhrase: string; bs: string };
}

// Departments to randomly assign (JSONPlaceholder has no departments)
const DEPT_MAP = ['Engineering', 'Marketing', 'HR', 'Finance', 'Sales'];

function mapUserToEmployee(user: JsonPlaceholderUser): Employee {
  return {
    id: user.id,
    name: user.name,
    username: user.username,
    email: user.email,
    phone: user.phone,
    website: user.website,
    // Deterministically assign department from id
    department: DEPT_MAP[user.id % DEPT_MAP.length],
    salary: 50000 + (user.id * 4500),
    isActive: user.id % 4 !== 0,  // every 4th employee is inactive
    joinDate: `202${user.id % 4}-0${(user.id % 9) + 1}-15`,
    address: {
      street: `${user.address.street}, ${user.address.suite}`,
      city: user.address.city,
      zipcode: user.address.zipcode,
    },
    company: {
      name: user.company.name,
      catchPhrase: user.company.catchPhrase,
    },
  };
}

export const employeeService = {
  /** Fetch all employees */
  getAll: async (): Promise<Employee[]> => {
    const { data } = await api.get<JsonPlaceholderUser[]>('/users');
    return data.map(mapUserToEmployee);
  },

  /** Fetch one employee by ID */
  getById: async (id: number): Promise<Employee> => {
    const { data } = await api.get<JsonPlaceholderUser>(`/users/${id}`);
    return mapUserToEmployee(data);
  },

  /** Create a new employee */
  create: async (dto: CreateEmployeeDto): Promise<Employee> => {
    // JSONPlaceholder doesn't really create, but simulates it
    const { data } = await api.post<JsonPlaceholderUser>('/users', dto);
    return mapUserToEmployee({ ...data, id: Date.now() } as JsonPlaceholderUser);
  },

  /** Update an employee */
  update: async (id: number, dto: UpdateEmployeeDto): Promise<Employee> => {
    const { data } = await api.patch<JsonPlaceholderUser>(`/users/${id}`, dto);
    return mapUserToEmployee(data);
  },

  /** Delete an employee */
  remove: async (id: number): Promise<void> => {
    await api.delete(`/users/${id}`);
  },
};
```

---

## 8.6 Handling Async State — The Pattern

Every API call has three states. Always model all three:

```tsx
interface AsyncState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
}
```

### `useApi` custom hook

```tsx
// src/hooks/useApi.ts
import { useState, useEffect, useCallback, useRef } from 'react';

interface ApiState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
}

function useApi<T>(apiFn: () => Promise<T>) {
  const [state, setState] = useState<ApiState<T>>({
    data: null,
    loading: true,
    error: null,
  });

  // Track if the component is still mounted
  const mountedRef = useRef(true);
  useEffect(() => {
    mountedRef.current = true;
    return () => { mountedRef.current = false; };
  }, []);

  const execute = useCallback(async () => {
    setState(s => ({ ...s, loading: true, error: null }));
    try {
      const data = await apiFn();
      if (mountedRef.current) setState({ data, loading: false, error: null });
    } catch (err) {
      if (mountedRef.current) setState({
        data: null,
        loading: false,
        error: err instanceof Error ? err.message : 'An error occurred',
      });
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => { execute(); }, [execute]);

  return { ...state, refetch: execute };
}

export default useApi;
```

---

## 8.7 Loading and Error UI Components

```tsx
// src/components/Spinner.tsx
import styles from './Spinner.module.css';

function Spinner({ message = 'Loading...' }: { message?: string }) {
  return (
    <div className={styles.wrapper}>
      <div className={styles.spin} aria-label="Loading" />
      <p className={styles.message}>{message}</p>
    </div>
  );
}
export default Spinner;
```

```css
/* src/components/Spinner.module.css */
.wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  gap: 16px;
  color: var(--color-gray-700);
}

.spin {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-gray-300);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.message { font-size: var(--font-size-sm); }
```

```tsx
// src/components/ErrorMessage.tsx
function ErrorMessage({
  message,
  onRetry,
}: {
  message: string;
  onRetry?: () => void;
}) {
  return (
    <div style={{
      padding: '24px',
      border: '1px solid var(--color-danger)',
      borderRadius: 'var(--radius-md)',
      background: 'var(--color-danger-bg)',
      color: 'var(--color-danger)',
      textAlign: 'center',
    }}>
      <p style={{ fontWeight: 600 }}>⚠️ Error</p>
      <p style={{ fontSize: '14px', marginTop: '8px' }}>{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          style={{
            marginTop: '12px', padding: '6px 20px',
            background: 'var(--color-danger)', color: 'white',
            border: 'none', borderRadius: '4px', cursor: 'pointer',
          }}
        >
          Retry
        </button>
      )}
    </div>
  );
}
export default ErrorMessage;
```

---

## 8.8 EMS Project — Fetch from JSONPlaceholder

### Updated `src/hooks/useEmployees.ts`

```tsx
import { useState, useMemo, useCallback, useEffect } from 'react';
import { employeeService } from '../services/employeeService';
import { Employee, Department } from '../types';

export function useEmployees() {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading]     = useState(true);
  const [error, setError]         = useState<string | null>(null);
  const [filter, setFilter]       = useState<Department>('All');
  const [search, setSearch]       = useState('');
  const [showInactive, setShowInactive] = useState(true);

  const loadEmployees = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await employeeService.getAll();
      setEmployees(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load employees');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadEmployees(); }, [loadEmployees]);

  const filtered = useMemo(() =>
    employees
      .filter(e => filter === 'All' || e.department === filter)
      .filter(e => showInactive || e.isActive)
      .filter(e =>
        e.name.toLowerCase().includes(search.toLowerCase()) ||
        e.email.toLowerCase().includes(search.toLowerCase())
      )
      .sort((a, b) => a.name.localeCompare(b.name)),
    [employees, filter, showInactive, search]
  );

  const stats = useMemo(() => ({
    total: employees.length,
    active: employees.filter(e => e.isActive).length,
    departments: new Set(employees.map(e => e.department)).size,
  }), [employees]);

  const addEmployee = useCallback(async (name: string, dept: Exclude<Department,'All'>) => {
    try {
      const emp = await employeeService.create({
        name, email: `${name.toLowerCase().replace(/\s+/g,'.')}@ibm.com`,
        department: dept, salary: 70000,
      });
      setEmployees(prev => [...prev, emp]);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create employee');
    }
  }, []);

  const removeEmployee = useCallback(async (id: number) => {
    try {
      await employeeService.remove(id);
      setEmployees(prev => prev.filter(e => e.id !== id));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete employee');
    }
  }, []);

  const toggleActive = useCallback((id: number) => {
    setEmployees(prev =>
      prev.map(e => e.id === id ? { ...e, isActive: !e.isActive } : e)
    );
  }, []);

  return {
    employees, filtered, loading, error,
    filter, search, showInactive, stats,
    setFilter, setSearch, setShowInactive,
    addEmployee, removeEmployee, toggleActive,
    refetch: loadEmployees,
  };
}
```

### Updated `src/App.tsx`

```tsx
import { useEmployees } from './hooks/useEmployees';
import EmployeeCard from './components/EmployeeCard';
import Spinner from './components/Spinner';
import ErrorMessage from './components/ErrorMessage';
import styles from './App.module.css';
import clsx from 'clsx';

const DEPARTMENTS = ['All', 'Engineering', 'Marketing', 'HR', 'Finance', 'Sales'] as const;

function App() {
  const {
    filtered, loading, error,
    filter, search, showInactive, stats,
    setFilter, setSearch, setShowInactive,
    addEmployee, removeEmployee, refetch,
  } = useEmployees();

  // quick-add local state
  const [newName, setNewName] = import_useState('');
  const [newDept, setNewDept] = import_useState<'Engineering'|'Marketing'|'HR'|'Finance'|'Sales'>('Engineering');

  const handleAdd = () => {
    if (!newName.trim()) return;
    addEmployee(newName.trim(), newDept);
    setNewName('');
  };

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <span style={{ fontSize: '24px' }}>🏢</span>
        <span className={styles.headerTitle}>IBM Employee Management System</span>
        {loading && <span style={{ marginLeft: 'auto', fontSize: '13px' }}>Refreshing…</span>}
      </header>

      <main className={styles.main}>

        {error && <ErrorMessage message={error} onRetry={refetch} />}

        {/* Stats strip */}
        <div className={styles.statsStrip}>
          {[
            { label: 'Total', value: stats.total },
            { label: 'Active', value: stats.active },
            { label: 'Departments', value: stats.departments },
          ].map(s => (
            <div key={s.label} className={styles.statCard}>
              <div className={styles.statValue}>{s.value}</div>
              <div className={styles.statLabel}>{s.label}</div>
            </div>
          ))}
        </div>

        {/* Search bar */}
        <input
          className={styles.addInput}
          value={search}
          onChange={e => setSearch(e.target.value)}
          placeholder="🔍 Search by name or email…"
          style={{ width: '100%', marginBottom: '16px' }}
        />

        {/* Add bar */}
        <div className={styles.addBar}>
          <input
            className={styles.addInput}
            value={newName}
            onChange={e => setNewName(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleAdd()}
            placeholder="New employee name…"
          />
          <select
            className={styles.addSelect}
            value={newDept}
            onChange={e => setNewDept(e.target.value as any)}
          >
            {DEPARTMENTS.filter(d => d !== 'All').map(d => (
              <option key={d}>{d}</option>
            ))}
          </select>
          <button className={styles.addBtn} onClick={handleAdd} disabled={!newName.trim()}>
            + Add
          </button>
        </div>

        {/* Filter bar */}
        <div className={styles.filterBar}>
          {DEPARTMENTS.map(dept => (
            <button
              key={dept}
              onClick={() => setFilter(dept as any)}
              className={clsx(styles.filterBtn, filter === dept && styles.filterBtnActive)}
            >
              {dept}
            </button>
          ))}
          <label style={{ marginLeft: 'auto', fontSize: '13px', display: 'flex', gap: '6px', alignItems: 'center' }}>
            <input type="checkbox" checked={showInactive} onChange={e => setShowInactive(e.target.checked)} />
            Show inactive
          </label>
        </div>

        <p className={styles.resultInfo}>
          Showing {filtered.length} of {stats.total} employees
        </p>

        {/* Content area */}
        {loading && !stats.total
          ? <Spinner message="Loading employees from server…" />
          : filtered.length > 0
            ? (
              <div className={styles.grid}>
                {filtered.map(emp => (
                  <EmployeeCard key={emp.id} employee={emp} onRemove={removeEmployee} />
                ))}
              </div>
            )
            : (
              <div className={styles.emptyState}>
                <div className={styles.emptyIcon}>🔍</div>
                <p className={styles.emptyTitle}>No employees found</p>
              </div>
            )
        }

      </main>
    </div>
  );
}

export default App;
```

> Replace `import_useState` with the actual `useState` import — written that way to avoid confusing the markdown renderer.

---

## 8.9 Axios Error Handling

```ts
import axios, { AxiosError } from 'axios';

async function safeApiCall<T>(fn: () => Promise<T>): Promise<T> {
  try {
    return await fn();
  } catch (err) {
    if (axios.isAxiosError(err)) {
      const e = err as AxiosError<{ message: string }>;

      if (!e.response) throw new Error('Network error. Check your connection.');

      switch (e.response.status) {
        case 400: throw new Error(e.response.data?.message ?? 'Bad request');
        case 401: throw new Error('Unauthorized. Please log in.');
        case 403: throw new Error('You don\'t have permission for this action.');
        case 404: throw new Error('Resource not found.');
        case 500: throw new Error('Server error. Please try again later.');
        default:  throw new Error(`Request failed (${e.response.status})`);
      }
    }
    throw err;
  }
}
```

---

## Summary

- Install: `npm install axios`
- Create `src/services/api.ts` — centralised Axios instance with interceptors
- Create `src/services/employeeService.ts` — typed API functions
- Always model three states: `loading`, `error`, `data`
- Use `.env` files for URLs — prefix with `VITE_`
- Extract the fetch logic into `useEmployees` hook so `App.tsx` stays clean

**Next → [Module 09: Routing with React Router](./09-routing.md)**
