import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Department } from '../../../core/models/department.model';

@Injectable({
  providedIn: 'root',
})
export class DepartmentService {

  private baseUrl = 'http://localhost:8080/api/v1/departments';

  constructor(private http: HttpClient) { }

  private getHeaders() {
    const token = localStorage.getItem('token');
    return {
      headers: new HttpHeaders({
        Authorization: `Bearer ${token}`
      })
    };
  }

  getAllDepartments(): Observable<any> {
    return this.http.get(this.baseUrl, this.getHeaders());
  }

  // add more methods 

  // getDepartmentById()
  
  // addDepartment()

}


