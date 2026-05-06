# Module 14 — Testing Angular Applications

## Learning Objectives
- Set up Jest as a faster alternative to Karma/Jasmine
- Write unit tests for services, pipes, and utilities
- Write component tests with `TestBed` and `ComponentFixture`
- Mock `HttpClient` with `HttpClientTestingModule`
- Test signal-based components
- Test NgRx store, reducers, and effects

---

## 14.1 Testing Setup — Jest (Recommended)

Angular's default test runner is Karma + Jasmine, but Jest is faster and has better DX.

```bash
# Remove Karma dependencies
npm remove karma karma-chrome-launcher karma-coverage karma-jasmine karma-jasmine-html-reporter

# Install Jest
npm install -D jest @types/jest jest-preset-angular
```

```ts
// jest.config.ts
import type { Config } from 'jest';

const config: Config = {
  preset:    'jest-preset-angular',
  setupFilesAfterFramework: ['<rootDir>/setup-jest.ts'],
  globalSetup: 'jest-preset-angular/global-setup',
  testPathPattern: '.*\\.spec\\.ts$',
  coverageDirectory: 'coverage',
  collectCoverageFrom: [
    'src/app/**/*.ts',
    '!src/app/**/*.module.ts',
    '!src/app/main.ts',
    '!src/app/models/**',
  ],
  coverageThreshold: {
    global: { statements: 80, branches: 75, functions: 80, lines: 80 },
  },
};
export default config;
```

```ts
// setup-jest.ts
import 'jest-preset-angular/setup-jest';
import '@testing-library/jest-dom';
```

```json
// package.json
{
  "scripts": {
    "test":          "jest",
    "test:watch":    "jest --watch",
    "test:coverage": "jest --coverage"
  }
}
```

---

## 14.2 Unit Testing — Services

```ts
// src/app/services/employee.service.spec.ts
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { EmployeeApiService } from './employee-api.service';
import { Employee } from '../models/employee.model';

const MOCK_USER = {
  id: 1, name: 'Alice Johnson', username: 'alice',
  email: 'alice@ibm.com', phone: '1234567890', website: 'alice.com',
  address: { street: '123 Main St', suite: '', city: 'NYC', zipcode: '10001' },
  company: { name: 'IBM', catchPhrase: '', bs: '' },
};

describe('EmployeeApiService', () => {
  let service: EmployeeApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports:   [HttpClientTestingModule],
      providers: [EmployeeApiService],
    });

    service  = TestBed.inject(EmployeeApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();   // ensure no unexpected HTTP calls
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAll() should return mapped employees', () => {
    let result: Employee[] | undefined;

    service.getAll().subscribe(employees => (result = employees));

    const req = httpMock.expectOne('https://jsonplaceholder.typicode.com/users');
    expect(req.request.method).toBe('GET');
    req.flush([MOCK_USER]);   // simulate server response

    expect(result).toHaveLength(1);
    expect(result![0].name).toBe('Alice Johnson');
    expect(result![0].email).toBe('alice@ibm.com');
    expect(typeof result![0].salary).toBe('number');
  });

  it('getById() should fetch single user', () => {
    let result: Employee | undefined;

    service.getById(1).subscribe(emp => (result = emp));

    const req = httpMock.expectOne('https://jsonplaceholder.typicode.com/users/1');
    req.flush(MOCK_USER);

    expect(result?.id).toBe(1);
  });

  it('getAll() should handle HTTP error', () => {
    let error: any;

    service.getAll().subscribe({ error: err => (error = err) });

    const req = httpMock.expectOne('https://jsonplaceholder.typicode.com/users');
    req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });

    expect(error).toBeTruthy();
  });
});
```

---

## 14.3 Unit Testing — Signal Service

