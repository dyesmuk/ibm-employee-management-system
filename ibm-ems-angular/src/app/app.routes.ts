import { Routes } from '@angular/router';
import { Home } from './features/home/home';
import { Parent } from './features/parent/parent';
import { Login } from './features/auth/login/login';
import { DepartmentList } from './features/department/department-list/department-list';
DepartmentList
export const routes: Routes = [
    { path: 'home', component: Home },
    { path: 'parent', component: Parent },
    { path: 'department-list', component: DepartmentList },
    { path: 'login', component: Login },
    { path: '', redirectTo: 'home', pathMatch: 'full' },

    // { path: '**', component: Page404 }
];

