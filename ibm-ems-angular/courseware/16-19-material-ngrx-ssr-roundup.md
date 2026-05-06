# Module 16 — Bonus: Angular Material UI

## Learning Objectives
- Install and theme Angular Material
- Use `MatTable` with sorting, pagination, and filtering
- Use `MatDialog` for confirmations and forms
- Use `MatSnackBar` for notifications
- Apply to EMS: replace custom card grid with a Material data table

---

## 16.1 Install Angular Material

```bash
ng add @angular/material
# Choose a theme, enable typography, include animations
```

This adds:
- `@angular/material` and `@angular/cdk`
- A theme to `styles.scss`
- `BrowserAnimationsModule` to `app.config.ts`

---

## 16.2 Custom Theme

```scss
// src/styles.scss
@use '@angular/material' as mat;

// Define custom palette
$ems-primary: mat.m2-define-palette(mat.$m2-blue-palette, 700);
$ems-accent:  mat.m2-define-palette(mat.$m2-amber-palette, A200, A100, A400);
$ems-warn:    mat.m2-define-palette(mat.$m2-red-palette);

// Build the theme
$ems-theme: mat.m2-define-light-theme((
  color: (
    primary: $ems-primary,
    accent:  $ems-accent,
    warn:    $ems-warn,
  ),
  typography: mat.m2-define-typography-config(
    $font-family: 'IBM Plex Sans, sans-serif',
  ),
  density: 0,
));

// Apply the theme
@include mat.all-component-themes($ems-theme);
```

---

## 16.3 MatTable — Employee Data Table

