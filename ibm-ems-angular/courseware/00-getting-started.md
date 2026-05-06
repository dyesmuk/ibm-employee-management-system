# Module 00 — Getting Started with Angular 21

## Learning Objectives
- Understand what Angular is and how it compares to React
- Install Angular CLI and scaffold the EMS project
- Understand every generated file and its purpose
- Trace the complete execution flow from browser to screen
- Clean boilerplate and print Hello World

---

## 0.1 What Is Angular?

Angular is a **complete platform** — not just a UI library. Google maintains it and ships everything in one package:

| Need | Angular's built-in solution |
|------|-----------------------------|
| UI rendering | Component + Template system |
| Reactivity | Signals (`signal`, `computed`, `effect`) |
| HTTP | `HttpClient` |
| Routing | `RouterModule` |
| Forms | `ReactiveFormsModule` / `FormsModule` |
| Dependency Injection | Built-in hierarchical DI container |
| Build tooling | Angular CLI (esbuild / Webpack) |
| Testing | TestBed + Jasmine / Jest |

### Angular vs React — key differences

| | Angular | React |
|-|---------|-------|
| Type | Full platform | UI library |
| Language | TypeScript only | JS or TS |
| Reactivity | Signals (built-in) | External (useState, Redux) |
| Routing | Built-in | External (React Router) |
| Forms | Built-in (Reactive + Template) | External (RHF, Formik) |
| DI | Built-in hierarchical | External (Context) |
| Templates | Separate HTML files | JSX inline |
| Change detection | Zone.js (default) or Zoneless | Virtual DOM diffing |
| Learning curve | Steeper initial | Gentler initial |
| Enterprise use | Very high | Very high |

---

## 0.2 Prerequisites

| Tool | Version | Check |
|------|---------|-------|
| Node.js | 20 LTS+ | `node -v` |
| npm | 10+ | `npm -v` |
| Angular CLI | 21+ | `ng version` |

```bash
npm install -g @angular/cli@latest
ng version
```

### Recommended VS Code Extensions

1. **Angular Language Service** — IntelliSense inside HTML templates
2. **Angular Snippets** — `a-component` → full scaffold
3. **ESLint** — code quality
4. **Prettier** — auto-format on save
5. **Angular DevTools** — Chrome extension (inspect component tree, signals)

---

## 0.3 Create the EMS Project

```bash
ng new ibm-ems-app \
  --standalone \
  --routing \
  --style=scss \
  --skip-tests=false
```

| Flag | What it does |
|------|-------------|
| `--standalone` | Use standalone components (no NgModule — Angular 21 default) |
| `--routing` | Generate `app.routes.ts` and `RouterOutlet` |
| `--style=scss` | Use SCSS (supports nesting and variables) |
| `--skip-tests=false` | Generate `.spec.ts` test files |

```bash
cd ibm-ems-app
ng serve --open   # opens http://localhost:4200 automatically
```

---

## 0.4 Project Structure — Every File Explained

```
ibm-ems-app/
│
├── src/
│   ├── app/
│   │   ├── app.component.ts        ← Root component class
│   │   ├── app.component.html      ← Root component template
│   │   ├── app.component.scss      ← Root component styles
│   │   ├── app.component.spec.ts   ← Root component test
│   │   ├── app.config.ts           ← Application-level providers (replaces AppModule)
│   │   └── app.routes.ts           ← Route definitions
│   │
│   ├── assets/                     ← Static files (images, fonts, icons)
│   ├── index.html                  ← The single HTML page (SPA shell)
│   ├── main.ts                     ← Entry point — calls bootstrapApplication()
│   └── styles.scss                 ← Global SCSS
│
├── angular.json                    ← CLI workspace config (build, serve, test settings)
├── package.json
├── tsconfig.json                   ← TypeScript root config
├── tsconfig.app.json               ← TS config for app source
└── tsconfig.spec.json              ← TS config for test files
```

### `src/index.html`

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>IbmEmsApp</title>
  <base href="/">          ← CRITICAL for router — sets URL base
  <meta name="viewport" content="width=device-width, initial-scale=1">
</head>
<body>
  <app-root></app-root>    ← Angular mounts your app here
</body>
</html>
```

### `src/main.ts`

```ts
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

bootstrapApplication(AppComponent, appConfig)
  .catch(err => console.error(err));
```

This is the **entry point**. It tells Angular:  
- Which component is the root (`AppComponent`)  
- What global providers/config to use (`appConfig`)

### `src/app/app.config.ts`

```ts
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
  ],
};
```

This replaces the old `AppModule`. All application-level providers go here (router, HttpClient, NgRx store, etc.).

### `src/app/app.component.ts`

```ts
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',        // matches <app-root> in index.html
  standalone: true,            // no NgModule required
  imports: [RouterOutlet],     // declare what this component needs
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  title = 'ibm-ems-app';
}
```

---

## 0.5 Full Execution Flow

```
① Browser loads http://localhost:4200
        │
        ▼
② Angular CLI dev server responds with index.html
        │
        ▼
③ Browser finds <app-root></app-root> (empty)
   Browser downloads <script> bundles injected by CLI
        │
        ▼
④ main.ts executes:
   bootstrapApplication(AppComponent, appConfig)
        │
        ▼
⑤ Angular creates the root Injector
   Registers all providers from appConfig:
   — ZoneChangeDetection
   — Router (with routes)
        │
        ▼
⑥ Angular instantiates AppComponent
   — runs constructor (injects dependencies)
   — processes @Component metadata
        │
        ▼
⑦ Angular compiles the template:
   — evaluates binding expressions
   — creates child components
   — applies directives and pipes
        │
        ▼
