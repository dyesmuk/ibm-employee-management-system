import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private baseUrl = 'http://localhost:8080/api/v1/auth';

  constructor(private http: HttpClient) { }

  login(username: string, password: string) {
    console.log(username, password);
    return this.http.post(`${this.baseUrl}/login`, {
      username,
      password
    });
  }

  register() { };

}
