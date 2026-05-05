import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private baseUrl = 'http://localhost:8080/api/v1/auth';

  constructor(private http: HttpClient) { }

  login(username: string, password: string) {
    return this.http.post(`${this.baseUrl}/login`, {
      username,
      password
    });
  }

  register() { };

}


/**
 *  test case ideas for login - 
 * 
 * A. postive test cases - with correct creds  
 * 1. status code - 200 
 *  response body - 
 * 2. token is jwt?
 * 3. jwt exipiry 
 * 4. username same as given username  - with bad creds 
 * 
 * A. postive test cases 
 *  5. status code
 * 6. error 
 * 7. message 
 *  
 */




