# 🏢 IBM EMS — Angular 21 Complete Courseware

> **Project:** Employee Management System  
> **Framework:** Angular 21 · TypeScript 5 · SCSS · Angular CLI  
> **Approach:** One evolving project — every module adds features to the same EMS app

---

## How This Course Works

You start with `ng new` and finish with a fully deployed, production-grade app.  
Every concept is **immediately applied** to the EMS project.

---

## Module Index

| # | File | Topics |
|---|------|--------|
| 00 | [00-getting-started.md](./00-getting-started.md) | Angular CLI, scaffold, file anatomy, execution flow, Hello World |
| 01 | [01-typescript-for-angular.md](./01-typescript-for-angular.md) | Decorators, interfaces, enums, generics, utility types |
| 02 | [02-components-templates.md](./02-components-templates.md) | Standalone components, signal inputs/outputs, lifecycle, binding syntax |
| 03 | [03-control-flow-pipes.md](./03-control-flow-pipes.md) | @if @for @switch @defer, built-in pipes, NgClass/NgStyle |
| 04 | [04-services-di.md](./04-services-di.md) | Injectable services, inject(), provider scopes, InjectionTokens |
| 05 | [05-routing.md](./05-routing.md) | Routes, lazy loading, RouterLink, guards, withComponentInputBinding |
| 06 | [06-http-client.md](./06-http-client.md) | HttpClient, interceptors, httpResource(), error handling |
| 07 | [07-signals.md](./07-signals.md) | signal(), computed(), effect(), linkedSignal(), resource() |
| 08 | [08-rxjs-observables.md](./08-rxjs-observables.md) | Observables, operators, Subjects, toSignal(), takeUntilDestroyed |
| 09 | [09-forms.md](./09-forms.md) | Reactive forms, FormBuilder, validators, FormArray, template-driven |
| 10 | [10-pipes-directives-interceptors.md](./10-pipes-directives-interceptors.md) | Custom pipes, attribute directives, structural directives, interceptors |
| 11 | [11-ngrx.md](./11-ngrx.md) | NgRx Store, Actions, Reducers, Effects, Selectors, SignalStore |
| 12 | [12-authentication.md](./12-authentication.md) | JWT, AuthService, CanActivate, functional guards, protected routes |
| 13 | [13-performance.md](./13-performance.md) | OnPush, zoneless, lazy routes, @defer, trackBy, virtual scroll |
| 14 | [14-testing.md](./14-testing.md) | TestBed, ComponentFixture, HttpClientTesting, Jest setup |
| 15 | [15-deployment.md](./15-deployment.md) | ng build, Docker, Nginx, GitHub Actions, environment configs |
| 16 | [16-angular-material-bonus.md](./16-angular-material-bonus.md) | CDK, MatTable, MatDialog, MatSnackBar, theming |
| 17 | [17-ngrx-deep-dive-bonus.md](./17-ngrx-deep-dive-bonus.md) | Effects, Entity adapter, RouterStore, NgRx SignalStore |
| 18 | [18-ssr-bonus.md](./18-ssr-bonus.md) | Angular Universal, SSR, SSG, hydration, TransferState |
| 19 | [19-course-roundup.md](./19-course-roundup.md) | Full feature list, ecosystem map, what to learn next |

---

## Angular 21 Key Features Used

| Feature | Module |
|---------|--------|
| Standalone components (default) | 02 |
| `@if` `@for` `@switch` `@defer` | 03 |
| `signal()` `computed()` `effect()` | 07 |
| `input()` `output()` `model()` | 02 |
| `viewChild()` `contentChild()` | 02 |
| `linkedSignal()` | 07 |
| `resource()` / `httpResource()` | 07 |
| Functional guards | 05, 12 |
| `inject()` function | 04 |
| `withComponentInputBinding()` | 05 |
| `takeUntilDestroyed()` | 08 |
| `DestroyRef` | 08 |
| Zoneless change detection | 13 |
| NgRx SignalStore | 11, 17 |
| `provideRouter` / `provideHttpClient` | 05, 06 |
| View Transitions API | 05 |

---

## Quick Start

```bash
npm install -g @angular/cli@latest
ng new ibm-ems-app --standalone --routing --style=scss
cd ibm-ems-app
ng serve
```

Open **http://localhost:4200**
