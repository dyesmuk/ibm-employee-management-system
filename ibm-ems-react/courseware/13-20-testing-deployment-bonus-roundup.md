# Module 13 — Testing React Applications

## Learning Objectives
- Set up Vitest + React Testing Library (RTL)
- Write unit tests for utilities and hooks
- Write component tests that simulate user interactions
- Mock API calls with MSW
- Test Redux-connected components

---

## 13.1 Setup

```bash
npm install -D vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event jsdom msw
```

### Configure Vitest

```ts
// vite.config.ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    globals:     true,       // no need to import describe/it/expect
    environment: 'jsdom',    // simulate the browser DOM
    setupFiles:  './src/test/setup.ts',
    css:         true,
  },
});
```

```ts
// src/test/setup.ts
import '@testing-library/jest-dom';
```

```json
// package.json — add scripts
{
  "scripts": {
    "test":          "vitest",
    "test:run":      "vitest run",
    "test:ui":       "vitest --ui",
    "test:coverage": "vitest run --coverage"
  }
}
```

---

## 13.2 Unit Tests — Pure Functions

```ts
// src/utils/formatters.ts
export const formatSalary = (n: number, currency = 'USD') =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(n);

export const getInitials = (name: string) =>
  name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);

export const isValidEmail = (email: string) =>
  /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
```

```ts
// src/utils/formatters.test.ts
import { describe, it, expect } from 'vitest';
import { formatSalary, getInitials, isValidEmail } from './formatters';

describe('formatSalary', () => {
  it('formats a number as USD', () => {
    expect(formatSalary(75000)).toBe('$75,000.00');
  });
  it('formats zero', () => {
    expect(formatSalary(0)).toBe('$0.00');
  });
});

describe('getInitials', () => {
  it('returns two initials for full name', () => {
    expect(getInitials('Alice Johnson')).toBe('AJ');
  });
  it('returns one initial for single name', () => {
    expect(getInitials('Alice')).toBe('A');
  });
});

describe('isValidEmail', () => {
  it('accepts valid emails', () => {
    expect(isValidEmail('alice@ibm.com')).toBe(true);
  });
  it('rejects invalid emails', () => {
    expect(isValidEmail('not-an-email')).toBe(false);
    expect(isValidEmail('@ibm.com')).toBe(false);
  });
});
```

---

## 13.3 Component Tests

```tsx
// src/components/EmployeeCard.test.tsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi, describe, it, expect } from 'vitest';
import EmployeeCard from './EmployeeCard';
import { Employee } from '../types';

const mockEmployee: Employee = {
  id: 1,
  name: 'Alice Johnson',
  email: 'alice@ibm.com',
  username: 'alice',
  phone: '1234567890',
  website: 'alice.com',
  department: 'Engineering',
  salary: 95000,
  isActive: true,
  joinDate: '2021-03-15',
};

describe('EmployeeCard', () => {
  it('renders employee name and department', () => {
    render(<EmployeeCard employee={mockEmployee} onRemove={() => {}} />);

    expect(screen.getByText('Alice Johnson')).toBeInTheDocument();
    expect(screen.getByText(/Engineering/i)).toBeInTheDocument();
  });

  it('shows Active badge for active employee', () => {
    render(<EmployeeCard employee={mockEmployee} onRemove={() => {}} />);
    expect(screen.getByText('Active', { exact: false })).toBeInTheDocument();
  });

  it('shows Inactive badge for inactive employee', () => {
    render(<EmployeeCard employee={{ ...mockEmployee, isActive: false }} onRemove={() => {}} />);
    expect(screen.getByText('Inactive', { exact: false })).toBeInTheDocument();
  });

  it('calls onRemove with correct id when × is clicked', async () => {
    const user = userEvent.setup();
    const onRemove = vi.fn();

    render(<EmployeeCard employee={mockEmployee} onRemove={onRemove} />);

    await user.click(screen.getByRole('button', { name: /remove alice/i }));

    expect(onRemove).toHaveBeenCalledTimes(1);
    expect(onRemove).toHaveBeenCalledWith(1);
  });

  it('formats salary with dollar sign', () => {
    render(<EmployeeCard employee={mockEmployee} onRemove={() => {}} />);
    expect(screen.getByText(/\$95,000/)).toBeInTheDocument();
  });
});
```