```ts
// src/app/pages/employees-table/employees-table.component.ts
import { Component, inject, viewChild, AfterViewInit, ChangeDetectionStrategy } from '@angular/core';
import { MatTableModule, MatTableDataSource }  from '@angular/material/table';
import { MatSortModule, MatSort }              from '@angular/material/sort';
import { MatPaginatorModule, MatPaginator }    from '@angular/material/paginator';
import { MatInputModule }                      from '@angular/material/input';
import { MatFormFieldModule }                  from '@angular/material/form-field';
import { MatChipsModule }                      from '@angular/material/chips';
import { MatButtonModule }                     from '@angular/material/button';
import { MatIconModule }                       from '@angular/material/icon';
import { MatDialogModule, MatDialog }          from '@angular/material/dialog';
import { MatSnackBar }                         from '@angular/material/snack-bar';
import { EmployeeStateService }                from '../../state/employee-state.service';
import { Employee }                            from '../../models/employee.model';
import { ConfirmDialogComponent }              from '../../components/confirm-dialog/confirm-dialog.component';
import { toObservable }                        from '@angular/core/rxjs-interop';
import { CurrencyPipe }                        from '@angular/common';

const DISPLAYED_COLUMNS = ['name', 'department', 'salary', 'status', 'joinDate', 'actions'];

@Component({
  selector: 'app-employees-table',
  standalone: true,
  imports: [
    MatTableModule, MatSortModule, MatPaginatorModule,
    MatInputModule, MatFormFieldModule, MatChipsModule,
    MatButtonModule, MatIconModule, MatDialogModule,
    CurrencyPipe,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="table-container mat-elevation-z2">

      <!-- Search filter -->
      <mat-form-field class="table-filter" appearance="outline">
        <mat-label>Filter employees</mat-label>
        <input matInput (keyup)="applyFilter($event)" placeholder="Name, email, department…" />
        <mat-icon matSuffix>search</mat-icon>
      </mat-form-field>

      <mat-table [dataSource]="dataSource" matSort class="employee-table">

        <!-- Name column -->
        <ng-container matColumnDef="name">
          <mat-header-cell *matHeaderCellDef mat-sort-header>Name</mat-header-cell>
          <mat-cell *matCellDef="let emp">
            <div class="name-cell">
              <div class="avatar">{{ emp.name.charAt(0) }}</div>
              <div>
                <div class="name">{{ emp.name }}</div>
                <div class="email">{{ emp.email }}</div>
              </div>
            </div>
          </mat-cell>
        </ng-container>

        <!-- Department column -->
        <ng-container matColumnDef="department">
          <mat-header-cell *matHeaderCellDef mat-sort-header>Department</mat-header-cell>
          <mat-cell *matCellDef="let emp">
            <mat-chip [highlighted]="true">{{ emp.department }}</mat-chip>
          </mat-cell>
        </ng-container>

        <!-- Salary column -->
        <ng-container matColumnDef="salary">
          <mat-header-cell *matHeaderCellDef mat-sort-header>Salary</mat-header-cell>
          <mat-cell *matCellDef="let emp">{{ emp.salary | currency:'USD':'symbol':'1.0-0' }}</mat-cell>
        </ng-container>

        <!-- Status column -->
        <ng-container matColumnDef="status">
          <mat-header-cell *matHeaderCellDef>Status</mat-header-cell>
          <mat-cell *matCellDef="let emp">
            <mat-chip [color]="emp.isActive ? 'primary' : 'warn'" highlighted>
              {{ emp.isActive ? 'Active' : 'Inactive' }}
            </mat-chip>
          </mat-cell>
        </ng-container>

        <!-- Join Date column -->
        <ng-container matColumnDef="joinDate">
          <mat-header-cell *matHeaderCellDef mat-sort-header>Joined</mat-header-cell>
          <mat-cell *matCellDef="let emp">{{ emp.joinDate | date:'mediumDate' }}</mat-cell>
        </ng-container>

        <!-- Actions column -->
        <ng-container matColumnDef="actions">
          <mat-header-cell *matHeaderCellDef>Actions</mat-header-cell>
          <mat-cell *matCellDef="let emp">
            <button mat-icon-button color="primary" [routerLink]="['/employees', emp.id, 'edit']"
                    aria-label="Edit employee">
              <mat-icon>edit</mat-icon>
            </button>
            <button mat-icon-button color="warn" (click)="confirmDelete(emp)"
                    aria-label="Delete employee">
              <mat-icon>delete</mat-icon>
            </button>
          </mat-cell>
        </ng-container>

        <!-- Header and row definitions -->
        <mat-header-row *matHeaderRowDef="displayedColumns; sticky: true"></mat-header-row>
        <mat-row *matRowDef="let row; columns: displayedColumns;"
                 [class.row--inactive]="!row.isActive"></mat-row>

        <!-- No data row -->
        <tr class="mat-row" *matNoDataRow>
          <td class="mat-cell no-data-cell" [attr.colspan]="displayedColumns.length">
            No employees match the filter "{{ filterValue }}"
          </td>
        </tr>
      </mat-table>

      <mat-paginator [pageSizeOptions]="[10, 25, 50]" showFirstLastButtons></mat-paginator>
    </div>
  `,
})
export class EmployeesTableComponent implements AfterViewInit {
  private state   = inject(EmployeeStateService);
  private dialog  = inject(MatDialog);
  private snackbar = inject(MatSnackBar);

  displayedColumns = DISPLAYED_COLUMNS;
  dataSource       = new MatTableDataSource<Employee>([]);
  filterValue      = '';

  sort      = viewChild.required(MatSort);
  paginator = viewChild.required(MatPaginator);

  constructor() {
    // Keep MatTableDataSource in sync with signal
    toObservable(this.state.employees).subscribe(emps => {
      this.dataSource.data = emps;
    });
  }

  ngAfterViewInit(): void {
    this.dataSource.sort      = this.sort();
    this.dataSource.paginator = this.paginator();
  }

  applyFilter(event: Event): void {
    this.filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = this.filterValue.trim().toLowerCase();
    this.dataSource.paginator?.firstPage();
  }

