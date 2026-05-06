# Module 10 — Forms and Form Validation

## Learning Objectives
- Build controlled forms with all input types
- Write manual validation with touched/error state
- Use React Hook Form + Zod for production-grade forms
- Apply to EMS: Create Employee and Edit Employee forms with full validation

---

## 10.1 Controlled vs Uncontrolled Inputs

### Controlled (React owns the value — recommended)

```tsx
const [name, setName] = useState('');

<input
  value={name}                         // React controls the value
  onChange={e => setName(e.target.value)}
/>
```

### Uncontrolled (DOM owns the value)

```tsx
const inputRef = useRef<HTMLInputElement>(null);

// Read value only when needed (e.g. on submit)
const value = inputRef.current?.value;

<input ref={inputRef} defaultValue="" />
```

**Use controlled inputs** for anything where you need:
- Real-time validation
- Derived values (format as user types)
- Resetting the form programmatically

---

## 10.2 All Input Types — Controlled

```tsx
function AllInputs() {
  const [text,     setText]     = useState('');
  const [email,    setEmail]    = useState('');
  const [password, setPassword] = useState('');
  const [number,   setNumber]   = useState(0);
  const [date,     setDate]     = useState('');
  const [checked,  setChecked]  = useState(false);
  const [radio,    setRadio]    = useState('male');
  const [select,   setSelect]   = useState('Engineering');
  const [textarea, setTextarea] = useState('');

  return (
    <form>
      {/* Text */}
      <input type="text"     value={text}     onChange={e => setText(e.target.value)} />

      {/* Email */}
      <input type="email"    value={email}    onChange={e => setEmail(e.target.value)} />

      {/* Password */}
      <input type="password" value={password} onChange={e => setPassword(e.target.value)} />

      {/* Number */}
      <input type="number" value={number}
        onChange={e => setNumber(Number(e.target.value))} min={0} />

      {/* Date */}
      <input type="date" value={date} onChange={e => setDate(e.target.value)} />

      {/* Checkbox */}
      <input type="checkbox" checked={checked}
        onChange={e => setChecked(e.target.checked)} />

      {/* Radio group */}
      {['male', 'female', 'other'].map(val => (
        <label key={val}>
          <input type="radio" value={val}
            checked={radio === val} onChange={() => setRadio(val)} />
          {val}
        </label>
      ))}

      {/* Select */}
      <select value={select} onChange={e => setSelect(e.target.value)}>
        <option value="">Select…</option>
        <option value="Engineering">Engineering</option>
        <option value="HR">HR</option>
      </select>

      {/* Textarea */}
      <textarea value={textarea} onChange={e => setTextarea(e.target.value)} rows={4} />
    </form>
  );
}
```

---

## 10.3 Manual Validation Pattern

Three pieces of state work together:

```
values  — the current form field values
errors  — validation error messages per field (empty = valid)
touched — which fields the user has interacted with (blur)

Show error only when:  touched[field] === true  AND  errors[field] exists
```

### Employee Create Form — manual validation

