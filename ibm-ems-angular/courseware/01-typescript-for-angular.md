# Module 01 — TypeScript for Angular

## Learning Objectives
- Understand decorators — the foundation of Angular metadata
- Write interfaces and enums for the EMS domain model
- Use generics, access modifiers, and utility types as Angular does
- Define the shared model file that every module imports

---

## 1.1 Why TypeScript Is Mandatory in Angular

Angular requires TypeScript for two reasons:

1. **Decorators** — Angular's `@Component`, `@Injectable`, `@Pipe` etc. are TypeScript decorator factories that attach metadata to classes. No JavaScript equivalent exists.
2. **AOT Compilation** — The Angular Compiler reads TypeScript types from templates at build time to validate expressions and generate optimised code.

```ts
// This is a decorator factory — it returns a decorator function
// Angular reads the metadata object to understand how to treat this class
@Component({
  selector: 'app-root',
  standalone: true,
  template: '<h1>Hello</h1>',
})
class AppComponent { }
```

---

## 1.2 Decorators Angular Uses

| Decorator | Applied to | Purpose |
|-----------|------------|---------|
| `@Component` | class | Declares a UI component |
| `@Injectable` | class | Makes a class injectable as a service |
| `@Pipe` | class | Declares a data transformation pipe |
| `@Directive` | class | Declares a structural/attribute directive |
| `@HostListener` | method | Listens to the host element's DOM events |
| `@HostBinding` | property | Binds to host element property/attribute |

> **Angular 21:** `@Input()`, `@Output()`, `@ViewChild()` decorators are replaced by the signal functions `input()`, `output()`, `viewChild()`. The decorator forms still work but signal functions are preferred.

---

## 1.3 Interfaces — Shape of Data

```ts
// src/app/models/employee.model.ts

export interface Employee {
  id:          number;
  name:        string;
  email:       string;
  phone:       string;
  department:  Department;
  salary:      number;
  isActive:    boolean;
  joinDate:    string;      // ISO date: '2021-03-15'
  username?:   string;      // ? = optional
  address?: {
    street:  string;
    city:    string;
    country: string;
  };
}

// DTO — what we SEND to the API (no id, no auto-fields)
export interface CreateEmployeeDto {
  name:       string;
  email:      string;
  phone:      string;
  department: Department;
  salary:     number;
  joinDate:   string;
}

// Partial update
export interface UpdateEmployeeDto extends Partial<CreateEmployeeDto> {
  isActive?: boolean;
}

// Reusable generic API response wrapper
export interface ApiResponse<T> {
  data:    T;
  message: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  data:       T[];
  total:      number;
  page:       number;
  perPage:    number;
  totalPages: number;
}
```

---

## 1.4 Enums

```ts
export enum Department {
  Engineering = 'Engineering',
  Marketing   = 'Marketing',
  HR          = 'HR',
  Finance     = 'Finance',
  Sales       = 'Sales',
}

export enum EmployeeStatus {
  Active   = 'active',
  Inactive = 'inactive',
  OnLeave  = 'on-leave',
}

// Get all values for use in dropdowns
export const ALL_DEPARTMENTS: Department[] = Object.values(Department);
// → ['Engineering', 'Marketing', 'HR', 'Finance', 'Sales']
```

---

## 1.5 Classes and Access Modifiers

```ts
export class EmployeeService {
  private _employees: Employee[] = [];    // only this class
  protected config:   Config;            // this class + subclasses
  public    apiUrl:   string;            // everyone (the default)
  readonly  version = '1.0';             // cannot be reassigned

  // TypeScript shorthand constructor — declares AND assigns in one line
  constructor(
    private http:   HttpClient,    // private field http
    private router: Router,        // private field router
  ) { }
}
```

---

## 1.6 Generics

```ts
// Generic function
function findById<T extends { id: number }>(list: T[], id: number): T | undefined {
  return list.find(item => item.id === id);
}

const emp = findById<Employee>(employees, 1);  // emp: Employee | undefined

// Generic class
class StateSignal<T> {
  private _value = signal<T | null>(null);
  readonly value = this._value.asReadonly();
  setValue(v: T) { this._value.set(v); }
}

// Generic service method
getAll<T>(url: string): Observable<T[]> {
  return this.http.get<T[]>(url);
}
```