```ts
// src/app/state/employee-state.service.spec.ts
import { TestBed } from '@angular/core/testing';
import { EmployeeStateService } from './employee-state.service';
import { EmployeeApiService }   from '../services/employee-api.service';
import { SEED_EMPLOYEES, Department } from '../models/employee.model';
import { of, throwError } from 'rxjs';

// Mock EmployeeApiService
const mockApiService = {
  getAll:  jest.fn().mockReturnValue(of(SEED_EMPLOYEES)),
  create:  jest.fn(),
  update:  jest.fn(),
  remove:  jest.fn().mockReturnValue(of(void 0)),
};

describe('EmployeeStateService', () => {
  let service: EmployeeStateService;

  beforeEach(() => {
    jest.clearAllMocks();

    TestBed.configureTestingModule({
      providers: [
        EmployeeStateService,
        { provide: EmployeeApiService, useValue: mockApiService },
      ],
    });

    service = TestBed.inject(EmployeeStateService);
  });

  it('should load employees on init', () => {
    expect(service.employees()).toHaveLength(SEED_EMPLOYEES.length);
    expect(service.loading()).toBe(false);
    expect(service.error()).toBeNull();
  });

  it('stats() should compute correctly', () => {
    const stats = service.stats();
    expect(stats.total).toBe(SEED_EMPLOYEES.length);
    expect(stats.active).toBe(SEED_EMPLOYEES.filter(e => e.isActive).length);
  });

  it('removeEmployee() should remove employee from list', () => {
    const initialCount = service.employees().length;
    service.removeEmployee(1);
    expect(mockApiService.remove).toHaveBeenCalledWith(1);
    // After mock resolves (of(void 0)), list should shrink
    expect(service.employees()).toHaveLength(initialCount - 1);
  });

  it('setFilter() should update the filter signal', () => {
    expect(service.filter().department).toBe('All');
    service.setFilter({ department: Department.Engineering });
    expect(service.filter().department).toBe(Department.Engineering);
  });

  it('filteredEmployees() should apply department filter', () => {
    service.setFilter({ department: Department.Engineering });
    const filtered = service.filteredEmployees();
    expect(filtered.every(e => e.department === Department.Engineering)).toBe(true);
  });

  it('filteredEmployees() should apply search filter', () => {
    service.setFilter({ search: 'alice' });
    const filtered = service.filteredEmployees();
    expect(filtered.every(e => e.name.toLowerCase().includes('alice'))).toBe(true);
  });

  it('loadEmployees() should set error on failure', () => {
    mockApiService.getAll.mockReturnValueOnce(throwError(() => new Error('Network error')));
    service.loadEmployees();
    expect(service.error()).toBe('Network error');
    expect(service.loading()).toBe(false);
  });
});
```

---

## 14.4 Unit Testing — Pipes

```ts
// src/app/pipes/salary.pipe.spec.ts
import { SalaryPipe } from './salary.pipe';

describe('SalaryPipe', () => {
  let pipe: SalaryPipe;

  beforeEach(() => { pipe = new SalaryPipe(); });

  it('should be created', () => {
    expect(pipe).toBeTruthy();
  });

  it('should format a number as USD', () => {
    expect(pipe.transform(95000)).toBe('$95,000');
  });

  it('should format as INR when currency is INR', () => {
    const result = pipe.transform(95000, 'INR');
    expect(result).toContain('95,000');
  });

  it('should return dash for null', () => {
    expect(pipe.transform(null)).toBe('—');
  });

  it('should return dash for undefined', () => {
    expect(pipe.transform(undefined)).toBe('—');
  });

  it('should format zero correctly', () => {
    expect(pipe.transform(0)).toBe('$0');
  });
});
```

```ts
// src/app/pipes/initials.pipe.spec.ts
import { InitialsPipe } from './initials.pipe';

describe('InitialsPipe', () => {
  const pipe = new InitialsPipe();

  it('should return two initials for a full name', () => {
    expect(pipe.transform('Alice Johnson')).toBe('AJ');
  });

  it('should return one initial for a single name', () => {
    expect(pipe.transform('Alice')).toBe('A');
  });

  it('should respect maxChars parameter', () => {
    expect(pipe.transform('Alice Mary Johnson', 3)).toBe('AMJ');
  });

  it('should return ? for null input', () => {
    expect(pipe.transform(null)).toBe('?');
  });

  it('should return ? for empty string', () => {
    expect(pipe.transform('')).toBe('?');
  });
});
```