```tsx
// src/pages/CreateEmployeePage.tsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { employeeService } from '../services/employeeService';
import styles from './CreateEmployeePage.module.css';

type Dept = 'Engineering' | 'Marketing' | 'HR' | 'Finance' | 'Sales';

interface FormValues {
  name:       string;
  email:      string;
  phone:      string;
  department: Dept;
  salary:     string;  // string while typing, convert to number on submit
  isActive:   boolean;
  joinDate:   string;
  notes:      string;
}

interface FormErrors {
  name?:       string;
  email?:      string;
  phone?:      string;
  department?: string;
  salary?:     string;
  joinDate?:   string;
}

const INITIAL: FormValues = {
  name: '', email: '', phone: '',
  department: 'Engineering', salary: '',
  isActive: true, joinDate: '', notes: '',
};

// Pure validation function — easy to unit-test
function validate(values: FormValues): FormErrors {
  const e: FormErrors = {};

  if (!values.name.trim())
    e.name = 'Name is required';
  else if (values.name.trim().length < 2)
    e.name = 'Name must be at least 2 characters';

  if (!values.email.trim())
    e.email = 'Email is required';
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email))
    e.email = 'Enter a valid email address';

  if (!values.phone.trim())
    e.phone = 'Phone is required';
  else if (values.phone.replace(/\D/g, '').length < 7)
    e.phone = 'Phone must have at least 7 digits';

  if (!values.salary)
    e.salary = 'Salary is required';
  else if (Number(values.salary) < 10000)
    e.salary = 'Salary must be at least $10,000';
  else if (Number(values.salary) > 10_000_000)
    e.salary = 'Salary seems unrealistically high';

  if (!values.joinDate)
    e.joinDate = 'Join date is required';
  else if (new Date(values.joinDate) > new Date())
    e.joinDate = 'Join date cannot be in the future';

  return e;
}

function CreateEmployeePage() {
  const navigate = useNavigate();
  const [values,     setValues]     = useState<FormValues>(INITIAL);
  const [errors,     setErrors]     = useState<FormErrors>({});
  const [touched,    setTouched]    = useState<Partial<Record<keyof FormValues, boolean>>>({});
  const [submitting, setSubmitting] = useState(false);
  const [apiError,   setApiError]   = useState<string | null>(null);

  // Generic change handler — works for all input types
  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value, type } = e.target;
    const checked = (e.target as HTMLInputElement).checked;

    const updated = {
      ...values,
      [name]: type === 'checkbox' ? checked : value,
    };
    setValues(updated);

    // Validate in real-time once a field has been touched
    if (touched[name as keyof FormValues]) {
      setErrors(validate(updated));
    }
  };

  const handleBlur = (
    e: React.FocusEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const field = e.target.name as keyof FormValues;
    setTouched(prev => ({ ...prev, [field]: true }));
    setErrors(validate(values));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Mark ALL fields touched so every error shows
    const allTouched = Object.keys(values).reduce(
      (acc, k) => ({ ...acc, [k]: true }),
      {} as Record<keyof FormValues, boolean>
    );
    setTouched(allTouched);

    const errs = validate(values);
    setErrors(errs);

    if (Object.keys(errs).length > 0) return; // stop if invalid

    setSubmitting(true);
    setApiError(null);
    try {
      await employeeService.create({
        name:       values.name.trim(),
        email:      values.email.trim().toLowerCase(),
        department: values.department,
        salary:     Number(values.salary),
      });
      navigate('/employees', { state: { message: `${values.name} added successfully!` } });
    } catch (err) {
      setApiError(err instanceof Error ? err.message : 'Failed to create employee');
    } finally {
      setSubmitting(false);
    }
  };

  // Helper: should we show an error for this field?
  const showError = (field: keyof FormErrors) =>
    touched[field] && errors[field];

  const isFormValid = Object.keys(validate(values)).length === 0;

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Add New Employee</h1>

      {apiError && (
        <div className={styles.apiBanner}>{apiError}</div>
      )}

      <form className={styles.form} onSubmit={handleSubmit} noValidate>

        {/* ── Name ── */}
        <div className={styles.field}>
          <label htmlFor="name" className={styles.label}>Full Name <span className={styles.required}>*</span></label>
          <input
            id="name" name="name" type="text"
            value={values.name}
            onChange={handleChange}
            onBlur={handleBlur}
            className={`${styles.input} ${showError('name') ? styles.inputError : ''}`}
            placeholder="Alice Johnson"
            autoComplete="name"
          />
          {showError('name') && <p className={styles.error}>{errors.name}</p>}
        </div>

        {/* ── Email ── */}
        <div className={styles.field}>
          <label htmlFor="email" className={styles.label}>Email <span className={styles.required}>*</span></label>
          <input
            id="email" name="email" type="email"
            value={values.email}
            onChange={handleChange}
            onBlur={handleBlur}
            className={`${styles.input} ${showError('email') ? styles.inputError : ''}`}
            placeholder="alice@ibm.com"
          />
          {showError('email') && <p className={styles.error}>{errors.email}</p>}
        </div>

        {/* ── Phone ── */}
        <div className={styles.field}>
          <label htmlFor="phone" className={styles.label}>Phone <span className={styles.required}>*</span></label>
          <input
            id="phone" name="phone" type="tel"
            value={values.phone}
            onChange={handleChange}
            onBlur={handleBlur}
            className={`${styles.input} ${showError('phone') ? styles.inputError : ''}`}
            placeholder="+91 98765 43210"
          />
          {showError('phone') && <p className={styles.error}>{errors.phone}</p>}
        </div>

        {/* ── Department + Salary (two columns) ── */}
        <div className={styles.row}>
          <div className={styles.field}>
            <label htmlFor="department" className={styles.label}>Department</label>
            <select
              id="department" name="department"
              value={values.department}
              onChange={handleChange}
              onBlur={handleBlur}
              className={styles.input}
            >
              {(['Engineering','Marketing','HR','Finance','Sales'] as Dept[]).map(d => (
                <option key={d} value={d}>{d}</option>
              ))}
            </select>
          </div>

          <div className={styles.field}>
            <label htmlFor="salary" className={styles.label}>Annual Salary (USD) <span className={styles.required}>*</span></label>
            <input
              id="salary" name="salary" type="number"
              value={values.salary}
              onChange={handleChange}
              onBlur={handleBlur}
              className={`${styles.input} ${showError('salary') ? styles.inputError : ''}`}
              placeholder="75000"
              min={0}
            />
            {showError('salary') && <p className={styles.error}>{errors.salary}</p>}
          </div>
        </div>

        {/* ── Join Date ── */}
        <div className={styles.field}>
          <label htmlFor="joinDate" className={styles.label}>Join Date <span className={styles.required}>*</span></label>
          <input
            id="joinDate" name="joinDate" type="date"
            value={values.joinDate}
            onChange={handleChange}
            onBlur={handleBlur}
            max={new Date().toISOString().split('T')[0]}
            className={`${styles.input} ${showError('joinDate') ? styles.inputError : ''}`}
          />
          {showError('joinDate') && <p className={styles.error}>{errors.joinDate}</p>}
        </div>

        {/* ── Active checkbox ── */}
        <div className={styles.checkboxField}>
          <input
            id="isActive" name="isActive" type="checkbox"
            checked={values.isActive}
            onChange={handleChange}
          />
          <label htmlFor="isActive">Active employee</label>
        </div>

        {/* ── Notes (textarea) ── */}
        <div className={styles.field}>
          <label htmlFor="notes" className={styles.label}>Notes (optional)</label>
          <textarea
            id="notes" name="notes"
            value={values.notes}
            onChange={handleChange}
            className={styles.input}
            rows={3}
            placeholder="Any additional notes about this employee…"
            style={{ resize: 'vertical' }}
          />
        </div>

        {/* ── Actions ── */}
        <div className={styles.actions}>
          <button
            type="button"
            className={styles.cancelBtn}
            onClick={() => navigate('/employees')}
          >
            Cancel
          </button>
          <button
            type="submit"
            className={styles.submitBtn}
            disabled={submitting}
          >
            {submitting ? 'Saving…' : 'Create Employee'}
          </button>
        </div>

      </form>
    </div>
  );
}
export default CreateEmployeePage;
```