---

## 13.4 Testing Forms

```tsx
// src/pages/CreateEmployeePage.test.tsx
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import CreateEmployeePage from './CreateEmployeePage';

// Wrap with router (page uses useNavigate)
const renderPage = () =>
  render(
    <MemoryRouter>
      <CreateEmployeePage />
    </MemoryRouter>
  );

describe('CreateEmployeePage', () => {
  it('shows validation errors when submitted empty', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(screen.getByRole('button', { name: /create employee/i }));

    await waitFor(() => {
      expect(screen.getByText(/name is required/i)).toBeInTheDocument();
      expect(screen.getByText(/email is required/i)).toBeInTheDocument();
    });
  });

  it('shows email error for invalid email', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.type(screen.getByLabelText(/full name/i), 'Alice');
    await user.type(screen.getByLabelText(/email/i), 'not-an-email');
    await user.tab(); // trigger blur

    expect(await screen.findByText(/valid email/i)).toBeInTheDocument();
  });

  it('enables submit when form is valid', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.type(screen.getByLabelText(/full name/i), 'Alice Johnson');
    await user.type(screen.getByLabelText(/email/i), 'alice@ibm.com');
    await user.type(screen.getByLabelText(/phone/i), '+1234567890');
    await user.type(screen.getByLabelText(/salary/i), '75000');
    await user.type(screen.getByLabelText(/join date/i), '2021-01-01');

    const submitBtn = screen.getByRole('button', { name: /create employee/i });
    expect(submitBtn).not.toBeDisabled();
  });
});
```

---

## 13.5 MSW — Mock Service Worker

MSW intercepts network requests at the network level. Your components run exactly as in production — they just get mock data.

```ts
// src/test/mocks/handlers.ts
import { http, HttpResponse } from 'msw';
import { Employee } from '../../types';

const mockEmployees: Employee[] = [
  { id: 1, name: 'Alice Johnson', email: 'alice@ibm.com', username: 'alice',
    phone: '123', website: 'alice.com', department: 'Engineering',
    salary: 95000, isActive: true, joinDate: '2021-03-15' },
];

export const handlers = [
  http.get('*/users', () => HttpResponse.json(mockEmployees)),
  http.get('*/users/:id', ({ params }) => {
    const emp = mockEmployees.find(e => e.id === Number(params.id));
    if (!emp) return new HttpResponse(null, { status: 404 });
    return HttpResponse.json(emp);
  }),
  http.post('*/users', async ({ request }) => {
    const body = await request.json() as Partial<Employee>;
    return HttpResponse.json({ ...body, id: 99 }, { status: 201 });
  }),
  http.delete('*/users/:id', () => new HttpResponse(null, { status: 204 })),
];
```

```ts
// src/test/mocks/server.ts
import { setupServer } from 'msw/node';
import { handlers } from './handlers';

export const server = setupServer(...handlers);
```

```ts
// src/test/setup.ts — add server lifecycle
import '@testing-library/jest-dom';
import { server } from './mocks/server';

beforeAll(()  => server.listen({ onUnhandledRequest: 'warn' }));
afterEach(()  => server.resetHandlers());
afterAll(()   => server.close());
```

---

## 13.6 Testing Redux Store

