import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AddToWishlistRequest, MoveToCartRequest, Wishlist, WishlistApiResponse } from '../models/wishlist.models';
import { AuthService } from './auth.service';
import { CartService } from './cart.service';

@Injectable({
  providedIn: 'root'
})
export class WishlistService {
  private apiUrl = `${environment.apiUrl}/api/wishlist`;
  
  // Expose wishlist item count for the Navbar badge
  private wishlistItemCountSubject = new BehaviorSubject<number>(0);
  wishlistItemCount$ = this.wishlistItemCountSubject.asObservable();

  constructor(
    private http: HttpClient, 
    private authService: AuthService,
    private cartService: CartService
  ) {
    // When the user logs in, automatically fetch their wishlist count
    this.authService.currentUser$.subscribe(user => {
      if (user && user.userId) {
        this.fetchWishlistItemCount(user.userId);
      } else {
        this.wishlistItemCountSubject.next(0);
      }
    });
  }

  getWishlist(userId: number): Observable<WishlistApiResponse<Wishlist>> {
    return this.http.get<WishlistApiResponse<Wishlist>>(`${this.apiUrl}/${userId}`).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.wishlistItemCountSubject.next(res.data.totalItems || 0);
        }
      })
    );
  }

  addToWishlist(bookId: number): Observable<WishlistApiResponse<Wishlist>> | null {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) return null;

    const request: AddToWishlistRequest = { userId: user.userId, bookId };
    return this.http.post<WishlistApiResponse<Wishlist>>(`${this.apiUrl}/add`, request).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.wishlistItemCountSubject.next(res.data.totalItems || 0);
        }
      })
    );
  }

  removeFromWishlist(wishlistItemId: number): Observable<WishlistApiResponse<Wishlist>> | null {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) return null;

    return this.http.delete<WishlistApiResponse<Wishlist>>(`${this.apiUrl}/${user.userId}/item/${wishlistItemId}`).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.wishlistItemCountSubject.next(res.data.totalItems || 0);
        }
      })
    );
  }

  removeBookFromWishlist(bookId: number): Observable<WishlistApiResponse<Wishlist>> | null {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) return null;

    return this.http.delete<WishlistApiResponse<Wishlist>>(`${this.apiUrl}/${user.userId}/book/${bookId}`).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.wishlistItemCountSubject.next(res.data.totalItems || 0);
        }
      })
    );
  }

  clearWishlist(): Observable<WishlistApiResponse<Wishlist>> | null {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) return null;

    return this.http.delete<WishlistApiResponse<Wishlist>>(`${this.apiUrl}/${user.userId}/clear`).pipe(
      tap(() => this.wishlistItemCountSubject.next(0))
    );
  }

  moveToCart(wishlistItemId: number, quantity: number = 1): Observable<WishlistApiResponse<Wishlist>> | null {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) return null;

    const request: MoveToCartRequest = { userId: user.userId, wishlistItemId, quantity };
    return this.http.post<WishlistApiResponse<Wishlist>>(`${this.apiUrl}/move-to-cart`, request).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.wishlistItemCountSubject.next(res.data.totalItems || 0);
          // Tell cart service to fetch its updated count
          this.cartService.getCart(user.userId).subscribe();
        }
      })
    );
  }

  isBookInWishlist(bookId: number): Observable<WishlistApiResponse<boolean>> | null {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) return null;

    return this.http.get<WishlistApiResponse<boolean>>(`${this.apiUrl}/${user.userId}/check/${bookId}`);
  }

  private fetchWishlistItemCount(userId: number) {
    this.http.get<WishlistApiResponse<number>>(`${this.apiUrl}/${userId}/count`).subscribe(res => {
      if (res.success) {
        this.wishlistItemCountSubject.next(res.data);
      }
    });
  }
}
