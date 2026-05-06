# Module 01 — JavaScript & TypeScript Refresh

## Learning Objectives
- Recall the ES6+ features React uses every day
- Understand TypeScript type annotations used in React
- Know when and how to use destructuring, spread, and async/await

> **Skip this module** if you're already comfortable with ES2022+ JavaScript and basic TypeScript.

---

## 1.1 `const` and `let` — Never Use `var`

```ts
// var — function scoped, hoisted, reassignable — AVOID
var name = 'Alice';

// let — block scoped, reassignable
let count = 0;
count = 1; // ✅

// const — block scoped, NOT reassignable
const MAX = 100;
MAX = 200; // ❌ TypeError

// BUT: const objects/arrays are still mutable
const user = { name: 'Alice' };
user.name = 'Bob';  // ✅ mutating the object is fine
user = {};          // ❌ reassigning the variable is not
```

**React rule:** Use `const` for everything. Use `let` only when you know you'll reassign (loop counters). Never `var`.

---

## 1.2 Arrow Functions

```ts
// Traditional function
function add(a: number, b: number): number {
  return a + b;
}

// Arrow function — same thing
const add = (a: number, b: number): number => {
  return a + b;
};

// Implicit return (single expression, no braces)
const add = (a: number, b: number): number => a + b;

// Single parameter — parens optional (but always include for consistency)
const double = (n: number) => n * 2;

// No parameters
const greet = () => 'Hello!';

// Returning an object — wrap in parens to avoid {} ambiguity
const makeUser = (name: string) => ({ name, active: true });
```

**Where React uses arrow functions constantly:**

```tsx
// 1. Event handlers
<button onClick={() => console.log('clicked')}>Click</button>

// 2. Array methods in JSX
{employees.map(emp => <EmployeeCard key={emp.id} employee={emp} />)}

// 3. useEffect callbacks
useEffect(() => { fetchData(); }, []);

// 4. useState functional updates
setCount(prev => prev + 1);
```

---

## 1.3 Destructuring

Destructuring extracts values from objects and arrays into named variables.

### Object destructuring

```ts
const employee = {
  id: 1,
  name: 'Alice Johnson',
  department: 'Engineering',
  salary: 95000,
  address: { city: 'Bengaluru', country: 'India' }
};

// Without destructuring
const name = employee.name;
const dept = employee.department;

// With destructuring
const { name, department } = employee;

// Rename while destructuring
const { name: fullName, department: dept } = employee;

// Default values
const { name, role = 'employee' } = employee; // role → 'employee' if missing

// Nested destructuring
const { address: { city } } = employee; // city → 'Bengaluru'

// In function parameters (very common in React for props)
function EmployeeCard({ name, department, salary }: Employee) {
  return <p>{name} — {department}</p>;
}
```

### Array destructuring

```ts
const colors = ['red', 'green', 'blue'];

const [first, second, third] = colors;
// first → 'red', second → 'green', third → 'blue'

// Skip elements with empty slots
const [, , third] = colors;

// useState always uses array destructuring
const [count, setCount] = useState(0);
//     ↑ value  ↑ setter
```

---

## 1.4 Spread Operator (`...`)

```ts
// Spread array
const a = [1, 2, 3];
const b = [4, 5, 6];
const merged = [...a, ...b]; // [1, 2, 3, 4, 5, 6]
const copy   = [...a];       // [1, 2, 3] — shallow copy

// Spread object
const defaults = { theme: 'light', lang: 'en', fontSize: 14 };
const overrides = { lang: 'hi', fontSize: 16 };
const config = { ...defaults, ...overrides };
// → { theme: 'light', lang: 'hi', fontSize: 16 }
// Right-side wins on conflict

// Very common in React state updates
setEmployee(prev => ({ ...prev, name: 'Bob' })); // update one field
setList(prev => [...prev, newItem]);             // add item to array
setList(prev => prev.filter(i => i.id !== id));  // remove item
```

### Rest parameters

