import { Routes } from '@angular/router';
import { Home } from './features/home/home';
import { Parent } from './features/parent/parent';
import { Login } from './features/auth/login/login';
import { DepartmentList } from './features/department/department-list/department-list';
import { DepartmentDetails } from './features/department/department-details/department-details';
import { DepartmentCreate } from './features/department/department-create/department-create';
import { Page404 } from './shared/components/page404/page404';
import { authGuard } from './core/guards/auth.guard';
import { Demo } from './shared/components/demo/demo';

export const routes: Routes = [
    { path: 'home', component: Home },
    { path: 'login', component: Login },
    { path: 'parent', component: Parent },
    { path: 'demo', component: Demo },
    { path: 'department-list', component: DepartmentList, canActivate: [authGuard] },
    { path: 'department-details', component: DepartmentDetails, canActivate: [authGuard] },
    { path: 'department-create', component: DepartmentCreate, canActivate: [authGuard] },
    { path: '', redirectTo: 'home', pathMatch: 'full' },
    { path: '**', component: Page404 },
];

// import { Routes } from '@angular/router';
// import { Home } from './features/home/home';
// import { Parent } from './features/parent/parent';
// import { Login } from './features/auth/login/login';
// import { DepartmentList } from './features/department/department-list/department-list';
// import { DepartmentDetails } from './features/department/department-details/department-details';
// import { DepartmentCreate } from './features/department/department-create/department-create';
// import { Page404 } from './shared/components/page404/page404';

// export const routes: Routes = [
//     { path: 'home', component: Home },
//     { path: 'parent', component: Parent },
//     { path: 'department-list', component: DepartmentList },
//     { path: 'department-details', component: DepartmentDetails },
//     { path: 'department-create', component: DepartmentCreate },
//     { path: 'login', component: Login },
//     { path: '', redirectTo: 'home', pathMatch: 'full' },
//     { path: '**', component: Page404 }
// ];


