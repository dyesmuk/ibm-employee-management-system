# Module 11 — Redux: Centralized State Management

## Learning Objectives
- Understand when and why to use Redux over local state
- Set up Redux Toolkit (RTK) — the modern, official way
- Create slices with reducers and async thunks
- Write selectors with `createSelector`
- Connect Redux to React components
- Apply to EMS: migrate employee state into a Redux store

---

## 11.1 When to Use Redux

| State type | Where it lives |
|-----------|---------------|
| Local UI state (modal open, input value) | `useState` in component |
| Shared state used by a subtree | `useState` lifted up + props / Context |
| Auth state (user, token) | Context or Redux |
| Server data (employees list) used across many pages | **Redux** |
| Complex state with many actions | **Redux** |
| State that needs DevTools time-travel | **Redux** |

> **Rule of thumb:** If the same data is needed on multiple pages, or if state changes come from many places, Redux is the right choice.

---

## 11.2 Install Redux Toolkit

```bash
npm install @reduxjs/toolkit react-redux
```

RTK is the official, opinionated toolset. It eliminates the massive boilerplate of classic Redux.

---

## 11.3 Architecture Overview

```
┌─────────────────── STORE ─────────────────────────┐
│  state = {                                         │
│    employees: { list, loading, error, filter },    │
│    auth:      { user, token },                     │
│  }                                                 │
└────────────────────────────────────────────────────┘
         ↑ dispatch(action)     ↓ useSelector(fn)
┌─────────────────────────────────────────────────────┐
│                    COMPONENTS                        │
└─────────────────────────────────────────────────────┘
```

**Data flow is strictly one-way:**
1. User interacts → component calls `dispatch(action)`
2. Redux runs the relevant reducer with `(currentState, action) → newState`
3. Store saves new state
4. React re-renders components that select the changed state

---

## 11.4 Store Setup

```ts
// src/store/index.ts
import { configureStore } from '@reduxjs/toolkit';
import employeesReducer from '../features/employees/employeesSlice';
import authReducer from '../features/auth/authSlice';

export const store = configureStore({
  reducer: {
    employees: employeesReducer,
    auth: authReducer,
  },
  // RTK includes redux-thunk by default — no extra setup needed
});

// Infer RootState and AppDispatch types automatically
export type RootState  = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
```

```ts
// src/store/hooks.ts — typed wrappers (always use these, not the plain ones)
import { useDispatch, useSelector } from 'react-redux';
import type { RootState, AppDispatch } from './index';

export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector = <T>(selector: (state: RootState) => T): T =>
  useSelector<RootState, T>(selector);
```

```tsx
// src/main.tsx — provide the store
import { Provider } from 'react-redux';
import { store } from './store';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Provider store={store}>       {/* ← makes store available everywhere */}
      <BrowserRouter>
        <ThemeProvider>
          <App />
        </ThemeProvider>
      </BrowserRouter>
    </Provider>
  </StrictMode>,
);
```

---

## 11.5 Employees Slice

A **slice** is a collection of Redux logic for one feature: initial state + reducers + actions all in one file.

