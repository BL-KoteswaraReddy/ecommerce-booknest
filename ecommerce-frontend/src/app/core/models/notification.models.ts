export interface NotificationApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
}

export interface NotificationDTO {
  id: number;
  userId: number;
  type: string;
  title: string;
  message: string;
  isRead: boolean;
  referenceId?: string;
  createdAt: string;
}

export interface NotificationCountDTO {
  userId: number;
  unreadCount: number;
  totalCount: number;
}

export interface SendNotificationRequest {
  userId: number;
  userEmail: string;
  type: string;
  title: string;
  message: string;
  referenceId?: string;
  sendEmail?: boolean;
}
