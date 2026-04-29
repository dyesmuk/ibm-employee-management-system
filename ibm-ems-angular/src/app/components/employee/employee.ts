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

  employeeData = { firstName: 'Sonu', lastName: 'Joshi', salary: 12.75 }

  demoClick = () => {
    console.log("button clicked!");
  };

  isDisabled = false;

  username = "";
  password = "";

  isLoggedIn = false;

  login = () => {
    console.log(this.username, this.password);
    this.isLoggedIn = true;
  };

}

