# Module 09 — Multi-Page Feeling with React Router

## Learning Objectives
- Understand client-side routing vs server-side routing
- Set up React Router 6 with nested routes and layouts
- Use `Link`, `NavLink`, `useNavigate`, `useParams`, `useSearchParams`
- Build protected routes
- Apply to EMS: Home, Employee List, Employee Detail, 404 pages

---

## 9.1 SPA Routing Explained

In a traditional multi-page website, each URL loads a completely new HTML file from the server. In a React SPA, there is only **one HTML file** — React Router intercepts navigation and swaps out components without a full page reload.

```
Traditional:
  /employees     → server sends employees.html
  /employees/1   → server sends employee-detail.html

SPA (React Router):
  /employees     → React swaps in <EmployeesPage />
  /employees/1   → React swaps in <EmployeeDetailPage />
  (same index.html, no server round-trip)
```

---

## 9.2 Install React Router

```bash
npm install react-router-dom
```

---

## 9.3 Core Components and Hooks

| API | Purpose |
|-----|---------|
| `<BrowserRouter>` | Provides routing context using the History API |
| `<Routes>` | Container for all `<Route>` definitions |
| `<Route path="..." element={...}>` | Maps a URL to a component |
| `<Link to="...">` | Navigate without page reload |
| `<NavLink to="...">` | Like Link, adds `active` class when route matches |
| `<Outlet />` | Where nested/child routes render inside a layout |
| `<Navigate to="...">` | Redirect declaratively |
| `useNavigate()` | Navigate programmatically |
| `useParams()` | Extract `:param` values from URL |
| `useSearchParams()` | Read/write query string `?key=value` |
| `useLocation()` | Get current location object |
| `useMatch()` | Test if a path matches current URL |

---

## 9.4 Setup — Wrap App in BrowserRouter

```tsx
// src/main.tsx
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider } from './context/ThemeContext';
import App from './App.tsx';
import './index.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <ThemeProvider>
        <App />
      </ThemeProvider>
    </BrowserRouter>
  </StrictMode>,
);
```

---

## 9.5 Route Structure

```tsx
// src/App.tsx
import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/layout/Layout';
import HomePage from './pages/HomePage';
import EmployeesPage from './pages/EmployeesPage';
import EmployeeDetailPage from './pages/EmployeeDetailPage';
import NotFoundPage from './pages/NotFoundPage';

function App() {
  return (
    <Routes>
      {/* All routes share the Layout (header + sidebar + footer) */}
      <Route path="/" element={<Layout />}>

        {/* index route renders at "/" */}
        <Route index element={<HomePage />} />

        {/* /employees — list */}
        <Route path="employees" element={<EmployeesPage />} />

        {/* /employees/:id — detail with URL param */}
        <Route path="employees/:id" element={<EmployeeDetailPage />} />

        {/* Redirect /staff → /employees */}
        <Route path="staff" element={<Navigate to="/employees" replace />} />

        {/* 404 — must be last inside the layout */}
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}

export default App;
```

---

## 9.6 Layout Component with Outlet

```tsx
// src/components/layout/Layout.tsx
import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';
import styles from './Layout.module.css';

function Layout() {
  return (
    <div className={styles.layout}>
      <Navbar />
      <main className={styles.main}>
        <Outlet />   {/* ← child routes render here */}
      </main>
    </div>
  );
}
export default Layout;
```

```css
/* src/components/layout/Layout.module.css */
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.main {
  flex: 1;
  padding: var(--space-6);
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
}
```

---

## 9.7 Navbar with NavLink

