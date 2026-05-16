import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../core/services/notification.service';
import { NotificationDTO } from '../../core/models/notification.models';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.component.html',
  styleUrl: './notifications.component.css'
})
export class NotificationsComponent implements OnInit {
  private notificationService = inject(NotificationService);
  private authService = inject(AuthService);

  notifications: NotificationDTO[] = [];
  unreadCount = 0;
  isLoading = false;
  error: string | null = null;
  userId: number = 0;

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user && user.userId) {
        this.userId = user.userId;
        this.loadNotifications();
        this.loadUnreadCount();
      } else {
        this.error = 'User not logged in';
      }
    });
  }

  loadNotifications(): void {
    this.isLoading = true;
    this.notificationService.getUserNotifications(this.userId, 0, 50).subscribe({
      next: (response) => {
        if (response.success) {
          this.notifications = response.data;
        } else {
          this.error = response.message;
          this.notifications = [];
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Failed to load notifications';
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  loadUnreadCount(): void {
    this.notificationService.getUnreadCount(this.userId).subscribe({
      next: (response) => {
        if (response.success) {
          this.unreadCount = response.data;
        }
      }
    });
  }

  markAsRead(notification: NotificationDTO): void {
    if (notification.isRead) return;

    this.notificationService.markAsRead(notification.id, this.userId).subscribe({
      next: (response) => {
        if (response.success) {
          notification.isRead = true;
          if (this.unreadCount > 0) {
            this.unreadCount--;
            this.notificationService.updateLocalUnreadCount(this.unreadCount);
          }
        }
      }
    });
  }

  markAllAsRead(): void {
    if (this.unreadCount === 0) return;

    this.notificationService.markAllAsRead(this.userId).subscribe({
      next: (response) => {
        if (response.success) {
          this.notifications.forEach(n => n.isRead = true);
          this.unreadCount = 0;
          this.notificationService.updateLocalUnreadCount(this.unreadCount);
        }
      }
    });
  }

  deleteNotification(notificationId: number): void {
    this.notificationService.deleteNotification(notificationId, this.userId).subscribe({
      next: (response) => {
        if (response.success) {
          const deletedNotif = this.notifications.find(n => n.id === notificationId);
          if (deletedNotif && !deletedNotif.isRead && this.unreadCount > 0) {
            this.unreadCount--;
            this.notificationService.updateLocalUnreadCount(this.unreadCount);
          }
          this.notifications = this.notifications.filter(n => n.id !== notificationId);
        }
      }
    });
  }

  getIconForType(type: string): string {
    switch (type) {
      case 'ORDER_PLACED': case 'ORDER_CONFIRMED': case 'ORDER_DISPATCHED': case 'ORDER_DELIVERED':
        return 'bi-box-seam';
      case 'PAYMENT_SUCCESS': case 'WALLET_CREDITED':
        return 'bi-check-circle';
      case 'PAYMENT_FAILED': case 'WALLET_DEBITED': case 'ORDER_CANCELLED':
        return 'bi-x-circle';
      case 'LOW_STOCK':
        return 'bi-exclamation-triangle';
      default:
        return 'bi-bell';
    }
  }

  getColorForType(type: string): string {
    switch (type) {
      case 'ORDER_PLACED': case 'ORDER_CONFIRMED': case 'ORDER_DISPATCHED': case 'ORDER_DELIVERED':
        return 'text-primary bg-primary-subtle';
      case 'PAYMENT_SUCCESS': case 'WALLET_CREDITED':
        return 'text-success bg-success-subtle';
      case 'PAYMENT_FAILED': case 'WALLET_DEBITED': case 'ORDER_CANCELLED': case 'LOW_STOCK':
        return 'text-danger bg-danger-subtle';
      default:
        return 'text-secondary bg-secondary-subtle';
    }
  }
}