```ts
// Collect remaining elements
function sum(first: number, ...rest: number[]): number {
  return rest.reduce((acc, n) => acc + n, first);
}
sum(1, 2, 3, 4); // 10

// Common in React: separating "own" props from pass-through props
function Wrapper({ className, ...rest }: Props) {
  return <div className={`wrapper ${className}`} {...rest} />;
}
```

---

## 1.5 Template Literals

```ts
const name = 'Alice';
const dept = 'Engineering';

// Old concatenation — messy
'Hello, ' + name + '! Department: ' + dept

// Template literal — clean
`Hello, ${name}! Department: ${dept}`

// Multi-line
const html = `
  <div>
    <h1>${name}</h1>
    <p>${dept}</p>
  </div>
`;

// Expressions inside ${}
`${count > 1 ? 'employees' : 'employee'}`
`$${salary.toLocaleString()}`
```

---

## 1.6 Array Methods

These are the backbone of list rendering in React.

### `.map()` — transform every item

```ts
const salaries = [50000, 75000, 90000];
const formatted = salaries.map(s => `$${s.toLocaleString()}`);
// ['$50,000', '$75,000', '$90,000']

// In React JSX:
{employees.map(emp => (
  <EmployeeCard key={emp.id} employee={emp} />
))}
```

### `.filter()` — keep items matching a condition

```ts
const active = employees.filter(emp => emp.isActive);
const engineers = employees.filter(emp => emp.department === 'Engineering');
```

### `.find()` — first matching item or undefined

```ts
const alice = employees.find(emp => emp.id === 1);
```

### `.findIndex()` — index of first match

```ts
const idx = employees.findIndex(emp => emp.id === 1);
```

### `.some()` / `.every()`

```ts
employees.some(emp => emp.salary > 100000);  // true if at least one matches
employees.every(emp => emp.isActive);         // true if ALL match
```

### `.reduce()` — accumulate into a single value

```ts
const totalSalary = employees.reduce((sum, emp) => sum + emp.salary, 0);
```

### `.sort()` — sort (mutates! always copy first)

```ts
const sorted = [...employees].sort((a, b) => a.name.localeCompare(b.name));
```

---

## 1.7 Modules — Import and Export

Every React file is a module.

```ts
// ── named exports ──────────────────────────────────
// utils/formatters.ts
export const formatSalary = (n: number) => `$${n.toLocaleString()}`;
export const formatDate = (d: string) => new Date(d).toLocaleDateString();

// Importing named exports
import { formatSalary, formatDate } from './utils/formatters';
import { formatSalary as fmt } from './utils/formatters'; // rename

// ── default export ─────────────────────────────────
// components/EmployeeCard.tsx
function EmployeeCard() { /* ... */ }
export default EmployeeCard;

// Importing default export (name can be anything)
import EmployeeCard from './components/EmployeeCard';

// ── mixed ──────────────────────────────────────────
export default EmployeeCard;
export type { Employee };        // type-only export

// Re-export everything from a folder's index.ts
export { EmployeeCard } from './EmployeeCard';
export { EmployeeList } from './EmployeeList';
```

---

## 1.8 Promises and Async / Await

API calls in React are async — you need to understand this.

### The problem with synchronous code

```ts
// This DOES NOT wait for the server to reply
const data = fetch('/api/employees');
console.log(data); // Promise { <pending> } — NOT the data!
```

### Promises

```ts
fetch('/api/employees')
  .then(response => response.json())
  .then(data => console.log(data))
  .catch(error => console.error(error));
```

### Async / Await — same thing, cleaner syntax

```ts
async function loadEmployees() {
  try {
    const response = await fetch('/api/employees');
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Failed:', error);
    throw error;
  }
}
```

### In React's `useEffect`

```tsx
useEffect(() => {
  // useEffect itself cannot be async
  // wrap the async logic in an inner function
  const load = async () => {
    const data = await loadEmployees();
    setEmployees(data);
  };
  load();
}, []);
```

