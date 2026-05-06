# Module 07 — Signals: Angular's Modern Reactivity System

## Learning Objectives
- Understand why Signals replace Zone.js-based change detection
- Use `signal()`, `computed()`, `effect()` with confidence
- Use `linkedSignal()` and `resource()` (Angular 19+)
- Convert between Signals and Observables with `toSignal` / `toObservable`
- Build the complete EMS state layer with Signals

---

## 7.1 The Problem Signals Solve

**Before Signals (Zone.js):**
```
Any async event fires (click, setTimeout, HTTP response)
  → Zone.js intercepts it
  → Angular runs change detection on EVERY component
  → re-renders whatever changed
```

**With Signals:**
```
A signal's value changes
  → Only the computed signals and effects that depend on it re-run
  → Only the DOM nodes bound to those signals update
```

Signals bring **fine-grained reactivity** — Angular knows exactly what changed and what needs updating.

---

## 7.2 `signal()` — Writable Value Container

```ts
import { signal } from '@angular/core';

// Create a signal with an initial value
const count    = signal(0);
const name     = signal('Alice');
const employee = signal<Employee | null>(null);
const list     = signal<Employee[]>([]);

// READ — call it like a function
console.log(count());       // 0
console.log(name());        // 'Alice'

// WRITE — three ways
count.set(5);                        // replace value
count.update(v => v + 1);           // update based on current value
list.update(l => [...l, newEmp]);   // immutable array update
employee.set(null);                  // reset

// READ-ONLY view — expose publicly, keep setter private
private _count = signal(0);
readonly count = this._count.asReadonly();   // Signal<number>, no .set()
```

---

## 7.3 `computed()` — Derived Signals

```ts
import { signal, computed } from '@angular/core';

const employees = signal<Employee[]>([]);
const filter    = signal<string>('All');
const search    = signal('');

// computed() re-runs ONLY when its dependencies change
const filtered = computed(() => {
  const dept = filter();      // dependency ①
  const term = search();      // dependency ②
  const list = employees();   // dependency ③

  return list
    .filter(e => dept === 'All' || e.department === dept)
    .filter(e => e.name.toLowerCase().includes(term.toLowerCase()))
    .sort((a, b) => a.name.localeCompare(b.name));
});

// Computed signals are read-only
// filtered.set(...)  → ❌ TypeScript error

// Chaining computeds
const stats = computed(() => ({
  total:    employees().length,
  active:   employees().filter(e => e.isActive).length,
  showing:  filtered().length,
  avgSal:   employees().reduce((s, e) => s + e.salary, 0) / (employees().length || 1),
}));
```

---

## 7.4 `effect()` — Run Side Effects When Signals Change

```ts
import { signal, effect } from '@angular/core';

const theme = signal<'light' | 'dark'>('light');

// effect() runs immediately, then re-runs whenever theme() changes
effect(() => {
  document.body.classList.toggle('dark-mode', theme() === 'dark');
  localStorage.setItem('ems-theme', theme());
});

// Effect with cleanup
effect((onCleanup) => {
  const interval = setInterval(() => ping(), 5000);
  onCleanup(() => clearInterval(interval));  // runs before next effect run and on destroy
});
```

### Rules for `effect()`

1. Effects track every signal read inside them automatically
2. Effects **cannot** write to a signal unless you pass `{ allowSignalWrites: true }`
3. Effects run **after rendering** — not synchronously
4. Use `effect()` for side effects: DOM updates, logging, localStorage, analytics
5. Do NOT use `effect()` to synchronise signals with each other — use `computed()` instead

```ts
// ❌ Wrong — use computed() for derived values
effect(() => {
  this.displayName.set(this.employee().name.toUpperCase());
});

// ✅ Correct
displayName = computed(() => this.employee().name.toUpperCase());
```

---

## 7.5 `linkedSignal()` — Resettable Signal (Angular 19+)

A `linkedSignal` is a writable signal that **resets itself** when a source signal changes:

```ts
import { signal, linkedSignal } from '@angular/core';

@Component({ ... })
export class PaginatedListComponent {
  selectedDept = signal<string>('All');

  // currentPage resets to 1 whenever selectedDept changes
  // but can also be set manually (e.g., user clicks "Next")
  currentPage = linkedSignal({
    source: this.selectedDept,
    computation: () => 1,
  });

  // Short form — when reset value doesn't depend on the source value
  pageSize = linkedSignal(() => this.selectedDept() === 'All' ? 10 : 5);

  nextPage()  { this.currentPage.update(p => p + 1); }
  prevPage()  { this.currentPage.update(p => Math.max(1, p - 1)); }
  goToPage(n: number) { this.currentPage.set(n); }
  // When selectedDept changes → currentPage automatically resets to 1
}
```

---

## 7.6 `resource()` — Async Signal Resource (Angular 19+)

`resource()` integrates asynchronous operations (HTTP, IndexedDB, etc.) directly into the signal graph:

```ts
import { signal, resource, inject } from '@angular/core';

@Component({ ... })
export class EmployeeListComponent {
  searchTerm = signal('');

  // resource automatically re-fetches when searchTerm() changes
  // Also handles loading, error, and abort automatically
  employeeSearch = resource({
    request:  () => ({ q: this.searchTerm() }),      // reactive params — re-triggers on change
    loader:   async ({ request, abortSignal }) => {
      const res = await fetch(
        `/api/employees?q=${request.q}`,
        { signal: abortSignal }                       // auto-cancels in-flight requests
      );
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return res.json() as Promise<Employee[]>;
    },
  });

  // All state exposed as Signals:
  // employeeSearch.value()      → Employee[] | undefined
  // employeeSearch.isLoading()  → boolean
  // employeeSearch.error()      → unknown
  // employeeSearch.status()     → 'idle' | 'loading' | 'resolved' | 'error'

  reload() { this.employeeSearch.reload(); }
}
```

### `httpResource()` — HTTP-specific shorthand

```ts
import { httpResource } from '@angular/core/rxjs-interop';

@Component({ ... })
export class EmployeeDetailComponent {
  id = input.required<string>();

  // Fetches automatically, re-fetches when id() changes
  empResource = httpResource<Employee>(() =>
    `https://jsonplaceholder.typicode.com/users/${this.id()}`
  );
}
```

---

## 7.7 `toSignal()` and `toObservable()` — Bridge

```ts
import { toSignal, toObservable } from '@angular/core/rxjs-interop';

// Observable → Signal
// Subscribes automatically, unsubscribes on component destroy
const employees$ = this.employeeApiService.getAll();
const employees  = toSignal(employees$, { initialValue: [] as Employee[] });

// Signal → Observable
// Useful for using RxJS operators on a signal value
const search$  = toObservable(this.searchSignal);
const results$ = search$.pipe(
  debounceTime(300),
  distinctUntilChanged(),
  switchMap(q => this.api.search(q)),
);
const results = toSignal(results$, { initialValue: [] as Employee[] });
```

---

## 7.8 EMS — Signal-Based State Service

```ts
// src/app/state/employee-state.service.ts
import {
  Injectable, signal, computed, effect, inject,
} from '@angular/core';
import { EmployeeApiService } from '../services/employee-api.service';
import {
  Employee, CreateEmployeeDto, UpdateEmployeeDto,
  EmployeeFilter, INITIAL_FILTER, ALL_DEPARTMENTS, Department,
} from '../models/employee.model';

@Injectable({ providedIn: 'root' })
export class EmployeeStateService {
  private api = inject(EmployeeApiService);

  // ── Private writable signals ────────────────────────────────────
  private _employees  = signal<Employee[]>([]);
  private _loading    = signal(false);
  private _error      = signal<string | null>(null);
  private _filter     = signal<EmployeeFilter>(INITIAL_FILTER);
  private _selectedId = signal<number | null>(null);

