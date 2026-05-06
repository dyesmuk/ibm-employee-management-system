# Module 04 — Services & Dependency Injection

## Learning Objectives
- Create injectable services with `@Injectable`
- Use the `inject()` function (preferred Angular 21 pattern)
- Understand provider scope: root, component, route-level
- Use `InjectionToken` for non-class values
- Apply to EMS: `EmployeeService` with signal-based state

---

## 4.1 What Is Dependency Injection?

DI is a design pattern where a class declares what it needs, and Angular's DI container provides it automatically.

```
Without DI:
class EmployeesComponent {
  private service = new EmployeeService(new HttpClient(new HttpBackend()));
  // tightly coupled — can't test, can't swap
}

With DI:
class EmployeesComponent {
  private service = inject(EmployeeService);
  // Angular resolves it — testable, swappable
}
```

**DI gives you:**
- **Singleton services** — one instance shared across components
- **Testability** — swap real service for a mock in tests
- **Lifecycle control** — Angular destroys services when their scope ends
- **Circular dependency detection** — caught at startup, not runtime

---

## 4.2 Creating a Service

```bash
ng generate service services/employee
```

```ts
// src/app/services/employee.service.ts
import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Employee, CreateEmployeeDto, SEED_EMPLOYEES, Department } from '../models/employee.model';

@Injectable({
  providedIn: 'root',   // ← singleton for the ENTIRE app
})
export class EmployeeService {
  // The modern inject() function — call at property declaration level
  private http = inject(HttpClient);

  // Signal-based internal state (Module 07 covers signals in depth)
  private _employees  = signal<Employee[]>(SEED_EMPLOYEES);
  private _loading    = signal(false);
  private _error      = signal<string | null>(null);

  // Expose as read-only signals — components can read, not write directly
  readonly employees  = this._employees.asReadonly();
  readonly loading    = this._loading.asReadonly();
  readonly error      = this._error.asReadonly();

  // Computed signals — derived from _employees automatically
  readonly stats = computed(() => ({
    total:       this._employees().length,
    active:      this._employees().filter(e => e.isActive).length,
    departments: new Set(this._employees().map(e => e.department)).size,
    avgSalary:   this._employees().length
      ? Math.round(this._employees().reduce((s, e) => s + e.salary, 0) / this._employees().length)
      : 0,
  }));

  getById(id: number): Employee | undefined {
    return this._employees().find(e => e.id === id);
  }

  add(dto: CreateEmployeeDto): void {
    const emp: Employee = {
      ...dto,
      id:       Date.now(),
      isActive: true,
      username: dto.name.toLowerCase().replace(/\s+/g, '.'),
    };
    this._employees.update(list => [...list, emp]);
  }

  update(id: number, changes: Partial<Employee>): void {
    this._employees.update(list =>
      list.map(e => e.id === id ? { ...e, ...changes } : e)
    );
  }

  remove(id: number): void {
    this._employees.update(list => list.filter(e => e.id !== id));
  }

  toggleActive(id: number): void {
    this._employees.update(list =>
      list.map(e => e.id === id ? { ...e, isActive: !e.isActive } : e)
    );
  }
}
```

---

## 4.3 inject() vs Constructor Injection

```ts
// ── Constructor injection (classic) ──────────────────────────────────────
@Component({ ... })
export class MyComponent {
  constructor(
    private employeeService: EmployeeService,
    private router: Router,
  ) { }
}

// ── inject() function (Angular 14+ — preferred in Angular 21) ─────────────
@Component({ ... })
export class MyComponent {
  private employeeService = inject(EmployeeService);
  private router          = inject(Router);
}
```

`inject()` can be called anywhere during the **injection context**:
- At class property declaration
- Inside the `constructor` body
- Inside a factory function passed to a provider

```ts
// inject() in a standalone function (guard, resolver, interceptor)
export const authGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);
  return auth.isLoggedIn() || router.createUrlTree(['/login']);
};
```

---

## 4.4 Provider Scope

```ts
// 1 — Root (app-wide singleton)
@Injectable({ providedIn: 'root' })
export class EmployeeService { }

// 2 — Component scope (each component instance gets its OWN instance)
@Component({
  providers: [EmployeeService],   // fresh instance, destroyed with component
})
export class EmployeesComponent { }

// 3 — Route-level (shared only within a lazy-loaded route)
export const routes: Routes = [{
  path: 'admin',
  loadComponent: () => import('./admin/admin.component'),
  providers: [AdminReportService],   // exists only when /admin is loaded
}];

// 4 — Platform scope (shared across multiple Angular apps on same page)
@Injectable({ providedIn: 'platform' })
export class SharedAnalyticsService { }
```