```tsx
// src/test/renderWithProviders.tsx
import { ReactElement } from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router-dom';
import { configureStore } from '@reduxjs/toolkit';
import employeesReducer from '../features/employees/employeesSlice';
import authReducer from '../features/auth/authSlice';

export function renderWithProviders(
  ui: ReactElement,
  {
    preloadedState = {},
    route = '/',
    ...renderOptions
  }: RenderOptions & { preloadedState?: Record<string, unknown>; route?: string } = {}
) {
  const testStore = configureStore({
    reducer: { employees: employeesReducer, auth: authReducer },
    preloadedState,
  });

  function Wrapper({ children }: { children: React.ReactNode }) {
    return (
      <Provider store={testStore}>
        <MemoryRouter initialEntries={[route]}>
          {children}
        </MemoryRouter>
      </Provider>
    );
  }

  return { store: testStore, ...render(ui, { wrapper: Wrapper, ...renderOptions }) };
}
```

```tsx
// src/pages/EmployeesPage.test.tsx
import { screen, waitFor } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import EmployeesPage from './EmployeesPage';

describe('EmployeesPage', () => {
  it('fetches and displays employees', async () => {
    renderWithProviders(<EmployeesPage />);

    // Initially shows spinner
    expect(screen.getByText(/loading/i)).toBeInTheDocument();

    // After fetch completes
    await waitFor(() => {
      expect(screen.getByText('Alice Johnson')).toBeInTheDocument();
    });
  });

  it('shows empty state when no employees match filter', async () => {
    renderWithProviders(<EmployeesPage />, {
      preloadedState: {
        employees: {
          list: [],
          loading: false,
          error: null,
          filter: 'All',
          search: '',
          showInactive: true,
          selectedId: null,
        },
      },
    });

    expect(screen.getByText(/no employees/i)).toBeInTheDocument();
  });
});
```

---

## 13.7 Coverage

```bash
npm run test:coverage
```

Add coverage thresholds:
```ts
// vite.config.ts
test: {
  coverage: {
    provider: 'v8',
    reporter: ['text', 'html', 'json'],
    include: ['src/**/*.{ts,tsx}'],
    exclude: ['src/test/**', 'src/**/*.d.ts', 'src/main.tsx'],
    thresholds: {
      statements: 80,
      branches:   75,
      functions:  80,
      lines:      80,
    },
  },
}
```

---

## Summary

| Tool | Purpose |
|------|---------|
| Vitest | Fast, Vite-native test runner |
| RTL `render` | Mount components in jsdom |
| RTL `screen` | Query the DOM by role, text, label |
| `userEvent` | Realistic keyboard and mouse interactions |
| `vi.fn()` | Mock functions, track calls |
| MSW | Intercept HTTP at network level |
| `renderWithProviders` | Utility that wraps with Redux + Router |

**Next → [Module 14: Deployment](./14-deployment.md)**

---

# Module 14 — Deploying the App to the Web

## 14.1 Production Build

```bash
npm run build
```

Output in `dist/`:
```
dist/
├── index.html
└── assets/
    ├── index-[hash].js     ← all JS bundled + minified
    └── index-[hash].css    ← all CSS bundled
```

Preview locally: `npm run preview` → http://localhost:4173

---

## 14.2 Environment Variables for Production

```bash
# .env.production
VITE_API_BASE_URL=https://api.ibm-ems.com/v1
```

---

## 14.3 Code Splitting

Split bundles so users only download what they need:

```tsx
// src/App.tsx
import { lazy, Suspense } from 'react';
import Spinner from './components/Spinner';

const EmployeesPage    = lazy(() => import('./pages/EmployeesPage'));
const EmployeeDetail   = lazy(() => import('./pages/EmployeeDetailPage'));
const CreateEmployee   = lazy(() => import('./pages/CreateEmployeePage'));

function App() {
  return (
    <Suspense fallback={<Spinner />}>
      <Routes>
        <Route path="employees" element={<EmployeesPage />} />
        <Route path="employees/:id" element={<EmployeeDetail />} />
        <Route path="employees/new" element={<CreateEmployee />} />
      </Routes>
    </Suspense>
  );
}
```

---

## 14.4 Deploy to Vercel

```bash
npm i -g vercel
vercel login
vercel --prod
```

