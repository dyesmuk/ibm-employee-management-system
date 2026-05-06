# Module 03 — Control Flow, Pipes & Directives

## Learning Objectives
- Use Angular 21's built-in control flow: `@if`, `@for`, `@switch`, `@defer`
- Understand when `@defer` is better than lazy loading
- Transform data with all major built-in pipes
- Use attribute directives `NgClass` and `NgStyle`
- Apply to EMS: employee list with `@for`, status `@if`, `@defer` for detail panels

---

## 3.1 `@if` — Conditional Rendering

Angular 21's block syntax replaces `*ngIf`. No import needed — it's part of the compiler.

```html
<!-- Simple if -->
@if (employee().isActive) {
  <span class="badge badge--active">Active</span>
}

<!-- if / else -->
@if (employee().isActive) {
  <span class="badge badge--active">Active</span>
} @else {
  <span class="badge badge--inactive">Inactive</span>
}

<!-- if / else if / else -->
@if (state() === 'loading') {
  <app-spinner />
} @else if (state() === 'error') {
  <p class="error">{{ errorMessage() }}</p>
} @else if (employees().length === 0) {
  <app-empty-state />
} @else {
  <div class="grid">...</div>
}

<!-- As alias — TypeScript knows the type inside the block -->
@if (selectedEmployee(); as emp) {
  <h2>{{ emp.name }}</h2>   <!-- emp is Employee, not Employee | null -->
  <p>{{ emp.department }}</p>
}
```

**Old syntax** (still compiles, not recommended):
```html
<span *ngIf="employee().isActive; else inactiveBlock">Active</span>
<ng-template #inactiveBlock><span>Inactive</span></ng-template>
```

---

## 3.2 `@for` — List Rendering

```html
<!-- Basic -->
@for (emp of employees(); track emp.id) {
  <app-employee-card [employee]="emp" (removed)="remove($event)" />
}

<!-- With @empty block -->
@for (emp of filteredEmployees(); track emp.id) {
  <app-employee-card [employee]="emp" (removed)="remove($event)" />
} @empty {
  <div class="empty-state">
    <span>🔍</span>
    <p>No employees match your search.</p>
  </div>
}

<!-- Loop context variables -->
@for (dept of departments; track dept; let i = $index, last = $last) {
  <button [class.last]="last">{{ i + 1 }}. {{ dept }}</button>
}
```

### Available loop variables

| Variable | Type | Meaning |
|----------|------|---------|
| `$index` | number | Position in the list (0-based) |
| `$first` | boolean | Is this the first item? |
| `$last` | boolean | Is this the last item? |
| `$even` | boolean | Is the index even? |
| `$odd` | boolean | Is the index odd? |
| `$count` | number | Total items in the collection |

### `track` — identity function

```html
<!-- ✅ Best — stable unique id -->
@for (emp of employees(); track emp.id) { }

<!-- ✅ Also good — unique string -->
@for (emp of employees(); track emp.email) { }

<!-- ⚠️ Avoid — index shifts on filter/reorder -->
@for (emp of employees(); track $index) { }

<!-- ❌ Never — object identity, always recreates DOM -->
@for (emp of employees(); track emp) { }
```

---

## 3.3 `@switch` — Multi-Branch Rendering

```html
@switch (employee().department) {
  @case ('Engineering') {
    <span class="dept dept--eng">⚙️ Engineering</span>
  }
  @case ('Marketing') {
    <span class="dept dept--mkt">📢 Marketing</span>
  }
  @case ('HR') {
    <span class="dept dept--hr">👥 HR</span>
  }
  @default {
    <span class="dept">{{ employee().department }}</span>
  }
}
```

---

## 3.4 `@defer` — Deferrable Views (Angular 17+)

`@defer` is one of Angular 21's most powerful features. It **lazily loads** a block of the template — the component's JavaScript is only downloaded when needed.

```html
<!-- Load details panel only when it enters the viewport -->
@defer (on viewport) {
  <app-employee-detail-panel [id]="employee().id" />
} @placeholder {
  <div class="detail-placeholder">Scroll to load details…</div>
} @loading (minimum 300ms) {
  <app-spinner />
} @error {
  <p class="error-msg">Failed to load panel.</p>
}
```

### Trigger options

