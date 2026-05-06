# Module 05 — Styling React Components

## Learning Objectives
- Use all four main styling approaches in React
- Understand CSS Modules — scoping, composition, TypeScript support
- Set up CSS custom properties (variables) for a design system
- Apply to EMS: migrate from inline styles to a proper CSS Module layout

---

## 5.1 The Four Approaches

| Approach | Scoped | Dynamic | Pseudo-selectors | Recommended for |
|----------|--------|---------|-----------------|----------------|
| Inline styles | ✅ | ✅ | ❌ | Quick prototypes, truly dynamic values |
| CSS Modules | ✅ | Limited | ✅ | **Production apps (our choice)** |
| Global CSS | ❌ | ❌ | ✅ | Resets, variables, utility classes |
| Tailwind CSS | ✅ | ✅ | ✅ | Utility-first teams |

---

## 5.2 Inline Styles

Already used in previous modules. Quick recap:

```tsx
// Styles as a JS object — camelCase properties
<div style={{ backgroundColor: '#0062ff', color: 'white', padding: '16px' }}>

// Separate style object — good for reuse within a file
const cardStyle: React.CSSProperties = {
  border: '1px solid #e0e0e0',
  borderRadius: '8px',
  padding: '20px',
  background: 'white',
};
<div style={cardStyle}>

// Dynamic values — inline styles shine here
<div style={{ opacity: isLoading ? 0.5 : 1, pointerEvents: isLoading ? 'none' : 'auto' }}>
```

**Limitations:** No `:hover`, no media queries, no `::before`/`::after`, verbose.

---

## 5.3 CSS Modules

CSS Modules scope class names to the component that imports them. Two components can use the same class name `.card` without colliding.

### How it works

```
EmployeeCard.module.css
  .card { ... }      →  compiled to  →   .card_a7x3f { ... }

Header.module.css
  .card { ... }      →  compiled to  →   .card_9qs2m { ... }
```

The hash suffix is generated at build time — completely automatic.

### Setup (already works in Vite — zero config)

Just name your file `ComponentName.module.css` and import it.

---

## 5.4 EMS Design System — Global Variables

First, set up your design tokens in `index.css`:

```css
/* src/index.css */

/* ── Reset ── */
*, *::before, *::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

/* ── Design tokens (CSS custom properties) ── */
:root {
  /* Brand colours */
  --color-primary:         #0062ff;
  --color-primary-dark:    #0043ce;
  --color-primary-light:   #d0e2ff;

  /* Neutral palette */
  --color-gray-900:  #161616;
  --color-gray-700:  #525252;
  --color-gray-500:  #8d8d8d;
  --color-gray-300:  #c6c6c6;
  --color-gray-100:  #f4f4f4;
  --color-white:     #ffffff;

  /* Semantic */
  --color-success:   #24a148;
  --color-success-bg:#defbe6;
  --color-danger:    #da1e28;
  --color-danger-bg: #fff1f1;
  --color-warning:   #f1c21b;
  --color-warning-bg:#fdf6d0;

  /* Typography */
  --font-sans: 'IBM Plex Sans', -apple-system, BlinkMacSystemFont,
    'Segoe UI', Roboto, sans-serif;
  --font-mono: 'IBM Plex Mono', 'Courier New', monospace;

  --font-size-xs:  11px;
  --font-size-sm:  13px;
  --font-size-md:  16px;
  --font-size-lg:  20px;
  --font-size-xl:  24px;
  --font-size-2xl: 32px;

  /* Spacing */
  --space-1:  4px;
  --space-2:  8px;
  --space-3:  12px;
  --space-4:  16px;
  --space-5:  20px;
  --space-6:  24px;
  --space-8:  32px;
  --space-12: 48px;

  /* Borders */
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 16px;
  --radius-full: 9999px;
  --border-color: #e0e0e0;

  /* Shadows */
  --shadow-sm: 0 1px 3px rgba(0,0,0,0.08);
  --shadow-md: 0 4px 12px rgba(0,0,0,0.12);
  --shadow-lg: 0 8px 24px rgba(0,0,0,0.16);
}

/* ── Base ── */
body {
  font-family: var(--font-sans);
  font-size: var(--font-size-md);
  color: var(--color-gray-900);
  background-color: var(--color-gray-100);
  line-height: 1.6;
}

a {
  color: var(--color-primary);
  text-decoration: none;
}
a:hover { text-decoration: underline; }

button {
  font-family: var(--font-sans);
  cursor: pointer;
}

/* ── Utility classes ── */
.visually-hidden {
  position: absolute;
  width: 1px; height: 1px;
  padding: 0; margin: -1px;
  overflow: hidden;
  clip: rect(0,0,0,0);
  white-space: nowrap;
  border-width: 0;
}
```