**SPA redirect config** (without this, `/employees/1` gives 404 on refresh):

```json
// vercel.json
{
  "rewrites": [{ "source": "/(.*)", "destination": "/" }]
}
```

---

## 14.5 Deploy to Netlify

```bash
npm run build
npx netlify deploy --prod --dir dist
```

```toml
# public/_redirects
/*    /index.html   200
```

---

## 14.6 Docker

```dockerfile
# Dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

```nginx
# nginx.conf
server {
  listen 80;
  root /usr/share/nginx/html;
  index index.html;
  location / { try_files $uri $uri/ /index.html; }
  location /assets/ { expires 1y; add_header Cache-Control "public, immutable"; }
}
```

```bash
docker build -t ibm-ems-app .
docker run -p 80:80 ibm-ems-app
```

---

## 14.7 GitHub Actions CI/CD

```yaml
# .github/workflows/deploy.yml
name: Build & Deploy

on:
  push:
    branches: [main]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'

      - run: npm ci
      - run: npm test -- --run
      - run: npm run build
        env:
          VITE_API_BASE_URL: ${{ secrets.VITE_API_BASE_URL }}

      - name: Deploy to Vercel
        uses: amondnet/vercel-action@v25
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id:     ${{ secrets.ORG_ID }}
          vercel-project-id: ${{ secrets.PROJECT_ID }}
          vercel-args: '--prod'
```

---

# Module 15 — Bonus: Working with Webpack

Vite uses Rollup under the hood for production builds. Understanding Webpack is still valuable as many enterprise codebases use it.

## 15.1 Core Concepts

| Concept | Description |
|---------|-------------|
| **Entry** | Starting file (`src/index.tsx`) |
| **Output** | Where bundled files go (`dist/`) |
| **Loaders** | Transform non-JS files (CSS, images, TypeScript) |
| **Plugins** | Extra build capabilities (HTML injection, env vars) |
| **Mode** | `development` or `production` (affects optimization) |

## 15.2 Minimal Webpack Config for React + TypeScript

```bash
npm install -D webpack webpack-cli webpack-dev-server
npm install -D babel-loader @babel/core @babel/preset-env @babel/preset-react @babel/preset-typescript
npm install -D html-webpack-plugin css-loader style-loader mini-css-extract-plugin
npm install -D ts-loader typescript
```

```js
// webpack.config.js
const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

const isDev = process.env.NODE_ENV !== 'production';

