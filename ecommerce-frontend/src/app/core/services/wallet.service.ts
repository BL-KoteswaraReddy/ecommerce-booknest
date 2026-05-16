import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthService } from './auth.service';
import { 
  AddMoneyRequest, 
  TransactionResponse, 
  Wallet, 
  WalletApiResponse, 
  WalletPaymentRequest, 
  WalletPaymentResponse,
  Transaction
} from '../models/wallet.models';

@Injectable({
  providedIn: 'root'
})
export class WalletService {
  private apiUrl = `${environment.apiUrl}/api/wallet`;
  
  // Real-time tracking of the user's wallet balance
  private walletBalanceSubject = new BehaviorSubject<number>(0);
  walletBalance$ = this.walletBalanceSubject.asObservable();

  constructor(private http: HttpClient, private authService: AuthService) {
    this.authService.currentUser$.subscribe(user => {
      if (user && user.userId) {
        this.fetchWalletBalance(user.userId);
      } else {
        this.walletBalanceSubject.next(0);
      }
    });
  }

  getWallet(userId: number): Observable<WalletApiResponse<Wallet>> {
    return this.http.get<WalletApiResponse<Wallet>>(`${this.apiUrl}/${userId}`).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.walletBalanceSubject.next(res.data.currentBalance);
        }
      })
    );
  }

  addMoney(amount: number, remarks: string = 'Added via Frontend'): Observable<WalletApiResponse<Wallet>> | null {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) return null;

    const request: AddMoneyRequest = { userId: user.userId, amount, remarks };
    return this.http.post<WalletApiResponse<Wallet>>(`${this.apiUrl}/add-money`, request).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.walletBalanceSubject.next(res.data.currentBalance);
        }
      })
    );
  }

  processPayment(request: WalletPaymentRequest): Observable<WalletApiResponse<WalletPaymentResponse>> {
    return this.http.post<WalletApiResponse<WalletPaymentResponse>>(`${this.apiUrl}/pay`, request).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.walletBalanceSubject.next(res.data.balanceAfter);
        }
      })
    );
  }

  getUserTransactions(page: number = 0, size: number = 10, sortBy: string = 'transactionDate', sortDir: string = 'desc'): Observable<WalletApiResponse<TransactionResponse>> | null {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) return null;

    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    return this.http.get<WalletApiResponse<TransactionResponse>>(`${this.apiUrl}/${user.userId}/transactions`, { params });
  }

  getUserTransactionsByType(type: string, page: number = 0, size: number = 10): Observable<WalletApiResponse<TransactionResponse>> | null {
    const user = this.authService.currentUserValue;
    if (!user || !user.userId) return null;

    let params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<WalletApiResponse<TransactionResponse>>(`${this.apiUrl}/${user.userId}/transactions/type/${type}`, { params });
  }

  private fetchWalletBalance(userId: number) {
    this.http.get<WalletApiResponse<number>>(`${this.apiUrl}/${userId}/balance`).subscribe({
      next: (res) => {
        if (res.success) {
          this.walletBalanceSubject.next(res.data);
        }
      },
      error: (err) => {
        // If a wallet doesn't exist, we might get an error (like 404).
        // Let's create it automatically if it's missing.
        if (err.status === 404) {
          this.createWallet(userId);
        }
      }
    });
  }

  private createWallet(userId: number) {
    this.http.post<WalletApiResponse<Wallet>>(`${this.apiUrl}/${userId}/create`, null).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.walletBalanceSubject.next(res.data.currentBalance);
        }
      },
      error: () => {
        this.walletBalanceSubject.next(0);
      }
    });
  }
}