```css
/* src/pages/CreateEmployeePage.module.css */
.page   { max-width: 640px; }
.title  { margin-bottom: var(--space-6); font-size: var(--font-size-xl); }

.apiBanner {
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-sm);
  background: var(--color-danger-bg);
  color: var(--color-danger);
  margin-bottom: var(--space-5);
  border-left: 4px solid var(--color-danger);
}

.form { display: flex; flex-direction: column; gap: var(--space-5); }

.row  { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4); }
@media (max-width: 500px) { .row { grid-template-columns: 1fr; } }

.field { display: flex; flex-direction: column; gap: var(--space-1); }

.label {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--color-gray-700);
}
.required { color: var(--color-danger); margin-left: 2px; }

.input {
  padding: var(--space-2) var(--space-3);
  border: 1.5px solid var(--color-gray-300);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-md);
  font-family: var(--font-sans);
  background: white;
  transition: border-color 0.15s, box-shadow 0.15s;
  width: 100%;
}
.input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-light);
}
.inputError {
  border-color: var(--color-danger);
  background: var(--color-danger-bg);
}
.inputError:focus {
  box-shadow: 0 0 0 3px rgba(218, 30, 40, 0.15);
}

.error {
  font-size: var(--font-size-xs);
  color: var(--color-danger);
  margin-top: 2px;
}

.checkboxField {
  display: flex; align-items: center; gap: var(--space-2);
  font-size: var(--font-size-sm);
}

.actions {
  display: flex; justify-content: flex-end;
  gap: var(--space-3); padding-top: var(--space-3);
  border-top: 1px solid var(--border-color);
}
.cancelBtn {
  padding: var(--space-2) var(--space-6);
  border: 1.5px solid var(--color-gray-300);
  border-radius: var(--radius-sm);
  background: white;
  font-size: var(--font-size-md);
  color: var(--color-gray-700);
  transition: background 0.15s;
}
.cancelBtn:hover { background: var(--color-gray-100); }
.submitBtn {
  padding: var(--space-2) var(--space-6);
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-md);
  font-weight: 600;
  transition: background 0.15s;
}
.submitBtn:hover:not(:disabled) { background: var(--color-primary-dark); }
.submitBtn:disabled { background: var(--color-gray-300); cursor: not-allowed; }
```

