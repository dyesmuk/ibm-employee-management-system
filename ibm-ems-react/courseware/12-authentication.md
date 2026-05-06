# Module 12 — Adding Authentication

## Learning Objectives
- Implement a complete login/logout flow with JWT
- Store and attach tokens securely
- Protect routes so only authenticated users can access them
- Show role-based UI elements
- Apply to EMS: Login page, AuthContext, protected `/employees` routes

---

## 12.1 Auth Flow Overview

```
User visits /employees
        │
        ▼
ProtectedRoute checks token
        │
   ┌────┴────┐
No token   Token found
   │           │
   ▼           ▼
Redirect    Verify token
to /login   with server
               │
          ┌────┴────┐
        Invalid    Valid
           │          │
           ▼          ▼
        Redirect    Set user
        to /login   in context → render page
```

---

## 12.2 Auth Service

```ts
// src/services/authService.ts
import api from './api';

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface AuthUser {
  id: number;
  name: string;
  email: string;
  role: 'admin' | 'user';
  token: string;
}

export const authService = {
  login: async (credentials: LoginCredentials): Promise<AuthUser> => {
    const { data } = await api.post<AuthUser>('/auth/login', credentials);
    return data;
  },

  logout: async (): Promise<void> => {
    await api.post('/auth/logout').catch(() => {}); // fire and forget
  },

  getProfile: async (): Promise<AuthUser> => {
    const { data } = await api.get<AuthUser>('/auth/me');
    return data;
  },

  // Token helpers
  saveToken:   (token: string) => localStorage.setItem('ems_token', token),
  getToken:    ()               => localStorage.getItem('ems_token'),
  removeToken: ()               => localStorage.removeItem('ems_token'),
};
```

### Mock auth (until you have a real backend)

```ts
// src/services/mockAuthService.ts
import { LoginCredentials, AuthUser } from './authService';

const MOCK_USERS = [
  { id: 1, name: 'Admin User',   email: 'admin@ibm.com', password: 'admin123',  role: 'admin' as const },
  { id: 2, name: 'Regular User', email: 'user@ibm.com',  password: 'user123',   role: 'user'  as const },
];

export const mockAuthService = {
  login: async ({ email, password }: LoginCredentials): Promise<AuthUser> => {
    await new Promise(r => setTimeout(r, 600)); // simulate network delay

    const user = MOCK_USERS.find(u => u.email === email && u.password === password);
    if (!user) throw new Error('Invalid email or password');

    const token = `mock-token-${user.id}-${Date.now()}`;
    return { id: user.id, name: user.name, email: user.email, role: user.role, token };
  },

  getProfile: async (): Promise<AuthUser> => {
    await new Promise(r => setTimeout(r, 200));
    const token = localStorage.getItem('ems_token');
    if (!token) throw new Error('No token');
    // In a real app the server validates the token
    // Here we just decode the mock token
    const id = Number(token.split('-')[2]);
    const user = MOCK_USERS.find(u => u.id === id);
    if (!user) throw new Error('Invalid token');
    return { id: user.id, name: user.name, email: user.email, role: user.role, token };
  },
};
```

---

## 12.3 Auth Context

```tsx
// src/context/AuthContext.tsx
import { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react';
import { mockAuthService as authService } from '../services/mockAuthService';
import type { AuthUser, LoginCredentials } from '../services/authService';

interface AuthContextValue {
  user:            AuthUser | null;
  loading:         boolean;       // initial token check
  loginLoading:    boolean;       // login request in flight
  error:           string | null;
  login:           (creds: LoginCredentials) => Promise<void>;
  logout:          () => void;
  isAuthenticated: boolean;
  isAdmin:         boolean;
  clearError:      () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user,         setUser]         = useState<AuthUser | null>(null);
  const [loading,      setLoading]      = useState(true);    // checking saved token
  const [loginLoading, setLoginLoading] = useState(false);
  const [error,        setError]        = useState<string | null>(null);

  // On app load: restore session from saved token
  useEffect(() => {
    const token = localStorage.getItem('ems_token');
    if (!token) { setLoading(false); return; }

    authService.getProfile()
      .then(profile => {
        setUser({ ...profile, token });
      })
      .catch(() => {
        localStorage.removeItem('ems_token'); // stale token
      })
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (creds: LoginCredentials) => {
    setLoginLoading(true);
    setError(null);
    try {
      const authUser = await authService.login(creds);
      localStorage.setItem('ems_token', authUser.token);
      setUser(authUser);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
      throw err; // re-throw so the form can handle it
    } finally {
      setLoginLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('ems_token');
    setUser(null);
    setError(null);
  }, []);

  const clearError = useCallback(() => setError(null), []);

  return (
    <AuthContext.Provider value={{
      user,
      loading,
      loginLoading,
      error,
      login,
      logout,
      isAuthenticated: !!user,
      isAdmin:         user?.role === 'admin',
      clearError,
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be inside <AuthProvider>');
  return ctx;
}
```

