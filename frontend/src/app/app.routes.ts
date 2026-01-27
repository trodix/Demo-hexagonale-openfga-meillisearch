import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'search',
    loadComponent: () => import('./components/product-search/product-search.component').then(m => m.ProductSearchComponent),
    canActivate: [authGuard]
  },
  {
    path: 'products/:id',
    loadComponent: () => import('./components/product-detail/product-detail').then(m => m.ProductDetail),
    canActivate: [authGuard]
  },
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/login'
  }
];