```tsx
// src/components/layout/Navbar.tsx
import { NavLink, Link } from 'react-router-dom';
import { useTheme } from '../../context/ThemeContext';
import styles from './Navbar.module.css';

function Navbar() {
  const { theme, toggleTheme } = useTheme();

  return (
    <header className={styles.navbar}>
      <Link to="/" className={styles.brand}>
        <span>🏢</span>
        <span className={styles.brandName}>IBM EMS</span>
      </Link>

      <nav className={styles.nav}>
        {[
          { to: '/', label: 'Home', end: true },
          { to: '/employees', label: 'Employees', end: false },
        ].map(({ to, label, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}                    // exact match for "/"
            className={({ isActive }) =>
              `${styles.link} ${isActive ? styles.linkActive : ''}`
            }
          >
            {label}
          </NavLink>
        ))}
      </nav>

      <button className={styles.themeBtn} onClick={toggleTheme}>
        {theme === 'dark' ? '☀️' : '🌙'}
      </button>
    </header>
  );
}
export default Navbar;
```

```css
/* src/components/layout/Navbar.module.css */
.navbar {
  background: var(--color-gray-900);
  color: white;
  padding: 0 var(--space-6);
  height: 56px;
  display: flex;
  align-items: center;
  gap: var(--space-6);
  position: sticky;
  top: 0;
  z-index: 100;
}
.brand {
  display: flex; align-items: center; gap: var(--space-2);
  color: white; text-decoration: none;
}
.brandName { font-weight: 700; font-size: var(--font-size-lg); }
.nav { display: flex; gap: var(--space-1); }
.link {
  color: var(--color-gray-300);
  text-decoration: none;
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  font-weight: 500;
  transition: color 0.15s, background 0.15s;
}
.link:hover { color: white; background: rgba(255,255,255,0.1); }
.linkActive { color: white; background: var(--color-primary); }
.themeBtn {
  margin-left: auto; background: none; border: none; color: white;
  font-size: 20px; cursor: pointer; padding: var(--space-1);
}
```

---

## 9.8 Pages

### `src/pages/HomePage.tsx`

```tsx
import { Link } from 'react-router-dom';
import styles from './HomePage.module.css';

function HomePage() {
  return (
    <div className={styles.hero}>
      <h1 className={styles.title}>Welcome to IBM EMS</h1>
      <p className={styles.subtitle}>
        Manage your organization's workforce in one place.
      </p>
      <Link to="/employees" className={styles.cta}>
        View Employees →
      </Link>
    </div>
  );
}
export default HomePage;
```

```css
/* src/pages/HomePage.module.css */
.hero {
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  min-height: 60vh; text-align: center; gap: var(--space-5);
}
.title { font-size: var(--font-size-2xl); font-weight: 700; }
.subtitle { font-size: var(--font-size-lg); color: var(--color-gray-700); max-width: 500px; }
.cta {
  display: inline-block;
  padding: var(--space-3) var(--space-8);
  background: var(--color-primary); color: white;
  border-radius: var(--radius-sm);
  font-weight: 600; font-size: var(--font-size-md);
  text-decoration: none; transition: background 0.15s;
}
.cta:hover { background: var(--color-primary-dark); text-decoration: none; }
```

### `src/pages/EmployeesPage.tsx`

Move `App.tsx` content here.

