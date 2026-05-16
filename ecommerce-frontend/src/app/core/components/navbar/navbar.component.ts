import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CartService } from '../../services/cart.service';
import { WishlistService } from '../../services/wishlist.service';
import { WalletService } from '../../services/wallet.service';
import { NotificationService } from '../../services/notification.service';
import { Users } from '../../models/auth.models';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit {
  currentUser: Users | null = null;
  searchQuery: string = '';
  cartItemCount: number = 0;
  wishlistItemCount: number = 0;
  walletBalance: number = 0;
  unreadNotificationCount: number = 0;

  constructor(
    private authService: AuthService, 
    private cartService: CartService,
    private wishlistService: WishlistService,
    private walletService: WalletService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user && user.userId) {
        this.notificationService.getUnreadCount(user.userId).subscribe();
      }
    });

    this.cartService.cartItemCount$.subscribe(count => {
      this.cartItemCount = count;
    });

    this.wishlistService.wishlistItemCount$.subscribe(count => {
      this.wishlistItemCount = count;
    });

    this.walletService.walletBalance$.subscribe(balance => {
      this.walletBalance = balance;
    });

    this.notificationService.unreadCount$.subscribe(count => {
      this.unreadNotificationCount = count;
    });
  }

  onSearch() {
    if (this.searchQuery.trim()) {
      this.router.navigate(['/catalog'], { queryParams: { search: this.searchQuery } });
      this.searchQuery = '';
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
