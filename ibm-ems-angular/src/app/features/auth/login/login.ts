import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth/auth.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  imports: [FormsModule, CommonModule]
})
export class Login {

  email = '';
  password = '';
  error = '';

  constructor(private authService: AuthService) { }

  login() {
    this.authService.login(this.email, this.password).subscribe({
      next: (res: any) => {
        console.log(res);

        localStorage.setItem('token', res.token);

      },
      error: (err) => {
        this.error = 'Invalid credentials';
      }
    });
  }
}