```tsx
// src/pages/EmployeesPage.tsx
import { useState } from 'react';
import { Link } from 'react-router-dom';
import clsx from 'clsx';
import { useEmployees } from '../hooks/useEmployees';
import EmployeeCard from '../components/EmployeeCard';
import Spinner from '../components/Spinner';
import ErrorMessage from '../components/ErrorMessage';
import styles from './EmployeesPage.module.css';

const DEPARTMENTS = ['All', 'Engineering', 'Marketing', 'HR', 'Finance', 'Sales'] as const;

function EmployeesPage() {
  const {
    filtered, loading, error,
    filter, search, showInactive, stats,
    setFilter, setSearch, setShowInactive,
    addEmployee, removeEmployee, refetch,
  } = useEmployees();

  const [newName, setNewName] = useState('');
  const [newDept, setNewDept] = useState<'Engineering'>('Engineering');

  const handleAdd = () => {
    if (!newName.trim()) return;
    addEmployee(newName.trim(), newDept);
    setNewName('');
  };

  if (loading && !stats.total) return <Spinner message="Loading employees…" />;

  return (
    <div>
      {error && <ErrorMessage message={error} onRetry={refetch} />}

      <div className={styles.toolbar}>
        <h1 className={styles.pageTitle}>Employees</h1>
        <div className={styles.addBar}>
          <input
            value={newName}
            onChange={e => setNewName(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleAdd()}
            placeholder="New employee name…"
            className={styles.input}
          />
          <select
            value={newDept}
            onChange={e => setNewDept(e.target.value as any)}
            className={styles.select}
          >
            {DEPARTMENTS.filter(d => d !== 'All').map(d => (
              <option key={d}>{d}</option>
            ))}
          </select>
          <button onClick={handleAdd} disabled={!newName.trim()} className={styles.addBtn}>
            + Add
          </button>
        </div>
      </div>

      <input
        value={search}
        onChange={e => setSearch(e.target.value)}
        placeholder="🔍 Search name or email…"
        className={styles.searchInput}
      />

      <div className={styles.filterBar}>
        {DEPARTMENTS.map(d => (
          <button
            key={d}
            onClick={() => setFilter(d as any)}
            className={clsx(styles.filterBtn, filter === d && styles.filterBtnActive)}
          >
            {d}
          </button>
        ))}
        <label className={styles.inactiveToggle}>
          <input type="checkbox" checked={showInactive} onChange={e => setShowInactive(e.target.checked)} />
          Show inactive
        </label>
      </div>

      <p className={styles.resultInfo}>
        {filtered.length} of {stats.total} employees
        {filter !== 'All' && ` · ${filter}`}
      </p>

      {filtered.length > 0
        ? (
          <div className={styles.grid}>
            {filtered.map(emp => (
              <Link
                key={emp.id}
                to={`/employees/${emp.id}`}
                style={{ textDecoration: 'none', color: 'inherit' }}
              >
                <EmployeeCard employee={emp} onRemove={e => { e.preventDefault(); removeEmployee(emp.id); }} />
              </Link>
            ))}
          </div>
        )
        : <p style={{ textAlign: 'center', padding: '60px', color: 'var(--color-gray-700)' }}>No employees found.</p>
      }
    </div>
  );
}
export default EmployeesPage;
```

### `src/pages/EmployeeDetailPage.tsx`

```tsx
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { employeeService } from '../services/employeeService';
import { Employee } from '../types';
import Spinner from '../components/Spinner';
import ErrorMessage from '../components/ErrorMessage';

function EmployeeDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [employee, setEmployee] = useState<Employee | null>(null);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState<string | null>(null);

  useEffect(() => {
    if (!id) return navigate('/employees');

    setLoading(true);
    employeeService.getById(Number(id))
      .then(setEmployee)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, [id, navigate]);

  if (loading) return <Spinner />;
  if (error)   return <ErrorMessage message={error} onRetry={() => navigate(0)} />;
  if (!employee) return null;

  return (
    <div style={{ maxWidth: '640px' }}>
      <Link
        to="/employees"
        style={{ color: 'var(--color-primary)', fontSize: '14px', textDecoration: 'none' }}
      >
        ← Back to Employees
      </Link>

      <h1 style={{ margin: '16px 0 4px', fontSize: '28px' }}>{employee.name}</h1>
      <p style={{ color: 'var(--color-gray-700)', marginBottom: '24px' }}>
        @{employee.username}
      </p>

      <div style={{
        background: 'white', border: '1px solid var(--border-color)',
        borderRadius: '8px', padding: '24px', display: 'grid',
        gridTemplateColumns: '1fr 1fr', gap: '16px',
      }}>
        {[
          { label: 'Email', value: employee.email },
          { label: 'Phone', value: employee.phone },
          { label: 'Department', value: employee.department },
          { label: 'Salary', value: `$${employee.salary.toLocaleString()}` },
          { label: 'Status', value: employee.isActive ? '✅ Active' : '⛔ Inactive' },
          { label: 'Joined', value: new Date(employee.joinDate).toLocaleDateString() },
          { label: 'Company', value: employee.company?.name ?? '—' },
          { label: 'City', value: employee.address?.city ?? '—' },
        ].map(({ label, value }) => (
          <div key={label}>
            <p style={{ fontSize: '12px', color: 'var(--color-gray-500)', marginBottom: '2px' }}>
              {label}
            </p>
            <p style={{ fontWeight: 500 }}>{value}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
export default EmployeeDetailPage;
```

