# Module 04 — Working with Lists and Conditionals

## Learning Objectives
- Render arrays of data with `.map()` and the `key` prop
- Understand why keys matter and how to pick them
- Apply every conditional rendering pattern
- Apply to EMS: department filter, active/inactive toggle, empty state

---

## 4.1 Rendering Lists

The primary pattern for rendering a list in React is JavaScript's `.map()` method, which transforms an array of data into an array of JSX elements.

```tsx
const fruits = ['Apple', 'Banana', 'Cherry'];

// .map() returns a new array — React renders it
<ul>
  {fruits.map(fruit => <li>{fruit}</li>)}
</ul>

// Multi-line: use () for the return
<ul>
  {fruits.map(fruit => (
    <li style={{ padding: '4px 0' }}>
      {fruit}
    </li>
  ))}
</ul>
```

This works, but React will print a warning in the console:
> **Warning: Each child in a list should have a unique "key" prop.**

---

## 4.2 The `key` Prop

`key` is a special prop that tells React how to match elements across re-renders. It must be **unique among siblings** and **stable** across renders.

```tsx
// ✅ Correct — unique stable ID
{employees.map(emp => (
  <EmployeeCard key={emp.id} employee={emp} onRemove={handleRemove} />
))}

// ✅ Also fine — unique string like email
{employees.map(emp => (
  <EmployeeCard key={emp.email} employee={emp} />
))}
```

### Why does key exist?

When the list changes (add, remove, reorder), React needs to figure out which DOM nodes to update, remove, or create. The key is React's tracking ID.

```
Before re-render:    After re-render:
key=1  Alice         key=1  Alice    → same element, keep
key=2  Bob           key=3  Carol    → key=2 gone, remove it
key=3  Carol         key=4  David    → new key=4, create it
```

Without keys, React re-renders the whole list from scratch — which is slow and breaks component-local state.

### ❌ Don't use array index as key

```tsx
// ❌ Problematic when the list can be reordered or filtered
{employees.map((emp, index) => (
  <EmployeeCard key={index} employee={emp} />
))}
```

If you remove the first item, all remaining items' indexes shift down by one. React sees keys `0, 1, 2` before and `0, 1` after — it thinks the THIRD item was removed, not the first. This causes subtle rendering bugs.

**Use array index only when:**
- The list is static (never changes order)
- Items have no persistent identity
- The list has no stateful child components

---

## 4.3 Lists of Components

```tsx
// src/components/EmployeeList.tsx

import { Employee } from '../types';
import EmployeeCard from './EmployeeCard';

interface EmployeeListProps {
  employees: Employee[];
  onRemove: (id: number) => void;
}

function EmployeeList({ employees, onRemove }: EmployeeListProps) {
  return (
    <div style={{
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
      gap: '16px',
    }}>
      {employees.map(emp => (
        <EmployeeCard
          key={emp.id}        // ← key on the outermost element returned by map
          employee={emp}
          onRemove={onRemove}
        />
      ))}
    </div>
  );
}

export default EmployeeList;
```

> **Note:** The `key` goes on the element returned by `.map()`, not inside the component. `EmployeeCard` cannot read `props.key` — it's reserved for React.

---

## 4.4 Conditional Rendering — All Five Patterns

### Pattern 1: Ternary (`? :`) — one thing OR another

```tsx
// Renders EITHER the active badge OR the inactive badge
<span style={{ color: employee.isActive ? 'green' : 'red' }}>
  {employee.isActive ? 'Active' : 'Inactive'}
</span>

// Larger blocks — still valid
{isLoading
  ? <Spinner />
  : <EmployeeList employees={employees} onRemove={handleRemove} />
}
```

### Pattern 2: Short-circuit (`&&`) — show OR nothing

```tsx
// Renders the element only when the condition is true
{employees.length === 0 && <p>No employees found.</p>}

{isAdmin && <button>Delete All</button>}

{error && <div className="error-banner">{error}</div>}
```

> ⚠️ **Gotcha:** `{0 && <p>Text</p>}` renders the number `0` on screen!  
> Fix: `{employees.length > 0 && <p>...</p>}` or `{!!count && ...}` or use ternary.

### Pattern 3: Nullish coalescing in rendering

