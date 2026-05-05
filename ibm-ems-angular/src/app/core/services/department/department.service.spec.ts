import { describe, it, expect, vi, beforeEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { DepartmentService } from './department.service';
import { Department } from '../../../core/models/department.model';

const BASE_URL = 'http://localhost:8080/api/v1/departments';

const mockDepartment: Department = {
  id: '1',
  name: 'Engineering',
  description: 'Software engineering team',
};

const mockDepartments: Department[] = [
  { id: '1', name: 'Engineering', description: 'Software engineering team' },
  { id: '2', name: 'HR', description: 'Human resources' },
  { id: '3', name: 'Finance', description: 'Finance department' },
];

describe('DepartmentService', () => {
  let service: DepartmentService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DepartmentService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(DepartmentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Ensures no outstanding HTTP requests
  });

  // ─── Instantiation ────────────────────────────────────────────────────────

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ─── getAllDepartments ─────────────────────────────────────────────────────

  describe('getAllDepartments()', () => {
    it('should GET all departments from the correct URL', () => {
      service.getAllDepartments().subscribe();

      const req = httpMock.expectOne(BASE_URL);
      expect(req.request.method).toBe('GET');
      req.flush(mockDepartments);
    });

    it('should return an array of departments', () => {
      let result: Department[] = [];

      service.getAllDepartments().subscribe((data) => (result = data));

      const req = httpMock.expectOne(BASE_URL);
      req.flush(mockDepartments);

      expect(result).toEqual(mockDepartments);
      expect(result.length).toBe(3);
    });

    it('should return an empty array when the API returns no departments', () => {
      let result: Department[] = [mockDepartment]; // pre-populated to confirm it gets replaced

      service.getAllDepartments().subscribe((data) => (result = data));

      const req = httpMock.expectOne(BASE_URL);
      req.flush([]);

      expect(result).toEqual([]);
    });

    it('should propagate HTTP errors', () => {
      let errorThrown = false;

      service.getAllDepartments().subscribe({
        error: () => (errorThrown = true),
      });

      const req = httpMock.expectOne(BASE_URL);
      req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });

      expect(errorThrown).toBe(true);
    });

    it('should propagate 404 errors', () => {
      let errorStatus = 0;

      service.getAllDepartments().subscribe({
        error: (err) => (errorStatus = err.status),
      });

      const req = httpMock.expectOne(BASE_URL);
      req.flush('Not found', { status: 404, statusText: 'Not Found' });

      expect(errorStatus).toBe(404);
    });
  });

  // ─── getDepartmentById ────────────────────────────────────────────────────

  describe('getDepartmentById()', () => {
    it('should GET a department at the correct URL', () => {
      service.getDepartmentById('1').subscribe();

      const req = httpMock.expectOne(`${BASE_URL}/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockDepartment);
    });

    it('should return the correct department', () => {
      let result: Department | null = null;

      service.getDepartmentById('1').subscribe((data) => (result = data));

      const req = httpMock.expectOne(`${BASE_URL}/1`);
      req.flush(mockDepartment);

      expect(result).toEqual(mockDepartment);
    });

    it('should handle numeric string IDs', () => {
      service.getDepartmentById('42').subscribe();

      const req = httpMock.expectOne(`${BASE_URL}/42`);
      expect(req.request.method).toBe('GET');
      req.flush({ id: '42', name: 'Marketing', description: 'Marketing team' });
    });

    it('should propagate 404 when department is not found', () => {
      let errorStatus = 0;

      service.getDepartmentById('999').subscribe({
        error: (err) => (errorStatus = err.status),
      });

      const req = httpMock.expectOne(`${BASE_URL}/999`);
      req.flush('Not found', { status: 404, statusText: 'Not Found' });

      expect(errorStatus).toBe(404);
    });

    it('should propagate 500 server errors', () => {
      let errorThrown = false;

      service.getDepartmentById('1').subscribe({
        error: () => (errorThrown = true),
      });

      const req = httpMock.expectOne(`${BASE_URL}/1`);
      req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });

      expect(errorThrown).toBe(true);
    });

    it('should trim whitespace from id in the URL (if caller does trimming)', () => {
      // Caller is responsible for trimming — service passes id as-is
      service.getDepartmentById('  1  ').subscribe();

      // Confirms the service appends exactly what it receives
      const req = httpMock.expectOne(`${BASE_URL}/  1  `);
      req.flush(mockDepartment);
    });
  });

  // ─── addDepartment ────────────────────────────────────────────────────────

  describe('addDepartment()', () => {
    const newDept: Department = { name: 'Design', description: 'UI/UX team' };
    const createdDept: Department = { id: '10', name: 'Design', description: 'UI/UX team' };

    it('should POST to the correct URL', () => {
      service.addDepartment(newDept).subscribe();

      const req = httpMock.expectOne(BASE_URL);
      expect(req.request.method).toBe('POST');
      req.flush(createdDept);
    });

    it('should send the department object as the request body', () => {
      service.addDepartment(newDept).subscribe();

      const req = httpMock.expectOne(BASE_URL);
      expect(req.request.body).toEqual(newDept);
      req.flush(createdDept);
    });

    it('should return the created department with an id from the server', () => {
      let result: Department | null = null;

      service.addDepartment(newDept).subscribe((data) => (result = data));

      const req = httpMock.expectOne(BASE_URL);
      req.flush(createdDept);

      expect(result).toEqual(createdDept);
      expect((result as any).id).toBe('10');
    });

    it('should propagate 400 validation errors from the server', () => {
      let errorStatus = 0;

      service.addDepartment({ name: '', description: '' }).subscribe({
        error: (err) => (errorStatus = err.status),
      });

      const req = httpMock.expectOne(BASE_URL);
      req.flush('Bad request', { status: 400, statusText: 'Bad Request' });

      expect(errorStatus).toBe(400);
    });

    it('should propagate 409 conflict when department already exists', () => {
      let errorStatus = 0;

      service.addDepartment(newDept).subscribe({
        error: (err) => (errorStatus = err.status),
      });

      const req = httpMock.expectOne(BASE_URL);
      req.flush('Conflict', { status: 409, statusText: 'Conflict' });

      expect(errorStatus).toBe(409);
    });

    it('should propagate 500 server errors', () => {
      let errorThrown = false;

      service.addDepartment(newDept).subscribe({
        error: () => (errorThrown = true),
      });

      const req = httpMock.expectOne(BASE_URL);
      req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });

      expect(errorThrown).toBe(true);
    });
  });
});