---

## 5.5 Refactoring EMS with CSS Modules

### `src/components/EmployeeCard.module.css`

```css
/* EmployeeCard.module.css */

.card {
  background: var(--color-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--space-5);
  position: relative;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.removeBtn {
  position: absolute;
  top: var(--space-3);
  right: var(--space-3);
  width: 28px;
  height: 28px;
  border-radius: var(--radius-full);
  border: none;
  background: transparent;
  color: var(--color-gray-500);
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  transition: background 0.15s, color 0.15s;
}

.removeBtn:hover {
  background: var(--color-danger-bg);
  color: var(--color-danger);
}

.name {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-gray-900);
  margin-bottom: var(--space-1);
  padding-right: var(--space-8);  /* room for remove button */
}

.meta {
  font-size: var(--font-size-sm);
  color: var(--color-gray-700);
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-top: var(--space-1);
}

.salary {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: var(--color-primary);
  margin-top: var(--space-3);
}

.footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--space-3);
}

/* Status badge */
.badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: 700;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.badgeActive {
  background: var(--color-success-bg);
  color: var(--color-success);
}

.badgeInactive {
  background: var(--color-danger-bg);
  color: var(--color-danger);
}
```

### `src/components/EmployeeCard.tsx` — using modules

```tsx
import { Employee } from '../types';
import styles from './EmployeeCard.module.css';

interface EmployeeCardProps {
  employee: Employee;
  onRemove: (id: number) => void;
}

function EmployeeCard({ employee, onRemove }: EmployeeCardProps) {
  const joinYear = new Date(employee.joinDate).getFullYear();

  return (
    <div className={styles.card}>
      <button
        className={styles.removeBtn}
        onClick={() => onRemove(employee.id)}
        aria-label={`Remove ${employee.name}`}
        title="Remove employee"
      >
        ×
      </button>

      <h3 className={styles.name}>{employee.name}</h3>

      <p className={styles.meta}>📧 {employee.email}</p>
      <p className={styles.meta}>🏢 {employee.department}</p>
      <p className={styles.meta}>📅 Since {joinYear}</p>

      <p className={styles.salary}>${employee.salary.toLocaleString()} / yr</p>

      <div className={styles.footer}>
        <span className={`${styles.badge} ${employee.isActive ? styles.badgeActive : styles.badgeInactive}`}>
          {employee.isActive ? 'Active' : 'Inactive'}
        </span>
      </div>
    </div>
  );
}

export default EmployeeCard;
```

### Using CSS Modules — key points

```tsx
import styles from './MyComponent.module.css';

// Single class
<div className={styles.card}>

// Multiple classes
<div className={`${styles.card} ${styles.highlighted}`}>

// Conditional class
<span className={`${styles.badge} ${active ? styles.badgeActive : styles.badgeInactive}`}>
```

---

## 5.6 `clsx` — Cleaner Conditional Classes

The backtick template approach gets messy fast. `clsx` is a tiny utility:

```bash
npm install clsx
```

```tsx
import clsx from 'clsx';

// Before:
<span className={`${styles.badge} ${active ? styles.badgeActive : styles.badgeInactive} ${large ? styles.badgeLg : ''}`}>

// After:
<span className={clsx(
  styles.badge,
  active ? styles.badgeActive : styles.badgeInactive,
  large && styles.badgeLg,
)}>
```

`clsx` accepts:
- Strings → included as-is
- `undefined/null/false` → ignored
- Objects `{ className: condition }` → included when condition is truthy

