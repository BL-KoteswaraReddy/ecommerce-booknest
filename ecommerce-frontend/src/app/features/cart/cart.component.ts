import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { AuthService } from '../../core/services/auth.service';
import { Cart, CartItem } from '../../core/models/cart.models';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.css'
})
export class CartComponent implements OnInit {
  cart: Cart | null = null;
  isLoading = true;
  isUpdating = false;

  constructor(private cartService: CartService, private authService: AuthService, private router: Router) {}

  ngOnInit() {
    this.loadCart();
  }

  loadCart() {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) return;

    this.isLoading = true;
    this.cartService.getCart(user.userId).subscribe({
      next: (res) => {
        if (res.success) {
          this.cart = res.data;
        }
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  updateQuantity(item: CartItem, change: number) {
    if (this.isUpdating) return;
    
    const newQuantity = item.quantity + change;
    if (newQuantity < 1) return; // Prevent 0 quantity, they should use Remove instead

    this.isUpdating = true;
    const req = this.cartService.updateCartItem(item.id, newQuantity);
    if (req) {
      req.subscribe({
        next: (res) => {
          if (res.success) this.cart = res.data;
          this.isUpdating = false;
        },
        error: () => this.isUpdating = false
      });
    }
  }

  removeItem(item: CartItem) {
    if (this.isUpdating) return;
    
    if (confirm(`Are you sure you want to remove "${item.bookTitle}" from your cart?`)) {
      this.isUpdating = true;
      const req = this.cartService.removeCartItem(item.id);
      if (req) {
        req.subscribe({
          next: (res) => {
            if (res.success) this.cart = res.data;
            this.isUpdating = false;
          },
          error: () => this.isUpdating = false
        });
      }
    }
  }

  clearCart() {
    if (this.isUpdating || !this.cart?.items?.length) return;

    if (confirm('Are you sure you want to clear your entire cart?')) {
      this.isUpdating = true;
      const req = this.cartService.clearCart();
      if (req) {
        req.subscribe({
          next: (res) => {
            if (res.success) this.cart = res.data;
            this.isUpdating = false;
          },
          error: () => this.isUpdating = false
        });
      }
    }
  }

  proceedToCheckout() {
    this.router.navigate(['/checkout']);
  }
}