module.exports = {
  entry: './src/main.tsx',

  output: {
    path: path.resolve(__dirname, 'dist'),
    filename: '[name].[contenthash].js',
    publicPath: '/',
    clean: true,
  },

  mode: isDev ? 'development' : 'production',
  devtool: isDev ? 'eval-source-map' : 'source-map',

  resolve: {
    extensions: ['.tsx', '.ts', '.js'],
    alias: { '@': path.resolve(__dirname, 'src') },
  },

  module: {
    rules: [
      // TypeScript + JSX
      {
        test: /\.(ts|tsx)$/,
        use: 'ts-loader',
        exclude: /node_modules/,
      },
      // CSS Modules
      {
        test: /\.module\.css$/,
        use: [
          isDev ? 'style-loader' : MiniCssExtractPlugin.loader,
          { loader: 'css-loader', options: { modules: true } },
        ],
      },
      // Global CSS
      {
        test: /\.css$/,
        exclude: /\.module\.css$/,
        use: [
          isDev ? 'style-loader' : MiniCssExtractPlugin.loader,
          'css-loader',
        ],
      },
      // Images
      { test: /\.(png|jpg|svg)$/, type: 'asset/resource' },
    ],
  },

  plugins: [
    new HtmlWebpackPlugin({ template: './index.html' }),
    !isDev && new MiniCssExtractPlugin({ filename: '[name].[contenthash].css' }),
  ].filter(Boolean),

  devServer: {
    port: 3000,
    hot: true,
    historyApiFallback: true,   // SPA routing
    open: true,
  },

  optimization: {
    splitChunks: {
      chunks: 'all',
      cacheGroups: {
        vendor: {
          test: /[\\/]node_modules[\\/]/,
          name: 'vendors',
        },
      },
    },
  },
};
```

## 15.3 Vite vs Webpack

| | Vite | Webpack |
|-|------|---------|
| Dev server startup | < 1s (ESM, no bundle) | 10-30s (full bundle) |
| HMR speed | Near-instant | Seconds |
| Config complexity | Minimal | Extensive |
| Ecosystem | Growing | Vast, mature |
| Enterprise adoption | Growing | Dominant |
| Custom loaders | Plugin API | Loader API |

---

# Module 16 — Bonus: Next.js

## 16.1 What Next.js Adds to React

| Feature | React (Vite) | Next.js |
|---------|-------------|---------|
| Rendering | Client-side only | CSR + SSR + SSG + ISR |
| Routing | Manual (React Router) | File-based (automatic) |
| API Routes | Separate backend needed | Built-in |
| SEO | Poor (empty HTML shell) | Excellent |
| Images | `<img>` | Optimized `<Image>` |
| Fonts | Manual | Automatic optimization |

## 16.2 Create a Next.js App

```bash
npx create-next-app@latest ibm-ems-next --typescript --tailwind --eslint --app
```

## 16.3 File-Based Routing

```
app/
├── layout.tsx               ← Root layout (wraps everything)
├── page.tsx                 ← / (home)
├── employees/
│   ├── page.tsx             ← /employees
│   ├── loading.tsx          ← shown while page loads
│   ├── error.tsx            ← shown on error
│   └── [id]/
│       └── page.tsx         ← /employees/:id
└── api/
    └── employees/
        └── route.ts         ← API: GET/POST /api/employees
```

## 16.4 Server Component (default in App Router)

```tsx
// app/employees/page.tsx — Server Component: runs on server, zero JS to client
async function EmployeesPage() {
  // Direct fetch — no useEffect, no loading state
  const employees = await fetch('https://jsonplaceholder.typicode.com/users', {
    next: { revalidate: 60 }, // ISR: revalidate every 60 seconds
  }).then(r => r.json());

  return (
    <ul>
      {employees.map((e: any) => (
        <li key={e.id}>{e.name}</li>
      ))}
    </ul>
  );
}
export default EmployeesPage;
```

## 16.5 Client Component

```tsx
'use client'; // ← marks as client component

import { useState } from 'react';

function SearchBar({ onSearch }: { onSearch: (q: string) => void }) {
  const [query, setQuery] = useState('');
  return (
    <input
      value={query}
      onChange={e => { setQuery(e.target.value); onSearch(e.target.value); }}
    />
  );
}
```

## 16.6 Server Actions

```ts
// app/employees/actions.ts
'use server';
import { revalidatePath } from 'next/cache';

export async function createEmployee(formData: FormData) {
  const name = formData.get('name') as string;
  await fetch('/api/employees', {
    method: 'POST', body: JSON.stringify({ name }),
    headers: { 'Content-Type': 'application/json' },
  });
  revalidatePath('/employees');
}
```

```tsx
// app/employees/page.tsx
import { createEmployee } from './actions';

export default function Page() {
  return (
    <form action={createEmployee}>
      <input name="name" required />
      <button type="submit">Add</button>
    </form>
  );
}
```

---

# Module 17 — Bonus: Animations in React Apps

## 17.1 CSS Transitions (No Library)

```css
/* Works automatically — just add transition */
.card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.2);
}

/* Fade-in on mount */
.fadeIn {
  animation: fadeIn 0.3s ease-in;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}
```

## 17.2 Framer Motion

```bash
npm install framer-motion
```

```tsx
import { motion, AnimatePresence } from 'framer-motion';