```tsx
// Show a dash if no department value
<td>{employee.department ?? '—'}</td>

// Show placeholder if no name
<h3>{employee.name || 'Unknown Employee'}</h3>
```

### Pattern 4: Early return — guard clauses

```tsx
function EmployeeDetail({ employeeId }: { employeeId: number | null }) {
  // Guard: nothing selected
  if (employeeId === null) {
    return <p>Select an employee to view details.</p>;
  }

  const employee = findEmployeeById(employeeId);

  // Guard: not found
  if (!employee) {
    return <p>Employee not found.</p>;
  }

  // Happy path — employee is guaranteed to exist here
  return (
    <div>
      <h2>{employee.name}</h2>
      <p>{employee.department}</p>
    </div>
  );
}
```

### Pattern 5: Variable assignment before return

```tsx
function StatusBanner({ status }: { status: 'loading' | 'error' | 'empty' | 'success' }) {
  // Compute what to show before the return
  let content: React.ReactNode;

  if (status === 'loading') {
    content = <div className="spinner">Loading...</div>;
  } else if (status === 'error') {
    content = <div className="error">Something went wrong.</div>;
  } else if (status === 'empty') {
    content = <p>No data available.</p>;
  } else {
    content = null;
  }

  return <div className="banner">{content}</div>;
}
```

---

## 4.5 Rendering Nothing

Returning `null` from a component renders nothing — no DOM node, no whitespace.

```tsx
function DevOnlyBanner() {
  if (!import.meta.env.DEV) return null;
  return <div className="dev-banner">⚠️ Development Mode</div>;
}
```

---

## 4.6 EMS Project — Department Filter + Empty State

Let's add filtering to the EMS.

### Update `src/App.tsx`

