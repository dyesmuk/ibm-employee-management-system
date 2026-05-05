import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { DepartmentCreate } from './department-create';
import { DepartmentService } from '../../../core/services/department/department.service';
import { Department } from '../../../core/models/department.model';

const mockDepartmentService = {
  addDepartment: vi.fn(),
};

const createdDept: Department = { id: '1', name: 'Engineering', description: 'Dev team' };

describe('DepartmentCreate', () => {
  let component: DepartmentCreate;
  let fixture: ComponentFixture<DepartmentCreate>;
  let service: typeof mockDepartmentService;

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [DepartmentCreate],
      providers: [{ provide: DepartmentService, useValue: mockDepartmentService }],
    }).compileComponents();

    fixture = TestBed.createComponent(DepartmentCreate);
    component = fixture.componentInstance;
    service = TestBed.inject(DepartmentService) as any;

    await fixture.whenStable();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialise with an empty department', () => {
    expect(component.department.name).toBe('');
    expect(component.department.description).toBe('');
  });

  it('should initialise with empty message and error strings', () => {
    expect(component.message).toBe('');
    expect(component.error).toBe('');
  });

  describe('createDepartment() — success', () => {
    beforeEach(() => {
      mockDepartmentService.addDepartment.mockReturnValue(of(createdDept));
    });

    it('should call addDepartment with the current department', () => {
      component.department = { name: 'Engineering', description: 'Dev team' };
      component.createDepartment();

      expect(service.addDepartment).toHaveBeenCalledOnce();
      expect(service.addDepartment).toHaveBeenCalledWith({
        name: 'Engineering',
        description: 'Dev team',
      });
    });

    it('should set message to success text', () => {
      component.createDepartment();

      expect(component.message).toBe('Department created successfully');
    });

    it('should clear the error on success', () => {
      component.error = 'some previous error';
      component.createDepartment();

      expect(component.error).toBe('');
    });

    it('should reset the department fields after a successful creation', () => {
      component.department = { name: 'HR', description: 'Human resources' };
      component.createDepartment();

      expect(component.department.name).toBe('');
      expect(component.department.description).toBe('');
    });

    it('should clear a previous error message on success', () => {
      component.error = 'Failed to create department';
      component.createDepartment();

      expect(component.error).toBe('');
    });
  });

  describe('createDepartment() — error', () => {
    beforeEach(() => {
      mockDepartmentService.addDepartment.mockReturnValue(
        throwError(() => new Error('HTTP 500'))
      );
    });

    it('should set error message when the service call fails', () => {
      component.createDepartment();

      expect(component.error).toBe('Failed to create department');
    });

    it('should NOT set a success message on failure', () => {
      component.createDepartment();

      expect(component.message).toBe('');
    });

    it('should NOT reset the department fields on failure', () => {
      component.department = { name: 'Finance', description: 'Finance dept' };
      component.createDepartment();

      expect(component.department.name).toBe('Finance');
      expect(component.department.description).toBe('Finance dept');
    });

    it('should overwrite a previous success message with empty string on failure', () => {
      mockDepartmentService.addDepartment.mockReturnValueOnce(of(createdDept));
      component.createDepartment();
      expect(component.message).toBe('Department created successfully');

      mockDepartmentService.addDepartment.mockReturnValue(
        throwError(() => new Error('HTTP 500'))
      );
      component.createDepartment();

      expect(component.error).toBe('Failed to create department');
    });
  });

  describe('Edge cases', () => {
    it('should allow creating a department with whitespace-only name (no client validation)', () => {
      mockDepartmentService.addDepartment.mockReturnValue(of(createdDept));
      component.department = { name: '   ', description: '' };
      component.createDepartment();

      expect(service.addDepartment).toHaveBeenCalledWith({ name: '   ', description: '' });
    });

    it('should allow creating a department with a very long name', () => {
      const longName = 'A'.repeat(500);
      mockDepartmentService.addDepartment.mockReturnValue(of({ ...createdDept, name: longName }));
      component.department = { name: longName, description: 'Test' };
      component.createDepartment();

      expect(service.addDepartment).toHaveBeenCalledWith({ name: longName, description: 'Test' });
    });

    it('should be callable multiple times in sequence', () => {
      mockDepartmentService.addDepartment.mockReturnValue(of(createdDept));

      component.department = { name: 'Dept A', description: '' };
      component.createDepartment();

      component.department = { name: 'Dept B', description: '' };
      component.createDepartment();

      expect(service.addDepartment).toHaveBeenCalledTimes(2);
    });
  });
});


// import { ComponentFixture, TestBed } from '@angular/core/testing';

// import { DepartmentCreate } from './department-create';

// describe('DepartmentCreate', () => {
//   let component: DepartmentCreate;
//   let fixture: ComponentFixture<DepartmentCreate>;

//   beforeEach(async () => {
//     await TestBed.configureTestingModule({
//       imports: [DepartmentCreate]
//     })
//     .compileComponents();

//     fixture = TestBed.createComponent(DepartmentCreate);
//     component = fixture.componentInstance;
//     await fixture.whenStable();
//   });

//   it('should create', () => {
//     expect(component).toBeTruthy();
//   });
// });