// Fade-in card on mount
function EmployeeCard({ employee }: Props) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, scale: 0.95 }}
      transition={{ duration: 0.25 }}
      whileHover={{ y: -4, boxShadow: '0 8px 24px rgba(0,0,0,0.15)' }}
      layout                              // animate reflow (filter/sort)
    >
      {/* card content */}
    </motion.div>
  );
}

// Animate list add/remove
function EmployeeList({ employees }: { employees: Employee[] }) {
  return (
    <AnimatePresence mode="popLayout">
      {employees.map(emp => (
        <motion.div
          key={emp.id}
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          exit={{ opacity: 0, x: 20 }}
          layout
        >
          <EmployeeCard employee={emp} />
        </motion.div>
      ))}
    </AnimatePresence>
  );
}

// Page transition
const pageVariants = {
  initial: { opacity: 0, x: -10 },
  animate: { opacity: 1, x: 0 },
  exit:    { opacity: 0, x: 10 },
};

function AnimatedPage({ children }: { children: React.ReactNode }) {
  return (
    <motion.div
      variants={pageVariants}
      initial="initial"
      animate="animate"
      exit="exit"
      transition={{ duration: 0.2 }}
    >
      {children}
    </motion.div>
  );
}
```

---

# Module 18 — Bonus: A Brief Introduction to Redux Saga

## 18.1 What is Redux Saga?

Redux Saga handles complex async side effects as **sagas** — generator functions that can be paused, cancelled, forked, and tested in isolation.

```bash
npm install redux-saga
```

## 18.2 Generator Functions

```ts
function* counter() {
  yield 1;
  yield 2;
  yield 3;
}

const gen = counter();
gen.next(); // { value: 1, done: false }
gen.next(); // { value: 2, done: false }
gen.next(); // { value: 3, done: true  }
```

## 18.3 Saga vs Thunk

| Scenario | Thunk | Saga |
|----------|-------|------|
| Simple API call | ✅ simple | overkill |
| Sequence of API calls | messy | ✅ clean |
| Cancel in-flight request | hard | ✅ `take`, `cancel` |
| Polling every N seconds | hard | ✅ `delay` loop |
| Race two calls, take first | hard | ✅ `race` |
| Testing async logic | hard to isolate | ✅ pure side effects |

## 18.4 Basic Saga for EMS

```ts
// src/sagas/employeesSaga.ts
import { call, put, takeLatest, takeEvery } from 'redux-saga/effects';
import { employeeService } from '../services/employeeService';
import {
  fetchEmployees, createEmployee,
} from '../features/employees/employeesSlice';

// Worker: does the actual async work
function* fetchEmployeesSaga() {
  try {
    const data: Employee[] = yield call(employeeService.getAll);
    yield put(fetchEmployees.fulfilled(data, '', undefined));
  } catch (err) {
    yield put(fetchEmployees.rejected(null, '', undefined, String(err)));
  }
}

function* createEmployeeSaga(action: ReturnType<typeof createEmployee.pending>) {
  try {
    const emp: Employee = yield call(employeeService.create, action.meta.arg);
    yield put(createEmployee.fulfilled(emp, '', action.meta.arg));
  } catch (err) {
    yield put(createEmployee.rejected(null, '', action.meta.arg, String(err)));
  }
}

// Watcher: listens for dispatched actions
export function* watchEmployees() {
  // takeLatest: cancels previous saga if same action dispatched again
  yield takeLatest(fetchEmployees.pending.type, fetchEmployeesSaga);
  yield takeEvery(createEmployee.pending.type, createEmployeeSaga);
}
```

```ts
// src/sagas/rootSaga.ts
import { all } from 'redux-saga/effects';
import { watchEmployees } from './employeesSaga';

export function* rootSaga() {
  yield all([
    watchEmployees(),
  ]);
}
```

```ts
// src/store/index.ts — add saga middleware
import createSagaMiddleware from 'redux-saga';
import { rootSaga } from '../sagas/rootSaga';

const sagaMiddleware = createSagaMiddleware();