```tsx
clsx(
  'base-class',
  { 'active': isActive, 'disabled': isDisabled },
  isLarge && 'large',
  maybeUndefined,          // silently ignored
)
```

---

## 5.7 App Layout — CSS Modules

```css
/* src/App.module.css */

.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: var(--color-gray-900);
  color: var(--color-white);
  padding: var(--space-4) var(--space-6);
  display: flex;
  align-items: center;
  gap: var(--space-3);
  box-shadow: 0 1px 3px rgba(0,0,0,0.3);
}

.headerTitle {
  font-size: var(--font-size-lg);
  font-weight: 600;
  letter-spacing: 0.5px;
}

.main {
  flex: 1;
  padding: var(--space-6);
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
}

/* Stats strip */
.statsStrip {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-8);
}

.statCard {
  background: var(--color-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--space-4) var(--space-5);
  text-align: center;
  min-width: 100px;
}

.statValue {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1;
  margin-bottom: var(--space-1);
}

.statLabel {
  font-size: var(--font-size-sm);
  color: var(--color-gray-700);
}

/* Filter bar */
.filterBar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.filterBtn {
  padding: 5px 16px;
  border-radius: var(--radius-full);
  border: 1.5px solid var(--color-primary);
  background: transparent;
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: 500;
  transition: background 0.15s, color 0.15s;
}

.filterBtn:hover {
  background: var(--color-primary-light);
}

.filterBtnActive {
  background: var(--color-primary);
  color: var(--color-white);
}

/* Quick add */
.addBar {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-6);
  padding: var(--space-4);
  background: var(--color-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
}

.addInput {
  flex: 1;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-gray-300);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-md);
  font-family: var(--font-sans);
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.addInput:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-light);
}

.addSelect {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-gray-300);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-md);
  background: white;
}

.addBtn {
  padding: var(--space-2) var(--space-5);
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-md);
  font-weight: 600;
  transition: background 0.15s;
}

.addBtn:hover:not(:disabled) {
  background: var(--color-primary-dark);
}

.addBtn:disabled {
  background: var(--color-gray-300);
  cursor: not-allowed;
}

/* Grid */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(290px, 1fr));
  gap: var(--space-4);
}

/* Empty state */
.emptyState {
  text-align: center;
  padding: 60px var(--space-6);
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-md);
  color: var(--color-gray-700);
}

.emptyIcon {
  font-size: 48px;
  margin-bottom: var(--space-3);
}

.emptyTitle {
  font-size: var(--font-size-lg);
  font-weight: 600;
  margin-bottom: var(--space-2);
}

.resultInfo {
  font-size: var(--font-size-sm);
  color: var(--color-gray-700);
  margin-bottom: var(--space-4);
}

/* Responsive */
@media (max-width: 600px) {
  .statsStrip { flex-wrap: wrap; }
  .addBar { flex-wrap: wrap; }
  .addInput { min-width: 100%; }
}
```

### `src/App.tsx` — styled version