  confirmDelete(employee: Employee): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title:   'Delete Employee',
        message: `Are you sure you want to remove ${employee.name}? This cannot be undone.`,
        confirm: 'Delete',
        cancel:  'Cancel',
      },
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.state.removeEmployee(employee.id);
        this.snackbar.open(`${employee.name} has been removed.`, 'Undo', { duration: 5000 });
      }
    });
  }
}
```

---

## 16.4 MatDialog — Confirm Dialog Component

```ts
// src/app/components/confirm-dialog/confirm-dialog.component.ts
import { Component, inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

export interface ConfirmDialogData {
  title:   string;
  message: string;
  confirm: string;
  cancel:  string;
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <p>{{ data.message }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="false">{{ data.cancel }}</button>
      <button mat-raised-button color="warn" [mat-dialog-close]="true" cdkFocusInitial>
        {{ data.confirm }}
      </button>
    </mat-dialog-actions>
  `,
})
export class ConfirmDialogComponent {
  data    = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
  dialogRef = inject(MatDialogRef<ConfirmDialogComponent>);
}
```

---

## 16.5 MatSnackBar — Notification Service

```ts
// src/app/services/snackbar-notification.service.ts
import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({ providedIn: 'root' })
export class SnackbarNotificationService {
  private snackBar = inject(MatSnackBar);

  success(message: string): void {
    this.snackBar.open(message, '✕', {
      duration:           3000,
      panelClass:         ['snackbar--success'],
      horizontalPosition: 'right',
      verticalPosition:   'bottom',
    });
  }

  error(message: string): void {
    this.snackBar.open(message, 'Dismiss', {
      duration:   5000,
      panelClass: ['snackbar--error'],
    });
  }
}
```

---

# Module 17 — Bonus: NgRx Deep Dive

## 17.1 NgRx Entity Adapter

Entity adapter provides pre-built reducer functions for CRUD operations on collections.

```bash
npm install @ngrx/entity
```

```ts
// src/app/store/employee.reducer.ts (with Entity)
import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { createReducer, on } from '@ngrx/store';
import { Employee } from '../models/employee.model';
import { EmployeeActions } from './employee.actions';

// EntityState adds 'ids' and 'entities' automatically
export interface EmployeeState extends EntityState<Employee> {
  loading:    boolean;
  error:      string | null;
  filter:     string;
  search:     string;
  selectedId: number | null;
}

// Adapter: choose sort key
const adapter: EntityAdapter<Employee> = createEntityAdapter<Employee>({
  selectId:  emp => emp.id,
  sortComparer: (a, b) => a.name.localeCompare(b.name),
});

const initialState: EmployeeState = adapter.getInitialState({
  loading:    false,
  error:      null,
  filter:     'All',
  search:     '',
  selectedId: null,
});

export const employeeReducer = createReducer(
  initialState,
  on(EmployeeActions.loadEmployeesSuccess, (state, { employees }) =>
    adapter.setAll(employees, { ...state, loading: false })
  ),
  on(EmployeeActions.createEmployeeSuccess, (state, { employee }) =>
    adapter.addOne(employee, state)
  ),
  on(EmployeeActions.updateEmployeeSuccess, (state, { employee }) =>
    adapter.upsertOne(employee, state)
  ),
  on(EmployeeActions.deleteEmployeeSuccess, (state, { id }) =>
    adapter.removeOne(id, state)
  ),
);

// Entity adapter provides selectors
export const { selectAll, selectEntities, selectIds, selectTotal } =
  adapter.getSelectors();
```

```ts
// Entity selectors
import { createFeatureSelector, createSelector } from '@ngrx/store';
import { selectAll } from './employee.reducer';

const selectFeature = createFeatureSelector<EmployeeState>('employees');

export const selectAllEmployees = createSelector(selectFeature, selectAll);
export const selectEmployeeById = (id: number) =>
  createSelector(
    createFeatureSelector<EmployeeState>('employees'),
    state => state.entities[id]
  );
```

---

## 17.2 NgRx SignalStore (Modern Alternative)

```ts
// src/app/store/employee.signal-store.ts
import { patchState, signalStore, withComputed, withMethods, withState } from '@ngrx/signals';
import { withEntities, setAllEntities, addEntity, removeEntity, updateEntity } from '@ngrx/signals/entities';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { computed, inject } from '@angular/core';
import { pipe, tap, switchMap, catchError, EMPTY } from 'rxjs';
import { Employee, EmployeeFilter, INITIAL_FILTER } from '../models/employee.model';
import { EmployeeApiService } from '../services/employee-api.service';

type EmployeeSignalState = {
  loading: boolean;
  error:   string | null;
  filter:  EmployeeFilter;
};

export const EmployeeSignalStore = signalStore(
  { providedIn: 'root' },

  withEntities<Employee>(),

  withState<EmployeeSignalState>({
    loading: false,
    error:   null,
    filter:  INITIAL_FILTER,
  }),

  withComputed(({ entities, filter }) => ({
    filteredEmployees: computed(() => {
      const f    = filter();
      const term = f.search.toLowerCase();
      return entities()
        .filter(e => f.department === 'All' || e.department === f.department)
        .filter(e => !f.onlyActive || e.isActive)
        .filter(e => e.name.toLowerCase().includes(term) || e.email.toLowerCase().includes(term))
        .sort((a, b) => a.name.localeCompare(b.name));
    }),
    stats: computed(() => ({
      total:    entities().length,
      active:   entities().filter(e => e.isActive).length,
    })),
  })),

  withMethods((store, api = inject(EmployeeApiService)) => ({

    // rxMethod — turns rxjs observable into a method
    loadAll: rxMethod<void>(
      pipe(
        tap(()    => patchState(store, { loading: true, error: null })),
        switchMap(() =>
          api.getAll().pipe(
            tap(emps => patchState(store, setAllEntities(emps), { loading: false })),
            catchError(err => {
              patchState(store, { loading: false, error: err.message });
              return EMPTY;
            }),
          )
        ),
      )
    ),

    add: rxMethod<Employee>(
      pipe(
        switchMap(emp =>
          api.create(emp).pipe(
            tap(created => patchState(store, addEntity(created))),
          )
        ),
      )
    ),

    remove(id: number): void {
      api.remove(id).subscribe(() => patchState(store, removeEntity(id)));
    },

    setFilter(partial: Partial<EmployeeFilter>): void {
      patchState(store, state => ({ filter: { ...state.filter, ...partial } }));
    },
  })),
);
```

```ts
// Usage: inject directly — no Provider wrapping needed
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
<p>{{ store.stats().total }} total · {{ store.stats().active }} active</p>
```

---

# Module 18 — Bonus: Server-Side Rendering (SSR)

## 18.1 What Is SSR?

| | CSR (default) | SSR | SSG |
|-|--------------|-----|-----|
| HTML sent to browser | Empty shell | Full page | Pre-built HTML |
| JS required to see content | Yes | No (initial) | No (initial) |
| SEO | Poor | Excellent | Excellent |
| First Paint | Slow | Fast | Fastest |
| Dynamic data | Yes | Yes | At build time |

---

## 18.2 Enable SSR

```bash
# Add SSR to existing project
ng add @angular/ssr
```

This adds:
- `src/main.server.ts` — server entry point
- `src/app/app.config.server.ts` — server-specific providers
- `server.ts` — Express server

```bash
ng build && node dist/ibm-ems-app/server/server.mjs
```

---

## 18.3 Server Config

```ts
// src/app/app.config.server.ts
import { mergeApplicationConfig, ApplicationConfig } from '@angular/core';
import { provideServerRendering } from '@angular/platform-server';
import { appConfig } from './app.config';

const serverConfig: ApplicationConfig = {
  providers: [
    provideServerRendering(),
  ],
};

export const config = mergeApplicationConfig(appConfig, serverConfig);
```

---

## 18.4 TransferState — Avoid Double Fetching

```ts
// Without TransferState:
// 1. Server fetches employees (renders HTML)
// 2. Client downloads HTML
// 3. Angular boots on client — fetches employees AGAIN!
// → Two identical HTTP requests

// With TransferState:
import { TransferState, makeStateKey } from '@angular/core';

const EMPLOYEES_KEY = makeStateKey<Employee[]>('employees');

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private transferState = inject(TransferState);
  private api           = inject(EmployeeApiService);

  getAll(): Observable<Employee[]> {
    // Check if server already fetched this data
    if (this.transferState.hasKey(EMPLOYEES_KEY)) {
      const cached = this.transferState.get(EMPLOYEES_KEY, []);
      this.transferState.remove(EMPLOYEES_KEY);
      return of(cached);                    // use cached data on client
    }

    return this.api.getAll().pipe(
      tap(employees => {
        // On server: save to state for client to pick up
        this.transferState.set(EMPLOYEES_KEY, employees);
      }),
    );
  }
}
```

---

## 18.5 SSG — Static Site Generation

```ts
// angular.json — configure prerender
{
  "prerender": {
    "routes": [
      "/",
      "/employees",
      "/login"
    ]
  }
}
```

```bash
ng build --prerender
# Generates static HTML for each specified route
```

---

# Module 19 — Course Roundup

## What You Built

Starting from `ng new`, you built a full **IBM Employee Management System** with:

| Feature | Modules |
|---------|---------|
| Standalone components with signal inputs | 02 |
| `@if` `@for` `@defer` control flow | 03 |
| Injectable services with DI | 04 |
| Multi-page routing with lazy loading | 05 |
| HttpClient + typed API service | 06 |
| Signal-based reactive state | 07 |
| RxJS streams with debounced search | 08 |
| Reactive forms with custom validators | 09 |
| Custom pipes and attribute directives | 10 |
| NgRx Store + Effects + SignalStore | 11 |
| JWT authentication + functional guards | 12 |
| OnPush + Zoneless change detection | 13 |
| Jest unit tests + component tests | 14 |
| Docker + Nginx + GitHub Actions CI/CD | 15 |
| Angular Material table + dialog | 16 |
| SSR with TransferState | 18 |

---

## Angular 21 Feature Checklist

```
✅ Standalone components (no NgModules)
✅ @if / @for / @switch / @defer
✅ signal() / computed() / effect()
✅ input() / output() / model()
✅ viewChild() / contentChild()
✅ linkedSignal()
✅ resource() / httpResource()
✅ Functional guards (CanActivateFn, CanMatchFn)
✅ inject() function
✅ withComponentInputBinding()
✅ provideRouter / provideHttpClient
✅ Functional HTTP interceptors
✅ takeUntilDestroyed() / DestroyRef
✅ toSignal() / toObservable()
✅ afterNextRender()
✅ Zoneless change detection
✅ NgRx SignalStore
✅ SSR with Angular Universal
```

---

## Final Project Structure

```
src/app/
├── components/
│   ├── confirm-dialog/
│   ├── employee-card/        ← Standalone, OnPush, signal inputs
│   ├── navbar/
│   └── spinner/
├── directives/
│   ├── auto-focus.directive.ts
│   ├── highlight.directive.ts
│   └── tooltip.directive.ts
├── guards/
│   └── auth.guard.ts         ← Functional guard
├── interceptors/
│   ├── auth.interceptor.ts   ← Functional interceptor
│   ├── error.interceptor.ts
│   └── logging.interceptor.ts
├── models/
│   ├── employee.model.ts     ← Interfaces, enums, seed data
│   └── auth.model.ts
├── pages/
│   ├── employee-detail/
│   ├── employee-form/        ← Reactive form with validators
│   ├── employees/            ← OnPush, signals, @for
│   ├── home/
│   └── login/
├── pipes/
│   ├── salary.pipe.ts
│   ├── initials.pipe.ts
│   └── time-ago.pipe.ts
├── services/
│   ├── api.service.ts
│   ├── auth.service.ts
│   ├── employee-api.service.ts
│   └── notification.service.ts
├── state/
│   └── employee-state.service.ts  ← Signal-based state
├── store/                          ← NgRx (optional)
│   ├── employee.actions.ts
│   ├── employee.reducer.ts
│   ├── employee.effects.ts
│   ├── employee.selectors.ts
│   └── employee.signal-store.ts   ← NgRx SignalStore
├── tokens/
│   └── app-config.token.ts
├── app.component.ts
├── app.config.ts
└── app.routes.ts
```

---

## What to Learn Next

### Immediate (1-3 months)
- **Angular CDK** — drag and drop, virtual scroll, accessibility
- **Angular Animations** — `@angular/animations` triggers and transitions
- **i18n** — `@angular/localize`, ICU expressions
- **PWA** — `ng add @angular/pwa`, service workers, offline support

### Intermediate (3-6 months)
- **Micro-frontends** — Module Federation with Angular
- **NativeScript** — native mobile apps with Angular
- **Analog.js** — Angular meta-framework (like Next.js for Angular)
- **Storybook** — build and document components in isolation

### Advanced (6+ months)
- **Custom Schematics** — automate code generation
- **Nx Monorepo** — manage multiple Angular apps/libs
- **WebSockets** — real-time features with RxJS
- **WebAssembly** — integrate Rust/C++ modules

---

## Angular Ecosystem Map

```
Core
├── Signals             ← reactivity (built-in)
├── RxJS                ← async streams
├── Angular Forms       ← reactive + template-driven
├── Angular Router      ← client-side routing
├── Angular HttpClient  ← HTTP requests
└── Angular CDK         ← primitives (scroll, overlay, a11y)

State Management
├── Signals + Service   ← simple (recommended starting point)
├── NgRx Store          ← complex, enterprise
├── NgRx SignalStore    ← modern lightweight NgRx
├── Akita               ← alternative store
└── NGXS                ← alternative store

UI Libraries
├── Angular Material    ← Google's official component library
├── PrimeNG             ← rich component library
├── Ng-Zorro            ← Ant Design for Angular
└── Carbon Components   ← IBM's design system

Testing
├── Jasmine + Karma     ← default (being phased out)
├── Jest                ← faster, better DX (recommended)
└── Playwright/Cypress  ← E2E testing

Build & Deployment
├── Angular CLI         ← scaffolding + build
├── esbuild             ← Angular 17+ default bundler
├── Nx                  ← monorepo tooling
└── Analog.js           ← SSR/SSG meta-framework

DevOps
├── GitHub Actions      ← CI/CD
├── Docker + Nginx      ← containerised deployment
├── Vercel / Netlify    ← quick SaaS deployment
└── Angular DevTools    ← profiler + component tree
```

---

## Resources

| Resource | URL |
|----------|-----|
| Angular Docs | https://angular.dev |
| Angular Blog | https://blog.angular.io |
| NgRx Docs | https://ngrx.io |
| Angular Material | https://material.angular.io |
| Angular CDK | https://material.angular.io/cdk |
| RxJS Docs | https://rxjs.dev |
| Angular DevTools | Chrome Web Store |
| Angular University | https://angular-university.io |
| This is Angular | https://this-is-angular.github.io |

---

## Final Note

Angular 21 is a modern, powerful framework. The combination of:
- **Standalone components** (no NgModule complexity)
- **Signals** (fine-grained reactivity)
- **New control flow** (`@if`, `@for`, `@defer`)
- **Functional APIs** (guards, interceptors, inject())

...makes it feel genuinely fresh and competitive. The learning curve is front-loaded, but once you understand the architecture, Angular's opinionation becomes a productivity superpower — especially for teams.

> "Angular's strict structure isn't a limitation — it's a blueprint that scales."

**🎉 Congratulations on completing the IBM EMS Angular 21 Courseware!**
