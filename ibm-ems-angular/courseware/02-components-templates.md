# Module 02 — Components and Templates

## Learning Objectives
- Understand every part of a standalone Angular 21 component
- Use signal-based `input()`, `output()`, `model()`, `viewChild()`
- Apply all four binding syntaxes: interpolation, property, event, two-way
- Implement lifecycle hooks the modern way
- Apply to EMS: build `EmployeeCardComponent` and `AppShellComponent`

---

## 2.1 Anatomy of an Angular 21 Component

```ts
import {
  Component, input, output, computed, signal,
  ChangeDetectionStrategy, viewChild, ElementRef,
  afterNextRender,
} from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Employee } from '../../models/employee.model';

@Component({
  // ① The HTML tag name — <app-employee-card>
  selector: 'app-employee-card',

  // ② Standalone = no NgModule needed (Angular 21 default)
  standalone: true,

  // ③ Dependencies this component uses in its template
  imports: [CurrencyPipe, DatePipe, RouterLink],

  // ④ Template — external file or inline string
  templateUrl: './employee-card.component.html',

  // ⑤ Styles — scoped to this component (Angular ViewEncapsulation)
  styleUrl: './employee-card.component.scss',

  // ⑥ Change detection strategy — OnPush = only update on signal/input change
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmployeeCardComponent {
  // Signal inputs (Angular 17+ preferred over @Input() decorator)
  employee = input.required<Employee>();    // required — parent must provide it
  compact  = input<boolean>(false);         // optional with default

  // Signal outputs (preferred over @Output() + EventEmitter)
  removed  = output<number>();             // emits the employee id
  selected = output<Employee>();

  // Computed signal — auto-updates when employee() changes
  displaySalary = computed(() =>
    new Intl.NumberFormat('en-US', {
      style: 'currency', currency: 'USD', maximumFractionDigits: 0,
    }).format(this.employee().salary)
  );

  joinYear = computed(() =>
    new Date(this.employee().joinDate).getFullYear()
  );

  // Signal query — typed reference to a DOM element in this template
  cardRef = viewChild<ElementRef<HTMLDivElement>>('cardEl');

  constructor() {
    // afterNextRender: runs once after the first render (replaces ngAfterViewInit for most cases)
    afterNextRender(() => {
      console.log('Card rendered:', this.cardRef()?.nativeElement);
    });
  }

  onRemove(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.removed.emit(this.employee().id);
  }

  onSelect(): void {
    this.selected.emit(this.employee());
  }
}
```

---

## 2.2 Binding Syntax — All Four Types

### ① Interpolation `{{ }}`

Evaluates a TypeScript expression and renders it as a string.

```html
<h3>{{ employee().name }}</h3>
<p>{{ employee().salary | currency }}</p>
<p>{{ employee().name.toUpperCase() }}</p>
<p>{{ employee().isActive ? 'Active' : 'Inactive' }}</p>
<p>Joined: {{ joinYear() }}</p>
```

> **Important:** because `employee` is a `Signal<Employee>`, you call it as a function: `employee()` — both in the class and in templates.

### ② Property Binding `[property]="expression"`

Sets a DOM property (not an HTML attribute) to a value.

```html
<!-- img src and alt -->
<img [src]="employee().avatarUrl ?? '/assets/default-avatar.svg'"
     [alt]="employee().name">

<!-- Disabled state -->
<button [disabled]="!employee().isActive">Edit</button>

<!-- Class binding — single class -->
<div [class.card--inactive]="!employee().isActive">

<!-- Multiple dynamic classes -->
<span [class]="badgeClass()">{{ employee().isActive ? 'Active' : 'Inactive' }}</span>

<!-- Style binding -->
<div [style.background-color]="employee().isActive ? '#defbe6' : '#fff1f1'">
<div [style.font-size.px]="compact() ? 12 : 16">

<!-- RouterLink property binding -->
<a [routerLink]="['/employees', employee().id]">View Profile</a>
```

### ③ Event Binding `(event)="handler()"`

```html
<!-- Click -->
<button (click)="onRemove($event)">Remove</button>

<!-- Keyboard events -->
<input (keydown.enter)="submit()">
<input (keyup.escape)="cancel()">

<!-- Input event -->
<input (input)="onSearch($event)">

<!-- Custom event from child component -->
<app-employee-card (removed)="handleRemove($event)" />
```

Reading `$event` in the class:

```ts
onSearch(event: Event): void {
  const value = (event.target as HTMLInputElement).value;
  this.searchTerm.set(value);
}
```

### ④ Two-Way Binding `[(binding)]`

```html
<!-- With FormsModule -->
<input [(ngModel)]="searchTerm">

<!-- Equivalent to: -->
<input [ngModel]="searchTerm" (ngModelChange)="searchTerm = $event">
```

With `model()` signals (Angular 17+):

```ts
// Component class
isExpanded = model<boolean>(false);  // writable, two-way bindable
```

