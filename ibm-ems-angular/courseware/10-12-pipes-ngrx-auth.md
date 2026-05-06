# Module 10 — Custom Pipes, Directives & Interceptors

## Learning Objectives
- Write custom pipes for salary formatting, initials, time-ago
- Write attribute directives for highlight and tooltip
- Write a structural directive to understand the pattern
- Apply to EMS: `SalaryPipe`, `InitialsPipe`, `HighlightDirective`, `TooltipDirective`

---

## 10.1 Custom Pipe

A pipe is a class decorated with `@Pipe` that implements `PipeTransform`. Pipes are **pure by default** — they only re-run when their input changes.

```bash
ng generate pipe pipes/salary
```

### `SalaryPipe`

```ts
// src/app/pipes/salary.pipe.ts
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'salary',
  standalone: true,   // standalone pipes — no NgModule needed
  pure: true,         // default: only recalculate when input changes
})
export class SalaryPipe implements PipeTransform {
  transform(
    value: number | null | undefined,
    currency: string = 'USD',
    showSymbol: boolean = true,
  ): string {
    if (value == null) return '—';
    return new Intl.NumberFormat('en-US', {
      style:                 'currency',
      currency,
      maximumFractionDigits: 0,
    }).format(value);
  }
}
```

```ts
// Usage — import in the component that uses it
@Component({
  standalone: true,
  imports: [SalaryPipe],
  template: `
    {{ employee().salary | salary }}          <!-- $95,000 -->
    {{ employee().salary | salary:'INR' }}    <!-- ₹95,000 -->
  `,
})
```

### `InitialsPipe`

```ts
// src/app/pipes/initials.pipe.ts
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'initials', standalone: true })
export class InitialsPipe implements PipeTransform {
  transform(name: string | null | undefined, maxChars = 2): string {
    if (!name) return '?';
    return name
      .split(/\s+/)
      .map(word => word.charAt(0).toUpperCase())
      .join('')
      .slice(0, maxChars);
  }
}
// {{ 'Alice Johnson' | initials }}    → 'AJ'
// {{ 'Alice Johnson' | initials:1 }}  → 'A'
```

### `TimeAgoPipe`

```ts
// src/app/pipes/time-ago.pipe.ts
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'timeAgo', standalone: true })
export class TimeAgoPipe implements PipeTransform {
  transform(dateStr: string | Date | null | undefined): string {
    if (!dateStr) return '—';

    const date = new Date(dateStr);
    const seconds = Math.floor((Date.now() - date.getTime()) / 1000);

    const intervals: [number, string][] = [
      [31_536_000, 'year'],
      [2_592_000,  'month'],
      [86_400,     'day'],
      [3_600,      'hour'],
      [60,         'minute'],
    ];

    for (const [secs, label] of intervals) {
      const count = Math.floor(seconds / secs);
      if (count >= 1) return `${count} ${label}${count > 1 ? 's' : ''} ago`;
    }
    return 'just now';
  }
}
// {{ '2021-03-15' | timeAgo }}  → '3 years ago'
```

### Impure Pipe (use sparingly)

```ts
@Pipe({
  name: 'employeeSearch',
  standalone: true,
  pure: false,   // re-runs on EVERY change detection cycle — use only when necessary
})
export class EmployeeSearchPipe implements PipeTransform {
  transform(employees: Employee[], search: string): Employee[] {
    if (!search) return employees;
    return employees.filter(e =>
      e.name.toLowerCase().includes(search.toLowerCase())
    );
  }
}
```

> Prefer computed signals over impure pipes for performance.

---

## 10.2 Attribute Directives

An attribute directive changes the appearance or behaviour of an element.

```bash
ng generate directive directives/highlight
```

### `HighlightDirective`

