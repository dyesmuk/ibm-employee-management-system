# Module 03 — Props, State, and Events

## Learning Objectives
- Pass data into components using props
- Manage changing data with `useState`
- Handle user interactions with event handlers
- Understand the re-render cycle
- Apply to EMS: make `EmployeeCard` accept real data, add/remove employees

---

## 3.1 Props — Passing Data Into Components

Right now our `EmployeeCard` is hardcoded with Alice's data. That's useless — we need to pass in different data for each employee.

**Props** (properties) are the way a parent component passes data **down** to a child.

```
Parent Component
  │  passes props
  ▼
Child Component
  (reads props, renders them)
  (CANNOT modify props)
```

### The rules of props

1. Props flow **one way** — parent → child only
2. Props are **read-only** in the child — never mutate them
3. They are just function parameters

### Adding props to `EmployeeCard`

```tsx
// src/types/index.ts  — define the shape once, reuse everywhere
export interface Employee {
  id: number;
  name: string;
  email: string;
  department: string;
  salary: number;
  isActive: boolean;
  joinDate: string;
}
```

```tsx
// src/components/EmployeeCard.tsx

import { Employee } from '../types';

// Define what props this component accepts
interface EmployeeCardProps {
  employee: Employee;
}

function EmployeeCard({ employee }: EmployeeCardProps) {
  return (
    <div style={{
      border: '1px solid #e0e0e0',
      borderRadius: '8px',
      padding: '20px',
      margin: '10px',
      background: 'white',
      maxWidth: '300px',
    }}>
      <h3 style={{ margin: '0 0 8px', color: '#161616' }}>{employee.name}</h3>
      <p style={{ margin: '4px 0', color: '#525252', fontSize: '14px' }}>
        {employee.department}
      </p>
      <p style={{ margin: '4px 0', color: '#525252', fontSize: '14px' }}>
        {employee.email}
      </p>
      <p style={{ margin: '8px 0 0', fontWeight: 600, color: '#0062ff' }}>
        ${employee.salary.toLocaleString()}
      </p>
      <span style={{
        display: 'inline-block',
        marginTop: '10px',
        padding: '2px 10px',
        borderRadius: '12px',
        fontSize: '12px',
        fontWeight: 600,
        background: employee.isActive ? '#defbe6' : '#fff1f1',
        color: employee.isActive ? '#0e6027' : '#a2191f',
      }}>
        {employee.isActive ? 'Active' : 'Inactive'}
      </span>
    </div>
  );
}

export default EmployeeCard;
```

### Using the component with props

```tsx
// src/App.tsx
import EmployeeCard from './components/EmployeeCard';
import { Employee } from './types';

// Static data for now — we'll fetch from API in Module 08
const sampleEmployee: Employee = {
  id: 1,
  name: 'Alice Johnson',
  email: 'alice@ibm.com',
  department: 'Engineering',
  salary: 95000,
  isActive: true,
  joinDate: '2021-03-15',
};

function App() {
  return (
    <div style={{ padding: '24px' }}>
      <h1>IBM Employee Management System</h1>
      <EmployeeCard employee={sampleEmployee} />
    </div>
  );
}

export default App;
```

### Props with default values

```tsx
interface EmployeeCardProps {
  employee: Employee;
  compact?: boolean;          // optional — may be undefined
  onSelect?: (id: number) => void;
}

// Default values with destructuring defaults
function EmployeeCard({ employee, compact = false, onSelect }: EmployeeCardProps) {
  // compact defaults to false if not provided
}
```

---

## 3.2 The `children` Prop

A special built-in prop. Whatever you put between opening and closing tags becomes `children`.

```tsx
interface CardProps {
  title: string;
  children: React.ReactNode;  // anything React can render
}

function Card({ title, children }: CardProps) {
  return (
    <div className="card">
      <h2>{title}</h2>
      <div className="card-body">{children}</div>
    </div>
  );
}

// Usage — content between tags becomes children
<Card title="Engineering Team">
  <EmployeeCard employee={alice} />
  <EmployeeCard employee={bob} />
</Card>
```

---

## 3.3 State — Data That Changes Over Time

Props are static from the child's point of view. **State** is data that belongs to a component and can change. When state changes, React re-renders the component.

### `useState` hook