  // ── Public read-only ────────────────────────────────────────────
  readonly loading    = this._loading.asReadonly();
  readonly error      = this._error.asReadonly();
  readonly filter     = this._filter.asReadonly();
  readonly selectedId = this._selectedId.asReadonly();

  // ── Computed ────────────────────────────────────────────────────
  readonly filteredEmployees = computed(() => {
    const { department, search, onlyActive } = this._filter();
    const term = search.toLowerCase();
    return this._employees()
      .filter(e => department === 'All' || e.department === department)
      .filter(e => !onlyActive || e.isActive)
      .filter(e =>
        e.name.toLowerCase().includes(term) ||
        e.email.toLowerCase().includes(term)
      )
      .sort((a, b) => a.name.localeCompare(b.name));
  });

  readonly selectedEmployee = computed(() =>
    this._selectedId() !== null
      ? this._employees().find(e => e.id === this._selectedId()) ?? null
      : null
  );

  readonly stats = computed(() => {
    const list = this._employees();
    return {
      total:       list.length,
      active:      list.filter(e => e.isActive).length,
      showing:     this.filteredEmployees().length,
      departments: new Set(list.map(e => e.department)).size,
      avgSalary:   list.length
        ? Math.round(list.reduce((s, e) => s + e.salary, 0) / list.length)
        : 0,
    };
  });

  readonly departmentSummary = computed(() =>
    ALL_DEPARTMENTS
      .filter(d => d !== 'All')
      .map(dept => ({
        name:  dept,
        count: this._employees().filter(e => e.department === dept).length,
      }))
  );

  constructor() {
    // Persist filter to localStorage
    effect(() => {
      localStorage.setItem('ems:filter', JSON.stringify(this._filter()));
    });

    // Restore filter on startup
    const saved = localStorage.getItem('ems:filter');
    if (saved) {
      try { this._filter.set(JSON.parse(saved)); } catch { /* ignore */ }
    }

    this.loadEmployees();
  }

  // ── Actions ─────────────────────────────────────────────────────
  loadEmployees(): void {
    this._loading.set(true);
    this._error.set(null);
    this.api.getAll().subscribe({
      next:  list  => { this._employees.set(list);         this._loading.set(false); },
      error: err   => { this._error.set(err.message ?? 'Load failed'); this._loading.set(false); },
    });
  }

  addEmployee(dto: CreateEmployeeDto): void {
    this.api.create(dto).subscribe({
      next:  emp  => this._employees.update(l => [...l, emp]),
      error: err  => this._error.set(err.message),
    });
  }

  updateEmployee(id: number, dto: UpdateEmployeeDto): void {
    this.api.update(id, dto).subscribe({
      next:  emp  => this._employees.update(l => l.map(e => e.id === id ? { ...e, ...emp } : e)),
      error: err  => this._error.set(err.message),
    });
  }

  removeEmployee(id: number): void {
    this.api.remove(id).subscribe({
      next:  ()   => this._employees.update(l => l.filter(e => e.id !== id)),
      error: err  => this._error.set(err.message),
    });
  }

  setFilter(partial: Partial<EmployeeFilter>): void {
    this._filter.update(f => ({ ...f, ...partial }));
  }

  resetFilter(): void { this._filter.set(INITIAL_FILTER); }

  selectEmployee(id: number | null): void { this._selectedId.set(id); }

