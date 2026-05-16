import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BookService } from '../../../core/services/book.service';
import { CartService } from '../../../core/services/cart.service';
import { Book } from '../../../core/models/book.models';
import { WishlistService } from '../../../core/services/wishlist.service';
import { AuthService } from '../../../core/services/auth.service';

import { ReviewsComponent } from './reviews/reviews.component';

import { ImageUrlPipe } from '../../../core/pipes/image-url.pipe';

@Component({
  selector: 'app-book-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, ReviewsComponent, ImageUrlPipe],
  templateUrl: './book-detail.component.html',
  styleUrl: './book-detail.component.css'
})
export class BookDetailComponent implements OnInit {
  book: Book | null = null;
  isLoading = true;
  quantity: number = 1;

  isAddingToCart = false;
  isWishlistActionLoading = false;
  isInWishlist = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookService: BookService,
    private cartService: CartService,
    private wishlistService: WishlistService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      const id = +params['id'];
      if (id) {
        this.loadBook(id);
      }
    });
  }

  loadBook(id: number) {
    this.isLoading = true;
    this.bookService.getBookById(id).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.book = res.data;
          this.checkWishlistStatus(id);
        } else {
          // If not found, redirect to catalog
          this.router.navigate(['/catalog']);
        }
        this.isLoading = false;
      },
      error: () => {
        this.router.navigate(['/catalog']);
        this.isLoading = false;
      }
    });
  }

  checkWishlistStatus(bookId: number) {
    const user = this.authService.currentUserValue;
    if (!user) return;
    
    const req = this.wishlistService.isBookInWishlist(bookId);
    if (req) {
      req.subscribe(res => {
        if (res.success) {
          this.isInWishlist = res.data;
        }
      });
    }
  }

  increaseQuantity() {
    if (this.book && this.quantity < this.book.stock) {
      this.quantity++;
    }
  }

  decreaseQuantity() {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }

  addToCart() {
    if (!this.book) return;
    
    this.isAddingToCart = true;
    const req = this.cartService.addToCart(this.book.id, this.quantity);
    
    if (req) {
      req.subscribe({
        next: (res) => {
          if (res.success) {
            // Success feedback could be a toast notification, for now we just alert
            alert(`Added ${this.quantity} copies of "${this.book?.title}" to your cart!`);
          }
          this.isAddingToCart = false;
        },
        error: () => {
          alert('Failed to add item to cart. Please try again.');
          this.isAddingToCart = false;
        }
      });
    } else {
      // User is not logged in, redirect to login
      this.router.navigate(['/login']);
      this.isAddingToCart = false;
    }
  }

  toggleWishlist() {
    if (!this.book || this.isWishlistActionLoading) return;

    if (!this.authService.currentUserValue) {
      this.router.navigate(['/login']);
      return;
    }

    this.isWishlistActionLoading = true;

    if (this.isInWishlist) {
      const req = this.wishlistService.removeBookFromWishlist(this.book.id);
      if (req) {
        req.subscribe({
          next: (res) => {
            if (res.success) this.isInWishlist = false;
            this.isWishlistActionLoading = false;
          },
          error: () => this.isWishlistActionLoading = false
        });
      }
    } else {
      const req = this.wishlistService.addToWishlist(this.book.id);
      if (req) {
        req.subscribe({
          next: (res) => {
            if (res.success) this.isInWishlist = true;
            this.isWishlistActionLoading = false;
          },
          error: () => this.isWishlistActionLoading = false
        });
      }
    }
  }
}