```tsx
import { useState } from 'react';

// Signature:
// const [currentValue, setterFunction] = useState(initialValue);

const [count, setCount] = useState(0);
//     ↑           ↑                ↑
//  current     function to    starting value
//   value      update it
```

### What happens when state changes?

```
1. setCount(count + 1) is called
2. React schedules a re-render
3. Component function runs again
4. count now has the new value
5. JSX is re-evaluated
6. React diffs new Virtual DOM vs previous
7. Only changed DOM nodes are updated
8. Browser repaints
```

This is why you **must use the setter** — directly mutating the variable skips step 2 and nothing updates.

```tsx
// ❌ Wrong — direct mutation, React doesn't know about it
count = count + 1;

// ✅ Correct — tells React to schedule re-render
setCount(count + 1);
```

### Counter example (classic)

```tsx
import { useState } from 'react';

function Counter() {
  const [count, setCount] = useState(0);

  return (
    <div>
      <p>Count: {count}</p>
      <button onClick={() => setCount(count + 1)}>+</button>
      <button onClick={() => setCount(count - 1)}>-</button>
      <button onClick={() => setCount(0)}>Reset</button>
    </div>
  );
}
```

---

## 3.4 State with Objects

When state is an object, always spread the old state and override only what changed.

```tsx
interface FormData {
  name: string;
  email: string;
  department: string;
}

function EmployeeForm() {
  const [form, setForm] = useState<FormData>({
    name: '',
    email: '',
    department: 'Engineering',
  });

  const updateField = (field: keyof FormData, value: string) => {
    // ✅ Spread preserves other fields, only overrides the one we change
    setForm(prev => ({ ...prev, [field]: value }));
  };

  // ❌ Wrong — mutates existing object
  // form.name = 'Alice'; setForm(form);

  return (
    <form>
      <input
        value={form.name}
        onChange={e => updateField('name', e.target.value)}
        placeholder="Name"
      />
      <input
        value={form.email}
        onChange={e => updateField('email', e.target.value)}
        placeholder="Email"
      />
      <p>Name: {form.name}</p>
    </form>
  );
}
```

---

## 3.5 State with Arrays

Always create a **new array** — never mutate the existing one.

```tsx
// Add an item
setEmployees(prev => [...prev, newEmployee]);

// Remove an item
setEmployees(prev => prev.filter(e => e.id !== idToRemove));

// Update one item
setEmployees(prev =>
  prev.map(e => e.id === targetId ? { ...e, isActive: false } : e)
);
```

---

## 3.6 Events

React wraps native DOM events in a "SyntheticEvent" that works consistently across browsers.

```tsx
// Click
const handleClick = (e: React.MouseEvent<HTMLButtonElement>) => {
  e.preventDefault();
  console.log('clicked');
};
<button onClick={handleClick}>Click</button>
<button onClick={() => handleClick}>Click</button>   // ❌ returns a function reference!
<button onClick={() => console.log('hi')}>Click</button>  // ✅ inline

// Important gotcha:
<button onClick={handleClick}>     // ✅ passes the function reference
<button onClick={handleClick()}>   // ❌ CALLS it immediately during render!

// Input change
const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
  setValue(e.target.value);
};
<input onChange={handleChange} />

// Form submit
const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
  e.preventDefault();  // ALWAYS prevent default on forms
  // process form data
};
<form onSubmit={handleSubmit}>

// Passing arguments — wrap in arrow function
const handleDelete = (id: number) => {
  setEmployees(prev => prev.filter(e => e.id !== id));
};
<button onClick={() => handleDelete(employee.id)}>Delete</button>
```

### Common event types reference

| Event | TypeScript Type | Element |
|-------|----------------|---------|
| `onClick` | `MouseEvent<HTMLButtonElement>` | button |
| `onChange` | `ChangeEvent<HTMLInputElement>` | input |
| `onChange` | `ChangeEvent<HTMLSelectElement>` | select |
| `onChange` | `ChangeEvent<HTMLTextAreaElement>` | textarea |
| `onSubmit` | `FormEvent<HTMLFormElement>` | form |
| `onKeyDown` | `KeyboardEvent<HTMLInputElement>` | input |
| `onFocus` | `FocusEvent<HTMLInputElement>` | input |
| `onBlur` | `FocusEvent<HTMLInputElement>` | input |

---

## 3.7 Lifting State Up

