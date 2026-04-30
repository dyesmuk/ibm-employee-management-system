import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  apiUrl = "https://jsonplaceholder.typicode.com/users";

  constructor(private http: HttpClient) { }

  getUserById(id: number = 2) {
    return this.http.get(`${this.apiUrl}/${id}`);
  }
}


// import { Injectable } from '@angular/core';
// import { HttpClient } from '@angular/common/http';

// @Injectable({
//   providedIn: 'root'
// })
// export class UserService {

//   constructor(private http: HttpClient) { }

//   getUserById() {
//     return this.http.get('https://jsonplaceholder.typicode.com/users/2');
//   }

//   getAllUsers() {
//     return this.http.get('https://jsonplaceholder.typicode.com/users');
//   }

// }