---

## 4.5 InjectionToken — Provide Non-Class Values

```ts
// src/app/tokens/app-config.token.ts
import { InjectionToken } from '@angular/core';

export interface AppConfig {
  apiBaseUrl:     string;
  requestTimeout: number;
  featureFlags: {
    darkMode:           boolean;
    analyticsEnabled:   boolean;
    experimentalSearch: boolean;
  };
}

export const APP_CONFIG = new InjectionToken<AppConfig>('APP_CONFIG');
```

```ts
// src/app/app.config.ts — register the value
import { APP_CONFIG } from './tokens/app-config.token';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    {
      provide:  APP_CONFIG,
      useValue: {
        apiBaseUrl:     'https://jsonplaceholder.typicode.com',
        requestTimeout: 10000,
        featureFlags: {
          darkMode:           false,
          analyticsEnabled:   true,
          experimentalSearch: false,
        },
      },
    },
  ],
};
```

```ts
// Inject the token anywhere
@Injectable({ providedIn: 'root' })
export class ApiService {
  private config = inject(APP_CONFIG);

  get baseUrl(): string { return this.config.apiBaseUrl; }
}
```

---

## 4.6 Notification Service (Practical Example)

```ts
// src/app/services/notification.service.ts
import { Injectable, signal } from '@angular/core';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';

export interface Notification {
  id:      number;
  type:    NotificationType;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private _notifications = signal<Notification[]>([]);
  readonly notifications = this._notifications.asReadonly();

  private idCounter = 0;

  show(message: string, type: NotificationType = 'info', durationMs = 3000): void {
    const id = ++this.idCounter;
    this._notifications.update(list => [...list, { id, type, message }]);
    if (durationMs > 0) {
      setTimeout(() => this.dismiss(id), durationMs);
    }
  }

  success(message: string) { this.show(message, 'success'); }
  error(message: string)   { this.show(message, 'error',   5000); }
  warning(message: string) { this.show(message, 'warning'); }

  dismiss(id: number): void {
    this._notifications.update(list => list.filter(n => n.id !== id));
  }
}
```

---

## Summary

| Concept | API |
|---------|-----|
| Singleton service | `@Injectable({ providedIn: 'root' })` |
| Component-scoped service | `providers: [MyService]` in `@Component` |
| Inject a service | `private svc = inject(MyService)` |
| Non-class value | `new InjectionToken<T>('name')` |
| Provide value | `{ provide: TOKEN, useValue: ... }` |
| Factory provider | `{ provide: TOKEN, useFactory: () => ... }` |

---

# Module 05 — Routing & Navigation

## Learning Objectives
- Configure app routes with `provideRouter` and feature flags
- Lazy-load components at the route level
- Navigate with `RouterLink`, `RouterLinkActive`, and `Router.navigate()`
- Read params with `ActivatedRoute` and signal-based `withComponentInputBinding`
- Implement functional guards and resolvers

---

## 5.1 Route Configuration

```ts
// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { authGuard }          from './guards/auth.guard';
import { unsavedChangesGuard } from './guards/unsaved-changes.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full',
  },
  {
    path: 'home',
    loadComponent: () =>
      import('./pages/home/home.component').then(m => m.HomeComponent),
    title: 'IBM EMS — Home',
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login.component').then(m => m.LoginComponent),
    title: 'Sign In',
  },
  {
    path: 'employees',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./pages/employees/employees.component').then(m => m.EmployeesComponent),
        title: 'Employees',
      },
      {
        path: 'new',
        loadComponent: () =>
          import('./pages/employee-form/employee-form.component').then(m => m.EmployeeFormComponent),
        title: 'Add Employee',
        canDeactivate: [unsavedChangesGuard],
      },
      {
        path: ':id',
        loadComponent: () =>
          import('./pages/employee-detail/employee-detail.component').then(m => m.EmployeeDetailComponent),
        title: 'Employee Details',
      },
      {
        path: ':id/edit',
        loadComponent: () =>
          import('./pages/employee-form/employee-form.component').then(m => m.EmployeeFormComponent),
        title: 'Edit Employee',
        canDeactivate: [unsavedChangesGuard],
      },
    ],
  },
  { path: '403', loadComponent: () => import('./pages/forbidden/forbidden.component').then(m => m.ForbiddenComponent) },
  { path: '**',  loadComponent: () => import('./pages/not-found/not-found.component').then(m => m.NotFoundComponent) },
];
```