```ts
// src/app/directives/highlight.directive.ts
import {
  Directive, ElementRef, HostListener, HostBinding,
  input, inject,
} from '@angular/core';

@Directive({
  selector: '[appHighlight]',    // applied as: <div appHighlight>
  standalone: true,
})
export class HighlightDirective {
  // Signal input — parent can pass custom color
  highlightColor = input<string>('#fff9c4', { alias: 'appHighlight' });
  defaultColor   = input<string>('transparent');

  private el = inject(ElementRef);

  @HostBinding('style.transition')
  transition = 'background-color 0.2s ease';

  @HostListener('mouseenter')
  onMouseEnter(): void {
    this.el.nativeElement.style.backgroundColor = this.highlightColor();
  }

  @HostListener('mouseleave')
  onMouseLeave(): void {
    this.el.nativeElement.style.backgroundColor = this.defaultColor();
  }
}
```

```html
<!-- Usage -->
<tr appHighlight>...</tr>
<tr [appHighlight]="'#e8f4fd'">...</tr>
```

### `AutoFocusDirective`

```ts
// src/app/directives/auto-focus.directive.ts
import { Directive, ElementRef, input, inject, afterNextRender } from '@angular/core';

@Directive({ selector: '[appAutoFocus]', standalone: true })
export class AutoFocusDirective {
  enabled = input<boolean>(true, { alias: 'appAutoFocus' });
  private el = inject(ElementRef);

  constructor() {
    afterNextRender(() => {
      if (this.enabled()) {
        this.el.nativeElement.focus();
      }
    });
  }
}
```

```html
<input type="text" appAutoFocus placeholder="Search..." />
<input type="text" [appAutoFocus]="isSearchVisible()" placeholder="Search..." />
```

### `TooltipDirective`

```ts
// src/app/directives/tooltip.directive.ts
import {
  Directive, ElementRef, HostListener,
  Renderer2, input, inject, OnDestroy,
} from '@angular/core';

@Directive({ selector: '[appTooltip]', standalone: true })
export class TooltipDirective implements OnDestroy {
  tooltipText     = input<string>('', { alias: 'appTooltip' });
  tooltipPosition = input<'top' | 'bottom' | 'left' | 'right'>('top');

  private el       = inject(ElementRef);
  private renderer = inject(Renderer2);
  private tooltip: HTMLElement | null = null;

  @HostListener('mouseenter')
  show(): void {
    if (!this.tooltipText()) return;

    this.tooltip = this.renderer.createElement('div');
    this.renderer.setProperty(this.tooltip!, 'textContent', this.tooltipText());
    this.renderer.addClass(this.tooltip!, 'app-tooltip');
    this.renderer.addClass(this.tooltip!, `app-tooltip--${this.tooltipPosition()}`);
    this.renderer.appendChild(document.body, this.tooltip!);

    const rect = this.el.nativeElement.getBoundingClientRect();
    this.renderer.setStyle(this.tooltip!, 'top',  `${rect.top - 36 + window.scrollY}px`);
    this.renderer.setStyle(this.tooltip!, 'left', `${rect.left + rect.width / 2}px`);
  }

  @HostListener('mouseleave')
  hide(): void {
    if (this.tooltip) {
      this.renderer.removeChild(document.body, this.tooltip);
      this.tooltip = null;
    }
  }

  ngOnDestroy(): void { this.hide(); }
}
```

```html
<button appTooltip="Click to remove employee" tooltipPosition="bottom">
  Remove
</button>
```

---

## 10.3 Custom HTTP Interceptor — Logging

```ts
// src/app/interceptors/logging.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { tap, finalize } from 'rxjs/operators';

export const loggingInterceptor: HttpInterceptorFn = (req, next) => {
  const start = Date.now();

  if (import.meta.env['DEV']) {
    console.log(`[HTTP] ${req.method} ${req.url}`);
  }

  return next(req).pipe(
    tap({
      next:  r   => { if (import.meta.env['DEV']) console.log(`[HTTP] Response:`, r); },
      error: err => { console.error(`[HTTP] Error:`, err); },
    }),
    finalize(() => {
      if (import.meta.env['DEV']) {
        console.log(`[HTTP] ${req.method} ${req.url} completed in ${Date.now() - start}ms`);
      }
    }),
  );
};
```

---

## Summary

