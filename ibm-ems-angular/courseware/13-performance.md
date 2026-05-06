# Module 13 — Performance Optimization

## Learning Objectives
- Understand Angular's change detection strategies
- Enable zoneless change detection with Signals
- Lazy-load routes and use `@defer` for template-level code splitting
- Use `trackBy` for efficient list rendering
- Apply Virtual Scrolling for large datasets
- Profile with Angular DevTools

---

## 13.1 Change Detection — Default vs OnPush

### Default (Zone.js)

```
Any async event anywhere in the app
  → Zone.js intercepts
  → Angular checks EVERY component in the tree
  → Updates DOM where values changed
```

This is safe but wasteful — most components haven't changed.

### OnPush Strategy

```ts
import { ChangeDetectionStrategy } from '@angular/core';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  // ...
})
export class EmployeeCardComponent { }
```

With `OnPush`, Angular only checks this component when:
1. An `@Input()` reference changes
2. A Signal bound in the template changes
3. An Observable used with `AsyncPipe` emits
4. An event fires from within this component
5. `ChangeDetectorRef.markForCheck()` is called

```ts
// Force a check manually (last resort)
import { ChangeDetectorRef, inject } from '@angular/core';

@Component({ changeDetection: ChangeDetectionStrategy.OnPush })
export class ManualCheckComponent {
  private cdr = inject(ChangeDetectorRef);

  someAsyncOperation() {
    // After non-signal async update
    this.cdr.markForCheck();
  }
}
```

---

## 13.2 Zoneless Change Detection (Angular 18+)

With Signals, Angular can skip Zone.js entirely — components only re-render when their bound signals change.

```ts
// src/app/app.config.ts
import {
  ApplicationConfig,
  provideExperimentalZonelessChangeDetection,   // Angular 18+
} from '@angular/core';

export const appConfig: ApplicationConfig = {
  providers: [
    // Replace provideZoneChangeDetection({ eventCoalescing: true })
    provideExperimentalZonelessChangeDetection(),
    provideRouter(routes),
    provideHttpClient(),
  ],
};
```

```json
// angular.json — remove zone.js from polyfills
{
  "build": {
    "options": {
      "polyfills": []   // remove "zone.js"
    }
  }
}
```

**Requirements for zoneless:**
- All components must use `ChangeDetectionStrategy.OnPush`
- State must be managed with Signals or `AsyncPipe`
- No direct DOM manipulation outside Angular

---

## 13.3 Lazy Loading Routes

Each lazy route creates a separate JS bundle — downloaded only when the user visits that route.

```ts
// src/app/app.routes.ts
export const routes: Routes = [
  {
    path: 'employees',
    loadComponent: () =>
      import('./pages/employees/employees.component')
        .then(m => m.EmployeesComponent),
    // ↑ employees.component.ts is NOT in the main bundle
    // Downloaded only when user navigates to /employees
  },
  {
    // Lazy-load an entire feature with its own routes
    path: 'admin',
    loadChildren: () =>
      import('./features/admin/admin.routes')
        .then(m => m.ADMIN_ROUTES),
  },
];
```

### Preloading Strategies

```ts
import { withPreloading, PreloadAllModules, NoPreloading } from '@angular/router';

provideRouter(
  routes,
  withPreloading(PreloadAllModules),   // preload all lazy bundles after initial load
  // withPreloading(NoPreloading),     // disable preloading (default)
  // withPreloading(customStrategy),   // custom strategy
)
```

### Custom Preloading Strategy

```ts
// src/app/strategies/selective-preloading.strategy.ts
import { Injectable } from '@angular/core';
import { PreloadingStrategy, Route } from '@angular/router';
import { Observable, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SelectivePreloadingStrategy implements PreloadingStrategy {
  preload(route: Route, load: () => Observable<any>): Observable<any> {
    // Only preload routes marked with data.preload = true
    return route.data?.['preload'] ? load() : of(null);
  }
}

// Usage in routes
{ path: 'employees', data: { preload: true }, loadComponent: () => import(...) }
```

---

## 13.4 `@defer` for Template-Level Code Splitting

`@defer` downloads the component's JS bundle lazily, triggered by user interaction or browser state.

```html
<!-- Heavy table only loads when user scrolls to it -->
@defer (on viewport) {
  <app-employee-data-table [employees]="employees()" />
} @placeholder (minimum 200ms) {
  <div class="table-skeleton">
    @for (row of [1,2,3,4,5]; track row) {
      <div class="skeleton-row"></div>
    }
  </div>
} @loading (minimum 500ms; after 100ms) {
  <div class="loading-bar"></div>
} @error {
  <p>Failed to load table. <button (click)="reload()">Retry</button></p>
}

<!-- Analytics panel: load after browser is idle -->
@defer (on idle; prefetch on hover) {
  <app-salary-analytics [employees]="employees()" />
} @placeholder {
  <div class="analytics-placeholder">📊 Analytics loading...</div>
}

<!-- Conditionally defer -->
@defer (when showDetails()) {
  <app-employee-full-profile [id]="selectedId()" />
}
```