---

## 14.5 Component Testing with TestBed

```ts
// src/app/components/employee-card/employee-card.component.spec.ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { RouterTestingModule } from '@angular/router/testing';
import { EmployeeCardComponent } from './employee-card.component';
import { Employee, Department } from '../../models/employee.model';

const mockEmployee: Employee = {
  id:         1,
  name:       'Alice Johnson',
  email:      'alice@ibm.com',
  phone:      '+91-9876543210',
  department: Department.Engineering,
  salary:     95000,
  isActive:   true,
  joinDate:   '2021-03-15',
};

describe('EmployeeCardComponent', () => {
  let component: EmployeeCardComponent;
  let fixture:   ComponentFixture<EmployeeCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        EmployeeCardComponent,   // standalone component
        RouterTestingModule,     // provides RouterLink
      ],
    }).compileComponents();

    fixture   = TestBed.createComponent(EmployeeCardComponent);
    component = fixture.componentInstance;

    // Set the required signal input
    fixture.componentRef.setInput('employee', mockEmployee);
    fixture.detectChanges();   // trigger initial change detection
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display employee name', () => {
    const name = fixture.debugElement.query(By.css('.card__name'));
    expect(name.nativeElement.textContent).toContain('Alice Johnson');
  });

  it('should display employee email', () => {
    const content = fixture.nativeElement.textContent;
    expect(content).toContain('alice@ibm.com');
  });

  it('should show Active badge for active employee', () => {
    const badge = fixture.debugElement.query(By.css('.card__badge--active'));
    expect(badge).toBeTruthy();
    expect(badge.nativeElement.textContent.trim()).toContain('Active');
  });

  it('should show Inactive badge for inactive employee', () => {
    fixture.componentRef.setInput('employee', { ...mockEmployee, isActive: false });
    fixture.detectChanges();

    const badge = fixture.debugElement.query(By.css('.card__badge--inactive'));
    expect(badge).toBeTruthy();
    expect(badge.nativeElement.textContent.trim()).toContain('Inactive');
  });

  it('should apply card--inactive class when employee is inactive', () => {
    fixture.componentRef.setInput('employee', { ...mockEmployee, isActive: false });
    fixture.detectChanges();

    const card = fixture.debugElement.query(By.css('.card--inactive'));
    expect(card).toBeTruthy();
  });

  it('should emit removed event with employee id on remove button click', () => {
    const removedSpy = jest.fn();
    component.removed.subscribe(removedSpy);

    const removeBtn = fixture.debugElement.query(By.css('.card__close'));
    removeBtn.triggerEventHandler('click', new MouseEvent('click'));

    expect(removedSpy).toHaveBeenCalledWith(1);
  });

  it('should display formatted salary', () => {
    const content = fixture.nativeElement.textContent;
    expect(content).toContain('$95,000');
  });

  it('should display join year', () => {
    const content = fixture.nativeElement.textContent;
    expect(content).toContain('2021');
  });
});
```

---

## 14.6 Testing Forms