When two sibling components need to share data, move the state **up** to their nearest common parent. The parent owns the state and passes it down as props along with setter callbacks.

```
          App  ← owns `employees` state
         /   \
    Controls  EmployeeList
   (add/remove) (shows list)
```

```tsx
// App is the "single source of truth"
function App() {
  const [employees, setEmployees] = useState<Employee[]>(initialEmployees);

  const addEmployee = (emp: Employee) => {
    setEmployees(prev => [...prev, emp]);
  };

  const removeEmployee = (id: number) => {
    setEmployees(prev => prev.filter(e => e.id !== id));
  };

  return (
    <div>
      <EmployeeControls onAdd={addEmployee} />
      <EmployeeList employees={employees} onRemove={removeEmployee} />
    </div>
  );
}
```

---

## 3.8 EMS Project — Employees as State

Now let's make the EMS show multiple employees and allow adding/removing.

### `src/types/index.ts`

```ts
export interface Employee {
  id: number;
  name: string;
  email: string;
  department: string;
  salary: number;
  isActive: boolean;
  joinDate: string;
}

export type Department = 'Engineering' | 'Marketing' | 'HR' | 'Finance' | 'Sales' | 'All';
```

### `src/data/employees.ts` — seed data

```ts
import { Employee } from '../types';

export const INITIAL_EMPLOYEES: Employee[] = [
  {
    id: 1,
    name: 'Alice Johnson',
    email: 'alice@ibm.com',
    department: 'Engineering',
    salary: 95000,
    isActive: true,
    joinDate: '2021-03-15',
  },
  {
    id: 2,
    name: 'Bob Smith',
    email: 'bob@ibm.com',
    department: 'Marketing',
    salary: 72000,
    isActive: true,
    joinDate: '2020-07-01',
  },
  {
    id: 3,
    name: 'Carol White',
    email: 'carol@ibm.com',
    department: 'Engineering',
    salary: 88000,
    isActive: false,
    joinDate: '2019-11-20',
  },
  {
    id: 4,
    name: 'David Lee',
    email: 'david@ibm.com',
    department: 'HR',
    salary: 68000,
    isActive: true,
    joinDate: '2022-01-10',
  },
];
```

### `src/components/EmployeeCard.tsx` — add remove button

```tsx
import { Employee } from '../types';

interface EmployeeCardProps {
  employee: Employee;
  onRemove: (id: number) => void;
}

function EmployeeCard({ employee, onRemove }: EmployeeCardProps) {
  return (
    <div style={{
      border: '1px solid #e0e0e0',
      borderRadius: '8px',
      padding: '20px',
      background: 'white',
      position: 'relative',
    }}>
      <button
        onClick={() => onRemove(employee.id)}
        style={{
          position: 'absolute',
          top: '12px',
          right: '12px',
          background: 'none',
          border: 'none',
          cursor: 'pointer',
          fontSize: '18px',
          color: '#6f6f6f',
        }}
        aria-label={`Remove ${employee.name}`}
      >
        ×
      </button>

      <h3 style={{ margin: '0 0 6px', paddingRight: '30px' }}>{employee.name}</h3>
      <p style={{ fontSize: '14px', color: '#525252', margin: '3px 0' }}>
        📧 {employee.email}
      </p>
      <p style={{ fontSize: '14px', color: '#525252', margin: '3px 0' }}>
        🏢 {employee.department}
      </p>
      <p style={{ fontSize: '14px', color: '#525252', margin: '3px 0' }}>
        📅 Joined {new Date(employee.joinDate).toLocaleDateString()}
      </p>
      <p style={{ fontWeight: 700, color: '#0062ff', margin: '10px 0 8px' }}>
        ${employee.salary.toLocaleString()} / yr
      </p>
      <span style={{
        display: 'inline-block',
        padding: '2px 10px',
        borderRadius: '12px',
        fontSize: '12px',
        fontWeight: 600,
        background: employee.isActive ? '#defbe6' : '#fff1f1',
        color: employee.isActive ? '#0e6027' : '#a2191f',
      }}>
        {employee.isActive ? 'Active' : 'Inactive'}
      </span>
    </div>
  );
}

export default EmployeeCard;
```

### `src/App.tsx` — full state management

