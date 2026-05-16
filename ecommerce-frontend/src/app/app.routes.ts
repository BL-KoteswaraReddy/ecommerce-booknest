import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  // Public Routes
  {
    path: '',
    loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'catalog',
    loadComponent: () => import('./features/catalog/catalog.component').then(m => m.CatalogComponent)
  },
  {
    path: 'books/:id',
    loadComponent: () => import('./features/catalog/book-detail/book-detail.component').then(m => m.BookDetailComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'oauth2/callback',
    loadComponent: () => import('./features/auth/oauth-callback/oauth-callback.component').then(m => m.OauthCallbackComponent)
  },

  // Protected Customer Routes
  {
    path: 'profile',
    loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent),
    canActivate: [authGuard]
  },
  {
    path: 'cart',
    loadComponent: () => import('./features/cart/cart.component').then(m => m.CartComponent),
    canActivate: [authGuard]
  },
  {
    path: 'wishlist',
    loadComponent: () => import('./features/wishlist/wishlist.component').then(m => m.WishlistComponent),
    canActivate: [authGuard]
  },
  {
    path: 'checkout',
    loadComponent: () => import('./features/checkout/checkout.component').then(m => m.CheckoutComponent),
    canActivate: [authGuard]
  },
  {
    path: 'orders',
    loadComponent: () => import('./features/orders/order-history/order-history.component').then(m => m.OrderHistoryComponent),
    canActivate: [authGuard]
  },
  {
    path: 'orders/:id',
    loadComponent: () => import('./features/orders/order-detail/order-detail.component').then(m => m.OrderDetailComponent),
    canActivate: [authGuard]
  },
  {
    path: 'wallet',
    loadComponent: () => import('./features/wallet/wallet.component').then(m => m.WalletComponent),
    canActivate: [authGuard]
  },
  {
    path: 'notifications',
    loadComponent: () => import('./features/notifications/notifications.component').then(m => m.NotificationsComponent),
    canActivate: [authGuard]
  },

  // Protected Admin Routes (We use the same authGuard for now, ideally an adminGuard would be better)
  {
    path: 'admin/books',
    loadComponent: () => import('./features/admin/book-management/book-list/book-list.component').then(m => m.BookListComponent),
    canActivate: [authGuard]
  },
  {
    path: 'admin/books/new',
    loadComponent: () => import('./features/admin/book-management/book-form/book-form.component').then(m => m.BookFormComponent),
    canActivate: [authGuard]
  },
  {
    path: 'admin/books/edit/:id',
    loadComponent: () => import('./features/admin/book-management/book-form/book-form.component').then(m => m.BookFormComponent),
    canActivate: [authGuard]
  },
  {
    path: 'admin/orders',
    loadComponent: () => import('./features/admin/order-management/admin-order-list/admin-order-list.component').then(m => m.AdminOrderListComponent),
    canActivate: [authGuard]
  },

  { path: '**', redirectTo: '' }
];