| Trigger | When the deferred block loads |
|---------|-------------------------------|
| `on idle` | Browser is idle (default — non-critical content) |
| `on viewport` | Placeholder enters the viewport |
| `on interaction` | User clicks or focuses the placeholder |
| `on hover` | User hovers over the placeholder |
| `on timer(2s)` | After a fixed delay |
| `when condition` | When a boolean expression becomes true |
| `on immediate` | As soon as possible after rendering |

### Prefetching

```html
<!-- Show placeholder, prefetch JS when user hovers, render on click -->
@defer (on interaction; prefetch on hover) {
  <app-employee-history [employeeId]="employee().id" />
} @placeholder {
  <button class="load-history-btn">📋 View History</button>
}

<!-- Defer non-critical analytics widget -->
@defer (on idle) {
  <app-analytics-widget />
} @placeholder {
  <div class="widget-placeholder"></div>
}
```

### `@defer` vs lazy route loading

| | Lazy route | `@defer` |
|-|-----------|---------|
| Unit | Entire page component | Block within a template |
| Trigger | URL navigation | User interaction, scroll, time |
| Use case | Page-level code split | Below-the-fold, accordion panels |

---

## 3.5 Built-In Pipes

Pipes transform values in templates. They are **pure** (no side effects) and can be chained.

```html
{{ value | pipeName }}
{{ value | pipeName:arg1:arg2 }}
{{ value | pipe1 | pipe2 }}          <!-- chain -->
```

> In standalone components you must **import** each pipe you use.

```ts
import { CurrencyPipe, DatePipe, TitleCasePipe, AsyncPipe, DecimalPipe } from '@angular/common';

@Component({
  standalone: true,
  imports: [CurrencyPipe, DatePipe, TitleCasePipe, AsyncPipe, DecimalPipe],
  ...
})
```

### DatePipe

```html
{{ employee().joinDate | date }}                 <!-- Jan 15, 2021 -->
{{ employee().joinDate | date:'shortDate' }}      <!-- 1/15/21 -->
{{ employee().joinDate | date:'yyyy-MM-dd' }}     <!-- 2021-01-15 -->
{{ employee().joinDate | date:'longDate' }}       <!-- January 15, 2021 -->
{{ employee().joinDate | date:'fullDate' }}       <!-- Friday, January 15, 2021 -->
{{ employee().joinDate | date:'medium' }}         <!-- Jan 15, 2021, 12:00:00 AM -->
```

### CurrencyPipe

```html
{{ employee().salary | currency }}                      <!-- $95,000.00 -->
{{ employee().salary | currency:'USD':'symbol':'1.0-0' }} <!-- $95,000 -->
{{ employee().salary | currency:'INR':'symbol':'1.0-0' }} <!-- ₹95,000 -->
```

### DecimalPipe

```html
{{ 1234567.89 | number }}              <!-- 1,234,567.89 -->
{{ 1234567.89 | number:'1.0-0' }}      <!-- 1,234,568 -->
{{ 0.85 | number:'1.2-2' }}            <!-- 0.85 -->
```

### PercentPipe

```html
{{ 0.854 | percent }}          <!-- 85% -->
{{ 0.854 | percent:'1.1-1' }}  <!-- 85.4% -->
```

### Case pipes

```html
{{ 'alice johnson' | titlecase }}  <!-- Alice Johnson -->
{{ 'Engineering'   | uppercase }}  <!-- ENGINEERING -->
{{ 'EMS'           | lowercase }}  <!-- ems -->
```

### SlicePipe

```html
{{ 'Hello World' | slice:0:5 }}         <!-- Hello -->
{{ employees()   | slice:0:3 }}         <!-- first 3 -->
```

### JsonPipe (debug only)

```html
<pre>{{ employee() | json }}</pre>
```

### AsyncPipe — subscribe/unsubscribe automatically

```html
<!-- Subscribes to an Observable or Promise, auto-unsubscribes on destroy -->
@if (employees$ | async; as list) {
  @for (emp of list; track emp.id) {
    <app-employee-card [employee]="emp" />
  }
} @else {
  <app-spinner />
}
```

---

## 3.6 Attribute Directives

### `NgClass`