export const store = configureStore({
  reducer: { employees: employeesReducer, auth: authReducer },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({ thunk: false }).concat(sagaMiddleware),
});

sagaMiddleware.run(rootSaga);
```

---

# Module 19 — React Hooks: Complete Reference

## All Built-in Hooks

| Hook | Purpose |
|------|---------|
| `useState` | Local component state |
| `useEffect` | Side effects, subscriptions, cleanup |
| `useContext` | Read context value |
| `useReducer` | Complex state with actions |
| `useRef` | DOM refs + mutable values |
| `useMemo` | Memoize computed values |
| `useCallback` | Memoize functions |
| `useId` | Stable unique ID (React 18+) |
| `useTransition` | Mark non-urgent updates (React 18+) |
| `useDeferredValue` | Defer heavy renders (React 18+) |
| `use` | Consume promises/context in render (React 19) |
| `useOptimistic` | Optimistic UI updates (React 19) |
| `useActionState` | Form action state (React 19) |

## `useReducer` — Complex State

```tsx
type Action =
  | { type: 'increment' }
  | { type: 'decrement' }
  | { type: 'reset' }
  | { type: 'setTo'; payload: number };

function reducer(state: number, action: Action): number {
  switch (action.type) {
    case 'increment': return state + 1;
    case 'decrement': return Math.max(0, state - 1);
    case 'reset':     return 0;
    case 'setTo':     return action.payload;
    default:          return state;
  }
}

function Counter() {
  const [count, dispatch] = useReducer(reducer, 0);
  return (
    <div>
      <p>{count}</p>
      <button onClick={() => dispatch({ type: 'increment' })}>+</button>
      <button onClick={() => dispatch({ type: 'setTo', payload: 10 })}>Set 10</button>
    </div>
  );
}
```

## React 19 — `useOptimistic`

```tsx
import { useOptimistic } from 'react';

function EmployeeList({ employees, onDelete }: Props) {
  const [optimisticList, deleteOptimistic] = useOptimistic(
    employees,
    (current, deletedId: number) => current.filter(e => e.id !== deletedId)
  );

  const handleDelete = async (id: number) => {
    deleteOptimistic(id);         // update UI immediately
    await employeeService.remove(id); // then call the real API
    // If API fails, optimistic update is automatically rolled back
  };

  return (
    <ul>
      {optimisticList.map(emp => (
        <li key={emp.id}>
          {emp.name}
          <button onClick={() => handleDelete(emp.id)}>Delete</button>
        </li>
      ))}
    </ul>
  );
}
```

## React 19 — `useActionState`

```tsx
import { useActionState } from 'react';

async function createEmployeeAction(
  prevState: { error: string | null; success: boolean },
  formData: FormData
) {
  const name = formData.get('name') as string;
  if (!name) return { error: 'Name is required', success: false };
  await employeeService.create({ name, email: '', department: 'Engineering', salary: 70000 });
  return { error: null, success: true };
}