| Feature | Key Points |
|---------|------------|
| Custom pipe | `@Pipe({ name, standalone: true })` + `PipeTransform.transform()` |
| Pure pipe (default) | Only recalculates when input reference changes |
| Impure pipe | Recalculates every CD cycle — use computed signals instead |
| Attribute directive | `@Directive({ selector: '[appName]' })` |
| `@HostListener` | Listen to host element DOM events |
| `@HostBinding` | Bind to host element properties |
| `inject(ElementRef)` | Get reference to the host DOM element |
| `inject(Renderer2)` | Safely manipulate DOM (SSR-safe) |

---

# Module 11 — NgRx: Enterprise State Management

## Learning Objectives
- Understand the Redux pattern: Store, Actions, Reducers, Effects, Selectors
- Use NgRx Store for the EMS employee list
- Handle async with NgRx Effects
- Use NgRx SignalStore (new lightweight alternative)
- Understand when NgRx is appropriate vs Signal services

---

## 11.1 When to Use NgRx

| State type | Solution |
|-----------|----------|
| Local component UI state | `signal()` |
| Shared feature state (single feature) | Signal service (`@Injectable`) |
| Cross-feature shared state | Signal service or **NgRx** |
| Complex async with caching | **NgRx** + Effects |
| Time-travel debugging needed | **NgRx** |
| Large team / strict architecture | **NgRx** |

NgRx adds boilerplate — use it when the benefits outweigh the cost.

---

## 11.2 Install NgRx

```bash
ng add @ngrx/store@latest
ng add @ngrx/effects@latest
ng add @ngrx/store-devtools@latest
```

---

## 11.3 Actions

```ts
// src/app/store/employee.actions.ts
import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { Employee, CreateEmployeeDto, UpdateEmployeeDto } from '../models/employee.model';

export const EmployeeActions = createActionGroup({
  source: 'Employee',
  events: {
    // Load
    'Load Employees':         emptyProps(),
    'Load Employees Success': props<{ employees: Employee[] }>(),
    'Load Employees Failure': props<{ error: string }>(),

    // Create
    'Create Employee':         props<{ dto: CreateEmployeeDto }>(),
    'Create Employee Success': props<{ employee: Employee }>(),
    'Create Employee Failure': props<{ error: string }>(),

    // Update
    'Update Employee':         props<{ id: number; dto: UpdateEmployeeDto }>(),
    'Update Employee Success': props<{ employee: Employee }>(),
    'Update Employee Failure': props<{ error: string }>(),

    // Delete
    'Delete Employee':         props<{ id: number }>(),
    'Delete Employee Success': props<{ id: number }>(),
    'Delete Employee Failure': props<{ error: string }>(),

    // Filter / UI
    'Set Filter':      props<{ department: string }>(),
    'Set Search':      props<{ search: string }>(),
    'Select Employee': props<{ id: number | null }>(),
  },
});
```

---

## 11.4 Reducer

```ts
// src/app/store/employee.reducer.ts
import { createReducer, on } from '@ngrx/store';
import { EmployeeActions } from './employee.actions';
import { Employee } from '../models/employee.model';

export interface EmployeeState {
  employees:   Employee[];
  loading:     boolean;
  error:       string | null;
  filter:      string;
  search:      string;
  selectedId:  number | null;
}

export const initialState: EmployeeState = {
  employees:  [],
  loading:    false,
  error:      null,
  filter:     'All',
  search:     '',
  selectedId: null,
};

export const employeeReducer = createReducer(
  initialState,

  // Load
  on(EmployeeActions.loadEmployees, state => ({ ...state, loading: true, error: null })),
  on(EmployeeActions.loadEmployeesSuccess, (state, { employees }) => ({
    ...state, loading: false, employees,
  })),
  on(EmployeeActions.loadEmployeesFailure, (state, { error }) => ({
    ...state, loading: false, error,
  })),

  // Create
  on(EmployeeActions.createEmployeeSuccess, (state, { employee }) => ({
    ...state, employees: [...state.employees, employee],
  })),

  // Update
  on(EmployeeActions.updateEmployeeSuccess, (state, { employee }) => ({
    ...state,
    employees: state.employees.map(e => e.id === employee.id ? employee : e),
  })),

  // Delete
  on(EmployeeActions.deleteEmployeeSuccess, (state, { id }) => ({
    ...state, employees: state.employees.filter(e => e.id !== id),
  })),

  // Filter / UI
  on(EmployeeActions.setFilter, (state, { department }) => ({ ...state, filter: department })),
  on(EmployeeActions.setSearch,  (state, { search })     => ({ ...state, search })),
  on(EmployeeActions.selectEmployee, (state, { id })     => ({ ...state, selectedId: id })),
);
```