  getById(id: number): Employee | undefined {
    return this._employees().find(e => e.id === id);
  }
}
```

---

## 7.9 Signals vs Observables — Decision Guide

| Scenario | Prefer | Why |
|----------|--------|-----|
| Component UI state | Signal | Simpler, automatic tracking |
| Derived/computed values | `computed()` | Lazy, memoised, always fresh |
| Side effects | `effect()` | Clean, auto-tracked |
| HTTP calls | Observable + `toSignal()` | RxJS operators (retry, timeout, cancel) |
| Real-time streams (WebSocket) | Observable + `toSignal()` | Event streams are naturally Observable |
| Complex async chains | Observable | `switchMap`, `combineLatest`, etc. |
| Template values | Signal | No `AsyncPipe` needed |
| Cross-component state | Service signals | Easy to share and read |

---

## Summary

| API | Purpose |
|-----|---------|
| `signal(value)` | Create a writable signal |
| `sig.set(v)` | Replace value |
| `sig.update(fn)` | Update based on current value |
| `sig.asReadonly()` | Expose as read-only |
| `computed(() => ...)` | Derived signal — auto-updates |
| `effect(() => ...)` | Side effect — runs on dependency change |
| `linkedSignal({ source, computation })` | Writable signal that resets on source change |
| `resource({ request, loader })` | Async signal resource |
| `httpResource(urlFn)` | HTTP-backed signal |
| `toSignal(obs$)` | Observable → Signal |
| `toObservable(sig)` | Signal → Observable |

---

# Module 08 — RxJS & Observables

## Learning Objectives
- Understand Observables and why Angular uses them for HTTP
- Use the most important RxJS operators
- Manage subscriptions with `takeUntilDestroyed` and `AsyncPipe`
- Apply to EMS: debounced search, combined streams, error handling

---

## 8.1 What Is an Observable?

An Observable is a **stream of values over time** — it can emit zero, one, or many values, and then complete or error.

```
Promise:   emits exactly ONE value (or error), then done
Observable: emits ZERO or MORE values over time, then optionally complete/error

HTTP GET   → Observable that emits one response, then completes
WebSocket  → Observable that emits many messages over time
Click events → Observable that emits on each click, never completes
```

---

## 8.2 Creating Observables

```ts
import { Observable, of, from, interval, timer, fromEvent, Subject } from 'rxjs';

// of — emit a fixed set of values, then complete
const nums$ = of(1, 2, 3);

// from — convert array / Promise / iterable to Observable
const employees$ = from(fetch('/api/employees').then(r => r.json()));

// interval — emit incrementing number every N ms
const tick$ = interval(1000);           // 0, 1, 2, 3, ...

// timer — emit once after delay, or emit then repeat
const delayed$ = timer(2000);           // emit 0 after 2 seconds

// fromEvent — listen to DOM events
const clicks$ = fromEvent(document, 'click');
```

---

## 8.3 Essential RxJS Operators

```ts
import {
  map, filter, tap, switchMap, mergeMap, concatMap, exhaustMap,
  catchError, retry, retryWhen, timeout,
  debounceTime, distinctUntilChanged, throttleTime,
  combineLatest, forkJoin, merge, zip,
  takeUntil, take, takeWhile, first,
  startWith, scan, reduce,
  shareReplay, share,
} from 'rxjs/operators';
```

### Transformation

```ts
// map — transform each value
employees$.pipe(
  map(list => list.filter(e => e.isActive)),
);

// switchMap — cancel previous, switch to new Observable
// Perfect for search: cancels the old HTTP request when user types again
search$.pipe(
  switchMap(term => this.api.search(term)),
);

// mergeMap — run in parallel (don't cancel)
ids$.pipe(
  mergeMap(id => this.api.getById(id)),
);

// concatMap — run one at a time, in order
actions$.pipe(
  concatMap(action => this.api.process(action)),
);

// exhaustMap — ignore new while current is running (good for login button)
loginClicks$.pipe(
  exhaustMap(creds => this.authService.login(creds)),
);
```

### Filtering

```ts
// filter — only pass values matching predicate
stream$.pipe(filter(e => e.isActive));

// debounceTime — wait N ms after last emission
searchInput$.pipe(debounceTime(300));

// distinctUntilChanged — only emit when value actually changes
searchInput$.pipe(distinctUntilChanged());

// Combined for search
searchInput$.pipe(
  debounceTime(300),
  distinctUntilChanged(),
  filter(term => term.length >= 2),
  switchMap(term => this.api.search(term)),
);
```

### Error Handling

```ts
// catchError — recover from error
this.api.getAll().pipe(
  catchError(err => {
    console.error(err);
    return of([]);      // emit empty array as fallback
  }),
);

// retry — retry N times before erroring
this.api.getAll().pipe(retry(3));