---

## 13.5 `trackBy` — Efficient List Re-Rendering

With the new `@for` syntax, `track` is **required** and serves the same purpose as React's `key` prop.

```html
<!-- ✅ Track by stable unique id — minimal DOM operations -->
@for (emp of employees(); track emp.id) {
  <app-employee-card [employee]="emp" />
}

<!-- ⚠️ Track by index — breaks when list reorders or filters -->
@for (emp of employees(); track $index) {
  <app-employee-card [employee]="emp" />
}
```

Old `*ngFor` still needs `trackBy`:

```ts
trackByEmployeeId(index: number, emp: Employee): number {
  return emp.id;
}
```

```html
<div *ngFor="let emp of employees(); trackBy: trackByEmployeeId">
```

---

## 13.6 Virtual Scrolling for Large Lists

Renders only the visible items — critical for lists with hundreds or thousands of rows.

```bash
ng add @angular/cdk
```

```ts
import { ScrollingModule } from '@angular/cdk/scrolling';

@Component({
  standalone: true,
  imports: [ScrollingModule, EmployeeCardComponent],
  template: `
    <cdk-virtual-scroll-viewport itemSize="200" class="employee-scroll-viewport">
      <div *cdkVirtualFor="let emp of employees(); trackBy: trackById">
        <app-employee-card [employee]="emp" (removed)="onRemove($event)" />
      </div>
    </cdk-virtual-scroll-viewport>
  `,
  styles: [`
    .employee-scroll-viewport {
      height: 600px;   /* fixed height required */
      overflow-y: auto;
    }
  `],
})
export class EmployeeListVirtualComponent {
  employees = input.required<Employee[]>();
  removed   = output<number>();

  trackById = (_: number, emp: Employee) => emp.id;
  onRemove(id: number) { this.removed.emit(id); }
}
```

---

## 13.7 OnPush + Signals — The Recommended Pattern

Combining `OnPush` with Signals gives you the best performance with the least effort:

```ts
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  template: `
    <!-- Signals in templates are tracked automatically -->
    @for (emp of filteredEmployees(); track emp.id) {
      <app-employee-card [employee]="emp" />
    }
    <p>Total: {{ stats().total }}</p>
  `,
})
export class EmployeesComponent {
  private state = inject(EmployeeStateService);

  // Reading signals in template = Angular knows exactly when to update
  filteredEmployees = this.state.filteredEmployees;   // Signal<Employee[]>
  stats             = this.state.stats;               // Signal<Stats>
}
```

Angular only re-renders this component when `filteredEmployees` or `stats` signal values change — not on every event in the app.

---

## 13.8 Profiling with Angular DevTools

Install the [Angular DevTools](https://chrome.google.com/webstore/detail/angular-devtools) Chrome extension.

### Component Tree Tab

- Inspect component hierarchy
- See each component's inputs/outputs
- View which signals are bound
- Edit signal values live

### Profiler Tab

1. Click **Start profiling**
2. Interact with your app
3. Click **Stop profiling**
4. View which components rendered on each change detection cycle
5. See render time per component
6. Identify "slow components" — the tall bars

### What to look for

```
Problem: A component re-renders on EVERY keystroke in search
Fix:     Add ChangeDetectionStrategy.OnPush
         Use signal inputs instead of @Input()

Problem: A list re-renders all items when one item changes
Fix:     Ensure track expression points to stable id
         Wrap item component with OnPush

Problem: Initial load is slow
Fix:     Lazy-load feature routes
         Use @defer (on viewport) for below-the-fold content

Problem: Scrolling through 1000 items is janky
Fix:     Use cdk-virtual-scroll-viewport
```

---

## 13.9 Bundle Analysis

```bash
ng build --configuration=production --stats-json
npx webpack-bundle-analyzer dist/ibm-ems-app/stats.json
```

This opens a visual treemap of your bundle — identify large dependencies and split them.

---

## 13.10 Performance Checklist

```
□ All components use ChangeDetectionStrategy.OnPush
□ All lists use @for with stable track expression
□ Route components are lazy-loaded (loadComponent)
□ Large feature sets use loadChildren
□ Heavy, below-fold content uses @defer (on viewport)
□ Lists with 100+ items use cdk-virtual-scroll-viewport
□ Images use loading="lazy" attribute
□ No RxJS subscriptions are left uncleaned (takeUntilDestroyed)
□ Profiler shows no unexpected re-renders
□ Bundle analyzer shows no surprise large packages
```

---

## Summary

| Technique | Impact | Effort |
|-----------|--------|--------|
| `ChangeDetectionStrategy.OnPush` | High | Low |
| Zoneless (`provideExperimentalZonelessChangeDetection`) | High | Medium |
| Lazy route loading (`loadComponent`) | High | Low |
| `@defer` for template content | High | Low |
| `trackBy` / `track` in loops | Medium | Low |
| Virtual scrolling (CDK) | Very high for large lists | Medium |
| Signals in templates | High | Low (new code) |
| Preloading strategy | Medium | Low |

**Next → [Module 14: Testing](./14-testing.md)**
