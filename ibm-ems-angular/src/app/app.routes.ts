import { Routes } from '@angular/router';
import { Home } from './features/home/home';
import { Parent } from './features/parent/parent';

export const routes: Routes = [
    { path: 'home', component: Home },
    { path: 'parent', component: Parent },
    { path: '', redirectTo: 'home', pathMatch: 'full' },
    // { path: '**', component: Page404 }
];