// timeout — error if no emission within N ms
this.api.getAll().pipe(timeout(5000));
```

### Combining Streams

```ts
// combineLatest — emit when ANY source emits, with latest from ALL
combineLatest([employees$, filter$, search$]).pipe(
  map(([employees, filter, search]) => applyFilter(employees, filter, search)),
);

// forkJoin — emit when ALL complete (like Promise.all)
forkJoin({
  employees:   this.api.getAll(),
  departments: this.api.getDepartments(),
}).subscribe(({ employees, departments }) => { ... });
```

---

## 8.4 Managing Subscriptions

Uncleaned subscriptions cause memory leaks. Angular 21 has clean solutions:

```ts
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DestroyRef, inject } from '@angular/core';

@Component({ ... })
export class SearchComponent {
  private destroyRef = inject(DestroyRef);

  // ✅ Modern: takeUntilDestroyed — auto-unsubscribes when component is destroyed
  constructor() {
    this.searchInput$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(q => this.api.search(q)),
      takeUntilDestroyed(this.destroyRef),   // no manual cleanup needed!
    ).subscribe(results => this.results.set(results));
  }
}

// ✅ Also fine: AsyncPipe in template — subscribes and unsubscribes automatically
// In class:
readonly employees$ = this.api.getAll();
// In template:
// @if (employees$ | async; as list) { ... }
```

---

## 8.5 Subject — Multicasting

```ts
import { Subject, BehaviorSubject, ReplaySubject } from 'rxjs';

// Subject — multicast Observable; late subscribers miss past values
const action$ = new Subject<string>();
action$.subscribe(a => console.log('subscriber 1:', a));
action$.next('save');
action$.subscribe(a => console.log('subscriber 2:', a));  // misses 'save'

// BehaviorSubject — has a current value; late subscribers get the LATEST
const filter$ = new BehaviorSubject<string>('All');
filter$.value;              // get current value without subscribing
filter$.next('Engineering');

