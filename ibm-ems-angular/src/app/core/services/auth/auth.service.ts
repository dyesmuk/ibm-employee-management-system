import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private baseUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) { }

  login(email: string, password: string) {
    return this.http.post(`${this.baseUrl}/login`, {
      email,
      password
    });
  }
}