```tsx
// src/main.tsx — add AuthProvider
<Provider store={store}>
  <BrowserRouter>
    <ThemeProvider>
      <AuthProvider>
        <App />
      </AuthProvider>
    </ThemeProvider>
  </BrowserRouter>
</Provider>
```

---

## 12.4 Login Page

```tsx
// src/pages/LoginPage.tsx
import { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import styles from './LoginPage.module.css';

function LoginPage() {
  const navigate  = useNavigate();
  const location  = useLocation();
  const { login, loginLoading, error, clearError, isAuthenticated } = useAuth();

  const [email,    setEmail]    = useState('');
  const [password, setPassword] = useState('');
  const [showPw,   setShowPw]   = useState(false);

  // Where to redirect after login
  const from = (location.state as any)?.from?.pathname ?? '/employees';

  // Already logged in?
  useEffect(() => {
    if (isAuthenticated) navigate(from, { replace: true });
  }, [isAuthenticated, from, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await login({ email, password });
      navigate(from, { replace: true });
    } catch {
      // error is already set in AuthContext
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.card}>

        <div className={styles.logo}>
          <span className={styles.logoIcon}>🏢</span>
          <h1 className={styles.logoText}>IBM EMS</h1>
        </div>

        <h2 className={styles.heading}>Sign in to your account</h2>

        {error && (
          <div className={styles.errorBanner} role="alert">
            <span>⚠️ {error}</span>
            <button onClick={clearError} className={styles.dismissBtn} aria-label="Dismiss error">×</button>
          </div>
        )}

        <form onSubmit={handleSubmit} className={styles.form} noValidate>

          <div className={styles.field}>
            <label htmlFor="email" className={styles.label}>Email address</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              className={styles.input}
              placeholder="you@ibm.com"
              autoComplete="email"
              autoFocus
              required
            />
          </div>

          <div className={styles.field}>
            <label htmlFor="password" className={styles.label}>Password</label>
            <div className={styles.pwWrapper}>
              <input
                id="password"
                type={showPw ? 'text' : 'password'}
                value={password}
                onChange={e => setPassword(e.target.value)}
                className={styles.input}
                placeholder="••••••••"
                autoComplete="current-password"
                required
              />
              <button
                type="button"
                className={styles.showPwBtn}
                onClick={() => setShowPw(v => !v)}
                tabIndex={-1}
              >
                {showPw ? '🙈' : '👁️'}
              </button>
            </div>
          </div>

          <button
            type="submit"
            className={styles.submitBtn}
            disabled={loginLoading || !email || !password}
          >
            {loginLoading ? 'Signing in…' : 'Sign In'}
          </button>

        </form>

        {/* Demo credentials hint */}
        <div className={styles.demoHint}>
          <p>Demo credentials:</p>
          <div className={styles.demoRow}>
            <code>admin@ibm.com / admin123</code>
            <button onClick={() => { setEmail('admin@ibm.com'); setPassword('admin123'); }}
              className={styles.fillBtn}>Fill</button>
          </div>
          <div className={styles.demoRow}>
            <code>user@ibm.com / user123</code>
            <button onClick={() => { setEmail('user@ibm.com'); setPassword('user123'); }}
              className={styles.fillBtn}>Fill</button>
          </div>
        </div>

      </div>
    </div>
  );
}
export default LoginPage;
```

```css
/* src/pages/LoginPage.module.css */
.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0043ce 0%, #001141 100%);
  padding: var(--space-5);
}
.card {
  background: white;
  border-radius: var(--radius-lg);
  padding: var(--space-8);
  width: 100%; max-width: 400px;
  box-shadow: 0 24px 48px rgba(0,0,0,0.3);
}
.logo {
  display: flex; align-items: center;
  gap: var(--space-2); margin-bottom: var(--space-6);
}
.logoIcon { font-size: 32px; }
.logoText  { font-size: var(--font-size-xl); font-weight: 700; }
.heading   { font-size: var(--font-size-lg); font-weight: 600; margin-bottom: var(--space-5); }

.errorBanner {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-sm);
  background: var(--color-danger-bg);
  color: var(--color-danger);
  margin-bottom: var(--space-4);
  font-size: var(--font-size-sm);
  border-left: 4px solid var(--color-danger);
}
.dismissBtn { background: none; border: none; color: inherit; font-size: 18px; cursor: pointer; padding: 0; }

.form  { display: flex; flex-direction: column; gap: var(--space-4); }
.field { display: flex; flex-direction: column; gap: var(--space-1); }
.label { font-size: var(--font-size-sm); font-weight: 600; color: var(--color-gray-700); }
.input {
  padding: var(--space-3) var(--space-3);
  border: 1.5px solid var(--color-gray-300);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-md);
  width: 100%;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-light);
}
.pwWrapper { position: relative; }
.pwWrapper .input { padding-right: 44px; }
.showPwBtn {
  position: absolute; right: 10px; top: 50%; transform: translateY(-50%);
  background: none; border: none; cursor: pointer; font-size: 18px;
}

.submitBtn {
  padding: var(--space-3);
  background: var(--color-primary);
  color: white; border: none;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-md);
  font-weight: 700;
  transition: background 0.15s;
  margin-top: var(--space-2);
}
.submitBtn:hover:not(:disabled) { background: var(--color-primary-dark); }
.submitBtn:disabled { background: var(--color-gray-300); cursor: not-allowed; }

.demoHint {
  margin-top: var(--space-6);
  padding: var(--space-3);
  background: var(--color-gray-100);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  color: var(--color-gray-700);
}
.demoHint p { font-weight: 600; margin-bottom: var(--space-2); }
.demoRow    { display: flex; align-items: center; justify-content: space-between; margin-top: 4px; }
.fillBtn    { font-size: 11px; padding: 2px 8px; background: var(--color-primary); color: white; border: none; border-radius: 3px; cursor: pointer; }
```

