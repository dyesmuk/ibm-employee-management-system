import { Routes } from '@angular/router';
import { Home } from './features/home/home';
import { Parent } from './features/parent/parent';
import { Department } from './features/department/department';
import { Login } from './features/auth/login/login';

export const routes: Routes = [
    { path: 'home', component: Home },
    { path: 'parent', component: Parent },
    { path: 'department', component: Department },
    { path: 'login', component: Login },
    { path: '', redirectTo: 'home', pathMatch: 'full' },

    // { path: '**', component: Page404 }
];