---

## 1.9 Optional Chaining and Nullish Coalescing

```ts
// Optional chaining ?.  — safe property access
const city = employee?.address?.city;       // undefined if any is null/undefined
const first = employees?.[0];               // undefined if array is null/undefined
const name = employee?.getName?.();         // safe method call

// Nullish coalescing ??  — default when null/undefined (not falsy!)
const role = employee.role ?? 'viewer';     // 'viewer' if role is null or undefined
const count = employee.count ?? 0;          // 0 only if count is null/undefined
                                             // (unlike || which also catches 0, '', false)

// Combine
const city = user?.address?.city ?? 'Unknown';
```

---

## 1.10 TypeScript Essentials for React

### Basic types

```ts
const name: string = 'Alice';
const age: number = 30;
const active: boolean = true;
const ids: number[] = [1, 2, 3];
const tuple: [string, number] = ['Alice', 30];
```

### Interfaces (preferred for objects and props)

```ts
interface Employee {
  id: number;
  name: string;
  email: string;
  department: string;
  salary: number;
  isActive: boolean;
  joinDate: string;
  address?: {           // optional — may be undefined
    city: string;
    country: string;
  };
}
```

### Type aliases (good for unions and primitives)

```ts
type Department = 'Engineering' | 'Marketing' | 'HR' | 'Finance' | 'Sales';
type Status = 'active' | 'inactive' | 'on-leave';
type ID = string | number;
```

### Generics — typed containers

```ts
// useState with type parameter
const [employees, setEmployees] = useState<Employee[]>([]);
const [selected, setSelected]   = useState<Employee | null>(null);
const [loading, setLoading]     = useState<boolean>(false);

// Generic function
function findById<T extends { id: number }>(list: T[], id: number): T | undefined {
  return list.find(item => item.id === id);
}
```

### Props typing — two patterns

```ts
// Pattern A — interface (preferred, gives better error messages)
interface EmployeeCardProps {
  employee: Employee;
  onSelect: (id: number) => void;
  isSelected?: boolean;
}
function EmployeeCard({ employee, onSelect, isSelected = false }: EmployeeCardProps) {}

// Pattern B — inline type
function EmployeeCard({ name, age }: { name: string; age: number }) {}
```

### Common React TypeScript types

```ts
import { ReactNode, ReactElement, CSSProperties, MouseEvent, ChangeEvent } from 'react';

// Children
interface Props {
  children: ReactNode;        // anything React can render
  children: ReactElement;     // must be a single React element
  children: string;           // only strings
}

// Event types
const handleClick = (e: MouseEvent<HTMLButtonElement>) => {};
const handleChange = (e: ChangeEvent<HTMLInputElement>) => {};
const handleSubmit = (e: FormEvent<HTMLFormElement>) => {};

// Inline styles
const style: CSSProperties = { color: 'red', fontSize: 14 };
```

---

## 1.11 Quick Reference Card

```ts
// Variables
const x = 1; let y = 2;

// Arrow functions
const fn = (a: number) => a * 2;
const fn = (a: number) => ({ value: a }); // object return

// Destructuring
const { name, age = 0 } = obj;
const [first, ...rest] = arr;

// Spread
const merged = { ...obj1, ...obj2 };
const copy   = [...arr, newItem];

// Template literal
`Hello ${name}, salary: $${sal.toLocaleString()}`

// Array methods
arr.map(x => x * 2)
arr.filter(x => x > 0)
arr.find(x => x.id === id)
arr.reduce((acc, x) => acc + x, 0)

// Async/await
const data = await fetchEmployees();

// Optional chaining + nullish coalescing
const city = user?.address?.city ?? 'N/A';

// TypeScript interface
interface Emp { id: number; name: string; role?: string; }
```

---

## Summary

You'll see every one of these patterns in the modules ahead. Refer back here whenever something looks unfamiliar.

**Next → [Module 02: JSX and Components](./02-jsx-and-components.md)**