// ReplaySubject(n) — replays last N values to new subscribers
const history$ = new ReplaySubject<Employee>(3);
```

---

## 8.6 EMS — Debounced Search with RxJS

```ts
// src/app/pages/employees/employees.component.ts
import {
  Component, signal, computed, inject,
  ChangeDetectionStrategy, viewChild, ElementRef,
  afterNextRender,
} from '@angular/core';
import { toObservable, toSignal, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { EmployeeStateService } from '../../state/employee-state.service';
import { EmployeeCardComponent } from '../../components/employee-card/employee-card.component';
import { RouterLink } from '@angular/router';
import { ALL_DEPARTMENTS } from '../../models/employee.model';

@Component({
  selector: 'app-employees',
  standalone: true,
  imports: [EmployeeCardComponent, RouterLink],
  templateUrl: './employees.component.html',
  styleUrl: './employees.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmployeesComponent {
  protected state = inject(EmployeeStateService);
  protected allDepts = ALL_DEPARTMENTS;

  searchInput = viewChild<ElementRef<HTMLInputElement>>('searchInput');

  // Local search signal — drives the debounced update to state
  localSearch = signal('');

  constructor() {
    // Auto-focus search input
    afterNextRender(() => this.searchInput()?.nativeElement.focus());

    // Debounce search: wait 300ms after user stops typing before updating state
    toObservable(this.localSearch).pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed(),    // auto-cleanup on component destroy
    ).subscribe(term => this.state.setFilter({ search: term }));
  }

  onRemove(id: number): void { this.state.removeEmployee(id); }
  onFilterChange(dept: string): void { this.state.setFilter({ department: dept as any }); }
  onActiveToggle(v: boolean): void { this.state.setFilter({ onlyActive: v }); }
}
```

---

## Summary

| RxJS Concept | Angular Use |
|--------------|-------------|
| Observable | HTTP responses, event streams |
| `switchMap` | Cancel-previous search, navigation |
| `debounceTime` | Search input debouncing |
| `catchError` | Handle HTTP errors gracefully |
| `forkJoin` | Load multiple endpoints in parallel |
| `combineLatest` | Merge filter + data streams |
| `takeUntilDestroyed()` | Auto-cleanup subscriptions |
| `AsyncPipe` | Subscribe in template, auto-unsubscribe |
| `toSignal()` | Observable → Signal |
| `toObservable()` | Signal → Observable |

---

# Module 09 — Forms: Reactive & Template-Driven

## Learning Objectives
- Build reactive forms with `FormBuilder`, `FormGroup`, `FormControl`
- Use built-in and custom validators
- Build dynamic forms with `FormArray`
- Understand template-driven forms for simple cases
- Apply to EMS: complete Create/Edit Employee form

---

## 9.1 Reactive Forms — Setup

```ts
// app.config.ts — import ReactiveFormsModule in the component, not globally
// In your component:
import { ReactiveFormsModule } from '@angular/forms';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule],
  ...
})
```

---

## 9.2 FormBuilder — Define the Form

```ts
// src/app/pages/employee-form/employee-form.component.ts
import {
  Component, OnInit, inject, signal, computed, input,
  ChangeDetectionStrategy,
} from '@angular/core';
import {
  FormBuilder, FormGroup, Validators, ReactiveFormsModule,
  AbstractControl, ValidationErrors,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { EmployeeStateService } from '../../state/employee-state.service';
import { Department } from '../../models/employee.model';

// ── Custom validators ────────────────────────────────────────────────────────
function salaryValidator(ctrl: AbstractControl): ValidationErrors | null {
  const v = ctrl.value;
  if (!v) return null;
  if (v < 10000)     return { tooLow:  { min: 10000,     actual: v } };
  if (v > 5_000_000) return { tooHigh: { max: 5_000_000, actual: v } };
  return null;
}

function noFutureDateValidator(ctrl: AbstractControl): ValidationErrors | null {
  if (!ctrl.value) return null;
  return new Date(ctrl.value) > new Date() ? { futureDate: true } : null;
}

// ── Component ────────────────────────────────────────────────────────────────
@Component({
  selector: 'app-employee-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './employee-form.component.html',
  styleUrl:    './employee-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmployeeFormComponent implements OnInit {
  // Route param injected via withComponentInputBinding
  id = input<string>('');

  private fb    = inject(FormBuilder);
  private state = inject(EmployeeStateService);
  private router = inject(Router);

  departments  = Object.values(Department);
  isEditMode   = computed(() => !!this.id());
  submitting   = signal(false);
  today        = new Date().toISOString().split('T')[0];

  // ── Form definition ──────────────────────────────────────────────────────
  form: FormGroup = this.fb.group({
    name: ['', [
      Validators.required,
      Validators.minLength(2),
      Validators.maxLength(100),
      Validators.pattern(/^[\p{L}\s\-']+$/u),   // unicode letters only
    ]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [
      Validators.required,
      Validators.pattern(/^[\d\s+\-().]+$/),
      Validators.minLength(7),
    ]],
    department: [Department.Engineering, Validators.required],
    salary: [null, [Validators.required, salaryValidator]],
    joinDate: ['', [Validators.required, noFutureDateValidator]],
    isActive: [true],
    notes: ['', Validators.maxLength(500)],
  });

  // ── Lifecycle ────────────────────────────────────────────────────────────
  ngOnInit(): void {
    if (this.isEditMode()) {
      const emp = this.state.getById(Number(this.id()));
      if (emp) {
        this.form.patchValue(emp);    // patchValue fills only the fields that exist
      } else {
        this.router.navigate(['/employees']);
      }
    }
  }

  // ── Getters for template (avoid repeating form.get('field')) ─────────────
  get nameCtrl()     { return this.form.get('name')!; }
  get emailCtrl()    { return this.form.get('email')!; }
  get phoneCtrl()    { return this.form.get('phone')!; }
  get salaryCtrl()   { return this.form.get('salary')!; }
  get joinDateCtrl() { return this.form.get('joinDate')!; }

  showError(ctrl: AbstractControl, error: string): boolean {
    return ctrl.hasError(error) && (ctrl.dirty || ctrl.touched);
  }

  hasAnyError(ctrl: AbstractControl): boolean {
    return ctrl.invalid && (ctrl.dirty || ctrl.touched);
  }

  // ── Submit ───────────────────────────────────────────────────────────────
  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();   // show all errors
      return;
    }

    this.submitting.set(true);
    const dto = this.form.getRawValue();

    if (this.isEditMode()) {
      this.state.updateEmployee(Number(this.id()), dto);
    } else {
      this.state.addEmployee(dto);
    }

    this.router.navigate(['/employees']);
  }

  reset(): void { this.form.reset({ department: Department.Engineering, isActive: true }); }

  hasUnsavedChanges(): boolean { return this.form.dirty; }
}
```

### `employee-form.component.html`

```html
<div class="form-page">
  <h1 class="form-page__title">
    {{ isEditMode() ? 'Edit Employee' : 'Add Employee' }}
  </h1>

  <form [formGroup]="form" (ngSubmit)="onSubmit()" class="emp-form" novalidate>

    <!-- ── Name ── -->
    <div class="field" [class.field--error]="hasAnyError(nameCtrl)">
      <label for="name" class="field__label">
        Full Name <span class="required">*</span>
      </label>
      <input id="name" type="text" formControlName="name"
             class="field__input" placeholder="Alice Johnson" />
      @if (showError(nameCtrl, 'required'))   { <p class="field__error">Name is required.</p> }
      @else if (showError(nameCtrl, 'minlength')) { <p class="field__error">At least 2 characters.</p> }
      @else if (showError(nameCtrl, 'pattern'))   { <p class="field__error">Letters and spaces only.</p> }
    </div>

    <!-- ── Email ── -->
    <div class="field" [class.field--error]="hasAnyError(emailCtrl)">
      <label for="email" class="field__label">Email <span class="required">*</span></label>
      <input id="email" type="email" formControlName="email"
             class="field__input" placeholder="alice@ibm.com" />
      @if (showError(emailCtrl, 'required')) { <p class="field__error">Email is required.</p> }
      @else if (showError(emailCtrl, 'email')) { <p class="field__error">Enter a valid email.</p> }
    </div>

    <!-- ── Phone ── -->
    <div class="field" [class.field--error]="hasAnyError(phoneCtrl)">
      <label for="phone" class="field__label">Phone <span class="required">*</span></label>
      <input id="phone" type="tel" formControlName="phone"
             class="field__input" placeholder="+91 98765 43210" />
      @if (showError(phoneCtrl, 'required')) { <p class="field__error">Phone is required.</p> }
      @else if (showError(phoneCtrl, 'pattern')) { <p class="field__error">Invalid phone number.</p> }
    </div>

    <!-- ── Department + Salary ── -->
    <div class="field-row">
      <div class="field">
        <label for="department" class="field__label">Department</label>
        <select id="department" formControlName="department" class="field__input">
          @for (dept of departments; track dept) {
            <option [value]="dept">{{ dept }}</option>
          }
        </select>
      </div>

      <div class="field" [class.field--error]="hasAnyError(salaryCtrl)">
        <label for="salary" class="field__label">
          Annual Salary (USD) <span class="required">*</span>
        </label>
        <input id="salary" type="number" formControlName="salary"
               class="field__input" placeholder="75000" min="0" />
        @if (showError(salaryCtrl, 'required'))  { <p class="field__error">Salary is required.</p> }
        @else if (showError(salaryCtrl, 'tooLow')) { <p class="field__error">Minimum is $10,000.</p> }
      </div>
    </div>

    <!-- ── Join Date ── -->
    <div class="field" [class.field--error]="hasAnyError(joinDateCtrl)">
      <label for="joinDate" class="field__label">
        Join Date <span class="required">*</span>
      </label>
      <input id="joinDate" type="date" formControlName="joinDate"
             [max]="today" class="field__input" />
      @if (showError(joinDateCtrl, 'required'))     { <p class="field__error">Join date required.</p> }
      @else if (showError(joinDateCtrl, 'futureDate')) { <p class="field__error">Cannot be in the future.</p> }
    </div>

    <!-- ── Active checkbox ── -->
    <div class="field field--checkbox">
      <input id="isActive" type="checkbox" formControlName="isActive" />
      <label for="isActive">Active employee</label>
    </div>

    <!-- ── Notes ── -->
    <div class="field">
      <label for="notes" class="field__label">Notes (optional)</label>
      <textarea id="notes" formControlName="notes" class="field__input"
                rows="3" style="resize: vertical;"></textarea>
    </div>

    <!-- ── Actions ── -->
    <div class="form-actions">
      <button type="button" class="btn btn--secondary" (click)="reset()">Reset</button>
      <a routerLink="/employees" class="btn btn--secondary">Cancel</a>
      <button type="submit" class="btn btn--primary" [disabled]="submitting()">
        {{ submitting() ? 'Saving…' : (isEditMode() ? 'Update' : 'Create Employee') }}
      </button>
    </div>

  </form>
</div>
```

---

## 9.3 FormArray — Dynamic Fields

```ts
// Skills FormArray example
this.form = this.fb.group({
  name:   ['', Validators.required],
  skills: this.fb.array([]),    // dynamic list
});

get skills(): FormArray {
  return this.form.get('skills') as FormArray;
}

addSkill(): void {
  this.skills.push(this.fb.control('', Validators.required));
}

removeSkill(index: number): void {
  this.skills.removeAt(index);
}
```

```html
<div formArrayName="skills">
  @for (skill of skills.controls; track $index; let i = $index) {
    <div style="display: flex; gap: 8px;">
      <input [formControlName]="i" class="field__input" placeholder="Skill name" />
      <button type="button" (click)="removeSkill(i)">Remove</button>
    </div>
  }
  <button type="button" (click)="addSkill()">+ Add Skill</button>
</div>
```

---

## 9.4 Template-Driven Forms (Simple Cases)

```ts
import { FormsModule } from '@angular/forms';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <form #f="ngForm" (ngSubmit)="onSubmit(f)">
      <input
        name="name" [(ngModel)]="model.name"
        required minlength="2"
        #nameCtrl="ngModel"
      />
      @if (nameCtrl.invalid && nameCtrl.touched) {
        <p>Name is invalid.</p>
      }
      <button type="submit" [disabled]="f.invalid">Save</button>
    </form>
  `,
})
export class SimpleFormComponent {
  model = { name: '' };
  onSubmit(form: NgForm) { console.log(form.value); }
}
```

---

## 9.5 Reactive vs Template-Driven

| | Template-Driven | Reactive |
|-|----------------|----------|
| Form logic | HTML template | TypeScript class |
| Validation | HTML attributes | Validator functions |
| Testing | Hard (needs DOM) | Easy (pure TS) |
| Dynamic fields | Difficult | Easy (`FormArray`) |
| Type safety | Limited | Full |
| Boilerplate | Less | More |
| Recommended for | Simple, 1-3 field forms | Everything else |

---

## Summary

| Reactive Forms API | Purpose |
|--------------------|---------|
| `fb.group({ ... })` | Create a form group |
| `fb.control(val, validators)` | Individual form control |
| `fb.array([])` | Dynamic list of controls |
| `[formGroup]="form"` | Bind form group to `<form>` |
| `formControlName="x"` | Bind control to input |
| `form.patchValue(obj)` | Fill partial values (edit mode) |
| `form.setValue(obj)` | Fill ALL values exactly |
| `form.markAllAsTouched()` | Show all errors on submit |
| `form.getRawValue()` | Get all values including disabled |
| `Validators.required` | Built-in validators |
| Custom validator | `(ctrl: AbstractControl) => ValidationErrors \| null` |

**Next → [Module 10: Custom Pipes, Directives & Interceptors](./10-pipes-directives-interceptors.md)**
