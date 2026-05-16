export interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

export interface Users {
  userId: number;
  fullName: string;
  email: string;
  role: string;
  mobile: string;
  createdAt: string;
  token?: string;
  provider?: string;
  providerId?: string;
}

export interface LoginRequest {
  email?: string;
  password?: string;
}

export interface RegisterRequest {
  fullName?: string;
  email?: string;
  password?: string;
  mobile?: string;
  role?: string;
}
