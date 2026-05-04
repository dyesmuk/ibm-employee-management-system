import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Department } from '../../../core/models/department.model';
import { DepartmentService } from '../../../core/services/department/department.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-department-details',
  imports: [FormsModule, CommonModule],
  templateUrl: './department-details.html',
  styleUrl: './department-details.css'
})
export class DepartmentDetails {
  department?: Department;
  error = '';
  id = '';

  constructor(private departmentService: DepartmentService) { }

  searchDepartment() {
    console.log(this.id);
    this.departmentService.getDepartmentById(this.id.trim()).subscribe({
      next: (res) => {
        this.department = res;
      },
      error: (err) => {
        this.error = 'Department not found';
        console.log(err);
      }
    });
  }


}

