import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiResponse, LoginRequest, RegisterRequest, Users } from '../models/auth.models';
import { BehaviorSubject, Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<Users | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  public get currentUserValue(): Users | null {
    return this.currentUserSubject.value;
  }

  register(request: RegisterRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/register`, request);
  }

  login(request: LoginRequest): Observable<ApiResponse<Users>> {
    return this.http.post<ApiResponse<Users>>(`${this.apiUrl}/signin`, request).pipe(
      tap(response => {
        if (response.data && response.data.token) {
          this.setSession(response.data);
        }
      })
    );
  }

  getProfile(email: string): Observable<ApiResponse<Users>> {
    return this.http.get<ApiResponse<Users>>(`${this.apiUrl}/profile/${encodeURIComponent(email)}`).pipe(
      tap(response => {
        if (response.data) {
          const currentSession = this.getUserFromStorage();
          const token = response.data.token ?? currentSession?.token;
          this.setSession({ ...response.data, token });
        }
      })
    );
  }

  changePassword(email: string, oldPassword: string, newPassword: string): Observable<ApiResponse<void>> {
    const params = new HttpParams()
      .set('oldPassword', oldPassword)
      .set('newPassword', newPassword);
    
    return this.http.put<ApiResponse<void>>(`${this.apiUrl}/change-password/${email}`, null, { params });
  }

  updateProfile(email: string, fullName: string, mobile: string): Observable<ApiResponse<Users>> {
    return this.http.put<ApiResponse<Users>>(`${this.apiUrl}/profile/${email}`, { fullName, mobile }).pipe(
      tap(response => {
        if (response.data) {
          const currentSession = this.getUserFromStorage();
          if (currentSession) {
            const token = response.data.token ?? currentSession.token;
            const updatedUser = { ...currentSession, ...response.data, token };
            this.setSession(updatedUser);
          }
        }
      })
    );
  }

  deleteUser(userId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/delete/${userId}`);
  }

  googleCallback(email: string, role: string): Observable<ApiResponse<Users>> {
    const params = new HttpParams()
      .set('email', email)
      .set('role', role);

    return this.http.get<ApiResponse<Users>>(`${this.apiUrl}/google/callback`, { params }).pipe(
      tap(response => {
        if (response.data && response.data.token) {
          this.setSession(response.data);
        }
      })
    );
  }

  logout() {
    localStorage.removeItem('booknest_user');
    this.currentUserSubject.next(null);
  }

  saveSession(user: Users) {
    this.setSession(user);
  }

  private setSession(user: Users) {
    localStorage.setItem('booknest_user', JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  private getUserFromStorage(): Users | null {
    const userStr = localStorage.getItem('booknest_user');
    if (userStr) {
      try {
        return JSON.parse(userStr) as Users;
      } catch (e) {
        return null;
      }
    }
    return null;
  }

  getToken(): string | null {
    return this.currentUserValue?.token || null;
  }
}
