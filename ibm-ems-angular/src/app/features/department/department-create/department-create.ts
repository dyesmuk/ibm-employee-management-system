import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Department } from '../../../core/models/department.model';
import { DepartmentService } from '../../../core/services/department/department.service';

@Component({
  selector: 'app-department-create',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './department-create.html',
})
export class DepartmentCreate {

  department: Department = {
    name: '',
    description: ''
  };

  message = '';
  error = '';

  constructor(private departmentService: DepartmentService) { }

  createDepartment() {

    this.departmentService.addDepartment(this.department).subscribe({
      next: (res) => {
        console.log(res);
        this.message = 'Department created successfully';
        this.error = '';

        this.department = {
          name: '',
          description: ''
        };
      },
      error: (err) => {
        this.error = 'Failed to create department';
      }
    });
  }
}