```ts
// src/app/pages/employee-form/employee-form.component.spec.ts
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { EmployeeFormComponent } from './employee-form.component';
import { EmployeeStateService }  from '../../state/employee-state.service';

const mockStateService = {
  getById:        jest.fn().mockReturnValue(null),
  addEmployee:    jest.fn(),
  updateEmployee: jest.fn(),
};

describe('EmployeeFormComponent', () => {
  let component: EmployeeFormComponent;
  let fixture:   ComponentFixture<EmployeeFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        EmployeeFormComponent,
        ReactiveFormsModule,
        RouterTestingModule,
      ],
      providers: [
        { provide: EmployeeStateService, useValue: mockStateService },
      ],
    }).compileComponents();

    fixture   = TestBed.createComponent(EmployeeFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('form should be invalid when empty', () => {
    expect(component.form.invalid).toBe(true);
  });

  it('name field should be required', () => {
    const nameCtrl = component.form.get('name')!;
    nameCtrl.setValue('');
    expect(nameCtrl.errors?.['required']).toBeTruthy();
  });

  it('email should validate format', () => {
    const emailCtrl = component.form.get('email')!;
    emailCtrl.setValue('not-an-email');
    expect(emailCtrl.errors?.['email']).toBeTruthy();

    emailCtrl.setValue('alice@ibm.com');
    expect(emailCtrl.errors).toBeNull();
  });

  it('salary should reject values below 10000', () => {
    const salaryCtrl = component.form.get('salary')!;
    salaryCtrl.setValue(5000);
    expect(salaryCtrl.errors?.['tooLow']).toBeTruthy();
  });

  it('submit should not fire when form is invalid', () => {
    const navigateSpy = jest.fn();
    (component as any).router.navigate = navigateSpy;

    component.onSubmit();
    expect(mockStateService.addEmployee).not.toHaveBeenCalled();
  });

  it('should mark all fields as touched on invalid submit', () => {
    component.onSubmit();
    expect(component.form.touched).toBe(true);
  });

  it('should call addEmployee on valid submit', () => {
    component.form.patchValue({
      name:       'Test User',
      email:      'test@ibm.com',
      phone:      '+911234567890',
      department: 'Engineering',
      salary:     75000,
      joinDate:   '2022-01-01',
      isActive:   true,
    });

    expect(component.form.valid).toBe(true);
    component.onSubmit();
    expect(mockStateService.addEmployee).toHaveBeenCalled();
  });
});
```

---

## 14.7 Testing NgRx Reducer

```ts
// src/app/store/employee.reducer.spec.ts
import { employeeReducer, initialState } from './employee.reducer';
import { EmployeeActions } from './employee.actions';
import { SEED_EMPLOYEES } from '../models/employee.model';

describe('EmployeeReducer', () => {

  it('should return initial state for unknown action', () => {
    const action = { type: 'UNKNOWN' } as any;
    const state  = employeeReducer(undefined, action);
    expect(state).toEqual(initialState);
  });

  it('should set loading=true on loadEmployees', () => {
    const state = employeeReducer(initialState, EmployeeActions.loadEmployees());
    expect(state.loading).toBe(true);
    expect(state.error).toBeNull();
  });

  it('should populate employees on loadEmployeesSuccess', () => {
    const state = employeeReducer(
      { ...initialState, loading: true },
      EmployeeActions.loadEmployeesSuccess({ employees: SEED_EMPLOYEES }),
    );
    expect(state.employees).toEqual(SEED_EMPLOYEES);
    expect(state.loading).toBe(false);
  });

  it('should set error on loadEmployeesFailure', () => {
    const state = employeeReducer(
      { ...initialState, loading: true },
      EmployeeActions.loadEmployeesFailure({ error: 'Network error' }),
    );
    expect(state.error).toBe('Network error');
    expect(state.loading).toBe(false);
  });

  it('should add employee on createEmployeeSuccess', () => {
    const stateWithEmp = { ...initialState, employees: [...SEED_EMPLOYEES] };
    const newEmp = { ...SEED_EMPLOYEES[0], id: 999, name: 'New Hire' };

    const state = employeeReducer(
      stateWithEmp,
      EmployeeActions.createEmployeeSuccess({ employee: newEmp }),
    );
    expect(state.employees).toHaveLength(SEED_EMPLOYEES.length + 1);
    expect(state.employees.find(e => e.id === 999)).toBeTruthy();
  });

  it('should remove employee on deleteEmployeeSuccess', () => {
    const stateWithEmps = { ...initialState, employees: [...SEED_EMPLOYEES] };

    const state = employeeReducer(
      stateWithEmps,
      EmployeeActions.deleteEmployeeSuccess({ id: 1 }),
    );
    expect(state.employees.find(e => e.id === 1)).toBeUndefined();
    expect(state.employees).toHaveLength(SEED_EMPLOYEES.length - 1);
  });

  it('should update filter on setFilter', () => {
    const state = employeeReducer(
      initialState,
      EmployeeActions.setFilter({ department: 'Engineering' }),
    );
    expect(state.filter).toBe('Engineering');
  });
});
```