---

## 5.2 Register Router with Feature Flags

```ts
// src/app/app.config.ts
import {
  provideRouter,
  withComponentInputBinding,   // route :param → component input()
  withViewTransitions,         // smooth CSS page transitions
  withPreloading,
  PreloadAllModules,
  withRouterConfig,
} from '@angular/router';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(
      routes,
      withComponentInputBinding(),           // map :id → input('id')
      withViewTransitions(),                 // View Transitions API
      withPreloading(PreloadAllModules),     // preload lazy bundles after idle
      withRouterConfig({ paramsInheritanceStrategy: 'always' }),
    ),
  ],
};
```

---

## 5.3 RouterOutlet and Navigation Links

```ts
// src/app/app.component.ts
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './components/navbar/navbar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent],
  template: `
    <app-navbar />
    <main class="main-content">
      <router-outlet />
    </main>
  `,
})
export class AppComponent {}
```

```html
<!-- RouterLink -->
<a routerLink="/employees">Employees</a>
<a [routerLink]="['/employees', employee().id]">View</a>
<a [routerLink]="['/employees', id(), 'edit']">Edit</a>
<a [routerLink]="['/employees']" [queryParams]="{ dept: 'Engineering' }">Filter</a>

<!-- RouterLinkActive — adds class when route is active -->
<a routerLink="/employees"
   routerLinkActive="nav__link--active"
   [routerLinkActiveOptions]="{ exact: false }">
  Employees
</a>
```

---

## 5.4 Reading Route Parameters

With `withComponentInputBinding()` enabled, route params become component `input()` signals automatically:

```ts
// Route: /employees/:id
// Component receives :id directly as an input

@Component({ ... })
export class EmployeeDetailComponent implements OnInit {
  // 'id' matches the :id in the route path — set by the Router
  id = input<string>('');

  private employeeService = inject(EmployeeService);

  employee = computed(() =>
    this.employeeService.getById(Number(this.id()))
  );

  ngOnInit(): void {
    if (!this.employee()) {
      inject(Router).navigate(['/employees']);
    }
  }
}
```

**Alternative — inject `ActivatedRoute`:**

```ts
@Component({ ... })
export class EmployeeDetailComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  // Convert Observable to Signal
  private params = toSignal(this.route.paramMap);
  employeeId = computed(() => Number(this.params()?.get('id') ?? 0));

  // Classic Observable approach
  constructor() {
    this.route.params.subscribe(p => this.loadEmployee(Number(p['id'])));
  }
}
```

---

## 5.5 Programmatic Navigation

```ts
@Component({ ... })
export class LoginComponent {
  private router = inject(Router);
  private route  = inject(ActivatedRoute);

  onLoginSuccess(): void {
    const returnUrl = this.route.snapshot.queryParams['returnUrl'] ?? '/employees';

    // Standard navigation
    this.router.navigate(['/employees']);

    // With params
    this.router.navigate(['/employees', emp.id, 'edit']);

    // With query params
    this.router.navigate(['/employees'], { queryParams: { dept: 'HR' } });

    // Replace current history entry (no back button)
    this.router.navigate([returnUrl], { replaceUrl: true });

    // Navigate relative to current route
    this.router.navigate(['../list'], { relativeTo: this.route });
  }
}
```

---

## 5.6 Functional Guards

```ts
// src/app/guards/auth.guard.ts
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// No class needed — just a function
export const authGuard: CanActivateFn = (_route, state) => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  if (auth.isLoggedIn()) return true;

  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url },
  });
};

// Admin-only guard
export const adminGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);
  return auth.isAdmin() || router.createUrlTree(['/403']);
};

// Prevent leaving unsaved form
export const unsavedChangesGuard: CanDeactivateFn<{ hasUnsavedChanges: () => boolean }> =
  (component) => {
    if (component.hasUnsavedChanges()) {
      return confirm('You have unsaved changes. Leave anyway?');
    }
    return true;
  };
```

