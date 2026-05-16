import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { WishlistService } from '../../core/services/wishlist.service';
import { AuthService } from '../../core/services/auth.service';
import { Wishlist, WishlistItem } from '../../core/models/wishlist.models';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './wishlist.component.html',
  styleUrl: './wishlist.component.css'
})
export class WishlistComponent implements OnInit {
  wishlist: Wishlist | null = null;
  isLoading = true;
  isUpdating = false;

  constructor(private wishlistService: WishlistService, private authService: AuthService) {}

  ngOnInit() {
    this.loadWishlist();
  }

  loadWishlist() {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) return;

    this.isLoading = true;
    this.wishlistService.getWishlist(user.userId).subscribe({
      next: (res) => {
        if (res.success) {
          this.wishlist = res.data;
        }
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  moveToCart(item: WishlistItem) {
    if (this.isUpdating) return;
    this.isUpdating = true;
    
    const req = this.wishlistService.moveToCart(item.id);
    if (req) {
      req.subscribe({
        next: (res) => {
          if (res.success) this.wishlist = res.data;
          this.isUpdating = false;
          alert(`Moved "${item.bookTitle}" to your cart!`);
        },
        error: () => {
          this.isUpdating = false;
          alert('Failed to move item to cart.');
        }
      });
    }
  }

  removeItem(item: WishlistItem) {
    if (this.isUpdating) return;
    
    if (confirm(`Are you sure you want to remove "${item.bookTitle}" from your wishlist?`)) {
      this.isUpdating = true;
      const req = this.wishlistService.removeFromWishlist(item.id);
      if (req) {
        req.subscribe({
          next: (res) => {
            if (res.success) this.wishlist = res.data;
            this.isUpdating = false;
          },
          error: () => this.isUpdating = false
        });
      }
    }
  }

  clearWishlist() {
    if (this.isUpdating || !this.wishlist?.items?.length) return;

    if (confirm('Are you sure you want to clear your entire wishlist?')) {
      this.isUpdating = true;
      const req = this.wishlistService.clearWishlist();
      if (req) {
        req.subscribe({
          next: (res) => {
            if (res.success) this.wishlist = res.data;
            this.isUpdating = false;
          },
          error: () => this.isUpdating = false
        });
      }
    }
  }
}