---

## 14.8 Testing Selectors

```ts
// src/app/store/employee.selectors.spec.ts
import { selectFilteredEmployees, selectStats } from './employee.selectors';
import { initialState } from './employee.reducer';
import { SEED_EMPLOYEES, Department } from '../models/employee.model';

describe('Employee Selectors', () => {

  const stateWithEmployees = {
    employees: {
      ...initialState,
      employees: SEED_EMPLOYEES,
    },
  };

  it('selectFilteredEmployees should return all when filter=All', () => {
    const result = selectFilteredEmployees(stateWithEmployees as any);
    expect(result).toHaveLength(SEED_EMPLOYEES.length);
  });

  it('selectFilteredEmployees should filter by department', () => {
    const state = {
      employees: {
        ...stateWithEmployees.employees,
        filter: Department.Engineering,
      },
    };
    const result = selectFilteredEmployees(state as any);
    expect(result.every(e => e.department === Department.Engineering)).toBe(true);
  });

  it('selectFilteredEmployees should filter by search term', () => {
    const state = {
      employees: { ...stateWithEmployees.employees, search: 'alice' },
    };
    const result = selectFilteredEmployees(state as any);
    expect(result.every(e => e.name.toLowerCase().includes('alice'))).toBe(true);
  });

  it('selectStats should compute correct totals', () => {
    const stats = selectStats(stateWithEmployees as any);
    expect(stats.total).toBe(SEED_EMPLOYEES.length);
    expect(stats.active).toBe(SEED_EMPLOYEES.filter(e => e.isActive).length);
  });
});
```

---

## 14.9 Coverage Report

```bash
npm run test:coverage

# Opens html report in browser
npx open-cli coverage/lcov-report/index.html
```

---

## 14.10 Testing Quick Reference

```ts
// Create component
const fixture = TestBed.createComponent(MyComponent);
const comp    = fixture.componentInstance;
fixture.detectChanges();

// Set signal input (Angular 17+)
fixture.componentRef.setInput('employee', mockEmployee);
fixture.detectChanges();

// Query DOM
const el  = fixture.debugElement.query(By.css('.class-name'));
const els = fixture.debugElement.queryAll(By.css('li'));
el.nativeElement.textContent;
el.nativeElement.click();

// Trigger event
el.triggerEventHandler('click', new MouseEvent('click'));
el.triggerEventHandler('input', { target: { value: 'text' } });

// Spy on output
const spy = jest.fn();
comp.removed.subscribe(spy);
// ... trigger event
expect(spy).toHaveBeenCalledWith(expectedValue);

// HTTP testing
const req = httpMock.expectOne('/api/employees');
req.flush(mockData);
httpMock.verify();

// Async testing
it('should handle async', fakeAsync(() => {
  comp.load();
  tick(1000);   // advance timer by 1 second
  fixture.detectChanges();
  expect(comp.data()).toBeDefined();
}));
```

---

## Summary

| Tool | Purpose |
|------|---------|
| `TestBed` | Angular DI container for tests |
| `ComponentFixture` | Controls a component instance in tests |
| `By.css()` | Query the DOM in tests |
| `setInput()` | Set signal inputs in tests |
| `HttpClientTestingModule` | Mock HTTP — no real requests |
| `HttpTestingController` | Verify and flush HTTP requests |
| `RouterTestingModule` | Provide Router in tests |
| `jest.fn()` | Create mock functions |
| `fakeAsync` / `tick` | Control async timing |

**Next → [Module 15: Deployment](./15-deployment.md)**