---

## 11.5 Selectors

```ts
// src/app/store/employee.selectors.ts
import { createSelector, createFeatureSelector } from '@ngrx/store';
import { EmployeeState } from './employee.reducer';

// Feature selector — points to this slice of state
const selectFeature = createFeatureSelector<EmployeeState>('employees');

// Simple selectors
export const selectAllEmployees  = createSelector(selectFeature, s => s.employees);
export const selectLoading       = createSelector(selectFeature, s => s.loading);
export const selectError         = createSelector(selectFeature, s => s.error);
export const selectFilter        = createSelector(selectFeature, s => s.filter);
export const selectSearch        = createSelector(selectFeature, s => s.search);
export const selectSelectedId    = createSelector(selectFeature, s => s.selectedId);

// Memoised composed selector — only recalculates when inputs change
export const selectFilteredEmployees = createSelector(
  selectAllEmployees,
  selectFilter,
  selectSearch,
  (employees, filter, search) =>
    employees
      .filter(e => filter === 'All' || e.department === filter)
      .filter(e =>
        e.name.toLowerCase().includes(search.toLowerCase()) ||
        e.email.toLowerCase().includes(search.toLowerCase())
      )
      .sort((a, b) => a.name.localeCompare(b.name))
);

export const selectStats = createSelector(
  selectAllEmployees,
  selectFilteredEmployees,
  (all, filtered) => ({
    total:    all.length,
    active:   all.filter(e => e.isActive).length,
    showing:  filtered.length,
    avgSalary: all.length
      ? Math.round(all.reduce((s, e) => s + e.salary, 0) / all.length)
      : 0,
  }),
);

export const selectSelectedEmployee = createSelector(
  selectAllEmployees,
  selectSelectedId,
  (employees, id) => id !== null ? employees.find(e => e.id === id) ?? null : null,
);
```

---

## 11.6 Effects

```ts
// src/app/store/employee.effects.ts
import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { EmployeeApiService } from '../services/employee-api.service';
import { EmployeeActions } from './employee.actions';
import { switchMap, map, catchError, mergeMap } from 'rxjs/operators';
import { of } from 'rxjs';

@Injectable()
export class EmployeeEffects {
  private actions$ = inject(Actions);
  private api      = inject(EmployeeApiService);

  // Load employees when action is dispatched
  loadEmployees$ = createEffect(() =>
    this.actions$.pipe(
      ofType(EmployeeActions.loadEmployees),
      switchMap(() =>
        this.api.getAll().pipe(
          map(employees => EmployeeActions.loadEmployeesSuccess({ employees })),
          catchError(err  => of(EmployeeActions.loadEmployeesFailure({ error: err.message }))),
        )
      ),
    )
  );

  // Create employee
  createEmployee$ = createEffect(() =>
    this.actions$.pipe(
      ofType(EmployeeActions.createEmployee),
      mergeMap(({ dto }) =>
        this.api.create(dto).pipe(
          map(employee => EmployeeActions.createEmployeeSuccess({ employee })),
          catchError(err  => of(EmployeeActions.createEmployeeFailure({ error: err.message }))),
        )
      ),
    )
  );

  // Delete employee
  deleteEmployee$ = createEffect(() =>
    this.actions$.pipe(
      ofType(EmployeeActions.deleteEmployee),
      mergeMap(({ id }) =>
        this.api.remove(id).pipe(
          map(() => EmployeeActions.deleteEmployeeSuccess({ id })),
          catchError(err => of(EmployeeActions.deleteEmployeeFailure({ error: err.message }))),
        )
      ),
    )
  );
}
```

---

