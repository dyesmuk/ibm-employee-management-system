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

  getAllDepartments(): Observable<Department[]> {
    console.log("getAllDepartments()");
    return this.http.get<Department[]>(this.baseUrl);
  };

  getDepartmentById(id: string): Observable<Department> {
    console.log("getDepartmentById");
    return this.http.get<Department>(`${this.baseUrl}/${id}`);
  };

  addDepartment(department: Department): Observable<Department> {
    console.log("addDepartment");
    return this.http.post<Department>(this.baseUrl, department);
  };

  // add more methods 

}





// import { Injectable } from '@angular/core';
// import { HttpClient, HttpHeaders } from '@angular/common/http';
// import { Observable } from 'rxjs';
// import { Department } from '../../../core/models/department.model';

// @Injectable({
//   providedIn: 'root',
// })
// export class DepartmentService {

//   private baseUrl = 'http://localhost:8080/api/v1/departments';

//   constructor(private http: HttpClient) { }

//   private getHeaders() {
//     const token = localStorage.getItem('token');
//     return {
//       headers: new HttpHeaders({
//         Authorization: `Bearer ${token}`
//       })
//     };
//   }

//   getAllDepartments(): Observable<Department[]> {
//     console.log("getAllDepartments()");
//     return this.http.get<Department[]>(this.baseUrl, this.getHeaders());
//   };

//   getDepartmentById(id: string): Observable<Department> {
//     console.log("getDepartmentById");
//     return this.http.get<Department>(`${this.baseUrl}/${id}`, this.getHeaders());
//   };

//   addDepartment(department: Department): Observable<Department> {
//     console.log("addDepartment");
//     return this.http.post<Department>(this.baseUrl, department, this.getHeaders());
//   };

//   // add more methods

// }