---

## 1.7 Utility Types

```ts
// Partial — all fields optional
type EmployeeUpdate = Partial<Employee>;

// Required — all fields required
type FullEmployee = Required<Employee>;

// Pick — cherry-pick fields
type EmployeeCard = Pick<Employee, 'id' | 'name' | 'department' | 'salary' | 'isActive'>;

// Omit — exclude fields
type NewEmployee = Omit<Employee, 'id'>;

// Record — key-value map
type DeptHeadcount = Record<Department, number>;
// → { Engineering: 3, Marketing: 2, ... }

// ReadonlyArray — immutable list
type EmployeeList = ReadonlyArray<Employee>;

// Extract / Exclude — filter union members
type ActiveStatus = Extract<EmployeeStatus, 'active' | 'on-leave'>;
```

---

## 1.8 Type Guards

```ts
function isEmployee(obj: unknown): obj is Employee {
  return (
    typeof obj === 'object' && obj !== null &&
    'id' in obj && 'name' in obj && 'email' in obj
  );
}

// Usage — TypeScript narrows the type inside the if block
const raw: unknown = JSON.parse(data);
if (isEmployee(raw)) {
  console.log(raw.name);  // raw is Employee here
}
```

---

## 1.9 EMS Model File — Create This Now

Every module imports from this file:

```ts
// src/app/models/employee.model.ts

export enum Department {
  Engineering = 'Engineering',
  Marketing   = 'Marketing',
  HR          = 'HR',
  Finance     = 'Finance',
  Sales       = 'Sales',
}

export interface Employee {
  id:         number;
  name:       string;
  email:      string;
  phone:      string;
  department: Department;
  salary:     number;
  isActive:   boolean;
  joinDate:   string;
  username?:  string;
  address?: { city: string; street: string; country: string; };
}

export interface CreateEmployeeDto {
  name:       string;
  email:      string;
  phone:      string;
  department: Department;
  salary:     number;
  joinDate:   string;
}

export interface UpdateEmployeeDto extends Partial<CreateEmployeeDto> {
  isActive?: boolean;
}

export interface EmployeeFilter {
  department: Department | 'All';
  search:     string;
  onlyActive: boolean;
}

export const INITIAL_FILTER: EmployeeFilter = {
  department: 'All',
  search:     '',
  onlyActive: false,
};

export const ALL_DEPARTMENTS: (Department | 'All')[] = [
  'All',
  Department.Engineering,
  Department.Marketing,
  Department.HR,
  Department.Finance,
  Department.Sales,
];

export const SEED_EMPLOYEES: Employee[] = [
  {
    id: 1, name: 'Alice Johnson',
    email: 'alice@ibm.com', phone: '+91-9876543210',
    department: Department.Engineering, salary: 95000,
    isActive: true, joinDate: '2021-03-15',
  },
  {
    id: 2, name: 'Bob Smith',
    email: 'bob@ibm.com', phone: '+91-9876543211',
    department: Department.Marketing, salary: 72000,
    isActive: true, joinDate: '2020-07-01',
  },
  {
    id: 3, name: 'Carol White',
    email: 'carol@ibm.com', phone: '+91-9876543212',
    department: Department.Engineering, salary: 88000,
    isActive: false, joinDate: '2019-11-20',
  },
  {
    id: 4, name: 'David Lee',
    email: 'david@ibm.com', phone: '+91-9876543213',
    department: Department.HR, salary: 68000,
    isActive: true, joinDate: '2022-01-10',
  },
  {
    id: 5, name: 'Eva Martinez',
    email: 'eva@ibm.com', phone: '+91-9876543214',
    department: Department.Finance, salary: 81000,
    isActive: true, joinDate: '2021-09-05',
  },
];
```

---

## Summary

| TypeScript Feature | Angular Use |
|--------------------|-------------|
| Decorators | `@Component`, `@Injectable`, `@Pipe` |
| Interfaces | Model shapes, DTOs, API responses |
| Enums | Department list, status values |
| Generics | `Observable<T>`, `HttpClient.get<T>()`, signals |
| Access modifiers | Encapsulation in services |
| Utility types | `Partial<>`, `Pick<>`, `Record<>` |

**Next → [Module 02: Components & Templates](./02-components-templates.md)**