## 11.7 Register in app.config.ts

```ts
import { provideStore }       from '@ngrx/store';
import { provideEffects }     from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';
import { employeeReducer }    from './store/employee.reducer';
import { EmployeeEffects }    from './store/employee.effects';

export const appConfig: ApplicationConfig = {
  providers: [
    provideStore({ employees: employeeReducer }),
    provideEffects([EmployeeEffects]),
    provideStoreDevtools({ maxAge: 25, logOnly: false }),
  ],
};
```

---

## 11.8 Use the Store in a Component

```ts
// src/app/pages/employees/employees.component.ts
import { Component, OnInit, inject, ChangeDetectionStrategy } from '@angular/core';
import { Store } from '@ngrx/store';
import { EmployeeActions } from '../../store/employee.actions';
import {
  selectFilteredEmployees, selectStats,
  selectLoading, selectError, selectFilter,
} from '../../store/employee.selectors';
import { EmployeeCardComponent } from '../../components/employee-card/employee-card.component';
import { AsyncPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-employees',
  standalone: true,
  imports: [EmployeeCardComponent, AsyncPipe, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (loading$ | async) {
      <div class="spinner-wrap"><div class="spinner"></div></div>
    } @else {
      <div class="employees-grid">
        @for (emp of employees$ | async; track emp.id) {
          <app-employee-card [employee]="emp" (removed)="onRemove($event)" />
        } @empty {
          <p>No employees found.</p>
        }
      </div>
    }
  `,
})
export class EmployeesComponent implements OnInit {
  private store = inject(Store);

  employees$ = this.store.select(selectFilteredEmployees);
  stats$     = this.store.select(selectStats);
  loading$   = this.store.select(selectLoading);
  error$     = this.store.select(selectError);
  filter$    = this.store.select(selectFilter);

  ngOnInit(): void {
    this.store.dispatch(EmployeeActions.loadEmployees());
  }

  onRemove(id: number): void {
    this.store.dispatch(EmployeeActions.deleteEmployee({ id }));
  }

  onFilterChange(dept: string): void {
    this.store.dispatch(EmployeeActions.setFilter({ department: dept }));
  }
}
```

---

## 11.9 NgRx SignalStore (Lightweight Alternative)

```bash
ng add @ngrx/signals@latest
```

```ts
// src/app/store/employee-signal.store.ts
import { signalStore, withState, withComputed, withMethods } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { withEntities, setAllEntities, removeEntity } from '@ngrx/signals/entities';
import { computed, inject } from '@angular/core';
import { pipe, tap, switchMap } from 'rxjs';
import { Employee } from '../models/employee.model';
import { EmployeeApiService } from '../services/employee-api.service';

type EmployeeSignalState = {
  loading: boolean;
  error:   string | null;
  filter:  string;
  search:  string;
};

export const EmployeeSignalStore = signalStore(
  { providedIn: 'root' },

  withEntities<Employee>(),    // provides entities(), entityMap(), ids(), total()

  withState<EmployeeSignalState>({
    loading: false,
    error:   null,
    filter:  'All',
    search:  '',
  }),

  withComputed(({ entities, filter, search }) => ({
    filteredEmployees: computed(() =>
      entities()
        .filter(e => filter() === 'All' || e.department === filter())
        .filter(e =>
          e.name.toLowerCase().includes(search().toLowerCase()) ||
          e.email.toLowerCase().includes(search().toLowerCase())
        )
    ),
    stats: computed(() => ({
      total:  entities().length,
      active: entities().filter(e => e.isActive).length,
    })),
  })),

  withMethods((store, api = inject(EmployeeApiService)) => ({
    loadAll: rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => api.getAll()),
        tap({
          next:  emps => patchState(store, setAllEntities(emps), { loading: false }),
          error: err  => patchState(store, { loading: false, error: err.message }),
        }),
      )
    ),
    remove(id: number): void {
      patchState(store, removeEntity(id));
    },
    setFilter(filter: string): void {
      patchState(store, { filter });
    },
  })),
);
```

```ts
// Usage in component
@Component({ ... })
export class EmployeesComponent {
  store = inject(EmployeeSignalStore);