---

## 5.7 Navbar Component

```ts
// src/app/components/navbar/navbar.component.ts
import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss',
})
export class NavbarComponent {
  protected auth = inject(AuthService);
}
```

```html
<!-- navbar.component.html -->
<header class="navbar">
  <a routerLink="/home" class="navbar__brand">🏢 IBM EMS</a>

  <nav class="navbar__nav">
    <a routerLink="/home" routerLinkActive="navbar__link--active"
       [routerLinkActiveOptions]="{ exact: true }" class="navbar__link">
      Home
    </a>
    @if (auth.isLoggedIn()) {
      <a routerLink="/employees" routerLinkActive="navbar__link--active"
         class="navbar__link">
        Employees
      </a>
    }
  </nav>

  <div class="navbar__actions">
    @if (auth.currentUser(); as user) {
      <span class="navbar__user">{{ user.name }}</span>
      <button class="btn btn--sm btn--secondary" (click)="auth.logout()">
        Sign Out
      </button>
    } @else {
      <a routerLink="/login" class="btn btn--sm btn--primary">Sign In</a>
    }
  </div>
</header>
```

---

# Module 06 — HTTP Client

## Learning Objectives
- Provide `HttpClient` with `provideHttpClient`
- Make typed GET, POST, PUT, PATCH, DELETE requests
- Write functional HTTP interceptors
- Handle errors centrally
- Use `httpResource()` — the signals-integrated HTTP API (Angular 19+)

---

## 6.1 Setup

```ts
// src/app/app.config.ts
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor }  from './interceptors/auth.interceptor';
import { errorInterceptor } from './interceptors/error.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withInterceptors([authInterceptor, errorInterceptor]),
    ),
  ],
};
```

---

## 6.2 Base API Service

```ts
// src/app/services/api.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { APP_CONFIG } from '../tokens/app-config.token';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http   = inject(HttpClient);
  private config = inject(APP_CONFIG);

  private url(path: string): string {
    return `${this.config.apiBaseUrl}${path}`;
  }

  get<T>(path: string, params?: Record<string, string | number>): Observable<T> {
    let httpParams = new HttpParams();
    if (params) {
      Object.entries(params).forEach(([k, v]) => {
        httpParams = httpParams.set(k, String(v));
      });
    }
    return this.http.get<T>(this.url(path), { params: httpParams });
  }

  post<TBody, TResponse = TBody>(path: string, body: TBody): Observable<TResponse> {
    return this.http.post<TResponse>(this.url(path), body);
  }

  put<TBody, TResponse = TBody>(path: string, body: TBody): Observable<TResponse> {
    return this.http.put<TResponse>(this.url(path), body);
  }

  patch<TBody, TResponse = TBody>(path: string, body: TBody): Observable<TResponse> {
    return this.http.patch<TResponse>(this.url(path), body);
  }

  delete<T = void>(path: string): Observable<T> {
    return this.http.delete<T>(this.url(path));
  }
}
```

---

## 6.3 Employee API Service

```ts
// src/app/services/employee-api.service.ts
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from './api.service';
import { Employee, CreateEmployeeDto, UpdateEmployeeDto, Department } from '../models/employee.model';

const DEPT_MAP = Object.values(Department);

function mapUser(user: any): Employee {
  return {
    id:         user.id,
    name:       user.name,
    email:      user.email,
    phone:      user.phone ?? '+91-9999999999',
    username:   user.username,
    department: DEPT_MAP[user.id % DEPT_MAP.length],
    salary:     50000 + user.id * 4500,
    isActive:   user.id % 4 !== 0,
    joinDate:   `202${user.id % 4}-${String((user.id % 9) + 1).padStart(2, '0')}-15`,
    address: {
      street:  user.address?.street ?? '',
      city:    user.address?.city ?? '',
      country: 'India',
    },
  };
}

@Injectable({ providedIn: 'root' })
export class EmployeeApiService {
  private api = inject(ApiService);

  getAll(): Observable<Employee[]> {
    return this.api.get<any[]>('/users').pipe(map(users => users.map(mapUser)));
  }

  getById(id: number): Observable<Employee> {
    return this.api.get<any>(`/users/${id}`).pipe(map(mapUser));
  }

  create(dto: CreateEmployeeDto): Observable<Employee> {
    return this.api.post<CreateEmployeeDto, any>('/users', dto).pipe(
      map(u => mapUser({ ...u, id: Date.now() })),
    );
  }

  update(id: number, dto: UpdateEmployeeDto): Observable<Employee> {
    return this.api.patch<UpdateEmployeeDto, any>(`/users/${id}`, dto).pipe(map(mapUser));
  }

  remove(id: number): Observable<void> {
    return this.api.delete<void>(`/users/${id}`);
  }
}
```