```tsx
import { useState, useMemo } from 'react';
import EmployeeCard from './components/EmployeeCard';
import { INITIAL_EMPLOYEES } from './data/employees';
import { Employee, Department } from './types';

let nextId = INITIAL_EMPLOYEES.length + 1;

const DEPARTMENTS: Department[] = ['All', 'Engineering', 'Marketing', 'HR', 'Finance', 'Sales'];

function App() {
  const [employees, setEmployees] = useState<Employee[]>(INITIAL_EMPLOYEES);
  const [filter, setFilter]       = useState<Department>('All');
  const [showInactive, setShowInactive] = useState(true);
  const [newName, setNewName]     = useState('');
  const [newDept, setNewDept]     = useState<Department>('Engineering');

  // Derived data — recalculates when employees, filter, or showInactive changes
  const filtered = useMemo(() => {
    return employees
      .filter(e => filter === 'All' || e.department === filter)
      .filter(e => showInactive || e.isActive);
  }, [employees, filter, showInactive]);

  const handleRemove = (id: number) =>
    setEmployees(prev => prev.filter(e => e.id !== id));

  const handleAdd = () => {
    if (!newName.trim()) return;
    const emp: Employee = {
      id: nextId++,
      name: newName.trim(),
      email: `${newName.toLowerCase().replace(/\s+/g, '.')}@ibm.com`,
      department: newDept === 'All' ? 'Engineering' : newDept,
      salary: 70000,
      isActive: true,
      joinDate: new Date().toISOString().split('T')[0],
    };
    setEmployees(prev => [...prev, emp]);
    setNewName('');
  };

  // Stats derived from full list (not filtered)
  const stats = {
    total: employees.length,
    active: employees.filter(e => e.isActive).length,
    departments: new Set(employees.map(e => e.department)).size,
  };

  return (
    <div style={{ padding: '24px', maxWidth: '1100px', margin: '0 auto' }}>

      {/* ── Header ── */}
      <h1 style={{ marginBottom: '4px' }}>🏢 IBM EMS</h1>

      {/* ── Stats row ── */}
      <div style={{ display: 'flex', gap: '24px', margin: '16px 0 32px' }}>
        {[
          { label: 'Total', value: stats.total },
          { label: 'Active', value: stats.active },
          { label: 'Departments', value: stats.departments },
        ].map(stat => (
          <div key={stat.label} style={{
            background: 'white',
            border: '1px solid #e0e0e0',
            borderRadius: '8px',
            padding: '12px 20px',
            textAlign: 'center',
          }}>
            <div style={{ fontSize: '28px', fontWeight: 700, color: '#0062ff' }}>
              {stat.value}
            </div>
            <div style={{ fontSize: '13px', color: '#525252' }}>{stat.label}</div>
          </div>
        ))}
      </div>

      {/* ── Quick Add ── */}
      <div style={{
        display: 'flex', gap: '10px', marginBottom: '24px',
        padding: '16px', background: 'white',
        borderRadius: '8px', border: '1px solid #e0e0e0',
      }}>
        <input
          value={newName}
          onChange={e => setNewName(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && handleAdd()}
          placeholder="New employee name..."
          style={{ flex: 1, padding: '8px 12px', border: '1px solid #ccc', borderRadius: '4px' }}
        />
        <select
          value={newDept}
          onChange={e => setNewDept(e.target.value as Department)}
          style={{ padding: '8px 12px', border: '1px solid #ccc', borderRadius: '4px' }}
        >
          {DEPARTMENTS.filter(d => d !== 'All').map(d => (
            <option key={d} value={d}>{d}</option>
          ))}
        </select>
        <button
          onClick={handleAdd}
          disabled={!newName.trim()}
          style={{
            padding: '8px 20px', background: '#0062ff', color: 'white',
            border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 600,
          }}
        >
          + Add
        </button>
      </div>

      {/* ── Filter bar ── */}
      <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '12px' }}>
        {DEPARTMENTS.map(dept => (
          <button
            key={dept}
            onClick={() => setFilter(dept)}
            style={{
              padding: '6px 16px',
              borderRadius: '20px',
              border: '1px solid',
              cursor: 'pointer',
              fontWeight: filter === dept ? 700 : 400,
              background: filter === dept ? '#0062ff' : 'white',
              color: filter === dept ? 'white' : '#0062ff',
              borderColor: '#0062ff',
              fontSize: '13px',
            }}
          >
            {dept}
          </button>
        ))}

        <label style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '14px' }}>
          <input
            type="checkbox"
            checked={showInactive}
            onChange={e => setShowInactive(e.target.checked)}
          />
          Show inactive
        </label>
      </div>

      {/* ── Results count ── */}
      <p style={{ color: '#525252', fontSize: '14px', marginBottom: '16px' }}>
        Showing {filtered.length} of {employees.length} employees
        {filter !== 'All' && ` · filtered by ${filter}`}
      </p>

      {/* ── Employee grid ── */}
      {filtered.length > 0
        ? (
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
            gap: '16px',
          }}>
            {filtered.map(emp => (
              <EmployeeCard
                key={emp.id}
                employee={emp}
                onRemove={handleRemove}
              />
            ))}
          </div>
        )
        : (
          // ── Empty state ──
          <div style={{
            textAlign: 'center', padding: '60px 20px',
            color: '#525252', border: '2px dashed #e0e0e0',
            borderRadius: '8px',
          }}>
            <p style={{ fontSize: '40px', marginBottom: '12px' }}>🔍</p>
            <p style={{ fontSize: '18px', fontWeight: 600, marginBottom: '8px' }}>
              No employees found
            </p>
            <p style={{ fontSize: '14px' }}>
              {filter !== 'All'
                ? `No employees in ${filter}. Try a different department.`
                : 'Add your first employee above.'}
            </p>
          </div>
        )
      }
    </div>
  );
}

export default App;
```

---

## 4.7 Rendering Fragments With Keys

When you need to return multiple adjacent elements from a map but don't want extra DOM nodes:

```tsx
// Fragment shorthand <> </> can't have a key prop
// Use the explicit <Fragment key={...}> syntax instead

import { Fragment } from 'react';

{employees.map(emp => (
  <Fragment key={emp.id}>
    <dt>{emp.name}</dt>
    <dd>{emp.department}</dd>
  </Fragment>
))}
```

---

## Summary

| Concept | Pattern |
|---------|---------|
| Render list | `arr.map(item => <El key={item.id} />)` |
| Key | Unique, stable ID — NOT array index (usually) |
| If/else render | `{cond ? <A /> : <B />}` |
| Show or nothing | `{cond && <A />}` |
| Guard clause | `if (!x) return <Fallback />;` before main return |
| Nothing | `return null` |
| Derived list | `useMemo` wrapping `.filter().map()` |

**Next → [Module 05: Styling](./05-styling.md)**
