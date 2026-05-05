import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth/auth.service';
import { CommonModule } from '@angular/common';
import {
  FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators
} from '@angular/forms';

// see the guide - 

// https://angular.dev/guide/forms/template-driven-forms 

@Component({
  selector: 'app-login',
  imports: [FormsModule, ReactiveFormsModule, CommonModule],
  templateUrl: './login.html',
})
export class Login {

  constructor(
    private authService: AuthService,
    private fb: FormBuilder
  ) { }


  templateDrivenUser = {
    username: '',
    password: '',
  };

  templateDrivenError = '';
  templateDrivenMessage = '';

  templateDrivenLogin() {
    this.authService.login(
      this.templateDrivenUser.username,
      this.templateDrivenUser.password
    ).subscribe({
      next: (res: any) => {
        localStorage.setItem('token', res.token);
        this.templateDrivenMessage = "Success!";
        this.templateDrivenError = "";
      },
      error: () => {
        this.templateDrivenError = "Invalid credentials";
        this.templateDrivenMessage = "";
      }
    });
  }


  reactiveLoginForm!: FormGroup;
  reactiveMessage = '';
  reactiveError = '';


  ngOnInit() {
    this.reactiveLoginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  reactiveLogin() {
    if (this.reactiveLoginForm.invalid) return;

    const { username, password } = this.reactiveLoginForm.value;

    this.authService.login(username, password).subscribe({
      next: (res: any) => {
        localStorage.setItem('token', res.token);
        this.reactiveMessage = "Success!";
        this.reactiveError = "";
      },
      error: () => {
        this.reactiveError = "Invalid credentials";
        this.reactiveMessage = "";
      }
    });
  }
}



// import { Component } from '@angular/core';
// import { AuthService } from '../../../core/services/auth/auth.service';

// import { FormsModule } from '@angular/forms';
// import { CommonModule } from '@angular/common';

// import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

// @Component({
//   selector: 'app-login',
//   templateUrl: './login.html',
//   imports: [FormsModule, CommonModule, ReactiveFormsModule]
// })
// export class Login {


//   constructor(private authService: AuthService, private fb: FormBuilder) { }


//   //   // 1. Template-driven form

//   templateUser = {
//     username: '',
//     password: '',
//   };

//   error = '';
//   message = '';


//   ngOnInit() {
//     this.reactiveLoginForm = this.fb.group({
//       username: ['', Validators.required],
//       password: ['', Validators.required]
//     });
//   }

//   //   // 1. Template-driven form
//   templateDrivenLogin() {
//     this.authService.login(this.templateUser.username, this.templateUser.password).subscribe({
//       next: (res: any) => {
//         localStorage.setItem('token', res.token);
//         console.log(res.token);
//         this.message = "Success!";
//         this.error = "";
//       },
//       error: () => {
//         this.error = "Invalid credentials";
//         this.message = "";
//       }
//     });
//   }


//   //   // 2. Reactive form
//   reactiveLoginForm!: FormGroup;
//   rError = '';
//   rMessage = '';

//   reactiveLogin() {
//     // if (this.reactiveLoginForm.invalid) return;
//     const { username, password } = this.reactiveLoginForm.value;
//     console.log(username, password);
//     this.authService.login(username, password).subscribe({
//       next: (res: any) => {
//         localStorage.setItem('token', res.token);
//         console.log(res.token);
//         this.rMessage = "Success!";
//         this.rError = "";
//       },
//       error: () => {
//         this.rError = "Invalid credentials";
//         this.rMessage = "";
//       }
//     });
//   }
// }