```html
<!-- Parent template -->
<app-accordion [(isExpanded)]="panelOpen" />
```

---

## 2.3 Signal Inputs vs Decorator Inputs

```ts
// ── OLD (decorator style — still works) ────────────────────────────────────
import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({ ... })
export class OldComponent {
  @Input() employee!: Employee;
  @Input({ required: true }) title!: string;
  @Input('color') btnColor = 'blue';       // with alias

  @Output() removed  = new EventEmitter<number>();
  @Output() selected = new EventEmitter<Employee>();
}

// ── NEW (signal functions — Angular 17+, preferred in Angular 21) ──────────
import { Component, input, output, model } from '@angular/core';

@Component({ ... })
export class NewComponent {
  employee = input.required<Employee>();   // → Signal<Employee>
  title    = input.required<string>();     // → Signal<string>
  btnColor = input<string>('blue', { alias: 'color' }); // → Signal<string>

  removed  = output<number>();             // → OutputEmitterRef<number>
  selected = output<Employee>();           // → OutputEmitterRef<Employee>

  isExpanded = model<boolean>(false);      // two-way bindable signal
}
```

### Differences

| | `@Input()` | `input()` |
|-|-----------|----------|
| Type | Plain class property | Signal (`Signal<T>`) |
| Access in template | `employee.name` | `employee().name` — call it! |
| React to changes | `ngOnChanges` | `effect(() => { this.employee(); })` |
| Type safety | Requires `!` or `= undefined` | Required enforced at compile-time |
| computed() | Not possible | Works naturally |

---

## 2.4 Signal Queries

```ts
import { viewChild, viewChildren, contentChild, ElementRef } from '@angular/core';

@Component({
  template: `
    <input #searchBox type="text" />
    @for (emp of employees(); track emp.id) {
      <app-employee-card #cards [employee]="emp" />
    }
  `
})
export class EmployeeListComponent {
  // viewChild — single element reference (Signal<T | undefined>)
  searchBox = viewChild<ElementRef<HTMLInputElement>>('searchBox');

  // viewChild.required — throws if not found
  form = viewChild.required<FormComponent>('form');

  // viewChildren — all matching (Signal<readonly T[]>)
  cards = viewChildren<EmployeeCardComponent>('cards');

  // contentChild — query projected content (ng-content)
  headerSlot = contentChild<ElementRef>('header');

  constructor() {
    afterNextRender(() => {
      // Safe to access DOM after first render
      this.searchBox()?.nativeElement.focus();
    });
  }
}
```

---

## 2.5 Lifecycle Hooks

```ts
import {
  Component, OnInit, OnDestroy, OnChanges,
  AfterViewInit, SimpleChanges, inject, DestroyRef,
} from '@angular/core';

@Component({ ... })
export class LifecycleDemo implements OnInit, OnChanges, AfterViewInit, OnDestroy {

  // Inject DestroyRef for modern cleanup (preferred over implementing OnDestroy)
  private destroyRef = inject(DestroyRef);

  constructor() {
    // Runs first. Inject services here.
    // Signal inputs are NOT set yet.
    this.destroyRef.onDestroy(() => {
      // Runs when component is destroyed — modern alternative to ngOnDestroy
      clearInterval(this.timer);
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Called before ngOnInit AND whenever a decorator @Input() changes.
    // For signal inputs — use effect() instead (see Module 07).
  }

  ngOnInit(): void {
    // Called once after the first ngOnChanges.
    // Signal inputs are set and readable.
    this.loadData();
  }

  ngAfterViewInit(): void {
    // Called after the template + child components are fully rendered.
    // Safe to access DOM elements via viewChild() here.
  }

  ngOnDestroy(): void {
    // Called before removal. Clean up subscriptions, timers.
    // Modern alternative: DestroyRef.onDestroy() in constructor.
  }
}
```

### Lifecycle order

```
constructor()
  ↓
ngOnChanges()    ← first call (decorator @Input values set)
  ↓
ngOnInit()       ← once, after first ngOnChanges
  ↓
ngAfterViewInit() ← template + children rendered
  ↓
[component lives — ngOnChanges runs on each @Input change]
  ↓
ngOnDestroy()    ← cleanup before removal
```

---

## 2.6 Content Projection with `ng-content`

```ts
@Component({
  selector: 'app-panel',
  standalone: true,
  template: `
    <section class="panel">
      <header class="panel__header">
        <ng-content select="[panelTitle]"></ng-content>
      </header>
      <div class="panel__body">
        <ng-content></ng-content>          <!-- default slot -->
      </div>
      <footer class="panel__footer">
        <ng-content select="[panelActions]"></ng-content>
      </footer>
    </section>
  `,
})
export class PanelComponent {}
```

```html
<app-panel>
  <h2 panelTitle>Engineering Team</h2>

  <app-employee-card [employee]="emp" />

  <button panelActions>View All</button>
</app-panel>
```

---

## 2.7 EMS — EmployeeCard Component