```ts
// src/features/employees/employeesSlice.ts
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { employeeService } from '../../services/employeeService';
import { Employee, Department, CreateEmployeeDto } from '../../types';
import type { RootState } from '../../store';

// ── Async Thunks ──────────────────────────────────────────────────────────────
// createAsyncThunk generates pending / fulfilled / rejected action types automatically

export const fetchEmployees = createAsyncThunk(
  'employees/fetchAll',
  async (_, { rejectWithValue }) => {
    try {
      return await employeeService.getAll();
    } catch (err) {
      return rejectWithValue(err instanceof Error ? err.message : 'Fetch failed');
    }
  }
);

export const createEmployee = createAsyncThunk(
  'employees/create',
  async (dto: CreateEmployeeDto, { rejectWithValue }) => {
    try {
      return await employeeService.create(dto);
    } catch (err) {
      return rejectWithValue(err instanceof Error ? err.message : 'Create failed');
    }
  }
);

export const deleteEmployee = createAsyncThunk(
  'employees/delete',
  async (id: number, { rejectWithValue }) => {
    try {
      await employeeService.remove(id);
      return id;   // return the id so we can remove it from state
    } catch (err) {
      return rejectWithValue(err instanceof Error ? err.message : 'Delete failed');
    }
  }
);

export const updateEmployee = createAsyncThunk(
  'employees/update',
  async ({ id, dto }: { id: number; dto: Partial<CreateEmployeeDto> }, { rejectWithValue }) => {
    try {
      return await employeeService.update(id, dto);
    } catch (err) {
      return rejectWithValue(err instanceof Error ? err.message : 'Update failed');
    }
  }
);

// ── State Shape ───────────────────────────────────────────────────────────────
interface EmployeesState {
  list:        Employee[];
  loading:     boolean;
  error:       string | null;
  filter:      Department;
  search:      string;
  showInactive: boolean;
  selectedId:  number | null;
}

const initialState: EmployeesState = {
  list:         [],
  loading:      false,
  error:        null,
  filter:       'All',
  search:       '',
  showInactive: true,
  selectedId:   null,
};

// ── Slice ─────────────────────────────────────────────────────────────────────
const employeesSlice = createSlice({
  name: 'employees',
  initialState,

  // Synchronous actions
  reducers: {
    setFilter(state, action: PayloadAction<Department>) {
      state.filter = action.payload;
    },
    setSearch(state, action: PayloadAction<string>) {
      state.search = action.payload;
    },
    setShowInactive(state, action: PayloadAction<boolean>) {
      state.showInactive = action.payload;
    },
    setSelectedId(state, action: PayloadAction<number | null>) {
      state.selectedId = action.payload;
    },
    toggleActive(state, action: PayloadAction<number>) {
      // RTK uses Immer under the hood — direct mutation IS safe here!
      const emp = state.list.find(e => e.id === action.payload);
      if (emp) emp.isActive = !emp.isActive;
    },
    clearError(state) {
      state.error = null;
    },
  },

  // Async action handlers
  extraReducers: (builder) => {

    // fetchEmployees
    builder
      .addCase(fetchEmployees.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchEmployees.fulfilled, (state, action) => {
        state.loading = false;
        state.list = action.payload;
      })
      .addCase(fetchEmployees.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // createEmployee
    builder
      .addCase(createEmployee.pending, (state) => { state.loading = true; })
      .addCase(createEmployee.fulfilled, (state, action) => {
        state.loading = false;
        state.list.push(action.payload);  // Immer makes this safe
      })
      .addCase(createEmployee.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });

    // deleteEmployee
    builder
      .addCase(deleteEmployee.fulfilled, (state, action) => {
        state.list = state.list.filter(e => e.id !== action.payload);
      })
      .addCase(deleteEmployee.rejected, (state, action) => {
        state.error = action.payload as string;
      });

    // updateEmployee
    builder
      .addCase(updateEmployee.fulfilled, (state, action) => {
        const idx = state.list.findIndex(e => e.id === action.payload.id);
        if (idx !== -1) state.list[idx] = action.payload;
      });
  },
});

export const {
  setFilter, setSearch, setShowInactive,
  setSelectedId, toggleActive, clearError,
} = employeesSlice.actions;

export default employeesSlice.reducer;

// ── Selectors ─────────────────────────────────────────────────────────────────
// Simple selectors
export const selectAllEmployees  = (s: RootState) => s.employees.list;
export const selectLoading       = (s: RootState) => s.employees.loading;
export const selectError         = (s: RootState) => s.employees.error;
export const selectFilter        = (s: RootState) => s.employees.filter;
export const selectSearch        = (s: RootState) => s.employees.search;
export const selectShowInactive  = (s: RootState) => s.employees.showInactive;
export const selectSelectedId    = (s: RootState) => s.employees.selectedId;
```

---

## 11.6 Memoized Selectors with `createSelector`

