import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { CatalogComponent } from './features/catalog/catalog.component';
import { AdminComponent } from './features/admin/admin.component';
import {CartComponent} from "./features/cart/cart.component";

export const routes: Routes = [
  { path: '', component: CatalogComponent },
  { path: 'catalog', component: CatalogComponent },
  { path: 'cart', component: CartComponent },
  { path: 'login', component: LoginComponent },
  { path: 'admin', component: AdminComponent },
  { path: '**', redirectTo: 'catalog' }
];