  constructor() {
    this.store.loadAll();
  }
}
```

```html
@for (emp of store.filteredEmployees(); track emp.id) {
  <app-employee-card [employee]="emp" (removed)="store.remove($event)" />
}
```

---

## Summary

| NgRx Concept | Purpose |
|-------------|---------|
| `createActionGroup` | Define related actions with type safety |
| `createReducer` | Pure function: `(state, action) → newState` |
| `createEffect` | Handle async — dispatches further actions |
| `createSelector` | Memoised, composable state queries |
| `store.dispatch(action)` | Trigger a state change |
| `store.select(selector)` | Read state as Observable |
| NgRx SignalStore | Lightweight, signal-based alternative |

---

# Module 12 — Authentication & Route Guards

## Learning Objectives
- Implement JWT login/logout with `AuthService`
- Store tokens securely
- Protect routes with functional `CanActivate` and `CanMatch` guards
- Show role-based UI
- Apply to EMS: full login page with token management

---

## 12.1 Auth Models

```ts
// src/app/models/auth.model.ts
export interface LoginCredentials { email: string; password: string; }

export interface AuthUser {
  id:    number;
  name:  string;
  email: string;
  role:  'admin' | 'user';
  token: string;
}
```

---

## 12.2 Auth Service

```ts
// src/app/services/auth.service.ts
import { Injectable, signal, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthUser, LoginCredentials } from '../models/auth.model';

// Mock users — replace with real API call
const MOCK_USERS = [
  { id: 1, name: 'Admin User',   email: 'admin@ibm.com', password: 'admin123', role: 'admin' as const },
  { id: 2, name: 'Regular User', email: 'user@ibm.com',  password: 'user123',  role: 'user'  as const },
];

const TOKEN_KEY = 'ems_token';
const USER_KEY  = 'ems_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private router = inject(Router);

  private _user         = signal<AuthUser | null>(this.restoreUser());
  private _loginLoading = signal(false);
  private _loginError   = signal<string | null>(null);

  readonly currentUser   = this._user.asReadonly();
  readonly loginLoading  = this._loginLoading.asReadonly();
  readonly loginError    = this._loginError.asReadonly();
  readonly token         = computed(() => this._user()?.token ?? null);
  readonly isLoggedIn    = computed(() => this._user() !== null);
  readonly isAdmin       = computed(() => this._user()?.role === 'admin');

  private restoreUser(): AuthUser | null {
    try {
      const saved = localStorage.getItem(USER_KEY);
      return saved ? JSON.parse(saved) : null;
    } catch { return null; }
  }

  async login(credentials: LoginCredentials): Promise<void> {
    this._loginLoading.set(true);
    this._loginError.set(null);

    // Simulate API delay
    await new Promise(r => setTimeout(r, 600));

    const user = MOCK_USERS.find(
      u => u.email === credentials.email && u.password === credentials.password,
    );

    this._loginLoading.set(false);

    if (!user) {
      this._loginError.set('Invalid email or password');
      throw new Error('Invalid credentials');
    }

    const authUser: AuthUser = {
      id: user.id, name: user.name, email: user.email, role: user.role,
      token: `mock-jwt-${user.id}-${Date.now()}`,
    };

    this._user.set(authUser);
    localStorage.setItem(USER_KEY, JSON.stringify(authUser));
  }

  logout(): void {
    this._user.set(null);
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(TOKEN_KEY);
    this.router.navigate(['/login']);
  }

  clearLoginError(): void { this._loginError.set(null); }
}
```

---

## 12.3 Functional Guards

```ts
// src/app/guards/auth.guard.ts
import { inject } from '@angular/core';
import { CanActivateFn, CanMatchFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const auth   = inject(AuthService);
  const router = inject(Router);
  if (auth.isLoggedIn()) return true;
  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

export const adminGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);
  return auth.isAdmin() || router.createUrlTree(['/403']);
};

