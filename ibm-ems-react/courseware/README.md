# 🧑‍💼 IBM EMS — Complete ReactJS Courseware

> **Project:** Employee Management System (EMS)  
> **Stack:** React 19.2 · TypeScript · Vite 6 · React Router 6 · Redux Toolkit · Axios · React Hook Form · Zod · Vitest  
> **Approach:** Single running project — every module adds new features to the same app

---

## How This Course Works

Every module introduces new React concepts **and immediately applies them** to the EMS project.  
You start with a blank screen and finish with a fully deployed, production-ready app.

```
Module 00 → scaffold the project
Module 01 → JS/TS refresh
Module 02 → print Hello World, understand JSX
Module 03 → first components, props, state
Module 04 → render employee list, add/remove logic
Module 05 → style the EMS with CSS Modules
Module 06 → debug tools and patterns
Module 07 → deeper hooks, custom hooks, context
Module 08 → fetch real employees from an API (Axios)
Module 09 → add multiple pages with React Router
Module 10 → employee create/edit forms with validation
Module 11 → move state into Redux
Module 12 → login / logout / protected routes
Module 13 → write tests for every layer
Module 14 → build and deploy to Vercel / Docker
Module 15 → Webpack deep-dive (bonus)
Module 16 → migrate to Next.js (bonus)
Module 17 → animate the EMS (bonus)
Module 18 → Redux Saga for complex async (bonus)
Module 19 → React Hooks complete reference
Module 20 → next steps and course roundup
```

---

## Quick Start

```bash
npm create vite@latest ibm-ems-app -- --template react-ts
cd ibm-ems-app
npm install
npm run dev
```

Then follow the modules in order.

---

## Module Index

| # | File | Topics |
|---|------|--------|
| 00 | [00-getting-started.md](./00-getting-started.md) | Vite scaffold, project anatomy, execution flow, Hello World |
| 01 | [01-js-ts-refresh.md](./01-js-ts-refresh.md) | const/let, arrow functions, destructuring, spread, modules, async/await, TypeScript essentials |
| 02 | [02-jsx-and-components.md](./02-jsx-and-components.md) | JSX rules, function components, rendering, export/import |
| 03 | [03-props-state-events.md](./03-props-state-events.md) | Props, useState, event handlers, two-way binding, lifting state |
| 04 | [04-lists-and-conditionals.md](./04-lists-and-conditionals.md) | .map(), key prop, conditional rendering patterns, filters |
| 05 | [05-styling.md](./05-styling.md) | Inline styles, CSS Modules, global CSS, variables, clsx, Tailwind intro |
| 06 | [06-debugging.md](./06-debugging.md) | React DevTools, console strategies, common bugs, Error Boundaries |
| 07 | [07-deep-dive-components.md](./07-deep-dive-components.md) | useEffect, useRef, useMemo, useCallback, React.memo, custom hooks, Context API, folder structure |
| 08 | [08-http-ajax.md](./08-http-ajax.md) | Axios setup, service layer, interceptors, loading/error states, env vars |
| 09 | [09-routing.md](./09-routing.md) | React Router 6, nested routes, Outlet, useParams, useNavigate, protected routes |
| 10 | [10-forms-validation.md](./10-forms-validation.md) | Controlled inputs, manual validation, React Hook Form, Zod, all input types |
| 11 | [11-redux.md](./11-redux.md) | Redux Toolkit, slices, createAsyncThunk, selectors, DevTools, migrate EMS |
| 12 | [12-authentication.md](./12-authentication.md) | JWT flow, AuthContext, login page, token storage, route guards |
| 13 | [13-testing.md](./13-testing.md) | Vitest, React Testing Library, userEvent, MSW, Redux testing, coverage |
| 14 | [14-deployment.md](./14-deployment.md) | Build, code splitting, env vars, Vercel, Netlify, Docker, CI/CD |
| 15 | [15-webpack-bonus.md](./15-webpack-bonus.md) | Webpack concepts, loaders, plugins, custom config |
| 16 | [16-nextjs-bonus.md](./16-nextjs-bonus.md) | Next.js App Router, SSR/SSG, Server Actions, metadata |
| 17 | [17-animations-bonus.md](./17-animations-bonus.md) | CSS transitions, Framer Motion, page transitions, list animations |
| 18 | [18-redux-saga-bonus.md](./18-redux-saga-bonus.md) | Generators, sagas, channels, saga vs thunk |
| 19 | [19-react-hooks-reference.md](./19-react-hooks-reference.md) | All built-in hooks + React 19 new hooks with examples |
| 20 | [20-course-roundup.md](./20-course-roundup.md) | What you built, next steps, ecosystem map, project ideas |

---

## Final App Features

| Feature | Modules |
|---------|---------|
| Employee list with search & filter | 03, 04 |
| CSS-styled cards and layout | 05 |
| Live API data (JSONPlaceholder) | 08 |
| Multi-page navigation | 09 |
| Create / Edit employee form | 10 |
| Global state with Redux | 11 |
| Login / Logout / Auth guards | 12 |
| Unit + integration tests | 13 |
| Production deployment | 14 |

---

## Tech Versions Used

```
react                  19.2.x
react-dom              19.2.x
typescript             5.x
vite                   6.x
react-router-dom       6.x
@reduxjs/toolkit       2.x
react-redux            9.x
axios                  1.x
react-hook-form        7.x
zod                    3.x
@hookform/resolvers    3.x
clsx                   2.x
vitest                 2.x
@testing-library/react 16.x
@testing-library/jest-dom 6.x
@testing-library/user-event 14.x
msw                    2.x
```
