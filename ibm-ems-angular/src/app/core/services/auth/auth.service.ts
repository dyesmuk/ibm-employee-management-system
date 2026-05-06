import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private baseUrl = 'http://localhost:8080/api/v1/auth';
  private platformId = inject(PLATFORM_ID);

  constructor(private http: HttpClient) { }

  login(username: string, password: string) {
    return this.http.post(`${this.baseUrl}/login`, { username, password });
  }

  logout() {
    if (!isPlatformBrowser(this.platformId)) return;
    localStorage.removeItem('token');
  }

  isLoggedIn(): boolean {
    if (!isPlatformBrowser(this.platformId)) return false;
    return !!localStorage.getItem('token');
  }

  register() { }
}

// import { Injectable } from '@angular/core';
// import { HttpClient } from '@angular/common/http';

// @Injectable({ providedIn: 'root' })
// export class AuthService {

//   private baseUrl = 'http://localhost:8080/api/v1/auth';

//   constructor(private http: HttpClient) { }

//   login(username: string, password: string) {
//     console.log(username, password);
//     return this.http.post(`${this.baseUrl}/login`, {
//       username,
//       password
//     });
//   }

//   register() { };

// }