---

## 6.4 HTTP Interceptors (Functional — Angular 15+)

```ts
// src/app/interceptors/auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).token();

  if (!token) return next(req);

  const authReq = req.clone({
    headers: req.headers.set('Authorization', `Bearer ${token}`),
  });
  return next(authReq);
};
```

```ts
// src/app/interceptors/error.interceptor.ts
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { NotificationService } from '../services/notification.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router       = inject(Router);
  const notification = inject(NotificationService);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      switch (err.status) {
        case 0:   notification.error('Network error — check your connection'); break;
        case 401: router.navigate(['/login']); break;
        case 403: router.navigate(['/403']); break;
        case 404: notification.error('Resource not found'); break;
        case 422: notification.error('Validation failed'); break;
        case 500: notification.error('Server error — please try again'); break;
        default:  notification.error(`Unexpected error (${err.status})`);
      }
      return throwError(() => err);
    }),
  );
};
```

---

## 6.5 `httpResource()` — Signals + HTTP (Angular 19+)

```ts
import { Component, input } from '@angular/core';
import { httpResource } from '@angular/core/rxjs-interop';

@Component({
  standalone: true,
  template: `
    @if (empResource.isLoading()) {
      <app-spinner />
    } @else if (empResource.error()) {
      <p>Error: {{ empResource.error() }}</p>
    } @else if (empResource.value(); as emp) {
      <h1>{{ emp.name }}</h1>
      <p>{{ emp.department }} · {{ emp.email }}</p>
    }
  `,
})
export class EmployeeDetailComponent {
  id = input.required<string>();

  // httpResource: re-fetches automatically when id() changes
  // value(), isLoading(), error(), status() are all Signals
  empResource = httpResource<Employee>(() =>
    `https://jsonplaceholder.typicode.com/users/${this.id()}`
  );
}
```

---

## 6.6 Updated EmployeeService with HTTP

```ts
// src/app/services/employee.service.ts
import { Injectable, signal, computed, inject } from '@angular/core';
import { EmployeeApiService } from './employee-api.service';
import { Employee, CreateEmployeeDto, UpdateEmployeeDto } from '../models/employee.model';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private api = inject(EmployeeApiService);

  private _employees = signal<Employee[]>([]);
  private _loading   = signal(false);
  private _error     = signal<string | null>(null);

  readonly employees = this._employees.asReadonly();
  readonly loading   = this._loading.asReadonly();
  readonly error     = this._error.asReadonly();

  readonly stats = computed(() => ({
    total:       this._employees().length,
    active:      this._employees().filter(e => e.isActive).length,
    departments: new Set(this._employees().map(e => e.department)).size,
  }));

  constructor() { this.load(); }

  load(): void {
    this._loading.set(true);
    this._error.set(null);
    this.api.getAll().subscribe({
      next:  list  => { this._employees.set(list);       this._loading.set(false); },
      error: err   => { this._error.set(err.message ?? 'Load failed'); this._loading.set(false); },
    });
  }

  add(dto: CreateEmployeeDto): void {
    this.api.create(dto).subscribe({
      next:  emp  => this._employees.update(l => [...l, emp]),
      error: err  => this._error.set(err.message),
    });
  }

  update(id: number, dto: UpdateEmployeeDto): void {
    this.api.update(id, dto).subscribe({
      next:  emp  => this._employees.update(l => l.map(e => e.id === id ? { ...e, ...emp } : e)),
      error: err  => this._error.set(err.message),
    });
  }

  remove(id: number): void {
    this.api.remove(id).subscribe({
      next:  ()   => this._employees.update(l => l.filter(e => e.id !== id)),
      error: err  => this._error.set(err.message),
    });
  }

  getById(id: number): Employee | undefined {
    return this._employees().find(e => e.id === id);
  }
}
```

**Next → [Module 07: Signals](./07-signals.md)**