// CanMatch — prevents lazy bundle from even loading
export const authCanMatch: CanMatchFn = () => {
  return inject(AuthService).isLoggedIn();
};
```

---

## 12.4 Login Page

```ts
// src/app/pages/login/login.component.ts
import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  private fb       = inject(FormBuilder);
  private auth     = inject(AuthService);
  private router   = inject(Router);
  private route    = inject(ActivatedRoute);

  showPassword = signal(false);

  form = this.fb.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  protected loading = this.auth.loginLoading;
  protected error   = this.auth.loginError;

  fill(email: string, password: string): void {
    this.form.setValue({ email, password });
  }

  async onSubmit(): Promise<void> {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.auth.clearLoginError();

    try {
      await this.auth.login(this.form.getRawValue() as any);
      const returnUrl = this.route.snapshot.queryParams['returnUrl'] ?? '/employees';
      this.router.navigate([returnUrl]);
    } catch { /* error set in auth service */ }
  }
}
```

### `login.component.html`

```html
<div class="login-page">
  <div class="login-card">
    <div class="login-card__logo">
      <span class="logo-icon">🏢</span>
      <h1 class="logo-text">IBM EMS</h1>
    </div>

    <h2 class="login-card__subtitle">Sign in to your account</h2>

    @if (error()) {
      <div class="alert alert--error" role="alert">
        ⚠️ {{ error() }}
        <button class="alert__close" (click)="auth.clearLoginError()">×</button>
      </div>
    }

    <form [formGroup]="form" (ngSubmit)="onSubmit()" class="login-form" novalidate>

      <div class="field">
        <label for="email" class="field__label">Email address</label>
        <input id="email" type="email" formControlName="email"
               class="field__input" placeholder="you@ibm.com"
               autocomplete="email" />
        @if (form.get('email')!.invalid && form.get('email')!.touched) {
          <p class="field__error">Enter a valid email.</p>
        }
      </div>

      <div class="field">
        <label for="password" class="field__label">Password</label>
        <div class="field__pw-wrap">
          <input id="password"
                 [type]="showPassword() ? 'text' : 'password'"
                 formControlName="password"
                 class="field__input" placeholder="••••••••"
                 autocomplete="current-password" />
          <button type="button" class="field__pw-toggle"
                  (click)="showPassword.set(!showPassword())">
            {{ showPassword() ? '🙈' : '👁️' }}
          </button>
        </div>
      </div>

      <button type="submit" class="btn btn--primary login-btn"
              [disabled]="loading()">
        {{ loading() ? 'Signing in…' : 'Sign In' }}
      </button>
    </form>

    <div class="demo-credentials">
      <p>Demo credentials:</p>
      <div class="demo-row">
        <code>admin@ibm.com / admin123</code>
        <button class="btn btn--sm btn--secondary"
                (click)="fill('admin@ibm.com', 'admin123')">Fill</button>
      </div>
      <div class="demo-row">
        <code>user@ibm.com / user123</code>
        <button class="btn btn--sm btn--secondary"
                (click)="fill('user@ibm.com', 'user123')">Fill</button>
      </div>
    </div>
  </div>
</div>
```

---

## 12.5 Role-Based UI

```html
<!-- Show admin controls only to admin users -->
@if (auth.isAdmin()) {
  <button class="btn btn--danger" (click)="deleteAll()">Delete All</button>
}

<!-- Show user info -->
@if (auth.currentUser(); as user) {
  <div class="user-badge">
    <span>{{ user.name }}</span>
    <span class="role-badge role-badge--{{ user.role }}">{{ user.role }}</span>
  </div>
}
```

---

## Summary

| Concept | Implementation |
|---------|----------------|
| Login | `AuthService.login()` — returns `Promise<void>`, sets signal |
| Token storage | `localStorage` (dev) / `httpOnly` cookie (prod) |
| Session restore | `restoreUser()` called in constructor |
| Route guard | `CanActivateFn` functional guard |
| Guard on route | `canActivate: [authGuard]` in routes |
| CanMatch guard | Prevents lazy bundle loading |
| Role check | `auth.isAdmin()` computed signal |
| Login redirect | `router.createUrlTree(['/login'], { queryParams: { returnUrl } })` |

**Next → [Module 13: Performance](./13-performance.md)**