```tsx
import { useState, useMemo } from 'react';
import clsx from 'clsx';
import EmployeeCard from './components/EmployeeCard';
import { INITIAL_EMPLOYEES } from './data/employees';
import { Employee, Department } from './types';
import styles from './App.module.css';

let nextId = INITIAL_EMPLOYEES.length + 1;
const DEPARTMENTS: Department[] = ['All', 'Engineering', 'Marketing', 'HR', 'Finance', 'Sales'];

function App() {
  const [employees, setEmployees] = useState<Employee[]>(INITIAL_EMPLOYEES);
  const [filter, setFilter]       = useState<Department>('All');
  const [showInactive, setShowInactive] = useState(true);
  const [newName, setNewName]     = useState('');
  const [newDept, setNewDept]     = useState<Exclude<Department,'All'>>('Engineering');

  const filtered = useMemo(() =>
    employees
      .filter(e => filter === 'All' || e.department === filter)
      .filter(e => showInactive || e.isActive),
    [employees, filter, showInactive]
  );

  const handleRemove = (id: number) =>
    setEmployees(prev => prev.filter(e => e.id !== id));

  const handleAdd = () => {
    if (!newName.trim()) return;
    setEmployees(prev => [...prev, {
      id: nextId++,
      name: newName.trim(),
      email: `${newName.toLowerCase().replace(/\s+/g, '.')}@ibm.com`,
      department: newDept,
      salary: 70000,
      isActive: true,
      joinDate: new Date().toISOString().split('T')[0],
    }]);
    setNewName('');
  };

  const stats = {
    total: employees.length,
    active: employees.filter(e => e.isActive).length,
    depts: new Set(employees.map(e => e.department)).size,
  };

  return (
    <div className={styles.page}>

      <header className={styles.header}>
        <span style={{ fontSize: '24px' }}>🏢</span>
        <span className={styles.headerTitle}>IBM Employee Management System</span>
      </header>

      <main className={styles.main}>

        {/* Stats */}
        <div className={styles.statsStrip}>
          {[
            { label: 'Total Employees', value: stats.total },
            { label: 'Active', value: stats.active },
            { label: 'Departments', value: stats.depts },
          ].map(s => (
            <div key={s.label} className={styles.statCard}>
              <div className={styles.statValue}>{s.value}</div>
              <div className={styles.statLabel}>{s.label}</div>
            </div>
          ))}
        </div>

        {/* Add bar */}
        <div className={styles.addBar}>
          <input
            className={styles.addInput}
            value={newName}
            onChange={e => setNewName(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleAdd()}
            placeholder="New employee name..."
          />
          <select
            className={styles.addSelect}
            value={newDept}
            onChange={e => setNewDept(e.target.value as Exclude<Department,'All'>)}
          >
            {DEPARTMENTS.filter(d => d !== 'All').map(d => (
              <option key={d}>{d}</option>
            ))}
          </select>
          <button
            className={styles.addBtn}
            onClick={handleAdd}
            disabled={!newName.trim()}
          >
            + Add Employee
          </button>
        </div>

        {/* Filter bar */}
        <div className={styles.filterBar}>
          {DEPARTMENTS.map(dept => (
            <button
              key={dept}
              className={clsx(styles.filterBtn, filter === dept && styles.filterBtnActive)}
              onClick={() => setFilter(dept)}
            >
              {dept}
            </button>
          ))}
          <label style={{ marginLeft: 'auto', fontSize: '13px', display: 'flex', gap: '6px', alignItems: 'center' }}>
            <input
              type="checkbox"
              checked={showInactive}
              onChange={e => setShowInactive(e.target.checked)}
            />
            Show inactive
          </label>
        </div>

        {/* Result info */}
        <p className={styles.resultInfo}>
          Showing {filtered.length} of {employees.length} employees
          {filter !== 'All' && ` · ${filter}`}
        </p>

        {/* Grid or empty state */}
        {filtered.length > 0
          ? (
            <div className={styles.grid}>
              {filtered.map(emp => (
                <EmployeeCard key={emp.id} employee={emp} onRemove={handleRemove} />
              ))}
            </div>
          )
          : (
            <div className={styles.emptyState}>
              <div className={styles.emptyIcon}>🔍</div>
              <p className={styles.emptyTitle}>No employees found</p>
              <p>
                {filter !== 'All'
                  ? `No employees in ${filter}.`
                  : 'Add your first employee above.'}
              </p>
            </div>
          )
        }

      </main>
    </div>
  );
}

export default App;
```

---

## 5.8 Bonus — Tailwind CSS Setup

If your team prefers Tailwind:

```bash
npm install -D tailwindcss @tailwindcss/vite
```

```ts
// vite.config.ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  plugins: [tailwindcss(), react()],
});
```

```css
/* src/index.css — replace with */
@import "tailwindcss";
```

```tsx
// Now use utility classes
<div className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm hover:shadow-md transition-shadow">
  <h3 className="text-lg font-semibold text-gray-900">{employee.name}</h3>
</div>
```

---

## Summary

- **CSS Modules** scope class names to the file — no collisions
- **Global CSS** + **CSS variables** = design tokens for the whole app
- **`clsx`** makes conditional class names readable
- Inline styles are fine for truly dynamic values; use CSS for everything else

**Next → [Module 06: Debugging](./06-debugging.md)**