```html
<!-- Object syntax — multiple conditions -->
<div [ngClass]="{
  'card--active':    employee().isActive,
  'card--inactive':  !employee().isActive,
  'card--compact':   compact()
}">

<!-- Prefer individual bindings when possible (simpler, faster) -->
<div
  [class.card--active]="employee().isActive"
  [class.card--inactive]="!employee().isActive"
  [class.card--compact]="compact()">
```

### `NgStyle`

```html
<div [ngStyle]="{ 'border-color': deptColor(), 'font-weight': 'bold' }">

<!-- Prefer individual bindings -->
<div [style.border-color]="deptColor()"
     [style.font-size.px]="compact() ? 12 : 16">
```

Both `NgClass` and `NgStyle` must be imported in standalone components:

```ts
import { NgClass, NgStyle } from '@angular/common';

@Component({ standalone: true, imports: [NgClass, NgStyle], ... })
```

---

## 3.7 EMS — Employees Page Template

```html
<!-- src/app/pages/employees/employees.component.html -->

<div class="employees-page">

  <!-- Stats strip -->
  <div class="stats-strip">
    @for (stat of stats(); track stat.label) {
      <div class="stat-card">
        <span class="stat-value">{{ stat.value }}</span>
        <span class="stat-label">{{ stat.label }}</span>
      </div>
    }
  </div>

  <!-- Page header -->
  <div class="page-header">
    <h1>Employees</h1>
    <a routerLink="/employees/new" class="btn btn--primary">+ Add Employee</a>
  </div>

  <!-- Search -->
  <input
    type="text"
    class="search-input"
    placeholder="🔍 Search by name or email..."
    [value]="search()"
    (input)="search.set($any($event.target).value)"
  />

  <!-- Department filter -->
  <div class="filter-bar">
    @for (dept of allDepartments; track dept) {
      <button
        class="filter-btn"
        [class.filter-btn--active]="filter() === dept"
        (click)="filter.set(dept)">
        {{ dept }}
      </button>
    }

    <label class="toggle-label">
      <input type="checkbox"
             [checked]="onlyActive()"
             (change)="onlyActive.set($any($event.target).checked)" />
      Active only
    </label>
  </div>

  <!-- Result count -->
  <p class="result-info">
    Showing {{ filteredEmployees().length }} of {{ allEmployees().length }} employees
    @if (filter() !== 'All') {
      · filtered by {{ filter() }}
    }
  </p>

  <!-- Loading / Error / Grid -->
  @if (loading()) {
    <div class="loading-state">
      <div class="spinner"></div>
      <p>Loading employees…</p>
    </div>
  } @else if (error()) {
    <div class="error-state">
      <p>⚠️ {{ error() }}</p>
      <button class="btn btn--primary" (click)="reload()">Retry</button>
    </div>
  } @else {

    @if (filteredEmployees().length > 0) {
      <div class="employees-grid">
        @for (emp of filteredEmployees(); track emp.id) {
          <app-employee-card
            [employee]="emp"
            (removed)="onRemove($event)"
          />
        }
      </div>
    } @else {
      <div class="empty-state">
        <span class="empty-icon">🔍</span>
        <h3 class="empty-title">No employees found</h3>
        @if (search()) {
          <p>No match for "{{ search() }}".</p>
        } @else if (filter() !== 'All') {
          <p>No one in {{ filter() }}. Try another department.</p>
        } @else {
          <p>Add your first employee above.</p>
        }
      </div>
    }

    <!-- Defer non-critical analytics strip — load only when idle -->
    @defer (on idle) {
      <app-department-breakdown [employees]="allEmployees()" />
    } @placeholder {
      <div style="height: 80px;"></div>
    }
  }

</div>
```

---

## Summary

| Feature | Syntax |
|---------|--------|
| Conditional | `@if (cond) { } @else if () { } @else { }` |
| Loop | `@for (item of list; track item.id) { } @empty { }` |
| Switch | `@switch (val) { @case ('x') { } @default { } }` |
| Lazy content | `@defer (on viewport) { } @placeholder { } @loading { }` |
| Class binding | `[class.name]="condition"` |
| Style binding | `[style.prop]="value"` or `[style.prop.px]="number"` |
| Pipe | `{{ value \| pipeName:arg }}` |

**Next → [Module 04: Services & Dependency Injection](./04-services-di.md)**
