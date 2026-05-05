import { ChangeDetectorRef, Component } from '@angular/core';
import { DepartmentService } from '../../../core/services/department/department.service';
import { CommonModule } from '@angular/common';
import { Department } from '../../../core/models/department.model';

@Component({
  selector: 'app-department-list',
  imports: [CommonModule],
  templateUrl: './department-list.html',
  styleUrl: './department-list.css'
})
export class DepartmentList {

  departments: Department[] = [];
  error = '';

  constructor(private departmentService: DepartmentService, private cdr: ChangeDetectorRef) { }

  getAll() {
    this.departmentService.getAllDepartments().subscribe({
      next: (res) => {
        console.log(res);
        this.departments = res;
        this.cdr.detectChanges();
        console.log(this.departments);
      },
      error: (err) => {
        this.error = 'Failed to load departments';
        console.log(err);
      }
    });
  }

};





