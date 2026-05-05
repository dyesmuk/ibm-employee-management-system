import { CommonModule, CurrencyPipe, UpperCasePipe, } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-employee',
  imports: [UpperCasePipe, CurrencyPipe, FormsModule, CommonModule],
  templateUrl: './employee.html',
  styleUrl: './employee.css',
})
export class Employee {

  employeeList = [
    { firstName: 'Sonu', lastName: 'Joshi', salary: 12.75 },
    { firstName: 'Monu', lastName: 'Joshi', salary: 11.50 },
    { firstName: 'Tonu', lastName: 'Joshi', salary: 15.25 },
    { firstName: 'Ponu', lastName: 'Joshi', salary: 13.00 }
  ];

  employeeData = { firstName: 'Sonu', lastName: 'Joshi', salary: 12.75 }

  demoClick = () => {
    console.log("button clicked!");
    this.isLoggedIn = !this.isLoggedIn;
  };

  isDisabled = false;

  username = "";
  password = "";

  isLoggedIn = false;

  login = () => {
    console.log(this.username, this.password);
  };

  role = "";

}

