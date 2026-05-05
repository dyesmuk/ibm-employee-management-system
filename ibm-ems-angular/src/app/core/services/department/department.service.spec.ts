import { describe, it, expect, vi, beforeEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { DepartmentService } from './department.service';
// import { Department } from '../../../core/models/department.model';

const BASE_URL = 'http://localhost:8080/api/v1/departments';

// const mockDepartments: Department[] = [];

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

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should GET all departments', () => {
    service.getAllDepartments().subscribe();
    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('GET');
  });

  // write more tests here 

});