### `src/pages/NotFoundPage.tsx`

```tsx
import { Link } from 'react-router-dom';

function NotFoundPage() {
  return (
    <div style={{ textAlign: 'center', padding: '80px 20px' }}>
      <p style={{ fontSize: '72px', marginBottom: '16px' }}>404</p>
      <h2 style={{ fontSize: '24px', marginBottom: '8px' }}>Page not found</h2>
      <p style={{ color: 'var(--color-gray-700)', marginBottom: '24px' }}>
        The page you're looking for doesn't exist.
      </p>
      <Link to="/" style={{
        padding: '10px 24px', background: 'var(--color-primary)', color: 'white',
        borderRadius: '4px', textDecoration: 'none', fontWeight: 600,
      }}>
        Go Home
      </Link>
    </div>
  );
}
export default NotFoundPage;
```

---

## 9.9 Protected Routes

```tsx
// src/components/ProtectedRoute.tsx
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';  // built in Module 12

interface Props {
  children: React.ReactNode;
  requiredRole?: 'admin' | 'user';
}

function ProtectedRoute({ children, requiredRole }: Props) {
  const { user, loading } = useAuth();
  const location = useLocation();

  // Still checking auth state
  if (loading) return <div>Checking authentication…</div>;

  // Not logged in — redirect to login, saving current location
  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Wrong role
  if (requiredRole && user.role !== requiredRole && user.role !== 'admin') {
    return <Navigate to="/403" replace />;
  }

  return <>{children}</>;
}
export default ProtectedRoute;
```

```tsx
// Using it in App.tsx
<Route path="employees" element={
  <ProtectedRoute>
    <EmployeesPage />
  </ProtectedRoute>
} />
```

---

## 9.10 Programmatic Navigation

```tsx
import { useNavigate } from 'react-router-dom';

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogin = async () => {
    await authService.login(credentials);

    // Go to the page they were trying to visit, or /employees
    const from = (location.state as any)?.from?.pathname ?? '/employees';
    navigate(from, { replace: true });
  };
}

// Other navigation patterns:
navigate('/employees');                          // push
navigate('/employees', { replace: true });      // replace (no back button)
navigate(-1);                                   // go back
navigate(1);                                    // go forward
navigate('/employees', { state: { message: 'Created!' } }); // pass state
```

---

## 9.11 Search Params (query string)

```tsx
import { useSearchParams } from 'react-router-dom';

// URL: /employees?dept=Engineering&page=2

function EmployeesPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const dept = searchParams.get('dept') ?? 'All';
  const page = Number(searchParams.get('page') ?? '1');

  const updateFilter = (newDept: string) => {
    setSearchParams({ dept: newDept, page: '1' });
  };

  // URL updates automatically — bookmarkable and shareable
  return (
    <select value={dept} onChange={e => updateFilter(e.target.value)}>
      {DEPARTMENTS.map(d => <option key={d}>{d}</option>)}
    </select>
  );
}
```

---

## Summary

| Task | API |
|------|-----|
| Set up routing | `<BrowserRouter>` in main.tsx |
| Define routes | `<Routes><Route path="..." element={...}>`|
| Layout with children | `<Route element={<Layout />}>` + `<Outlet />` |
| Navigate link | `<Link to="...">` |
| Active-aware link | `<NavLink className={({ isActive }) => ...}>` |
| URL parameter | `path="employees/:id"` + `useParams()` |
| Query string | `useSearchParams()` |
| Programmatic nav | `useNavigate()` |
| Redirect | `<Navigate to="...">` |
| 404 | `<Route path="*">` (must be last) |
| Auth guard | `<ProtectedRoute>` wrapper |

**Next → [Module 10: Forms and Validation](./10-forms-validation.md)**
