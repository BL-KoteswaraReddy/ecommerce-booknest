import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { BehaviorSubject, Observable, catchError, of, tap, timeout } from 'rxjs';
import { AddToCartRequest, Cart, CartApiResponse, UpdateCartItemRequest } from '../models/cart.models';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private apiUrl = `${environment.apiUrl}/api/cart`;
  
  // Expose cart item count for the Navbar badge
  private cartItemCountSubject = new BehaviorSubject<number>(0);
  cartItemCount$ = this.cartItemCountSubject.asObservable();

  constructor(private http: HttpClient, private authService: AuthService) {
    // When the user logs in, automatically fetch their cart count
    this.authService.currentUser$.subscribe(user => {
      if (user && user.userId) {
        this.fetchCartItemCount(user.userId);
      } else {
        this.cartItemCountSubject.next(0);
      }
    });
  }

  getCart(userId: number): Observable<CartApiResponse<Cart>> {
    return this.http.get<CartApiResponse<Cart>>(`${this.apiUrl}/${userId}`).pipe(
      timeout(8000),
      tap(res => {
        if (res.success && res.data) {
          this.cartItemCountSubject.next(res.data.totalItems || 0);
        }
      })
    );
  }

  addToCart(bookId: number, quantity: number = 1): Observable<CartApiResponse<Cart>> | null {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) return null;

    const request: AddToCartRequest = { userId: user.userId, bookId, quantity };
    return this.http.post<CartApiResponse<Cart>>(`${this.apiUrl}/add`, request).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.cartItemCountSubject.next(res.data.totalItems || 0);
        }
      })
    );
  }

  updateCartItem(cartItemId: number, quantity: number): Observable<CartApiResponse<Cart>> | null {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) return null;

    const request: UpdateCartItemRequest = { userId: user.userId, cartItemId, quantity };
    return this.http.put<CartApiResponse<Cart>>(`${this.apiUrl}/update`, request).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.cartItemCountSubject.next(res.data.totalItems || 0);
        }
      })
    );
  }

  removeCartItem(cartItemId: number): Observable<CartApiResponse<Cart>> | null {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) return null;

    return this.http.delete<CartApiResponse<Cart>>(`${this.apiUrl}/${user.userId}/item/${cartItemId}`).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.cartItemCountSubject.next(res.data.totalItems || 0);
        }
      })
    );
  }

  clearCart(): Observable<CartApiResponse<Cart>> | null {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) return null;

    return this.http.delete<CartApiResponse<Cart>>(`${this.apiUrl}/${user.userId}/clear`).pipe(
      tap(() => this.cartItemCountSubject.next(0))
    );
  }

  private fetchCartItemCount(userId: number) {
    this.http.get<CartApiResponse<number>>(`${this.apiUrl}/${userId}/count`).pipe(
      timeout(5000),
      catchError(() => of({ success: false, message: 'Unable to load cart count', data: 0, timestamp: new Date().toISOString() }))
    ).subscribe(res => {
      if (res.success) {
        this.cartItemCountSubject.next(res.data);
      }
    });
  }
}
