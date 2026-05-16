import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { 
  NotificationApiResponse, 
  NotificationDTO, 
  SendNotificationRequest 
} from '../models/notification.models';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private http = inject(HttpClient);
  // Assuming the API gateway or backend is at this URL. Adjust if needed.
  private apiUrl = `${environment.apiUrl || 'http://localhost:8080'}/api/notifications`;

  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  /**
   * Send a notification
   */
  sendNotification(request: SendNotificationRequest): Observable<NotificationApiResponse<NotificationDTO>> {
    return this.http.post<NotificationApiResponse<NotificationDTO>>(this.apiUrl, request);
  }

  /**
   * Get user notifications with pagination
   */
  getUserNotifications(userId: number, page: number = 0, size: number = 20): Observable<NotificationApiResponse<NotificationDTO[]>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
      
    return this.http.get<NotificationApiResponse<NotificationDTO[]>>(`${this.apiUrl}/user/${userId}`, { params }).pipe(
      catchError(() => of({ success: false, message: 'Unable to load notifications', data: [] }))
    );
  }

  /**
   * Mark a specific notification as read
   */
  markAsRead(notificationId: number, userId: number): Observable<NotificationApiResponse<NotificationDTO>> {
    let params = new HttpParams().set('userId', userId.toString());
    return this.http.put<NotificationApiResponse<NotificationDTO>>(`${this.apiUrl}/${notificationId}/read`, null, { params });
  }

  /**
   * Mark all user notifications as read
   */
  markAllAsRead(userId: number): Observable<NotificationApiResponse<void>> {
    return this.http.put<NotificationApiResponse<void>>(`${this.apiUrl}/user/${userId}/read-all`, null);
  }

  /**
   * Delete a notification
   */
  deleteNotification(notificationId: number, userId: number): Observable<NotificationApiResponse<void>> {
    let params = new HttpParams().set('userId', userId.toString());
    return this.http.delete<NotificationApiResponse<void>>(`${this.apiUrl}/${notificationId}`, { params });
  }

  /**
   * Get the unread notifications count for a user
   */
  getUnreadCount(userId: number): Observable<NotificationApiResponse<number>> {
    return this.http.get<NotificationApiResponse<number>>(`${this.apiUrl}/user/${userId}/unread-count`).pipe(
      tap(response => {
        if (response.success) {
          this.unreadCountSubject.next(response.data);
        }
      }),
      catchError(() => {
        this.unreadCountSubject.next(0);
        return of({ success: false, message: 'Unable to load unread count', data: 0 });
      })
    );
  }

  /**
   * Manually update the unread count locally
   */
  updateLocalUnreadCount(count: number): void {
    this.unreadCountSubject.next(count);
  }
}