```tsx
import { useState } from 'react';
import EmployeeCard from './components/EmployeeCard';
import { INITIAL_EMPLOYEES } from './data/employees';
import { Employee } from './types';

// Simple unique ID generator (we'll use a proper one later)
let nextId = INITIAL_EMPLOYEES.length + 1;

function App() {
  const [employees, setEmployees] = useState<Employee[]>(INITIAL_EMPLOYEES);
  const [newName, setNewName]   = useState('');
  const [newDept, setNewDept]   = useState('Engineering');

  const handleRemove = (id: number) => {
    setEmployees(prev => prev.filter(e => e.id !== id));
  };

  const handleAdd = () => {
    if (!newName.trim()) return;

    const emp: Employee = {
      id: nextId++,
      name: newName.trim(),
      email: `${newName.toLowerCase().replace(/\s+/g, '.')}@ibm.com`,
      department: newDept,
      salary: 70000,
      isActive: true,
      joinDate: new Date().toISOString().split('T')[0],
    };

    setEmployees(prev => [...prev, emp]);
    setNewName('');
  };

  return (
    <div style={{ padding: '24px', maxWidth: '1100px', margin: '0 auto' }}>
      {/* Header */}
      <h1 style={{ marginBottom: '4px' }}>🏢 IBM Employee Management System</h1>
      <p style={{ color: '#525252', marginBottom: '32px' }}>
        {employees.length} employee{employees.length !== 1 ? 's' : ''}
      </p>

      {/* Quick add form */}
      <div style={{
        display: 'flex',
        gap: '10px',
        marginBottom: '32px',
        padding: '16px',
        background: 'white',
        borderRadius: '8px',
        border: '1px solid #e0e0e0',
      }}>
        <input
          value={newName}
          onChange={e => setNewName(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && handleAdd()}
          placeholder="Employee name..."
          style={{
            flex: 1,
            padding: '8px 12px',
            border: '1px solid #ccc',
            borderRadius: '4px',
            fontSize: '14px',
          }}
        />
        <select
          value={newDept}
          onChange={e => setNewDept(e.target.value)}
          style={{ padding: '8px 12px', border: '1px solid #ccc', borderRadius: '4px' }}
        >
          <option>Engineering</option>
          <option>Marketing</option>
          <option>HR</option>
          <option>Finance</option>
          <option>Sales</option>
        </select>
        <button
          onClick={handleAdd}
          disabled={!newName.trim()}
          style={{
            padding: '8px 20px',
            background: '#0062ff',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            fontWeight: 600,
          }}
        >
          Add
        </button>
      </div>

      {/* Employee grid */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
        gap: '16px',
      }}>
        {employees.map(emp => (
          <EmployeeCard
            key={emp.id}
            employee={emp}
            onRemove={handleRemove}
          />
        ))}
      </div>

      {employees.length === 0 && (
        <p style={{ textAlign: 'center', color: '#6f6f6f', marginTop: '60px' }}>
          No employees. Add one above.
        </p>
      )}
    </div>
  );
}

export default App;
```

---

## 3.9 Re-render Visualized

Open React DevTools → Components tab. Click the settings ⚙ icon → check "Highlight updates when components render". Now when you type in the input or add an employee, you'll see blue flashes around components that re-render.

Notice:
- Typing in the name input re-renders `App` (state lives there) but NOT `EmployeeCard` components (their props didn't change)
- Adding an employee re-renders `App` AND renders a new `EmployeeCard`
- Removing an employee re-renders `App` and the removed card disappears

---

## 3.10 The Functional Update Pattern

When your new state depends on the previous state, use the **functional update** form:

```tsx
// ❌ May use stale state (batch updates issue)
setCount(count + 1);
setCount(count + 1);
setCount(count + 1);
// count ends up as count + 1, not count + 3

// ✅ Always uses the latest state
setCount(prev => prev + 1);
setCount(prev => prev + 1);
setCount(prev => prev + 1);
// count ends up as count + 3
```

Always use the functional form when updating based on previous state.

---

## Summary

| Concept | Key Rule |
|---------|----------|
| Props | Read-only in child, flow parent → child |
| State | `useState` — triggers re-render when changed |
| Events | Pass handler reference, don't invoke it |
| Object state | Spread: `{ ...prev, field: value }` |
| Array state | New array: `[...prev, item]`, `.filter()`, `.map()` |
| Lifting state | Move shared state to nearest common parent |

**Next → [Module 04: Lists and Conditionals](./04-lists-and-conditionals.md)**