---

## 10.4 React Hook Form + Zod (Production Approach)

Manual validation gets unwieldy for large forms. Use **React Hook Form** (uncontrolled under the hood for performance) + **Zod** (schema validation with TypeScript inference).

```bash
npm install react-hook-form zod @hookform/resolvers
```

### Define the schema

```ts
// src/schemas/employeeSchema.ts
import { z } from 'zod';

export const employeeSchema = z.object({
  name: z
    .string()
    .min(2, 'Name must be at least 2 characters')
    .max(100, 'Name too long'),

  email: z
    .string()
    .email('Enter a valid email address'),

  phone: z
    .string()
    .min(7, 'Phone must have at least 7 digits')
    .regex(/^[\d\s\+\-\(\)]+$/, 'Phone number contains invalid characters'),

  department: z.enum(
    ['Engineering', 'Marketing', 'HR', 'Finance', 'Sales'],
    { errorMap: () => ({ message: 'Select a department' }) }
  ),

  salary: z
    .number({ invalid_type_error: 'Enter a valid number' })
    .min(10000, 'Minimum salary is $10,000')
    .max(10_000_000, 'Salary too high'),

  joinDate: z
    .string()
    .min(1, 'Join date is required')
    .refine(d => new Date(d) <= new Date(), 'Join date cannot be in the future'),

  isActive: z.boolean(),

  notes: z.string().max(500, 'Notes too long').optional(),
});

// TypeScript type auto-inferred from schema — no duplication!
export type EmployeeFormData = z.infer<typeof employeeSchema>;
```

### RHF form component