```bash
ng g c components/employee-card --standalone
```

### `employee-card.component.ts`

```ts
import {
  Component, input, output, computed, ChangeDetectionStrategy,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { Employee } from '../../models/employee.model';

@Component({
  selector: 'app-employee-card',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './employee-card.component.html',
  styleUrl: './employee-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmployeeCardComponent {
  employee = input.required<Employee>();
  removed  = output<number>();

  initials = computed(() =>
    this.employee().name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
  );

  salary = computed(() =>
    new Intl.NumberFormat('en-US', {
      style: 'currency', currency: 'USD', maximumFractionDigits: 0,
    }).format(this.employee().salary)
  );

  joinYear = computed(() => new Date(this.employee().joinDate).getFullYear());

  onRemove(e: Event): void {
    e.preventDefault();
    e.stopPropagation();
    this.removed.emit(this.employee().id);
  }
}
```

### `employee-card.component.html`

```html
<article class="card" [class.card--inactive]="!employee().isActive">

  <button class="card__close"
          (click)="onRemove($event)"
          [attr.aria-label]="'Remove ' + employee().name">×</button>

  <a [routerLink]="['/employees', employee().id]" class="card__body">

    <div class="card__avatar">{{ initials() }}</div>

    <h3 class="card__name">{{ employee().name }}</h3>

    <ul class="card__meta">
      <li>📧 {{ employee().email }}</li>
      <li>📞 {{ employee().phone }}</li>
      <li>🏢 {{ employee().department }}</li>
      <li>📅 Since {{ joinYear() }}</li>
    </ul>

    <p class="card__salary">{{ salary() }} / yr</p>

    <span class="card__badge"
          [class.card__badge--active]="employee().isActive"
          [class.card__badge--inactive]="!employee().isActive">
      {{ employee().isActive ? 'Active' : 'Inactive' }}
    </span>

  </a>
</article>
```

### `employee-card.component.scss`

```scss
.card {
  background: white;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  position: relative;
  transition: box-shadow 0.2s ease, transform 0.2s ease;

  &:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
  &--inactive { opacity: 0.65; }

  &__close {
    position: absolute; top: 10px; right: 10px;
    width: 26px; height: 26px; border-radius: 50%;
    border: none; background: transparent;
    color: var(--color-gray-300); font-size: 18px; cursor: pointer;
    display: flex; align-items: center; justify-content: center;
    transition: background 0.15s, color 0.15s;
    &:hover { background: var(--color-danger-bg); color: var(--color-danger); }
  }

  &__body { display: block; padding: 20px; text-decoration: none; color: inherit; }

  &__avatar {
    width: 48px; height: 48px; border-radius: 50%;
    background: var(--color-primary); color: white;
    font-weight: 700; font-size: 18px;
    display: flex; align-items: center; justify-content: center;
    margin-bottom: 12px;
  }

  &__name {
    font-size: 17px; font-weight: 600;
    color: var(--color-gray-900); margin-bottom: 8px;
    padding-right: 28px;
  }

  &__meta {
    list-style: none; padding: 0;
    li { font-size: 13px; color: var(--color-gray-700); margin: 3px 0; }
  }

  &__salary {
    font-size: 16px; font-weight: 700;
    color: var(--color-primary); margin: 12px 0 8px;
  }

  &__badge {
    display: inline-block; padding: 2px 10px;
    border-radius: var(--radius-full);
    font-size: 11px; font-weight: 700;
    text-transform: uppercase; letter-spacing: 0.5px;

    &--active   { background: var(--color-success-bg); color: var(--color-success); }
    &--inactive { background: var(--color-danger-bg);  color: var(--color-danger); }
  }
}
```

---

## 2.8 App Shell with RouterOutlet

```ts
// src/app/app.component.ts
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './components/navbar/navbar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent],
  template: `
    <app-navbar />
    <main class="main">
      <router-outlet />
    </main>
  `,
  styles: [`
    .main {
      max-width: 1200px;
      margin: 0 auto;
      padding: 24px;
    }
  `]
})
export class AppComponent {}
```

---

## Summary

| Concept | Angular 21 API |
|---------|----------------|
| Declare component | `@Component({ standalone: true, imports: [...] })` |
| Required input signal | `name = input.required<T>()` — call with `name()` |
| Optional input signal | `name = input<T>(defaultValue)` |
| Event output | `event = output<PayloadType>()` |
| Two-way signal | `value = model<T>(init)` → `[(value)]="field"` |
| Derived value | `display = computed(() => this.name().toUpperCase())` |
| DOM query | `el = viewChild<ElementRef>('refName')` |
| Interpolation | `{{ expression }}` |
| Property binding | `[property]="expression"` |
| Event binding | `(event)="handler($event)"` |
| Two-way binding | `[(ngModel)]="field"` |

**Next → [Module 03: Control Flow, Directives & Pipes](./03-control-flow-pipes.md)**