---

## 12.5 Protected Route Component

```tsx
// src/components/ProtectedRoute.tsx
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface Props {
  children: React.ReactNode;
  adminOnly?: boolean;
}

function ProtectedRoute({ children, adminOnly = false }: Props) {
  const { isAuthenticated, isAdmin, loading } = useAuth();
  const location = useLocation();

  // Still restoring session — show nothing (prevents flash of login page)
  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <p>Restoring session…</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    // Save current location so we can redirect back after login
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (adminOnly && !isAdmin) {
    return <Navigate to="/403" replace />;
  }

  return <>{children}</>;
}
export default ProtectedRoute;
```

---

## 12.6 Updated App Router with Auth

```tsx
// src/App.tsx
import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/layout/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import HomePage from './pages/HomePage';
import EmployeesPage from './pages/EmployeesPage';
import EmployeeDetailPage from './pages/EmployeeDetailPage';
import CreateEmployeePage from './pages/CreateEmployeePage';
import NotFoundPage from './pages/NotFoundPage';

function App() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/login" element={<LoginPage />} />

      {/* Protected — all inside Layout */}
      <Route path="/" element={
        <ProtectedRoute>
          <Layout />
        </ProtectedRoute>
      }>
        <Route index element={<HomePage />} />
        <Route path="employees" element={<EmployeesPage />} />
        <Route path="employees/new" element={<CreateEmployeePage />} />
        <Route path="employees/:id" element={<EmployeeDetailPage />} />

        {/* Admin-only */}
        <Route path="admin" element={
          <ProtectedRoute adminOnly>
            <div><h1>Admin Panel</h1></div>
          </ProtectedRoute>
        } />

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}
export default App;
```

---

## 12.7 User Menu in Navbar

```tsx
// src/components/layout/UserMenu.tsx
import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useOnClickOutside } from '../../hooks/useOnClickOutside';
import styles from './UserMenu.module.css';

function UserMenu() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useOnClickOutside(menuRef, () => setOpen(false));

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!user) return null;

  const initials = user.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);

  return (
    <div className={styles.wrapper} ref={menuRef}>
      <button className={styles.avatar} onClick={() => setOpen(o => !o)} aria-label="Open user menu">
        {initials}
      </button>

      {open && (
        <div className={styles.dropdown}>
          <div className={styles.userInfo}>
            <p className={styles.userName}>{user.name}</p>
            <p className={styles.userEmail}>{user.email}</p>
            {isAdmin && <span className={styles.adminBadge}>Admin</span>}
          </div>
          <hr className={styles.divider} />
          {isAdmin && (
            <button className={styles.item} onClick={() => { navigate('/admin'); setOpen(false); }}>
              ⚙️ Admin Panel
            </button>
          )}
          <button className={styles.item} onClick={handleLogout}>
            🚪 Sign Out
          </button>
        </div>
      )}
    </div>
  );
}
export default UserMenu;
```

---

## 12.8 `useOnClickOutside` Hook

```ts
// src/hooks/useOnClickOutside.ts
import { useEffect, RefObject } from 'react';

export function useOnClickOutside(
  ref: RefObject<HTMLElement | null>,
  handler: () => void
) {
  useEffect(() => {
    const listener = (e: MouseEvent | TouchEvent) => {
      if (!ref.current || ref.current.contains(e.target as Node)) return;
      handler();
    };
    document.addEventListener('mousedown', listener);
    document.addEventListener('touchstart', listener);
    return () => {
      document.removeEventListener('mousedown', listener);
      document.removeEventListener('touchstart', listener);
    };
  }, [ref, handler]);
}
```

---

## Summary

| Topic | Key Point |
|-------|-----------|
| Token storage | `localStorage` (simple) or `httpOnly cookie` (production) |
| Session restore | `useEffect` on mount → call `/auth/me` with saved token |
| AuthContext | `user`, `login`, `logout`, `isAuthenticated`, `isAdmin` |
| ProtectedRoute | Check `isAuthenticated` → redirect to `/login` with `from` state |
| Redirect after login | `navigate(from, { replace: true })` |
| Token in requests | Axios request interceptor adds `Authorization: Bearer <token>` |
| Mock auth | `mockAuthService` — no backend required for development |

**Next → [Module 13: Testing](./13-testing.md)**