```ts
// src/features/employees/employeesSelectors.ts
import { createSelector } from '@reduxjs/toolkit';
import {
  selectAllEmployees, selectFilter,
  selectSearch, selectShowInactive,
} from './employeesSlice';

// Filtered + sorted list — only recalculates when inputs change
export const selectFilteredEmployees = createSelector(
  [selectAllEmployees, selectFilter, selectSearch, selectShowInactive],
  (list, filter, search, showInactive) =>
    list
      .filter(e => filter === 'All' || e.department === filter)
      .filter(e => showInactive || e.isActive)
      .filter(e =>
        e.name.toLowerCase().includes(search.toLowerCase()) ||
        e.email.toLowerCase().includes(search.toLowerCase())
      )
      .sort((a, b) => a.name.localeCompare(b.name))
);

// Stats summary
export const selectStats = createSelector(
  selectAllEmployees,
  (list) => ({
    total:       list.length,
    active:      list.filter(e => e.isActive).length,
    departments: new Set(list.map(e => e.department)).size,
    avgSalary:   list.length
      ? Math.round(list.reduce((s, e) => s + e.salary, 0) / list.length)
      : 0,
  })
);

// Select one employee by id
export const selectEmployeeById = (id: number) =>
  createSelector(selectAllEmployees, list => list.find(e => e.id === id));
```

---

## 11.7 Using Redux in Components