⑧ Change detection runs:
   — walks the component tree
   — patches DOM where values changed
        │
        ▼
⑨ Browser paints — user sees the UI
        │
        ▼
⑩ Router reads the current URL
   Renders the matched route component
   inside <router-outlet>
```

---

## 0.6 Angular CLI Commands

```bash
# Serve with hot reload
ng serve
ng serve --port 4300 --open

# Generate components, services, etc.
ng generate component components/employee-card --standalone
ng g c components/employee-card --standalone     # shorthand

ng generate service services/employee
ng g s services/employee

ng generate interface models/employee
ng g interface models/employee

ng generate pipe pipes/salary-format
ng generate directive directives/highlight
ng generate guard guards/auth --implements CanActivate
ng generate interceptor interceptors/auth-token

# Build
ng build                              # dev build
ng build --configuration=production   # prod build → dist/

# Tests
ng test                               # Karma + Jasmine (default)
ng test --no-watch --code-coverage    # single run with coverage

# Lint
ng lint
```

---

## 0.7 Clean Boilerplate — Hello World

### `src/app/app.component.ts`

```ts
import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <div class="app">
      <h1>Hello World</h1>
      <p>IBM Employee Management System</p>
    </div>
  `,
  styles: [`
    .app { padding: 24px; font-family: sans-serif; }
  `]
})
export class AppComponent {}
```

### `src/styles.scss`

```scss
/* ── Reset ── */
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

/* ── Design Tokens ── */
:root {
  --color-primary:       #0062ff;
  --color-primary-dark:  #0043ce;
  --color-primary-light: #d0e2ff;
  --color-gray-900:      #161616;
  --color-gray-700:      #525252;
  --color-gray-300:      #c6c6c6;
  --color-gray-100:      #f4f4f4;
  --color-white:         #ffffff;
  --color-success:       #24a148;
  --color-success-bg:    #defbe6;
  --color-danger:        #da1e28;
  --color-danger-bg:     #fff1f1;
  --color-warning:       #f1c21b;
  --color-border:        #e0e0e0;

  --font-sans: 'IBM Plex Sans', -apple-system, BlinkMacSystemFont,
               'Segoe UI', Roboto, sans-serif;
  --font-mono: 'IBM Plex Mono', 'Courier New', monospace;

  --radius-sm:   4px;
  --radius-md:   8px;
  --radius-full: 9999px;
  --shadow-sm:   0 1px 3px rgba(0,0,0,0.08);
  --shadow-md:   0 4px 12px rgba(0,0,0,0.12);
  --shadow-lg:   0 8px 24px rgba(0,0,0,0.16);
}

body {
  font-family: var(--font-sans);
  font-size: 16px;
  line-height: 1.6;
  color: var(--color-gray-900);
  background: var(--color-gray-100);
}

a { color: var(--color-primary); text-decoration: none; }
a:hover { text-decoration: underline; }
button { font-family: var(--font-sans); cursor: pointer; }

/* ── Utility classes ── */
.btn {
  display: inline-flex; align-items: center;
  padding: 10px 20px; border: none;
  border-radius: var(--radius-sm);
  font-size: 14px; font-weight: 600;
  transition: background 0.15s, opacity 0.15s;
  cursor: pointer;

  &--primary   { background: var(--color-primary);   color: white; }
  &--secondary { background: white; color: var(--color-primary); border: 1.5px solid var(--color-primary); }
  &--danger    { background: var(--color-danger);     color: white; }
  &--sm        { padding: 6px 14px; font-size: 13px; }

  &:hover:not(:disabled) { opacity: 0.9; }
  &:disabled { opacity: 0.5; cursor: not-allowed; }
}
```

Save — browser shows **Hello World**.

---

## 0.8 Angular 21 — What Changed From Earlier Versions

| Before Angular 15-16 | Angular 21 |
|----------------------|------------|
| NgModules required | **Standalone components** — no NgModule needed |
| `*ngIf`, `*ngFor` | `@if`, `@for`, `@switch`, `@defer` |
| `@Input()` decorator | `input()` signal function |
| `@Output()` decorator | `output()` signal function |
| `@ViewChild()` decorator | `viewChild()` signal query |
| `new EventEmitter()` | `output<T>()` |
| Zone.js always | **Zoneless** option with signals |
| `ngOnDestroy` for cleanup | `DestroyRef`, `takeUntilDestroyed()` |
| Class-based guards | **Functional guards** |
| `resolve` with classes | `ResolveFn` functional resolvers |

---

## 0.9 EMS Project Plan

| Module | Feature added to EMS |
|--------|---------------------|
| 02 | `EmployeeCardComponent` with signal inputs |
| 03 | Employee list with `@for`, `@if`, pipes |
| 04 | `EmployeeService` with DI |
| 05 | Pages + routing: Home, List, Detail |
| 06 | Real API calls with `HttpClient` |
| 07 | Signal-based state management |
| 08 | RxJS for search debouncing |
| 09 | Create/Edit employee form |
| 10 | Custom `SalaryPipe`, `HighlightDirective` |
| 11 | NgRx global store |
| 12 | Login, JWT, route guards |
| 14 | Tests for all layers |
| 15 | Docker + production deployment |

---

## Summary

- Angular is a **full platform** — routing, HTTP, forms, DI all built-in
- Angular 21 uses **standalone components** — no NgModules
- `main.ts` → `bootstrapApplication(AppComponent, appConfig)` is the entry point
- `app.config.ts` is where global providers are registered
- The CLI generates all boilerplate with `ng generate`

**Next → [Module 01: TypeScript for Angular](./01-typescript-for-angular.md)**
