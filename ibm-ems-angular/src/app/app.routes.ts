import { Routes } from '@angular/router';
import { Home } from './features/home/home';
import { Parent } from './features/parent/parent';
import { Login } from './features/auth/login/login';
import { DepartmentList } from './features/department/department-list/department-list';
import { DepartmentDetails } from './features/department/department-details/department-details';
import { DepartmentCreate } from './features/department/department-create/department-create';
import { Page404 } from './shared/components/page404/page404';

export const routes: Routes = [
    { path: 'home', component: Home },
    { path: 'parent', component: Parent },
    { path: 'department-list', component: DepartmentList },
    { path: 'department-details', component: DepartmentDetails },
    { path: 'department-create', component: DepartmentCreate },
    { path: 'login', component: Login },
    { path: '', redirectTo: 'home', pathMatch: 'full' },
    { path: '**', component: Page404 }
];