```tsx
// src/pages/EmployeesPage.tsx — refactored to use Redux
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import clsx from 'clsx';
import { useAppDispatch, useAppSelector } from '../store/hooks';
import {
  fetchEmployees,
  deleteEmployee,
  setFilter,
  setSearch,
  setShowInactive,
} from '../features/employees/employeesSlice';
import {
  selectFilteredEmployees,
  selectStats,
} from '../features/employees/employeesSelectors';
import {
  selectLoading,
  selectError,
  selectFilter,
  selectSearch,
  selectShowInactive,
} from '../features/employees/employeesSlice';
import EmployeeCard from '../components/EmployeeCard';
import Spinner from '../components/Spinner';
import ErrorMessage from '../components/ErrorMessage';
import styles from './EmployeesPage.module.css';

const DEPARTMENTS = ['All', 'Engineering', 'Marketing', 'HR', 'Finance', 'Sales'] as const;

function EmployeesPage() {
  const dispatch = useAppDispatch();

  // Read from store — no local state for these anymore
  const employees    = useAppSelector(selectFilteredEmployees);
  const stats        = useAppSelector(selectStats);
  const loading      = useAppSelector(selectLoading);
  const error        = useAppSelector(selectError);
  const filter       = useAppSelector(selectFilter);
  const search       = useAppSelector(selectSearch);
  const showInactive = useAppSelector(selectShowInactive);

  // Fetch on mount
  useEffect(() => {
    dispatch(fetchEmployees());
  }, [dispatch]);

  const handleDelete = (id: number) => {
    dispatch(deleteEmployee(id));
  };

  if (loading && !stats.total) return <Spinner message="Loading employees…" />;

  return (
    <div>
      {error && (
        <ErrorMessage
          message={error}
          onRetry={() => dispatch(fetchEmployees())}
        />
      )}

      {/* Stats */}
      <div style={{ display: 'flex', gap: '16px', marginBottom: '24px' }}>
        {[
          { label: 'Total',       value: stats.total },
          { label: 'Active',      value: stats.active },
          { label: 'Departments', value: stats.departments },
          { label: 'Avg Salary',  value: `$${stats.avgSalary.toLocaleString()}` },
        ].map(s => (
          <div key={s.label} style={{
            background: 'white', border: '1px solid var(--border-color)',
            borderRadius: '8px', padding: '12px 20px', textAlign: 'center',
          }}>
            <div style={{ fontSize: '24px', fontWeight: 700, color: 'var(--color-primary)' }}>
              {s.value}
            </div>
            <div style={{ fontSize: '13px', color: 'var(--color-gray-700)' }}>{s.label}</div>
          </div>
        ))}
      </div>

      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '16px' }}>
        <h1>Employees</h1>
        <Link to="/employees/new" style={{
          padding: '8px 20px', background: 'var(--color-primary)', color: 'white',
          borderRadius: '4px', textDecoration: 'none', fontWeight: 600,
        }}>
          + Add Employee
        </Link>
      </div>

      {/* Search */}
      <input
        value={search}
        onChange={e => dispatch(setSearch(e.target.value))}
        placeholder="🔍 Search name or email…"
        style={{
          width: '100%', padding: '10px 14px', marginBottom: '12px',
          border: '1.5px solid var(--color-gray-300)', borderRadius: '4px', fontSize: '15px',
        }}
      />

      {/* Filters */}
      <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '8px' }}>
        {DEPARTMENTS.map(d => (
          <button
            key={d}
            onClick={() => dispatch(setFilter(d as any))}
            style={{
              padding: '5px 16px', borderRadius: '20px', fontSize: '13px',
              border: '1.5px solid var(--color-primary)', cursor: 'pointer',
              background: filter === d ? 'var(--color-primary)' : 'white',
              color: filter === d ? 'white' : 'var(--color-primary)',
            }}
          >
            {d}
          </button>
        ))}
        <label style={{ marginLeft: 'auto', display: 'flex', gap: '6px', alignItems: 'center', fontSize: '13px' }}>
          <input
            type="checkbox"
            checked={showInactive}
            onChange={e => dispatch(setShowInactive(e.target.checked))}
          />
          Show inactive
        </label>
      </div>

      <p style={{ fontSize: '13px', color: 'var(--color-gray-700)', marginBottom: '16px' }}>
        {employees.length} of {stats.total} employees
      </p>

      {/* Grid */}
      {employees.length > 0
        ? (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px,1fr))', gap: '16px' }}>
            {employees.map(emp => (
              <Link key={emp.id} to={`/employees/${emp.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                <EmployeeCard
                  employee={emp}
                  onRemove={e => { e.preventDefault(); handleDelete(emp.id); }}
                />
              </Link>
            ))}
          </div>
        )
        : <p style={{ textAlign: 'center', padding: '60px', color: 'var(--color-gray-700)' }}>
            No employees match your filters.
          </p>
      }
    </div>
  );
}
export default EmployeesPage;
```

---

## 11.8 Redux DevTools

Install the **Redux DevTools** browser extension:
- [Chrome](https://chrome.google.com/webstore/detail/redux-devtools/lmhkpmbekcpmknklioeibfkpmmfibljd)
- [Firefox](https://addons.mozilla.org/en-US/firefox/addon/reduxdevtools/)

Once installed, open DevTools → **Redux** tab:

```
Action Log                  State Inspector
─────────────────────       ──────────────────────────────
employees/fetchAll/pending  employees:
employees/fetchAll/fulfilled  list: [{id:1, name:"Alice"...}]
employees/setFilter           loading: false
employees/setSearch           filter: "Engineering"
                              search: ""
```

**Time-travel debugging:** Click any past action → state rewinds to that exact point. This is one of Redux's killer features.

---

## 11.9 Redux vs Context — Summary

| | Context | Redux |
|-|---------|-------|
| Setup | Simple | More boilerplate |
| Performance | Re-renders all consumers on change | Granular with selectors |
| DevTools | None | Excellent time-travel |
| Async support | Manual | `createAsyncThunk` |
| Best for | Theme, Auth, small apps | Large apps, complex data flows |

---

## Summary

| Concept | RTK API |
|---------|---------|
| Create store | `configureStore({ reducer: { ... } })` |
| Provide to React | `<Provider store={store}>` |
| Typed hooks | `useAppDispatch`, `useAppSelector` |
| Create slice | `createSlice({ name, initialState, reducers })` |
| Async action | `createAsyncThunk('name', asyncFn)` |
| Handle async | `builder.addCase(thunk.pending/fulfilled/rejected)` |
| Memoized selector | `createSelector([inputs], outputFn)` |

**Next → [Module 12: Authentication](./12-authentication.md)**