```tsx
// src/pages/CreateEmployeePageRHF.tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useNavigate } from 'react-router-dom';
import { employeeSchema, EmployeeFormData } from '../schemas/employeeSchema';
import { employeeService } from '../services/employeeService';
import styles from './CreateEmployeePage.module.css';

function CreateEmployeePageRHF() {
  const navigate = useNavigate();

  const {
    register,            // connects inputs to RHF
    handleSubmit,        // wraps your submit function
    watch,               // read a field value live
    reset,               // reset form to defaults
    formState: {
      errors,            // validation errors per field
      isSubmitting,      // true while async submit runs
      isDirty,           // true if any field changed from default
      isValid,           // true when no validation errors
      touchedFields,     // which fields have been blurred
    },
  } = useForm<EmployeeFormData>({
    resolver: zodResolver(employeeSchema),
    mode: 'onBlur',      // validate on blur, revalidate on change
    defaultValues: {
      name: '', email: '', phone: '',
      department: 'Engineering', salary: 70000,
      joinDate: '', isActive: true, notes: '',
    },
  });

  const nameValue = watch('name'); // live preview

  const onSubmit = async (data: EmployeeFormData) => {
    // data is fully typed and validated — no manual parsing needed
    await employeeService.create({
      name:       data.name,
      email:      data.email,
      department: data.department,
      salary:     data.salary,
    });
    navigate('/employees');
  };

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>
        Add Employee {nameValue && <small style={{ fontWeight: 400, color: '#525252' }}>— {nameValue}</small>}
      </h1>

      <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate>

        {/* Name */}
        <div className={styles.field}>
          <label htmlFor="name" className={styles.label}>
            Full Name <span className={styles.required}>*</span>
          </label>
          <input
            id="name"
            type="text"
            className={`${styles.input} ${errors.name ? styles.inputError : ''}`}
            placeholder="Alice Johnson"
            {...register('name')}   {/* ← registers: onChange, onBlur, ref, name */}
          />
          {errors.name && <p className={styles.error}>{errors.name.message}</p>}
        </div>

        {/* Email */}
        <div className={styles.field}>
          <label htmlFor="email" className={styles.label}>Email *</label>
          <input
            id="email" type="email"
            className={`${styles.input} ${errors.email ? styles.inputError : ''}`}
            {...register('email')}
          />
          {errors.email && <p className={styles.error}>{errors.email.message}</p>}
        </div>

        {/* Phone */}
        <div className={styles.field}>
          <label htmlFor="phone" className={styles.label}>Phone *</label>
          <input
            id="phone" type="tel"
            className={`${styles.input} ${errors.phone ? styles.inputError : ''}`}
            {...register('phone')}
          />
          {errors.phone && <p className={styles.error}>{errors.phone.message}</p>}
        </div>

        {/* Department + Salary */}
        <div className={styles.row}>
          <div className={styles.field}>
            <label htmlFor="department" className={styles.label}>Department</label>
            <select id="department" className={styles.input} {...register('department')}>
              {['Engineering','Marketing','HR','Finance','Sales'].map(d => (
                <option key={d} value={d}>{d}</option>
              ))}
            </select>
            {errors.department && <p className={styles.error}>{errors.department.message}</p>}
          </div>

          <div className={styles.field}>
            <label htmlFor="salary" className={styles.label}>Salary (USD) *</label>
            <input
              id="salary" type="number"
              className={`${styles.input} ${errors.salary ? styles.inputError : ''}`}
              {...register('salary', { valueAsNumber: true })}
            />
            {errors.salary && <p className={styles.error}>{errors.salary.message}</p>}
          </div>
        </div>

        {/* Join Date */}
        <div className={styles.field}>
          <label htmlFor="joinDate" className={styles.label}>Join Date *</label>
          <input
            id="joinDate" type="date"
            max={new Date().toISOString().split('T')[0]}
            className={`${styles.input} ${errors.joinDate ? styles.inputError : ''}`}
            {...register('joinDate')}
          />
          {errors.joinDate && <p className={styles.error}>{errors.joinDate.message}</p>}
        </div>

        {/* Active */}
        <div className={styles.checkboxField}>
          <input id="isActive" type="checkbox" {...register('isActive')} />
          <label htmlFor="isActive">Active employee</label>
        </div>

        {/* Notes */}
        <div className={styles.field}>
          <label htmlFor="notes" className={styles.label}>Notes (optional)</label>
          <textarea
            id="notes" rows={3}
            className={styles.input}
            style={{ resize: 'vertical' }}
            {...register('notes')}
          />
          {errors.notes && <p className={styles.error}>{errors.notes.message}</p>}
        </div>

        <div className={styles.actions}>
          <button type="button" className={styles.cancelBtn} onClick={() => reset()}>
            Reset
          </button>
          <button type="button" className={styles.cancelBtn} onClick={() => navigate('/employees')}>
            Cancel
          </button>
          <button
            type="submit"
            className={styles.submitBtn}
            disabled={isSubmitting || !isDirty}
          >
            {isSubmitting ? 'Saving…' : 'Create Employee'}
          </button>
        </div>

      </form>
    </div>
  );
}
export default CreateEmployeePageRHF;
```

---

## 10.5 Add Routes for Forms

```tsx
// src/App.tsx
import CreateEmployeePage from './pages/CreateEmployeePage';
// or: import CreateEmployeePage from './pages/CreateEmployeePageRHF';

<Route path="/" element={<Layout />}>
  <Route index element={<HomePage />} />
  <Route path="employees" element={<EmployeesPage />} />
  <Route path="employees/new" element={<CreateEmployeePage />} />
  <Route path="employees/:id" element={<EmployeeDetailPage />} />
  <Route path="employees/:id/edit" element={<CreateEmployeePage />} />
  {/* pass employee id, form pre-fills values for edit */}
  <Route path="*" element={<NotFoundPage />} />
</Route>
```

---

## 10.6 RHF vs Manual — When to Use What

| Scenario | Manual | RHF + Zod |
|----------|--------|-----------|
| 1-3 fields, quick prototype | ✅ simpler | Overkill |
| 5+ fields with complex validation | Gets messy | ✅ clean |
| Same schema for API + form | Manual mismatch | ✅ share Zod schema |
| Async validation (username taken?) | Custom | ✅ `validate` option |
| Multi-step wizard | Hard | ✅ `useFormContext` |
| Team familiarity | | ✅ industry standard |

---

## Summary

| Topic | Key Point |
|-------|-----------|
| Controlled input | `value` + `onChange` — React owns the value |
| Validation state | `values`, `errors`, `touched` — show error only when touched |
| Submit flow | Mark all touched → validate → call API → redirect |
| RHF `register` | Spreads `name`, `onChange`, `onBlur`, `ref` onto input |
| Zod schema | Single source of truth for type + validation rules |
| `zodResolver` | Connects Zod schema to RHF |

**Next → [Module 11: Redux](./11-redux.md)**