function CreateForm() {
  const [state, formAction, isPending] = useActionState(
    createEmployeeAction,
    { error: null, success: false }
  );

  return (
    <form action={formAction}>
      <input name="name" />
      {state.error && <p>{state.error}</p>}
      {state.success && <p>Created!</p>}
      <button type="submit" disabled={isPending}>
        {isPending ? 'Creating…' : 'Create'}
      </button>
    </form>
  );
}
```

---

# Module 20 — Next Steps and Course Roundup

## What You've Built

Starting from `npm create vite@latest`, you built a production-ready **IBM Employee Management System** with:

| Feature | Module |
|---------|--------|
| Component composition + JSX | 02, 03 |
| Employee list, filter, search | 03, 04 |
| CSS Modules design system | 05 |
| React DevTools + Error Boundaries | 06 |
| Custom hooks, Context, useMemo | 07 |
| Axios API layer | 08 |
| Multi-page routing | 09 |
| Form with full validation | 10 |
| Redux state management | 11 |
| Login/Logout, protected routes | 12 |
| Unit + integration tests | 13 |
| Production deployment | 14 |

## Next Topics to Explore

### Immediate (1-3 months)
- **TanStack Query** — better data fetching than useEffect + useState
- **Storybook** — build and document UI components in isolation
- **TypeScript generics** — deeper TypeScript patterns

### Intermediate (3-6 months)
- **React Performance** — Profiler, `startTransition`, `useDeferredValue`
- **Web Accessibility (a11y)** — ARIA, keyboard nav, screen readers
- **Playwright or Cypress** — end-to-end testing

### Advanced (6+ months)
- **Micro-frontends** — Module Federation
- **GraphQL + Apollo Client** — alternative to REST
- **React Server Components** (Next.js 14+)

## Ecosystem Map

```
Data Fetching    → TanStack Query, SWR
State            → Zustand (simple), Jotai (atomic), Redux Toolkit (complex)
Forms            → React Hook Form + Zod (our choice), Formik
Routing          → React Router (our choice), TanStack Router
Styling          → CSS Modules (our choice), Tailwind, styled-components
Animation        → Framer Motion (our choice), React Spring
UI Components    → Radix UI, shadcn/ui, MUI, Ant Design
Testing          → Vitest + RTL (our choice), Jest, Playwright
Build            → Vite (our choice), Turbopack, Webpack
Framework        → Next.js, Remix, TanStack Start
Deployment       → Vercel, Netlify, AWS, Docker
```

## Project Ideas to Build Next

| Project | New skills practiced |
|---------|---------------------|
| Kanban board (Trello clone) | Drag-and-drop, nested context |
| Real-time chat | WebSockets, optimistic updates |
| E-commerce store | Cart, Stripe payments, orders |
| Data dashboard | Recharts, CSV export, filters |
| Blog with CMS | Next.js SSG, MDX, slug routing |

---

## Final Project Structure

```
ibm-ems-app/
├── public/
├── src/
│   ├── components/
│   │   ├── layout/        Navbar, Layout, UserMenu
│   │   ├── EmployeeCard   (+ module.css)
│   │   ├── ErrorBoundary
│   │   ├── ErrorMessage
│   │   └── Spinner
│   ├── context/
│   │   ├── AuthContext
│   │   └── ThemeContext
│   ├── data/              seed employees
│   ├── features/
│   │   ├── employees/     slice, selectors
│   │   └── auth/          slice
│   ├── hooks/
│   │   ├── useDebounce
│   │   ├── useEmployees
│   │   ├── useLocalStorage
│   │   └── useOnClickOutside
│   ├── pages/
│   │   ├── HomePage
│   │   ├── LoginPage
│   │   ├── EmployeesPage
│   │   ├── EmployeeDetailPage
│   │   ├── CreateEmployeePage
│   │   └── NotFoundPage
│   ├── schemas/           Zod schemas
│   ├── services/          api.ts, authService, employeeService
│   ├── store/             index.ts, hooks.ts
│   ├── test/              setup.ts, mocks/, renderWithProviders
│   ├── types/             index.ts
│   ├── utils/             formatters.ts
│   ├── App.tsx
│   ├── index.css
│   └── main.tsx
├── .env
├── .env.production
├── vite.config.ts
├── tsconfig.json
└── package.json
```

---

## Thank You & Happy Coding 🚀

> "The best way to learn React is to build something real."

Every concept in this course is actively used in production React applications today. The EMS project you've built demonstrates patterns used at companies like IBM, Meta, Airbnb, and Netflix.

### Resources
- [react.dev](https://react.dev) — Official React docs (excellent)
- [Redux Toolkit](https://redux-toolkit.js.org) — RTK docs
- [React Router](https://reactrouter.com) — Router docs
- [Zod](https://zod.dev) — Schema validation docs
- [Testing Library](https://testing-library.com) — RTL docs
- [Framer Motion](https://www.framer.com/motion) — Animation docs
- [Next.js](https://nextjs.org/docs) — Next.js docs
