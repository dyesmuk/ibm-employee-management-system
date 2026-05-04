import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DepartmentCreate } from './department-create';

describe('DepartmentCreate', () => {
  let component: DepartmentCreate;
  let fixture: ComponentFixture<DepartmentCreate>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DepartmentCreate]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DepartmentCreate);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
